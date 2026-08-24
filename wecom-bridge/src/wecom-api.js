import axios from 'axios'

const BASE_URL = 'https://qyapi.weixin.qq.com/cgi-bin'

/**
 * 企业微信 API 客户端
 * 管理 access_token 自动刷新
 */
export class WecomAPI {
  constructor(corpid, secret, agentid) {
    this.corpid = corpid
    this.secret = secret
    this.agentid = agentid
    this.accessToken = null
    this.tokenExpireAt = 0
  }

  /**
   * 获取 access_token (带缓存, 提前 5 分钟刷新)
   */
  async getAccessToken() {
    const now = Date.now()
    if (this.accessToken && now < this.tokenExpireAt - 300000) {
      return this.accessToken
    }

    const res = await axios.get(`${BASE_URL}/gettoken`, {
      params: { corpid: this.corpid, corpsecret: this.secret },
    })

    if (res.data.errcode !== 0) {
      throw new Error(`gettoken failed: ${res.data.errmsg} (code: ${res.data.errcode})`)
    }

    this.accessToken = res.data.access_token
    // access_token 有效期 7200 秒
    this.tokenExpireAt = now + res.data.expires_in * 1000
    return this.accessToken
  }

  /**
   * 发送应用消息
   * @param {string} touser - 接收人 userid (多个用 | 分隔)
   * @param {string} content - 文本内容
   */
  async sendTextMessage(touser, content) {
    const token = await this.getAccessToken()
    const res = await axios.post(
      `${BASE_URL}/message/send?access_token=${token}`,
      {
        touser,
        msgtype: 'text',
        agentid: this.agentid,
        text: { content },
      }
    )

    if (res.data.errcode !== 0) {
      throw new Error(`send message failed: ${res.data.errmsg} (code: ${res.data.errcode})`)
    }

    return res.data
  }

  /**
   * 发送文本卡片消息 (更醒目)
   */
  async sendCardMessage(touser, title, description, url = '') {
    const token = await this.getAccessToken()
    const res = await axios.post(
      `${BASE_URL}/message/send?access_token=${token}`,
      {
        touser,
        msgtype: 'textcard',
        agentid: this.agentid,
        textcard: {
          title,
          description,
          url: url || 'https://work.weixin.qq.com',
          btntxt: '查看详情',
        },
      }
    )

    if (res.data.errcode !== 0) {
      throw new Error(`send card failed: ${res.data.errmsg} (code: ${res.data.errcode})`)
    }

    return res.data
  }
}
