import axios from 'axios'
import { config } from './config.js'

// 内部 API (localhost): 必须绕过沙箱代理
const httpInternal = axios.create({
  baseURL: config.server.internalBaseUrl,
  proxy: false,
  timeout: 10000,
})

// 外部 LLM: 尊重 HTTP(S)_PROXY 环境变量 (沙箱里需走代理才能出网; 本机无代理则直连)
// 但若 LLM 指向本机 (Ollama), 必须绕过代理, 否则 localhost 请求会被代理拦截
function proxyFromEnv() {
  const p = process.env.HTTPS_PROXY || process.env.https_proxy || process.env.HTTP_PROXY || process.env.http_proxy
  if (!p) return false
  try {
    const u = new URL(p)
    return { host: u.hostname, port: parseInt(u.port, 10) || 80 }
  } catch { return false }
}
function isLocalhost(url) {
  try {
    const u = new URL(url)
    return u.hostname === 'localhost' || u.hostname === '127.0.0.1' || u.hostname === '::1'
  } catch { return false }
}

const { baseUrl: LLM_BASE, apiKey: LLM_KEY, model: LLM_MODEL, pollIntervalMs: POLL } = config.llm

const httpLLM = axios.create({
  timeout: 60000,
  proxy: isLocalhost(LLM_BASE) ? false : proxyFromEnv(),
})

// 每个用户的对话历史 (进程内存, 断线不持久)
const histories = new Map()
const MAX_HISTORY = 10 // 保留最近 10 轮

const SYSTEM_PROMPT = `你是一个名为 WorkBuddy 的微信个人助手，运行在用户的个人微信里。
用户通过微信给你发消息，你需要理解他的意图并帮忙完成任务（搜索资料、写代码、数据分析、总结、提醒、翻译等），然后把结果发回微信。
要求：
- 用简体中文，语气自然、像朋友帮忙
- 适合手机屏幕阅读：多用换行和短句，不要使用 Markdown 标题(#)和过宽表格
- 如果任务需要，可以给出代码片段、列表、要点
- 遇到无法完成的，诚实说明并给出替代建议`

async function getPending() {
  const res = await httpInternal.get('/internal/messages')
  return res.data.messages || []
}

// 企业微信文本消息上限约 2048 字节, 超出会被 API 拒绝, 故按字节截断
function truncateUtf8(str, maxBytes) {
  let s = String(str)
  while (Buffer.byteLength(s, 'utf8') > maxBytes) s = s.slice(0, s.length - 1)
  return s
}

async function sendReply(touser, content) {
  const safe = truncateUtf8(content, 1900)
  await httpInternal.post('/internal/send', { touser, content: safe })
}

async function ack(msgId) {
  try { await httpInternal.post('/internal/ack', { msgId }) } catch (_) { /* ignore */ }
}

async function markReplied(msgId) {
  try {
    const r = await httpInternal.post('/internal/mark-replied', { msgId })
    return r.data?.ok === true
  } catch (_) { return false }
}

async function callLLM(userid, userText) {
  let history = histories.get(userid) || []
  history.push({ role: 'user', content: userText })
  const messages = [
    { role: 'system', content: SYSTEM_PROMPT },
    ...history.slice(-MAX_HISTORY),
  ]
  let reply
  if (config.llm.mode === 'tunnel') {
    // 云端 -> /internal/llm -> 隧道 -> 本地 Ollama
    const r = await httpInternal.post('/internal/llm', { messages }, {
      timeout: 65000,
      headers: { 'x-internal-token': config.tunnel.token },
    })
    reply = r.data?.content || '(空回复)'
  } else {
    const headers = { 'Content-Type': 'application/json' }
    // Ollama 本地端点不需要鉴权; 仅当配置了真实 Key 时才带 Authorization
    if (LLM_KEY && LLM_KEY !== 'ollama') {
      headers['Authorization'] = `Bearer ${LLM_KEY}`
    }
    const resp = await httpLLM.post(
      `${LLM_BASE}/chat/completions`,
      {
        model: LLM_MODEL,
        messages,
        max_tokens: 2000,
        temperature: 0.7,
        stream: false,
      },
      { headers }
    )
    reply = resp.data?.choices?.[0]?.message?.content || '(空回复)'
  }
  history.push({ role: 'assistant', content: reply })
  if (history.length > MAX_HISTORY * 2) history = history.slice(-MAX_HISTORY * 2)
  histories.set(userid, history)
  return reply
}

async function processMessage(msg) {
  console.log(`[worker] 处理消息 from=${msg.from}: "${msg.content}"`)
  try {
    const reply = await callLLM(msg.from, msg.content)
    // 二次确认本消息还没被回复 (防与 WorkBuddy 自动化竞态)
    if (!(await markReplied(msg.msgId))) {
      console.log(`[worker] 消息 ${msg.msgId} 已被其他处理者回复, 跳过`)
      return
    }
    await sendReply(msg.from, reply)
    console.log(`[worker] 已回复 ${msg.from}`)
  } catch (e) {
    console.error('[worker] 处理失败:', e.message)
    try {
      await sendReply(msg.from, '⚠️ 智能回复暂时不可用（模型接口异常），稍后我再试。你也可直接在 WorkBuddy 对话框里 @我 处理。')
      await markReplied(msg.msgId)
    } catch (_) { /* ignore */ }
  }
}

async function sendAck(msg) {
  // 没配大模型时, 先回一条"收到"让用户知道机器人活着, 正式回复交给 WorkBuddy 自动化
  try {
    await sendReply(
      msg.from,
      '⏳ 收到！WorkBuddy 正在处理你的请求，稍后回复你～\n（开启实时智能回复：把你的 hy3 / ModelScope token 发我即可秒回）'
    )
    await ack(msg.msgId)
    console.log(`[worker] 已发"收到"回执 to ${msg.from}`)
  } catch (e) {
    console.error('[worker] 发回执失败:', e.message)
  }
}

async function loop() {
  try {
    const messages = await getPending()
    if (messages.length) {
      console.log(`[worker] 取到 ${messages.length} 条待处理消息`)
      for (const m of messages) {
        if (LLM_KEY) {
          await processMessage(m)
        } else if (!m.acked) {
          await sendAck(m)
        }
      }
    }
  } catch (e) {
    console.error('[worker] 轮询失败:', e.message)
  }
}

console.log(`WeCom Bridge Worker 启动 (轮询间隔 ${POLL}ms, LLM=${LLM_MODEL}, 已配置Key=${!!LLM_KEY})`)
// 延迟首轮, 等服务先起来 (避免与 server 同时启动时 ECONNREFUSED)
setTimeout(loop, 2000)
setInterval(loop, POLL)
