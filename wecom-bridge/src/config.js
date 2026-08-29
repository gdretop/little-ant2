import 'dotenv/config'

export const config = {
  wecom: {
    corpid: process.env.WECOM_CORPID || '',
    agentid: parseInt(process.env.WECOM_AGENTID || '0', 10),
    secret: process.env.WECOM_SECRET || '',
    token: process.env.WECOM_TOKEN || '',
    encodingAESKey: process.env.WECOM_ENCODING_AES_KEY || '',
  },
  server: {
    port: parseInt(process.env.PORT || '3000', 10),
    // 容器内 bridge 监听 PORT, worker 通过此地址访问 /internal/* ; 默认跟随 PORT
    internalBaseUrl: process.env.INTERNAL_BASE_URL || `http://localhost:${process.env.PORT || '3000'}`,
  },
  // 对话 Agent
  // mode: 'direct' 直接调用 LLM_BASE_URL; 'tunnel' 经反向隧道用本地 Ollama (云端部署用)
  llm: {
    baseUrl: process.env.LLM_BASE_URL || 'http://localhost:11434/v1',
    apiKey: process.env.LLM_API_KEY || 'ollama',
    model: process.env.LLM_MODEL || 'qwen2.5:7b',
    pollIntervalMs: parseInt(process.env.POLL_INTERVAL_MS || '3000', 10),
    mode: process.env.LLM_MODE || 'direct', // 'direct' | 'tunnel'
  },
  // 反向隧道配置 (LLM_MODE=tunnel 时生效)
  tunnel: {
    url: process.env.TUNNEL_URL || 'ws://localhost:3000/tunnel',
    token: process.env.TUNNEL_TOKEN || 'change-me-tunnel-token',
  },
  // 部署后对外公开地址 (如 https://wecom.littleant.com), 仅用于启动日志提示企微后台应填的回调 URL
  publicBaseUrl: process.env.PUBLIC_BASE_URL || '',
}

export function validateConfig() {
  const { corpid, agentid, secret, token, encodingAESKey } = config.wecom
  const feishuEnabled = !!process.env.FEISHU_APP_ID && !!process.env.FEISHU_APP_SECRET

  // 至少配置一种入口 (企业微信 或 飞书)
  if (!corpid && !feishuEnabled) {
    console.error('[ERROR] 未配置任何入口: 需要企业微信(WECOM_*) 或 飞书(FEISHU_APP_ID/SECRET) 至少其一')
    process.exit(1)
  }

  // 仅当配置了企业微信时, 才校验其必填项
  if (corpid) {
    const missing = []
    if (!agentid) missing.push('WECOM_AGENTID')
    if (!secret) missing.push('WECOM_SECRET')
    if (!token) missing.push('WECOM_TOKEN')
    if (!encodingAESKey) missing.push('WECOM_ENCODING_AES_KEY')
    if (missing.length > 0) {
      console.error(`[ERROR] 缺少企业微信配置: ${missing.join(', ')}`)
      process.exit(1)
    }
    if (encodingAESKey.length !== 43) {
      console.error('[ERROR] WECOM_ENCODING_AES_KEY 应为 43 个字符')
      process.exit(1)
    }
    console.log('[config] 企业微信入口: 已启用')
  } else {
    console.log('[config] 企业微信入口: 未配置 (跳过校验)')
  }

  if (feishuEnabled) {
    console.log('[config] 飞书入口: 已启用 (长连接收消息, 无需域名)')
  }
}
