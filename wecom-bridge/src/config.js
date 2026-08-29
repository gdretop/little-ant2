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
    // 默认引擎: 'workbuddy' (调 CodeBuddy CLI, 能干活) | 'ollama' (本地模型, 秒回)
    engine: process.env.LLM_ENGINE || 'workbuddy',
  },
  // WorkBuddy (CodeBuddy Code CLI) 引擎配置
  workbuddy: {
    cli: process.env.WORKBUDDY_CLI || '/Applications/WorkBuddy.app/Contents/Resources/app.asar.unpacked/cli/bin/codebuddy',
    timeoutMs: parseInt(process.env.WORKBUDDY_TIMEOUT_MS || '180000', 10),
    // CLI 执行时的工作目录 (决定它能"看到"和操作哪些文件)
    cwd: process.env.WORKBUDDY_CWD || process.env.HOME || '/tmp',
    // 非交互执行需跳过权限确认 (否则会卡住等待输入)
    skipPermissions: process.env.WORKBUDDY_SKIP_PERMISSIONS !== '0',
    // CLI 每次都是新会话, 靠拼接最近几轮历史维持对话连贯
    historyTurns: parseInt(process.env.WORKBUDDY_HISTORY_TURNS || '6', 10),
    // ===== 模型策略: 默认用免费的, 复杂任务自动升到最高级 =====
    // 账户实测可用: auto / hy4-preview / hy4-preview-x / hy3-preview / hy3-preview-agent / deepseek-v4-pro
    // 首选: 经济档 (日常闲聊/简单问答, 用户指定优先 Hy4 preview)
    model: process.env.WORKBUDDY_MODEL || 'hy4-preview',
    // 旗舰档 (复杂任务自动升级 /pro 前缀强制使用; -x 为增强版)
    premiumModel: process.env.WORKBUDDY_PREMIUM_MODEL || 'hy4-preview-x',
    // 次选: 首选失败时退到 Hy3 (用户指定"其次 Hy3"; -agent 变体支持工具调用)
    fallbackModel: process.env.WORKBUDDY_FALLBACK_MODEL || 'hy3-preview-agent',
    // 自动升级开关: '0' 关闭 (则永远只用经济档)
    autoFallback: process.env.WORKBUDDY_AUTO_FALLBACK !== '0',
    // 复杂度自动升级: '0' 关闭 (则只在失败时才升级)
    autoEscalate: process.env.WORKBUDDY_AUTO_ESCALATE !== '0',
    // 超过此长度(字符)视为复杂任务, 自动用旗舰档
    escalateLength: parseInt(process.env.WORKBUDDY_ESCALATE_LENGTH || '200', 10),
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
  const dingtalkEnabled = !!process.env.DINGTALK_CLIENT_ID && !!process.env.DINGTALK_CLIENT_SECRET

  // 至少配置一种入口 (企业微信 或 钉钉)
  if (!corpid && !dingtalkEnabled) {
    console.error('[ERROR] 未配置任何入口: 需要企业微信(WECOM_*) 或 钉钉(DINGTALK_CLIENT_ID/SECRET) 至少其一')
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

  if (dingtalkEnabled) {
    console.log('[config] 钉钉入口: 已启用 (Stream 模式长连接收消息, 无需域名)')
  }
}
