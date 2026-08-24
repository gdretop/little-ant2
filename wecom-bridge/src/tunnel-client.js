// ============================================================
// 本地隧道客户端 (运行在你的 Mac 上, 常驻)
// 主动长连云端 bridge 的 /tunnel WebSocket, 收到 LLM 请求时
// 调用本机 Ollama (http://localhost:11434/v1) 并把结果回传。
// 这样云端 bridge 能"反向"使用你本地的免费大模型, 无需任何公网端口。
// ============================================================
import WebSocket from 'ws'
import axios from 'axios'
import { config } from './config.js'

const WS_URL = (process.env.TUNNEL_URL || 'ws://localhost:3000/tunnel')
  + '?token=' + (process.env.TUNNEL_TOKEN || 'change-me-tunnel-token')

const OLLAMA_BASE = config.llm.baseUrl   // 本地 Ollama: http://localhost:11434/v1
const OLLAMA_MODEL = config.llm.model

let ws
let heartbeat

function connect() {
  ws = new WebSocket(WS_URL)
  ws.on('open', () => {
    console.log('[tunnel-client] 已连上云端 bridge ✅')
    heartbeat = setInterval(() => {
      if (ws.readyState === 1) ws.send(JSON.stringify({ type: 'ping' }))
    }, 25000)
  })
  ws.on('message', async (data) => {
    let msg
    try { msg = JSON.parse(data.toString()) } catch { return }
    if (msg.type !== 'chat') return
    try {
      const resp = await axios.post(
        `${OLLAMA_BASE}/chat/completions`,
        {
          model: OLLAMA_MODEL,
          messages: msg.messages,
          max_tokens: 2000,
          temperature: 0.7,
          stream: false,
        },
        { timeout: 60000, proxy: false } // 本机 Ollama 必须绕过代理
      )
      const content = resp.data?.choices?.[0]?.message?.content || ''
      ws.send(JSON.stringify({ type: 'chat_result', id: msg.id, content }))
    } catch (e) {
      ws.send(JSON.stringify({
        type: 'chat_result',
        id: msg.id,
        content: '⚠️ 本地模型调用失败: ' + e.message,
      }))
    }
  })
  ws.on('close', () => {
    clearInterval(heartbeat)
    console.log('[tunnel-client] 断开, 5s 后重连...')
    setTimeout(connect, 5000)
  })
  ws.on('error', () => { /* 由 close 统一处理重连 */ })
}

connect()
