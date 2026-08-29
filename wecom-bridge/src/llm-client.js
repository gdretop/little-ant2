// 统一的 LLM 调用入口, 支持三种引擎:
//   - workbuddy: 调用本机 CodeBuddy Code CLI (有工具能力, 能干活; 较慢)
//   - ollama/direct: 直接调用本地/远程 Ollama (OpenAI 兼容 /v1/chat/completions; 秒回)
//   - ollama/tunnel: 经反向隧道调用本机 Ollama (云端部署时, LLM 算力留在用户 Mac)
// worker(企微) 与 dingtalk-ingest(钉钉) 共用此模块, 保证行为一致。
import axios from 'axios'
import { spawn } from 'child_process'
import { config } from './config.js'
import { requestLLM, tunnelConnected } from './tunnel-server.js'

// 外部 LLM: 本机 Ollama 需绕过代理; 远程 LLM 走环境代理
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

const httpLLM = axios.create({
  timeout: 60000,
  proxy: isLocalhost(config.llm.baseUrl) ? false : proxyFromEnv(),
})

export const SYSTEM_PROMPT = `你是一个名为 WorkBuddy 的个人 AI 助手，运行在用户的设备上。
用户通过聊天软件（飞书/微信等）给你发消息，你需要理解他的意图并帮忙完成任务（搜索资料、写代码、数据分析、总结、提醒、翻译等），然后把结果发回去。
要求：
- 用简体中文，语气自然、像朋友帮忙
- 适合手机屏幕阅读：多用换行和短句，不要使用 Markdown 标题(#)和过宽表格
- 如果任务需要，可以给出代码片段、列表、要点
- 遇到无法完成的，诚实说明并给出替代建议`

// ===== WorkBuddy (CodeBuddy Code CLI) 引擎 =====
// 通过子进程调用 `codebuddy --print "<prompt>"`, 输出即最终结果。
// 相比 Ollama: 具备工具能力(读写文件/执行命令/调连接器), 但更慢。

// CLI 每次是新会话, 需把历史拼进 prompt 才能保持连贯
function flattenForWorkBuddy(messages, historyTurns) {
  const sys = messages.filter((m) => m.role === 'system').map((m) => m.content).join('\n\n')
  const rest = messages.filter((m) => m.role !== 'system')
  const recent = rest.slice(-(historyTurns * 2 + 1))
  const conv = recent
    .map((m) => `${m.role === 'user' ? '用户' : '助手'}: ${m.content}`)
    .join('\n')
  return `${sys ? sys + '\n\n---\n\n' : ''}以下是你和用户最近的对话:\n${conv}\n\n请回答用户最后一条消息。直接给出回答内容, 不要复述问题, 不要写"助手:"前缀。`
}

export async function callWorkBuddy(messages, { timeout, cwd } = {}) {
  const wb = config.workbuddy
  const prompt = flattenForWorkBuddy(messages, wb.historyTurns)
  const ms = timeout || wb.timeoutMs
  const args = ['--print', prompt]
  if (wb.skipPermissions) args.push('--dangerously-skip-permissions')

  return new Promise((resolve, reject) => {
    let done = false
    let out = ''
    let err = ''
    let child
    try {
      child = spawn(wb.cli, args, {
        cwd: cwd || wb.cwd,
        env: { ...process.env },
        stdio: ['ignore', 'pipe', 'pipe'],
      })
    } catch (e) {
      return reject(new Error('WorkBuddy 启动失败: ' + e.message))
    }

    const timer = setTimeout(() => {
      if (done) return
      done = true
      try { child.kill('SIGKILL') } catch { /* ignore */ }
      reject(new Error(`WorkBuddy 超时 (${Math.round(ms / 1000)}s)`))
    }, ms)

    child.stdout.on('data', (d) => { out += d.toString() })
    child.stderr.on('data', (d) => { err += d.toString(); })
    child.on('error', (e) => {
      if (done) return
      done = true
      clearTimeout(timer)
      reject(new Error('WorkBuddy 执行出错: ' + e.message))
    })
    child.on('close', (code) => {
      if (done) return
      done = true
      clearTimeout(timer)
      const text = out.trim()
      if (text) return resolve(text)
      reject(new Error(`WorkBuddy 无输出 (exit=${code})${err ? ' | ' + err.slice(0, 300) : ''}`))
    })
  })
}

// 统一调度: engine 为 'workbuddy' 或 'ollama'
export async function callEngine(messages, engine) {
  if (engine === 'workbuddy') return await callWorkBuddy(messages)
  return await callLLM(messages)
}

// messages: 完整 OpenAI 格式消息数组 (含 system)
export async function callLLM(messages) {
  if (config.llm.mode === 'tunnel') {
    if (!tunnelConnected()) throw new Error('本地客户端未连接 (隧道未建立)')
    return await requestLLM(messages)
  }
  const headers = { 'Content-Type': 'application/json' }
  if (config.llm.apiKey && config.llm.apiKey !== 'ollama') {
    headers['Authorization'] = `Bearer ${config.llm.apiKey}`
  }
  const resp = await httpLLM.post(
    `${config.llm.baseUrl}/chat/completions`,
    {
      model: config.llm.model,
      messages,
      max_tokens: 2000,
      temperature: 0.7,
      stream: false,
    },
    { headers }
  )
  return resp.data?.choices?.[0]?.message?.content || '(空回复)'
}
