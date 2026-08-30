// 统一的 LLM 调用入口, 支持三种引擎:
//   - workbuddy: 调用本机 CodeBuddy Code CLI (有工具能力, 能干活; 较慢)
//   - ollama/direct: 直接调用本地/远程 Ollama (OpenAI 兼容 /v1/chat/completions; 秒回)
//   - ollama/tunnel: 经反向隧道调用本机 Ollama (云端部署时, LLM 算力留在用户 Mac)
// worker(企微) 与 dingtalk-ingest(钉钉) 共用此模块, 保证行为一致。
import axios from 'axios'
import { spawn } from 'child_process'
import fs, { existsSync } from 'fs'
import path from 'path'
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

// ===== ant-harness 工作区感知 =====
// 让 WorkBuddy 引擎(有文件工具能力) 知道 ant-harness 里有哪些可复用 skills 与 knowledge,
// 在用户问题涉及本地知识/技能时主动查阅并按步骤执行。
function readFrontmatterField(file, field) {
  try {
    const txt = fs.readFileSync(file, 'utf8')
    const m = txt.match(new RegExp('^' + field + ':\\s*(.+)$', 'm'))
    return m ? m[1].trim().replace(/^["']|["']$/g, '') : ''
  } catch {
    return ''
  }
}

let _workspaceInventory = null
function getWorkspaceInventory() {
  if (_workspaceInventory) return _workspaceInventory
  const root = config.workbuddy.workspaceRoot
  const parts = []
  const skillsDir = path.join(root, 'skills')
  const knowledgeDir = path.join(root, 'knowledge')
  if (fs.existsSync(skillsDir)) {
    const skills = fs
      .readdirSync(skillsDir, { withFileTypes: true })
      .filter((d) => d.isDirectory())
      .map((d) => {
        const sk = path.join(skillsDir, d.name, 'SKILL.md')
        const desc = fs.existsSync(sk) ? readFrontmatterField(sk, 'description') : ''
        return `- skills/${d.name}${desc ? '：' + desc : ''}`
      })
    if (skills.length) parts.push('【可复用技能 skills/】\n' + skills.join('\n'))
  }
  if (fs.existsSync(knowledgeDir)) {
    const topics = fs
      .readdirSync(knowledgeDir, { withFileTypes: true })
      .filter((d) => d.isDirectory())
      .map((d) => d.name)
    if (topics.length) parts.push('【知识库 knowledge/】\n' + topics.map((t) => `- ${t}`).join('\n'))
  }
  _workspaceInventory = parts.join('\n\n')
  return _workspaceInventory
}

// 每次调用 WorkBuddy 时附在 prompt 末尾, 让它"懂"有哪些本地技能/知识可用
export const WORKBUDDY_CONTEXT = (() => {
  const root = config.workbuddy.workspaceRoot
  const inv = getWorkspaceInventory()
  return `你当前运行在个人工作区 ant-harness (${root}) 下。
该工作区维护了一套可复用的【技能库 skills/】与【知识库 knowledge/】，并有一个总索引文件 workspace-tree。
当用户的问题涉及这些已有知识、技能，或可在此工作区完成的任务时，请：
1. 先用 Read 工具查看 ${root}/workspace-tree 了解整体结构；
2. 查看对应的 skills/<名称>/SKILL.md 或 knowledge/<主题>/ 下的文件，理解可用资源与步骤；
3. 按技能文档的步骤执行（如调用命令、读写文件、发钉钉通知等）；
4. 若用户只是闲聊或问题与本地知识无关，正常回答即可，无需强制查阅。

当前工作区可用资源清单：
${inv || '(暂无)'}
（注意：以上清单在机器人进程启动时生成，若你新增了技能/知识，请提示用户重启机器人以刷新本清单。）`
})()

// ===== WorkBuddy (CodeBuddy Code CLI) 引擎 =====
// 通过子进程调用 `codebuddy --print "<prompt>"`, 输出即最终结果。
// 相比 Ollama: 具备工具能力(读写文件/执行命令/调连接器), 但更慢。

// ⚠️ 关键坑: codebuddy 脚本首行是 `#!/usr/bin/env node`。
// 本服务由 launchd 启动时 PATH 是最小化的(/usr/bin:/bin:/usr/sbin:/sbin),
// 找不到用户安装的 node → 直接 spawn 脚本会 exit=127 "env: node: No such file or directory"。
// 因此必须显式用 node 的绝对路径来启动脚本, 并把 node 所在目录注入子进程 PATH。
function resolveNodeBin() {
  // 优先用当前进程的 node (一定是可用的)
  if (process.execPath && existsSync(process.execPath)) return process.execPath
  const candidates = [
    process.env.WORKBUDDY_NODE,
    '/Users/littleant/.workbuddy/binaries/node/versions/22.22.2/bin/node',
    '/opt/homebrew/bin/node',
    '/usr/local/bin/node',
    '/usr/bin/node',
  ].filter(Boolean)
  for (const c of candidates) if (existsSync(c)) return c
  return 'node' // 兜底: 交给 PATH
}

// 构造子进程环境: 补上 node / homebrew 目录, 保证 CLI 内部再次调用 node 也能找到
function buildChildEnv(nodeBin) {
  const extra = []
  if (nodeBin && nodeBin !== 'node') extra.push(path.dirname(nodeBin))
  // 补上 /usr/sbin /sbin: 否则 CLI 内部调用 ioreg 等系统命令会 "command not found"
  extra.push('/opt/homebrew/bin', '/usr/local/bin', '/usr/sbin', '/sbin')
  const base = process.env.PATH || '/usr/bin:/bin:/usr/sbin:/sbin'
  return {
    ...process.env,
    PATH: [...new Set([...extra, base].filter(Boolean))].join(':'),
    HOME: process.env.HOME || '/Users/littleant',
  }
}

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

// 单次调用: 指定模型执行, 返回输出文本
function runWorkBuddy(prompt, model, { timeout, cwd } = {}) {
  const wb = config.workbuddy
  const ms = timeout || wb.timeoutMs
  const args = ['--print', prompt]
  if (model) args.push('--model', model)
  if (wb.skipPermissions) args.push('--dangerously-skip-permissions')

  return new Promise((resolve, reject) => {
    let done = false
    let out = ''
    let err = ''
    let child
    const nodeBin = resolveNodeBin()
    try {
      // 用绝对路径的 node 启动脚本, 避免 launchd 最小 PATH 导致 exit=127
      child = spawn(nodeBin, [wb.cli, ...args], {
        cwd: cwd || wb.cwd,
        env: buildChildEnv(nodeBin),
        stdio: ['ignore', 'pipe', 'pipe'],
      })
    } catch (e) {
      return reject(new Error(`WorkBuddy 启动失败 (node=${nodeBin}): ${e.message}`))
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

// 复杂任务关键词: 命中则用旗舰档 (否则用免费/经济档)
const PREMIUM_HINTS = [
  // 分析/设计类
  '分析', '设计方案', '架构', '技术方案', '总结', '对比', '调研', '研究',
  '评估', '规划', '策略', '原理', '报告',
  // 工程类
  '优化', '重构', '写代码', '实现', '调试', '排查', '定位问题', '报错', '异常', 'bug',
  '脚本', '函数', '程序', '搭建', '知识库', '数据库', '框架',
  // 英文
  'code', 'implement', 'debug', 'analyze', 'design', 'architect', 'refactor', 'optimize',
]

// 根据任务复杂度选择模型: 免费/经济档 还是 旗舰档
export function pickWorkBuddyModel(messages, { forcePremium = false } = {}) {
  const wb = config.workbuddy
  if (forcePremium) return wb.premiumModel
  if (!wb.autoEscalate) return wb.model

  const last = [...messages].reverse().find((m) => m.role === 'user')
  const text = (last?.content || '').trim()
  // 长文本 → 复杂
  if (text.length > wb.escalateLength) return wb.premiumModel
  // 含复杂度关键词 / 代码块 → 复杂
  const lower = text.toLowerCase()
  if (lower.includes('```')) return wb.premiumModel
  if (PREMIUM_HINTS.some((h) => lower.includes(h.toLowerCase()))) return wb.premiumModel
  return wb.model
}

// 对外入口: 自动选模型; 失败时沿「所选模型 → 旗舰档 → 兜底」逐级升级
export async function callWorkBuddy(messages, opts = {}) {
  const wb = config.workbuddy
  const prompt = flattenForWorkBuddy(messages, wb.historyTurns) + '\n\n' + WORKBUDDY_CONTEXT
  const primary = pickWorkBuddyModel(messages, opts)

  // 尝试顺序: 首选 → 次选(Hy3) → 旗舰档 (去重后依次降级/升级尝试)
  // 简单任务: hy4-preview → hy3 → hy4-preview-x
  // 复杂任务: hy4-preview-x → hy4-preview → hy3
  const order = [wb.model, wb.fallbackModel, wb.premiumModel].filter(Boolean)
  const chain = [primary, ...order.filter((m) => m !== primary)]
  if (!wb.autoFallback) chain.length = 1 // 关闭自动切换则只用首选

  let lastErr
  for (let i = 0; i < chain.length; i++) {
    const model = chain[i]
    try {
      const out = await runWorkBuddy(prompt, model, opts)
      if (i > 0) console.warn(`[workbuddy] ✅ 已升级到 ${model} 完成`)
      return out
    } catch (e) {
      lastErr = e
      const next = i < chain.length - 1 ? `, 尝试升级到 ${chain[i + 1]}` : ' (已无更高可用)'
      console.warn(`[workbuddy] 模型 ${model} 失败 (${e.message})${next}`)
    }
  }
  throw lastErr
}

// 统一调度: engine 为 'workbuddy' 或 'ollama'
export async function callEngine(messages, engine, opts = {}) {
  if (engine === 'workbuddy') return await callWorkBuddy(messages, opts)
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
