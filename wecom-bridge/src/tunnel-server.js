// ============================================================
// 云端隧道服务端 (运行在 CloudBase 容器里的 bridge 进程)
// 挂载到 Express 的 HTTP server 上, 提供 WebSocket 端点 /tunnel,
// 供本地 Mac 客户端长连接。云端 worker 经 /internal/llm -> requestLLM()
// 把 LLM 请求通过隧道发给本地 Ollama 处理, 结果原路返回。
// ============================================================
import { WebSocketServer } from 'ws'
import crypto from 'crypto'

const TUNNEL_TOKEN = process.env.TUNNEL_TOKEN || 'change-me-tunnel-token'

const pending = new Map() // id -> { resolve, reject, timer }
let clientWs = null
let wss = null

export function attachTunnel(server) {
  wss = new WebSocketServer({ noServer: true })

  server.on('upgrade', (req, socket, head) => {
    let url
    try { url = new URL(req.url, 'http://localhost') } catch { socket.destroy(); return }
    // 只接管 /tunnel 路径, 其它 upgrade 请求原样放过
    if (url.pathname !== '/tunnel') return
    const token = url.searchParams.get('token')
    if (token !== TUNNEL_TOKEN) {
      socket.write('HTTP/1.1 401 Unauthorized\r\n\r\n')
      socket.destroy()
      return
    }
    wss.handleUpgrade(req, socket, head, (ws) => {
      clientWs = ws
      console.log('[tunnel] 本地客户端已连接 ✅')
      ws.on('message', (data) => handleMessage(data))
      ws.on('close', () => {
        if (clientWs === ws) { clientWs = null; console.log('[tunnel] 本地客户端断开') }
      })
      ws.on('error', () => {})
    })
  })
}

function handleMessage(data) {
  let msg
  try { msg = JSON.parse(data.toString()) } catch { return }
  if (msg.type === 'chat_result') {
    const p = pending.get(msg.id)
    if (!p) return
    clearTimeout(p.timer)
    pending.delete(msg.id)
    p.resolve(msg.content)
  }
  // 'pong' 等其它类型忽略
}

// 云端调用: 把对话 messages 经隧道发给本地 Ollama, 返回生成的文本
export function requestLLM(messages, { timeout = 60000 } = {}) {
  return new Promise((resolve, reject) => {
    if (!clientWs || clientWs.readyState !== 1) {
      return reject(new Error('本地客户端未连接'))
    }
    const id = crypto.randomUUID()
    const timer = setTimeout(() => {
      pending.delete(id)
      reject(new Error('本地 LLM 超时 (60s)'))
    }, timeout)
    pending.set(id, { resolve, reject, timer })
    clientWs.send(JSON.stringify({ type: 'chat', id, messages }))
  })
}

export function tunnelConnected() {
  return !!clientWs && clientWs.readyState === 1
}
