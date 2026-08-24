import crypto from 'crypto'

/**
 * 企业微信消息加解密
 * 文档: https://developer.work.weixin.qq.com/document/path/90930
 */

// 从 EncodingAESKey (43字符 base64) 派生 32 字节 AES 密钥
function deriveKey(encodingAESKey) {
  return Buffer.from(encodingAESKey + '=', 'base64')
}

// PKCS7 填充
function pkcs7Pad(buf, blockSize = 32) {
  const padLen = blockSize - (buf.length % blockSize)
  return Buffer.concat([buf, Buffer.alloc(padLen, padLen)])
}

// PKCS7 去填充
function pkcs7Unpad(buf) {
  const padLen = buf[buf.length - 1]
  if (padLen < 1 || padLen > 32) return buf
  return buf.subarray(0, buf.length - padLen)
}

/**
 * 计算签名
 * @param {string} token - 企业微信后台配置的 Token
 * @param {string} timestamp
 * @param {string} nonce
 * @param {string} encrypted - 加密后的密文
 * @returns {string} sha1 签名
 */
export function calcSignature(token, timestamp, nonce, encrypted) {
  const arr = [token, timestamp, nonce, encrypted]
  arr.sort()
  return crypto.createHash('sha1').update(arr.join('')).digest('hex')
}

/**
 * 验证签名
 */
export function verifySignature(token, timestamp, nonce, encrypted, sig) {
  const computed = calcSignature(token, timestamp, nonce, encrypted)
  return computed === sig
}

/**
 * 加密消息
 * @param {string} reply - 回复消息内容 (XML)
 * @param {string} corpid - 企业 corpid
 * @param {string} encodingAESKey
 * @returns {string} base64 加密结果
 */
export function encryptMessage(reply, corpid, encodingAESKey) {
  const key = deriveKey(encodingAESKey)
  const iv = key.subarray(0, 16)

  const random = crypto.randomBytes(16)
  const msgBuf = Buffer.from(reply, 'utf8')
  const corpidBuf = Buffer.from(corpid, 'utf8')
  const lenBuf = Buffer.alloc(4)
  lenBuf.writeUInt32BE(msgBuf.length, 0)

  const raw = Buffer.concat([random, lenBuf, msgBuf, corpidBuf])
  const padded = pkcs7Pad(raw, 32)

  const cipher = crypto.createCipheriv('aes-256-cbc', key, iv)
  cipher.setAutoPadding(false)
  const encrypted = Buffer.concat([cipher.update(padded), cipher.final()])

  return encrypted.toString('base64')
}

/**
 * 解密消息
 * @param {string} encrypted - base64 密文
 * @param {string} corpid - 企业 corpid (用于校验)
 * @param {string} encodingAESKey
 * @returns {{content: string, corpid: string}} 解密后的消息内容和企业ID
 */
export function decryptMessage(encrypted, corpid, encodingAESKey) {
  const key = deriveKey(encodingAESKey)
  const iv = key.subarray(0, 16)

  const decipher = crypto.createDecipheriv('aes-256-cbc', key, iv)
  decipher.setAutoPadding(false)
  const decrypted = Buffer.concat([
    decipher.update(Buffer.from(encrypted, 'base64')),
    decipher.final(),
  ])

  const unpadded = pkcs7Unpad(decrypted)
  // 跳过 16 字节随机串, 读 4 字节消息长度, 取消息内容, 剩余是 corpid
  const msgLen = unpadded.readUInt32BE(16)
  const msg = unpadded.subarray(20, 20 + msgLen).toString('utf8')
  const receivedCorpid = unpadded.subarray(20 + msgLen).toString('utf8')

  if (receivedCorpid !== corpid) {
    throw new Error(`corpid mismatch: expected ${corpid}, got ${receivedCorpid}`)
  }

  return { content: msg, corpid: receivedCorpid }
}

/**
 * 构建加密回复包 (XML)
 */
export function buildEncryptedReply(replyMsg, corpid, encodingAESKey, token) {
  const timestamp = Math.floor(Date.now() / 1000).toString()
  const nonce = crypto.randomBytes(8).toString('hex')
  const encrypted = encryptMessage(replyMsg, corpid, encodingAESKey)
  const signature = calcSignature(token, timestamp, nonce, encrypted)

  return `<xml>
<Encrypt><![CDATA[${encrypted}]]></Encrypt>
<MsgSignature><![CDATA[${signature}]]></MsgSignature>
<TimeStamp>${timestamp}</TimeStamp>
<Nonce><![CDATA[${nonce}]]></Nonce>
</xml>`
}
