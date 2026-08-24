// 本地全链路自测: 验证反向隧道 (云端 bridge → 隧道 → 本地 Ollama)
// 启动 server + tunnel-client, 等隧道连通后调用 /internal/llm,
// 确认回复来自本机 Ollama (证明 云端→隧道→本地 链路打通)。
// 用法: node scripts/test-tunnel.mjs
import 'dotenv/config'
import { spawn } from 'child_process'
import axios from 'axios'

const NODE = process.execPath
const PORT = process.env.PORT || '3000'
const BASE = `http://localhost:${PORT}`
const sleep = (ms) => new Promise((r) => setTimeout(r, ms))

function start(name, args) {
  const p = spawn(NODE, args, { env: process.env, stdio: ['ignore', 'pipe', 'pipe'] })
  p.stdout.on('data', (d) => process.stdout.write(`[${name}] ${d}`))
  p.stderr.on('data', (d) => process.stderr.write(`[${name}] ${d}`))
  return p
}

async function waitTunnel(timeoutMs = 20000) {
  const start = Date.now()
  while (Date.now() - start < timeoutMs) {
    try {
      const r = await axios.get(`${BASE}/health`, { timeout: 2000 })
      if (r.data?.tunnelConnected) return true
    } catch {
      /* server not ready yet */
    }
    await sleep(500)
  }
  return false
}

async function main() {
  console.log('🚀 启动 server + tunnel-client (本机 Ollama 经隧道) ...\n')
  const server = start('server', ['src/server.js'])
  await sleep(1500) // 等 server attach 隧道端点
  const client = start('tunnel-client', ['src/tunnel-client.js'])

  if (!(await waitTunnel())) {
    console.error('\n❌ 隧道客户端未能连上 server (tunnelConnected=false)')
    server.kill('SIGTERM')
    client.kill('SIGTERM')
    process.exit(1)
  }
  console.log('\n✅ 隧道已连通 (本地 Ollama 客户端在线)')

  const messages = [
    { role: 'system', content: '你是一个测试助手, 务必用一句话且不超过 15 个字回答。' },
    { role: 'user', content: '用中文回答: 1+1 等于几?' },
  ]
  let content = ''
  try {
    const r = await axios.post(
      `${BASE}/internal/llm`,
      { messages },
      { timeout: 70000, headers: { 'x-internal-token': process.env.TUNNEL_TOKEN || '' } }
    )
    content = r.data?.content || ''
  } catch (e) {
    console.error('\n❌ 调用 /internal/llm 失败:', e.response?.data || e.message)
    server.kill('SIGTERM')
    client.kill('SIGTERM')
    process.exit(1)
  }

  if (!content || content.trim().length < 1) {
    console.error('\n❌ 隧道调用本地 LLM 返回空')
    server.kill('SIGTERM')
    client.kill('SIGTERM')
    process.exit(1)
  }

  console.log('\n🤖 本地 Ollama 经隧道返回:', content.trim())
  console.log('✅ 全链路测试通过: 云端(/internal/llm) → 隧道 → 本地 Ollama → 回发\n')

  server.kill('SIGTERM')
  client.kill('SIGTERM')
  await sleep(500)
  process.exit(0)
}

main().catch((e) => {
  console.error(e)
  process.exit(1)
})
