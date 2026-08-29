// 飞书入口: 使用官方 Node SDK 的「长连接(WebSocket)」模式接收消息。
// 关键点: 机器人主动外连飞书服务器, 无需任何公网 IP / 域名 / 证书 / 内网穿透。
// 收到消息 -> 调本地 Ollama (经 llm-client, 支持 direct/tunnel) -> 用飞书 API 回发。
//
// 前置 (在飞书开放平台配置):
//   1. 创建企业自建应用, 添加「机器人」能力
//   2. 事件与回调 -> 订阅方式选「使用长连接接收事件」
//   3. 添加事件 im.message.receive_v1
//   4. 权限: im:message, im:message:send_as_bot (以及 im:message.p2p_msg:readonly)
//   5. 拿到 App ID / App Secret, 填入环境变量 FEISHU_APP_ID / FEISHU_APP_SECRET
import * as Lark from '@larksuiteoapi/node-sdk'
import { config } from './config.js'
import { callLLM, SYSTEM_PROMPT } from './llm-client.js'

const APP_ID = process.env.FEISHU_APP_ID
const APP_SECRET = process.env.FEISHU_APP_SECRET

if (!APP_ID || !APP_SECRET) {
  console.error('[feishu] 未配置 FEISHU_APP_ID / FEISHU_APP_SECRET, 不启动飞书入口')
  process.exit(1)
}

const client = new Lark.Client({ appId: APP_ID, appSecret: APP_SECRET })
const wsClient = new Lark.WSClient({
  appId: APP_ID,
  appSecret: APP_SECRET,
  loggerLevel: Lark.LoggerLevel.info,
})

// 每个用户的对话历史 (进程内存)
const histories = new Map()
const MAX_HISTORY = 10

// 消息去重: 飞书长连接若 3s 内未 ACK 会重推, 用 message_id 去重避免重复回复
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

async function replyText(chatId, text) {
  const safe = truncateUtf8(text, 3000)
  try {
    await client.im.v1.message.create({
      params: { receive_id_type: 'chat_id' },
      data: {
        receive_id: chatId,
        content: JSON.stringify({ text: safe }),
        msg_type: 'text',
      },
    })
  } catch (e) {
    console.error('[feishu] 回发失败:', e.message)
  }
}

// 异步处理 (不阻塞事件 ACK, 避免 3s 超时重推)
async function handleAsync(data) {
  const msg = data?.message
  if (!msg) return
  if (!markSeen(msg.message_id)) return // 重复推送, 跳过

  // 仅处理单聊(p2p); 群聊暂不自动回复, 避免刷屏 (后续可加 @机器人 判断)
  if (msg.chat_type && msg.chat_type !== 'p2p') {
    console.log('[feishu] 忽略群聊消息 (chat_type=%s)', msg.chat_type)
    return
  }

  let text = ''
  try {
    const c = JSON.parse(msg.content || '{}')
    text = c.text || ''
  } catch { text = '' }
  if (!text) {
    await replyText(msg.chat_id, '⚠️ 暂时只支持文本消息。')
    return
  }

  const userId = msg.sender?.sender_id?.open_id || msg.chat_id
  let history = histories.get(userId) || []
  history.push({ role: 'user', content: text })
  const messages = [{ role: 'system', content: SYSTEM_PROMPT }, ...history.slice(-MAX_HISTORY)]

  console.log(`[feishu] 收到 from=${userId}: "${text}"`)
  try {
    const reply = await callLLM(messages)
    history.push({ role: 'assistant', content: reply })
    if (history.length > MAX_HISTORY * 2) history = history.slice(-MAX_HISTORY * 2)
    histories.set(userId, history)
    await replyText(msg.chat_id, reply)
    console.log(`[feishu] 已回复 ${userId}`)
  } catch (e) {
    console.error('[feishu] 处理失败:', e.message)
    await replyText(msg.chat_id, '⚠️ 本地 AI 调用失败: ' + e.message)
  }
}

wsClient.start({
  eventDispatcher: new Lark.EventDispatcher({}).register({
    'im.message.receive_v1': (data) => {
      // 立即返回, 异步处理, 保证快速 ACK
      handleAsync(data).catch((e) => console.error('[feishu] 未捕获异常:', e))
    },
  }),
})

console.log('[feishu] 长连接入口已启动 (等待飞书推送消息...)')
