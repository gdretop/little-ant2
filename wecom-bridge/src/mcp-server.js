import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js'
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js'
import { z } from 'zod'
import axios from 'axios'

const BASE_URL = process.env.INTERNAL_BASE_URL || 'http://localhost:3000'

// 本地通信强制直连，避免环境代理变量 (http_proxy 等) 拦截 localhost 请求
const http = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  proxy: false,
})

const server = new McpServer({
  name: 'wecom-bridge',
  version: '1.0.0',
})

// ===== 工具 1: 获取待处理微信消息 =====
server.tool(
  'get_pending_wechat_messages',
  '获取并清空企业微信消息队列中所有待处理消息。每次调用会取出全部新消息并清空队列。返回消息内容和发送者ID，用于后续回复。',
  {},
  async () => {
    try {
      const res = await http.get('/internal/messages', { timeout: 5000 })
      const { count, messages } = res.data

      if (count === 0) {
        return {
          content: [{
            type: 'text',
            text: '没有新的微信消息。',
          }],
        }
      }

      const summary = messages.map((m, i) =>
        `[${i + 1}] 发送者: ${m.from}\n    内容: ${m.content}\n    时间: ${new Date(parseInt(m.timestamp) * 1000).toLocaleString('zh-CN')}`
      ).join('\n\n')

      return {
        content: [{
          type: 'text',
          text: `收到 ${count} 条新消息:\n\n${summary}`,
        }],
      }
    } catch (err) {
      return {
        content: [{
          type: 'text',
          text: `获取消息失败: ${err.message}\n请确认 Bridge 服务正在运行 (npm start)`,
        }],
        isError: true,
      }
    }
  }
)

// ===== 工具 2: 发送微信消息 =====
server.tool(
  'send_wechat_message',
  '通过企业微信发送文本消息给指定用户。touser 是企业微信用户ID（通常从收到的消息中获取）。',
  {
    touser: z.string().describe('接收人企业微信 userid, 多人用 | 分隔'),
    content: z.string().describe('消息文本内容'),
  },
  async ({ touser, content }) => {
    try {
      const res = await http.post('/internal/send', { touser, content })

      return {
        content: [{
          type: 'text',
          text: `消息已发送给 ${touser}`,
        }],
      }
    } catch (err) {
      const detail = err.response?.data?.error || err.message
      return {
        content: [{
          type: 'text',
          text: `发送失败: ${detail}`,
        }],
        isError: true,
      }
    }
  }
)

// ===== 工具 3: 发送卡片消息 =====
server.tool(
  'send_wechat_card',
  '通过企业微信发送文本卡片消息（比普通文本更醒目，带标题和描述）。',
  {
    touser: z.string().describe('接收人企业微信 userid'),
    title: z.string().describe('卡片标题'),
    description: z.string().optional().describe('卡片描述文字'),
    url: z.string().optional().describe('点击卡片跳转的链接'),
  },
  async ({ touser, title, description, url }) => {
    try {
      const res = await http.post('/internal/send-card', {
        touser,
        title,
        description: description || '',
        url: url || '',
      })

      return {
        content: [{
          type: 'text',
          text: `卡片消息已发送给 ${touser}`,
        }],
      }
    } catch (err) {
      const detail = err.response?.data?.error || err.message
      return {
        content: [{
          type: 'text',
          text: `发送失败: ${detail}`,
        }],
        isError: true,
      }
    }
  }
)

// ===== 启动 =====
const transport = new StdioServerTransport()
await server.connect(transport)
console.error('[wecom-bridge MCP] server started on stdio')
