// 本地联调: 模拟企业微信回调, 向 Bridge 注入一条加密消息, 验证 worker 实时回复链路
import 'dotenv/config'
import { encryptMessage, calcSignature } from '../src/crypto.js'
import axios from 'axios'

const BASE = process.env.INTERNAL_BASE_URL || 'http://127.0.0.1:3000'
const token = process.env.WECOM_TOKEN
const corpid = process.env.WECOM_CORPID
const aesKey = process.env.WECOM_ENCODING_AES_KEY

const text = process.argv[2] || '你好，这是实时回复联调测试，请回复确认'
const from = process.argv[3] || 'LinYuWang'
const msgId = 'test_' + Date.now()

const plain = `<xml><MsgType><![CDATA[text]]></MsgType><FromUserName><![CDATA[${from}]]></FromUserName><CreateTime>${Math.floor(Date.now() / 1000)}</CreateTime><MsgId>${msgId}</MsgId><Content><![CDATA[${text}]]></Content></xml>`

const enc = encryptMessage(plain, corpid, aesKey)
const ts = String(Math.floor(Date.now() / 1000))
const nonce = 'inject' + Math.random().toString(36).slice(2, 10)
const sig = calcSignature(token, ts, nonce, enc)

const body = `<xml><Encrypt><![CDATA[${enc}]]></Encrypt></xml>`
await axios.post(`${BASE}/callback?msg_signature=${sig}&timestamp=${ts}&nonce=${nonce}`, body, {
  headers: { 'Content-Type': 'text/xml' },
  proxy: false,
})
console.log(`[inject] 已注入测试消息 -> ${from}: "${text}" (msgId=${msgId})`)
console.log('[inject] 等待 worker 处理 (约 3~8s)...')
await new Promise(r => setTimeout(r, 7000))

const health = await axios.get(`${BASE}/health`, { proxy: false })
console.log('[inject] 队列剩余待回复:', health.data.pendingMessages)
