// 钉钉入口: 使用官方 Node SDK 的「Stream 模式」(WebSocket 长连接) 接收机器人消息。
// 关键点: 机器人主动外连钉钉, 无需任何公网 IP / 域名 / 证书 / 内网穿透。
// 收到消息 -> 调本地 Ollama (经 llm-client, 支持 direct/tunnel) -> 用 sessionWebhook 回发。
//
// 前置 (在钉钉开发者后台配置):
//   1. 创建企业内部应用, 获取 Client ID (AppKey) / Client Secret (AppSecret)
//   2. 应用能力 -> 添加「机器人」, 消息接收模式选「Stream 模式」
//   3. 发布应用 (可见范围选自己或全员)
//   4. 把 Client ID / Client Secret 填入 DINGTALK_CLIENT_ID / DINGTALK_CLIENT_SECRET
import 'dotenv/config'
import { DWClient, TOPIC_ROBOT } from 'dingtalk-stream'
import axios from 'axios'
import { config } from './config.js'
import { callEngine, SYSTEM_PROMPT } from './llm-client.js'

const CLIENT_ID = process.env.DINGTALK_CLIENT_ID
const CLIENT_SECRET = process.env.DINGTALK_CLIENT_SECRET

if (!CLIENT_ID || !CLIENT_SECRET) {
  console.error('[dingtalk] 未配置 DINGTALK_CLIENT_ID / DINGTALK_CLIENT_SECRET, 不启动钉钉入口')
  process.exit(1)
}

const client = new DWClient({ clientId: CLIENT_ID, clientSecret: CLIENT_SECRET })

// 每个用户的对话历史 (进程内存)
const histories = new Map()
const MAX_HISTORY = 10

// 消息去重: Stream 模式若 60s 内未 ACK 会重推, 用 msgId 去重避免重复回复
const seen = new Set()
function markSeen(id) {
  if (seen.has(id)) return false
  seen.add(id)
  setTimeout(() => seen.delete(id), 10 * 60 * 1000)
  return true
}

function truncateUtf8(str, maxBytes) {
  let s = String(str)
  while (Buffer.byteLength(s, 'utf8') > maxBytes) s = s.slice(0, s.length - 1)
  return s
}

// 通过 sessionWebhook 回发 (Stream 模式机器人专用回发通道, 有时效)
// WorkBuddy 输出可能很长, 超出钉钉限制时截断并提示
async function replyViaWebhook(sessionWebhook, text) {
  const raw = String(text)
  const MAX_BYTES = 3000
  let safe = raw
  if (Buffer.byteLength(raw, 'utf8') > MAX_BYTES) {
    safe = truncateUtf8(raw, MAX_BYTES - 60) + '\n\n…(内容过长已截断)'
  }
  try {
    await axios.post(
      sessionWebhook,
      { msgtype: 'text', text: { content: safe } },
      { timeout: 15000, proxy: false, headers: { 'Content-Type': 'application/json' } },
    )
  } catch (e) {
    console.error('[dingtalk] 回发失败:', e.message)
  }
}

// 异步处理 (不阻塞 ACK, 避免 60s 超时重推)
async function handleAsync(msg) {
  if (!markSeen(msg.msgId)) return // 重复推送, 跳过

  // 仅处理单聊(conversationType === '1'); 群聊暂不自动回复, 避免刷屏 (后续可加 @机器人 判断)
  if (msg.conversationType && msg.conversationType !== '1') {
    console.log('[dingtalk] 忽略群聊消息 (conversationType=%s)', msg.conversationType)
    return
  }

  let text = ''
  if (msg.msgtype === 'text' && msg.text) text = msg.text.content || ''
  if (!text) {
    await replyViaWebhook(msg.sessionWebhook, '⚠️ 暂时只支持文本消息。')
    return
  }

  // 引擎路由: 默认 WorkBuddy(能干活); 以 /ol 开头则切到本地 Ollama(秒回)
  // 也支持 /wb 显式指定 WorkBuddy, 便于以后把默认引擎改成 ollama
  let engine = config.llm.engine || 'workbuddy'
  let cmdMatch = text.match(/^\/(ol|wb)\s*/i)
  if (cmdMatch) {
    engine = cmdMatch[1].toLowerCase() === 'ol' ? 'ollama' : 'workbuddy'
    text = text.slice(cmdMatch[0].length).trim()
    if (!text) {
      await replyViaWebhook(
        msg.sessionWebhook,
        engine === 'ollama'
          ? '已切换到本地模型模式。用法: /ol 你的问题'
          : '已切换到 WorkBuddy 模式。用法: /wb 你的问题',
      )
      return
    }
  }

  const userId = msg.senderStaffId || msg.senderId || msg.conversationId
  let history = histories.get(userId) || []
  history.push({ role: 'user', content: text })
  const messages = [{ role: 'system', content: SYSTEM_PROMPT }, ...history.slice(-MAX_HISTORY)]

  console.log(`[dingtalk] 收到 from=${userId} [${engine}]: "${text}"`)
  const t0 = Date.now()
  try {
    const reply = await callEngine(messages, engine)
    history.push({ role: 'assistant', content: reply })
    if (history.length > MAX_HISTORY * 2) history = history.slice(-MAX_HISTORY * 2)
    histories.set(userId, history)
    await replyViaWebhook(msg.sessionWebhook, reply)
    console.log(`[dingtalk] 已回复 ${userId} [${engine}] 耗时 ${((Date.now() - t0) / 1000).toFixed(1)}s`)
  } catch (e) {
    console.error(`[dingtalk] 处理失败 [${engine}]:`, e.message)
    const hint = engine === 'workbuddy' ? '\n\n(可加 /ol 前缀改用本地模型)' : ''
    await replyViaWebhook(msg.sessionWebhook, `⚠️ ${engine === 'workbuddy' ? 'WorkBuddy' : '本地模型'}调用失败: ${e.message}${hint}`)
  }
}

client.registerCallbackListener(TOPIC_ROBOT, (res) => {
  const messageId = res.headers?.messageId
  // 立即 ACK, 防止服务端在 60s 内重推
  if (messageId) client.socketCallBackResponse(messageId, {})
  let msg
  try {
    msg = JSON.parse(res.data || '{}')
  } catch {
    return
  }
  handleAsync(msg).catch((e) => console.error('[dingtalk] 未捕获异常:', e))
})

client
  .connect()
  .then(() => {
    console.log('[dingtalk] Stream 长连接已建立 ✅ (等待钉钉推送消息...)')
    console.log(`[dingtalk] 默认引擎: ${config.llm.engine}  |  /ol <问题>=本地Ollama  /wb <问题>=WorkBuddy`)
  })
  .catch((e) => {
    console.error('[dingtalk] 连接失败:', e.message)
    process.exit(1)
  })
