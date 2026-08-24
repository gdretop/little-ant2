import express from 'express'
import proxy from 'express-http-proxy'
import { config, validateConfig } from './config.js'
import { decryptMessage, verifySignature, buildEncryptedReply } from './crypto.js'
import { WecomAPI } from './wecom-api.js'
import { attachTunnel, requestLLM, tunnelConnected } from './tunnel-server.js'

validateConfig()

const app = express()
// 仅给回调路由用 text 解析 (企业微信回调体是 XML 密文), 其余用 JSON
app.use(express.json())

const wecom = new WecomAPI(config.wecom.corpid, config.wecom.secret, config.wecom.agentid)

// ===== 消息队列 =====
// 存储从企业微信收到的待处理消息
const messageQueue = []
// 记录最近处理过的 msgId, 防止重复
const processedMsgIds = new Set()

/**
 * 简易 XML 字段提取 (企业微信消息格式简单, 无需完整 XML 解析器)
 */
function extractXmlField(xml, field) {
  const re = new RegExp(`<${field}>(?:<!\\[CDATA\\[)?(.*?)(?:\\]\\]>)?</${field}>`, 's')
  const m = xml.match(re)
  return m ? m[1].trim() : ''
}

function extractEncryptFromXml(xml) {
  return extractXmlField(xml, 'Encrypt')
}

// ===== 企业微信回调 =====

/**
 * GET /callback - 企业微信 URL 验证
 * 企业微信后台配置回调 URL 时会发送 GET 请求验证
 */
app.get('/callback', (req, res) => {
  const { msg_signature, timestamp, nonce, echostr } = req.query

  if (!msg_signature || !timestamp || !nonce || !echostr) {
    return res.status(400).send('missing params')
  }

  // 验证签名
  const valid = verifySignature(
    config.wecom.token, timestamp, nonce, echostr, msg_signature
  )

  if (!valid) {
    console.error('[callback] 签名验证失败')
    return res.status(403).send('signature verification failed')
  }

  // 解密 echostr 并返回明文
  try {
    const { content } = decryptMessage(
      echostr, config.wecom.corpid, config.wecom.encodingAESKey
    )
    console.log('[callback] URL 验证成功')
    res.send(content)
  } catch (err) {
    console.error('[callback] 解密失败:', err.message)
    res.status(500).send('decrypt failed')
  }
})

/**
 * POST /callback - 接收企业微信消息
 */
app.post('/callback', express.text({ type: '*/*' }), async (req, res) => {
  const { msg_signature, timestamp, nonce } = req.query
  const body = typeof req.body === 'string' ? req.body : ''

  if (!msg_signature || !timestamp || !nonce || !body) {
    return res.status(400).send('missing params')
  }

  // 提取密文
  const encrypted = extractEncryptFromXml(body)
  if (!encrypted) {
    return res.status(400).send('no Encrypt field')
  }

  // 验证签名
  const valid = verifySignature(
    config.wecom.token, timestamp, nonce, encrypted, msg_signature
  )

  if (!valid) {
    console.error('[callback] 消息签名验证失败')
    return res.status(403).send('signature verification failed')
  }

  // 解密
  try {
    const { content } = decryptMessage(
      encrypted, config.wecom.corpid, config.wecom.encodingAESKey
    )

    // 解析消息
    const msgType = extractXmlField(content, 'MsgType')
    const fromUser = extractXmlField(content, 'FromUserName')
    const createTime = extractXmlField(content, 'CreateTime')
    const msgId = extractXmlField(content, 'MsgId')

    console.log(`[callback] 收到消息: type=${msgType}, from=${fromUser}, msgId=${msgId}`)

    // 去重
    if (msgId && processedMsgIds.has(msgId)) {
      console.log('[callback] 重复消息, 跳过')
      return res.send('success')
    }
    if (msgId) {
      processedMsgIds.add(msgId)
      // 防止 Set 无限增长
      if (processedMsgIds.size > 1000) {
        const first = processedMsgIds.values().next().value
        processedMsgIds.delete(first)
      }
    }

    // 只处理文本消息, 其他类型记录日志
    if (msgType === 'text') {
      const text = extractXmlField(content, 'Content')
      messageQueue.push({
        from: fromUser,
        content: text,
        timestamp: createTime,
        msgId: msgId,
        receivedAt: Date.now(),
        acked: false,   // worker 是否已发过"收到"回执
        claimed: false, // 是否已被 worker 领取处理中 (防重复领取)
        replied: false, // WorkBuddy/worker 是否已发过正式回复
      })
      // 防止内存无限增长
      if (messageQueue.length > 200) messageQueue.shift()
      console.log(`[callback] 入队: "${text}" from ${fromUser} (队列: ${messageQueue.length} 条)`)
    } else if (msgType === 'event') {
      const eventType = extractXmlField(content, 'Event')
      console.log(`[callback] 事件: ${eventType} from ${fromUser}`)
      // 订阅/进入应用等事件, 可以回复欢迎语
      if (eventType === 'enter' || eventType === 'subscribe') {
        try {
          await wecom.sendTextMessage(fromUser, '我在呢, 说点什么?')
        } catch (e) {
          console.error('[callback] 回复欢迎语失败:', e.message)
        }
      }
    } else {
      console.log(`[callback] 不支持的消息类型: ${msgType}, 已忽略`)
    }

    // 企业微信要求在 5 秒内响应, 否则会重试
    // 直接返回 success, 后续处理由 WorkBuddy 异步完成
    res.send('success')
  } catch (err) {
    console.error('[callback] 处理失败:', err.message)
    res.status(500).send('processing failed')
  }
})

// ===== 内部 API (供 MCP Server 调用) =====

/**
 * GET /internal/messages - 获取尚未正式回复的消息 (不清除, 由 mark-replied 标记)
 * 返回字段含 acked, 供 worker 判断是否需要先发"收到"回执
 */
app.get('/internal/messages', (req, res) => {
  const now = Date.now()
  const messages = messageQueue
    .filter(m => !m.replied && (!m.claimed || (m.claimedAt && now - m.claimedAt > 180000)))
    .map(m => {
      // 领取: 标记为处理中, 防止同一轮询周期内被重复取出 (本地大模型较慢时尤其重要)
      m.claimed = true
      m.claimedAt = now
      return {
        from: m.from,
        content: m.content,
        timestamp: m.timestamp,
        msgId: m.msgId,
        acked: m.acked,
      }
    })
  res.json({ count: messages.length, messages })
})

/**
 * POST /internal/ack - 标记某条消息已发"收到"回执 (worker 去重用)
 */
app.post('/internal/ack', (req, res) => {
  const { msgId } = req.body || {}
  const m = messageQueue.find(x => x.msgId === msgId)
  if (m) m.acked = true
  res.json({ ok: true })
})

/**
 * POST /internal/mark-replied - 标记某条消息已正式回复 (防重复回复)
 * 返回 ok:false 若已回复, 调用方可据此避免竞态
 */
app.post('/internal/mark-replied', (req, res) => {
  const { msgId } = req.body || {}
  const m = messageQueue.find(x => x.msgId === msgId)
  if (!m) return res.json({ ok: false, reason: 'not_found' })
  if (m.replied) return res.json({ ok: false, reason: 'already' })
  m.replied = true
  res.json({ ok: true })
})

/**
 * POST /internal/send - 通过企业微信发送消息
 * body: { touser: string, content: string }
 */
app.post('/internal/send', async (req, res) => {
  try {
    const { touser, content } = req.body
    if (!touser || !content) {
      return res.status(400).json({ error: 'missing touser or content' })
    }
    const result = await wecom.sendTextMessage(touser, content)
    res.json({ success: true, result })
  } catch (err) {
    console.error('[internal/send] 发送失败:', err.message)
    res.status(500).json({ error: err.message })
  }
})

/**
 * POST /internal/send-card - 发送卡片消息
 * body: { touser, title, description, url }
 */
app.post('/internal/send-card', async (req, res) => {
  try {
    const { touser, title, description, url } = req.body
    if (!touser || !title) {
      return res.status(400).json({ error: 'missing touser or title' })
    }
    const result = await wecom.sendCardMessage(touser, title, description || '', url || '')
    res.json({ success: true, result })
  } catch (err) {
    console.error('[internal/send-card] 发送失败:', err.message)
    res.status(500).json({ error: err.message })
  }
})

/**
 * POST /internal/llm - 经反向隧道调用本地 Ollama (云端部署时用)
 * body: { messages: [...] } -> { content: string }
 */
app.post('/internal/llm', async (req, res) => {
  // 内部鉴权: /internal/llm 暴露在公网域名下, 必须携带共享密钥,
  // 否则任何人都能借你的本地 Ollama 算力反问。
  const auth = req.headers['x-internal-token'] || req.query.token
  if (auth !== config.tunnel.token) {
    return res.status(403).json({ error: 'forbidden' })
  }
  try {
    const { messages } = req.body || {}
    if (!Array.isArray(messages)) return res.status(400).json({ error: 'missing messages' })
    if (!tunnelConnected()) return res.status(503).json({ error: '本地客户端未连接' })
    const content = await requestLLM(messages)
    res.json({ content })
  } catch (err) {
    res.status(502).json({ error: err.message })
  }
})

/**
 * GET /health - 健康检查
 */
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    pendingMessages: messageQueue.length,
    tunnelConnected: tunnelConnected(), // 本地 Ollama 客户端是否在线
    llmMode: config.llm.mode,
    uptime: process.uptime(),
  })
})

// ===== 反向代理: 把非本桥自有路由的请求转发到同容器的 Java 应用 =====
// CloudBase 只暴露一个端口(80), 本桥作为边缘: /callback /tunnel /internal /health 自己处理,
// 其余路径(原 Java Web 服务)代理到 JAVA_UPSTREAM(默认 127.0.0.1:8080)。
// 这样微信回调/隧道和原有 Java 服务共用同一个公网域名, 互不干扰。
const BRIDGE_PREFIXES = ['/callback', '/tunnel', '/internal', '/health']
const JAVA_UPSTREAM = process.env.JAVA_UPSTREAM || 'http://127.0.0.1:8080'
app.use(
  '/',
  proxy(JAVA_UPSTREAM, {
    // 本桥自有路由不代理, 由上面的 Express 路由处理
    filter: (req) =>
      !BRIDGE_PREFIXES.some(
        (p) => req.path === p || req.path.startsWith(p + '/')
      ),
    proxyReqPathResolver: (req) => req.url,
  })
)

// ===== 启动 =====
const PORT = config.server.port
const server = app.listen(PORT, () => {
  console.log('')
  console.log('========================================')
  console.log('  WeCom Bridge for WorkBuddy')
  console.log('========================================')
  console.log(`  本地服务:  http://localhost:${PORT}`)
  console.log(`  回调地址:  http://localhost:${PORT}/callback`)
  console.log(`  健康检查: http://localhost:${PORT}/health`)
  if (config.publicBaseUrl) {
    console.log(`  企微回调:  ${config.publicBaseUrl.replace(/\/$/, '')}/callback`)
  } else {
    console.log(`  企微回调:  <你的企业域名>/callback  (设 PUBLIC_BASE_URL 可在此显示)`)
  }
  console.log(`  待处理消息: ${messageQueue.length} 条`)
  console.log('========================================')
  console.log('')
  console.log('等待企业微信消息...')
  console.log('')
})
attachTunnel(server)
console.log(`[tunnel] 反向隧道端点已挂载: /tunnel (LLM_MODE=${config.llm.mode})`)
