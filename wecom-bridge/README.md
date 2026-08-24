# WeCom Bridge for WorkBuddy

通过企业微信实现 WorkBuddy 与个人微信的双向通信。

## 架构

两种部署方式（任选其一）：

**A. 推荐 · 微信云托管（固定公网地址）+ 反向隧道（本地 Ollama 免费秒回）**

```
个人微信 → 微信插件 → 企业微信 → 云托管 Bridge(POST /callback 解密入队)
                                        │  server + worker 跑在云端(固定HTTPS域名)
                                        │  worker 调 /internal/llm
                                        ▼
                              云托管 /tunnel (WebSocket 端点)
                                        │  ←── 反向 WS 长连接(本地Mac主动外连, 无需开端口)
                                        ▼
                              本地 Mac: tunnel-client → 本机 Ollama(qwen2.5:7b) → 回发
```

- 云端拿到**固定 HTTPS 域名**（重启不变），企业微信回调永远有效。
- 大模型仍在**你本机 Ollama** 跑，免费、无需任何 API Key、数据不出本机。
- 本地 Mac 只需保持 `tunnel-client` 常驻（断线自动重连），不用公网 IP、不用开端口。

**B. 本地调试 · cloudflared/ngrok 穿透（随机地址，重启要改回调 URL）**

```
你的微信 ←→ 企业微信(微信插件) ←→ 企业微信自建应用 ←→ [隧道: cloudflared/ngrok] ←→ 本地 Bridge(含 worker)
```

- 适合本机一键联调；免费隧道每次重启地址会变，需回企业微信后台更新回调 URL。

> 下面的「第一至九步」是通用配置（企业微信注册、自建应用、回调、MCP、自动化），与部署方式无关；
> 末尾「推荐部署：微信云托管 + 反向隧道」专讲方式 A 的上云步骤。

## 前置条件

- Node.js >= 18
- 企业微信账号（个人可免费注册）
- 方式 A：一个微信云托管（CloudBase 云托管）服务，分配固定 HTTPS 域名
- 方式 B：内网穿透 cloudflared（免注册 `brew install cloudflared`）或 ngrok（需注册）
- 本机 Ollama（方式 A 必须，方式 B 推荐）：`brew install ollama && ollama pull qwen2.5:7b`

## 第一步: 注册企业微信

1. 打开 https://work.weixin.qq.com/ 点击「企业注册」
2. 填写信息完成注册 (个人也可以注册, 选「其他组织」即可)
3. 注册完成后进入管理后台

## 第二步: 创建自建应用

1. 管理后台 → 「应用管理」→ 「自建」→ 「创建应用」
2. 填写应用名称 (如 "WorkBuddy助手"), 可见范围选所有人
3. 创建完成后记录以下信息:
   - **AgentId** (应用 ID, 如 1000002)
   - **Secret** (应用密钥, 点击查看获取)
4. 在「我的企业」页面记录 **企业ID (CorpID)**

## 第三步: 配置接收消息

1. 进入应用详情 → 「接收消息」→ 「设置API接收**
2. **URL**: 填你的 ngrok 公网地址 + `/callback` (见第五步)
3. **Token**: 点击「随机获取」, 记录下来
4. **EncodingAESKey**: 点击「随机获取」, 记录下来 (43 字符)
5. 先不要点保存 (等服务跑起来再保存验证)

## 第四步: 启用微信插件 (让消息到达个人微信)

1. 管理后台 → 「我的企业」→ 「微信插件」
2. 确保已开启「微信可访问」
3. 用个人微信关注企业微信的「微信插件」(扫码关注)
4. 这样企业微信应用消息就能推送到你的个人微信了

## 第五步: 安装和配置 Bridge

```bash
# 进入项目目录
cd wecom-bridge

# 复制配置模板
cp .env.example .env

# 编辑 .env, 填入第二步和第三步获取的信息
vim .env
```

.env 文件需要填写:

```
WECOM_CORPID=ww你的企业ID
WECOM_AGENTID=1000002
WECOM_SECRET=你的应用Secret
WECOM_TOKEN=你的回调Token
WECOM_ENCODING_AES_KEY=你的43位EncodingAESKey
```

安装依赖:

```bash
npm install
```

## 第六步: 启动内网穿透隧道

> **推荐: 一键启动 (自动选隧道)**
>
> ```bash
> ./scripts/start-all.sh
> ```
> 脚本会自动选择隧道 (见下方说明) 并同时启动 Bridge 服务, 启动后直接打印公网回调 URL。

### 隧道选择

**方案 A: cloudflared (推荐, 免注册)**

```bash
brew install cloudflared
```

无需注册任何账号, 没有人机验证。启动后得到类似地址:
```
https://random-words.trycloudflare.com
```

> 注意: ngrok 注册页使用 Google reCAPTCHA, 国内直连无法通过验证 (ERR_NGROK_1205)。
> 有代理环境可挂代理注册 ngrok; 没有则直接用 cloudflared, 体验无差别。

**方案 B: ngrok (需注册, 有代理环境)**

```bash
ngrok http 3000
```

ngrok 会输出类似:
```
Forwarding   https://abc123.ngrok-free.app -> http://localhost:3000
```

### 填写回调 URL

记下隧道的 https 地址, 填到第三步的回调 URL 中 (cloudflared 同理):
```
https://abc123.ngrok-free.app/callback
```

回到企业微信管理后台, 保存第三步的 API 接收配置, 应该显示验证成功。

> **注意**: 免费隧道每次重启 URL 都会变。URL 变了之后需要到企业微信后台更新回调 URL,
> 再用 `./scripts/status.sh` 查看当前公网地址。

## 第七步: 启动 Bridge 服务

```bash
# 推荐: 一键启动 (隧道 + 服务一起)
./scripts/start-all.sh

# 或只启动服务 (隧道需单独开)
./scripts/start.sh
# 或
npm start

# 查看状态 / 停止
./scripts/status.sh
./scripts/stop.sh
```

看到以下输出说明启动成功:
```
========================================
  WeCom Bridge for WorkBuddy
========================================
  本地服务:  http://localhost:3000
  回调地址:  http://localhost:3000/callback
========================================
  等待企业微信消息...
```

现在用企业微信应用给这个应用发条消息, 终端应该能看到:
```
[callback] 收到消息: type=text, from=YourName, msgId=xxx
[callback] 入队: "你好" from YourName (队列: 1 条)
```

## 第八步: 配置 WorkBuddy MCP

将下面的配置合并到 WorkBuddy 的 MCP 配置文件 `~/.workbuddy/mcp.json`:

```json
{
  "mcpServers": {
    "wecom-bridge": {
      "command": "node",
      "args": ["/Users/littleant/WorkBuddy/2026-08-23-21-40-18/wecom-bridge/src/mcp-server.js"],
      "env": {
        "INTERNAL_BASE_URL": "http://localhost:3000"
      }
    }
  }
}
```

如果已有其他 MCP server, 把 `wecom-bridge` 这一项加到 `mcpServers` 对象里即可。

配好后重启 WorkBuddy, 在 WorkBuddy 里就能直接用了:
- "帮我发条微信消息告诉小王项目搞定了" → WorkBuddy 调用 `send_wechat_message` 工具
- "查一下微信有没有新消息" → WorkBuddy 调用 `get_pending_wechat_messages` 工具

## 第九步: 创建自动化 (微信触发机器人)

在 WorkBuddy 中创建一个自动化任务:

- **名称**: 微信消息自动回复
- **类型**: recurring
- **频率**: 每小时执行 (RRULE: `FREQ=HOURLY;INTERVAL=1`)
- **MCP 连接器**: wecom-bridge
- **提示词**: 
  ```
  检查企业微信消息队列，如果有新消息，逐条处理：
  1. 理解消息内容，判断用户想让你做什么
  2. 执行相应的操作（如搜索、分析、生成内容等）
  3. 把结果通过 send_wechat_message 工具回复给发送者
  如果没有新消息，不做任何操作。
  ```

这样每小时 WorkBuddy 会自动检查微信消息并处理回复。

## MCP 工具列表

Bridge 通过 MCP 暴露以下工具给 WorkBuddy:

| 工具 | 说明 |
|------|------|
| `get_pending_wechat_messages` | 获取并清空消息队列, 返回所有新消息 |
| `send_wechat_message` | 发送文本消息 (touser + content) |
| `send_wechat_card` | 发送卡片消息 (touser + title + description + url) |

## 实时微信机器人（需求② · 推荐方案）

上面的「第九步自动化」依赖 WorkBuddy 小时级轮询，延迟高。更实时的做法是**内置 worker 进程**：收到微信消息后立刻调用大模型生成回复，几秒内回发微信。

worker 默认使用 **本机 Ollama 本地大模型**（推荐，无需任何 API Key、无需付费、本地秒回）。前提是本机已安装并运行 Ollama 且已拉取模型（如 `ollama pull qwen2.5:7b`）。

> 上云部署时，worker 跑在云端容器、`LLM_MODE=tunnel`，它通过反向隧道把 LLM 请求转发到你**本机 Mac 的 Ollama**（见上文「推荐部署：微信云托管 + 反向隧道」）。大模型算力始终在你自己机器上。

### 配置大模型（免费首选：本地 Ollama）

```bash
# 默认方案 (推荐, 完全免费、无需 Key): 本机 Ollama
LLM_BASE_URL=http://localhost:11434/v1
LLM_MODEL=qwen2.5:7b
LLM_API_KEY=ollama   # 占位即可, Ollama 不需要真实鉴权
```

若本机没有 Ollama，也可改用云端免费大模型（需注册拿 key）：

```bash
# 方案B: 硅基流动 SiliconFlow (注册送免费额度)
# LLM_BASE_URL=https://api.siliconflow.cn/v1
# LLM_MODEL=Qwen/Qwen3-8B
# LLM_API_KEY=你的_sk-xxxx

# 方案C: 腾讯混元 Hunyuan-lite (永久免费, 需腾讯云密钥)
# LLM_BASE_URL=https://api.hunyuan.cloud.tencent.com/v1
# LLM_MODEL=hunyuan-lite
# LLM_API_KEY=你的腾讯云SecretId/SecretKey组合的Key
```

> 不填 `LLM_API_KEY` 时，worker 仍会运行，但只会回「收到」回执，不会真正调用模型。

### 启动（含 worker）

```bash
./scripts/start-all.sh --daemon
```

`--daemon` 会同时拉起：隧道 + Bridge 服务 + **实时 worker**。
查看状态（含 worker 是否在跑）：

```bash
./scripts/status.sh
```

### 使用

在个人微信里给应用发任意消息（如「帮我查下杭州天气」），worker 每隔 3 秒轮询队列，调用 hy3 生成回答并回发到你的微信。日志见 `/tmp/wecom-worker.log`。

### 架构

```
个人微信 → 微信插件 → 企业微信 → Bridge POST /callback (解密入队)
                                    ↓
        Bridge GET /internal/messages ← worker 轮询
                                    ↓
        Bridge POST /internal/send ← worker 调 hy3 生成回复
                                    ↓
        企业微信 → 微信插件 → 个人微信 (收到回答)
```

## 推荐部署：微信云托管（固定地址）+ 反向隧道

这是**最终推荐方案**：云端拿到固定 HTTPS 域名（企业微信回调永久有效），大模型仍跑在**你本机 Ollama**（免费、无需 Key、数据不出本机）。本地 Mac 只需常驻一个 `tunnel-client` 进程，主动外连云端，**不用公网 IP、不用开任何端口**。

> 你已确认的环境：云托管域名 `springboot-kgxg-53361-9-1318542298.sh.run.tcloudbase.com`，隧道密钥 `0dabc130600246f276f16b607233f981522deeb698b5315b`。下文直接套用这两个值。

### 原理

```
企业微信 → 云托管 Bridge(POST /callback 解密入队)
              └ server + worker 同容器运行
                  worker 取消息 → POST /internal/llm → requestLLM()
                      → 通过 /tunnel(WS) 下发到本地 Mac
                          → tunnel-client → 本机 Ollama → 返回
                      → worker 把回复经企业微信发回你的微信
```

关键点：云托管只暴露**一个 HTTP 端口（3000）**，所以 `frps` 这类需要额外 TCP 端口的中继跑不进去；改用**反向 WebSocket 隧道**——本地 Mac 主动连出去，云端转发 LLM 请求，完美规避端口限制。

### 第一步：部署 Bridge 到云托管（云端）

1. 微信云托管后台新建**服务**，来源选「代码仓库」或「上传代码/镜像」，服务端口填 `3000`。
2. 本仓库已是容器友好结构：`Dockerfile` + `scripts/start-container.sh` 会在**同一容器**里拉起 `server.js`（含 `/tunnel` 端点）+ `worker.js`。
3. 在云托管「环境变量」里设置（与 `.env` 一致，外加隧道相关）：

   | 变量 | 值 |
   |------|----|
   | `WECOM_CORPID` / `WECOM_AGENTID` / `WECOM_SECRET` | 企业微信后台拿到的（同 `.env`） |
   | `WECOM_TOKEN` / `WECOM_ENCODING_AES_KEY` | 企业微信后台拿到的（同 `.env`） |
   | `PORT` | `3000` |
   | `LLM_MODE` | `tunnel` |
   | `TUNNEL_URL` | `wss://springboot-kgxg-53361-9-1318542298.sh.run.tcloudbase.com/tunnel` |
   | `TUNNEL_TOKEN` | `0dabc130600246f276f16b607233f981522deeb698b5315b` |

4. 部署完成后，云托管会分配固定 HTTPS 域名（即上面的 `springboot-kgxg-53361-9-1318542298.sh.run.tcloudbase.com`，重启不变）。
5. 到企业微信后台「接收消息 → 设置 API 接收」，URL 填：

   ```
   https://springboot-kgxg-53361-9-1318542298.sh.run.tcloudbase.com/callback
   ```

   Token / EncodingAESKey 同 `.env`，保存时应显示「验证成功」。

### 第二步：本地 Mac 常驻隧道客户端（本地）

1. 本机保持 Ollama 运行（`ollama serve` 或桌面App常驻，且已 `ollama pull qwen2.5:7b`）。
2. 本机 `.env` 只需关注隧道两项（本地不再跑 server/worker，可删掉相关进程）：

   ```
   TUNNEL_URL=wss://springboot-kgxg-53361-9-1318542298.sh.run.tcloudbase.com/tunnel
   TUNNEL_TOKEN=0dabc130600246f276f16b607233f981522deeb698b5315b
   ```

3. 启动本地客户端（断线每 5 秒自动重连）：

   ```bash
   node src/tunnel-client.js
   # 常驻推荐: ./scripts/supervisor.sh   (只保活 tunnel-client)
   # 或 macOS 登录项 / launchd 让它开机自启
   ```

   看到 `已连上云端 bridge ✅` 即成功。云端日志会打 `[tunnel] 本地客户端已连接 ✅`。

### 第三步：本地模拟自测（不依赖上云也能验证隧道代码）

本机起 server + tunnel-client，直接打 `/internal/llm` 验证「云端→隧道→本地Ollama→回发」全链路：

```bash
node scripts/test-tunnel.mjs
```

预期输出含：

```
✅ 隧道已连通 (本地 Ollama 客户端在线)
🤖 本地 Ollama 经隧道返回: 等于2。
✅ 全链路测试通过: 云端(/internal/llm) → 隧道 → 本地 Ollama → 回发
```

（该脚本会自己拉起 server+client、跑完自动退出并清理，不会留下进程。）

### 安全说明

- `/tunnel` 握手校验 `TUNNEL_TOKEN`，云端与本地必须一致，否则连不上。
- `/internal/llm` 额外校验请求头 `x-internal-token`（值同为 `TUNNEL_TOKEN`），防止公网任意调用你本机 Ollama 算力。
- 其余 `/internal/*` 为容器内部/本机通信设计；上云后建议配合云托管的「访问控制 / 仅内网」策略，不要把这些路径对外暴露。

### 常见坑

- **云端报 503「本地客户端未连接」**：本地 `tunnel-client` 没起或断了。查本地客户端的 `已连上云端 bridge` 日志、确认 `TUNNEL_URL`/`TUNNEL_TOKEN` 与云端一致。
- **回调保存失败**：`WECOM_TOKEN`/`WECOM_ENCODING_AES_KEY` 与后台不一致；或云托管还没部署好（域名尚不可达）。
- **回复慢**：本机 Ollama 首次冷启会加载模型，之后秒回。
- **本机换网络/重启**：客户端自动重连，无需改任何配置；云端域名固定不动。

## 常见问题

### 回调 URL 验证失败
- 确认 ngrok 正在运行且地址正确
- 确认 .env 中 Token 和 EncodingAESKey 与企业微信后台一致
- 检查 server.js 控制台输出的错误日志

### 发消息失败 (errcode 40014)
- access_token 无效, 检查 CorpID 和 Secret 是否正确
- 重启 Bridge 服务刷新 token

### 消息收不到
- 确认应用「可见范围」包含你
- 确认企业微信「微信插件」已关注
- 检查 ngrok 是否还在运行 (免费版会自动断开)

### ngrok 免费版限制
- 免费版每次启动域名会变, 需要重新在企业微信后台更新 URL
- 可考虑用 frp 自建穿透, 或购买 ngrok 固定域名

## 项目结构

```
wecom-bridge/
├── package.json          # 依赖管理 (express, ws, axios, dotenv ...)
├── Dockerfile            # 云托管/容器镜像 (同容器跑 server + worker)
├── .dockerignore
├── .env.example          # 配置模板 (云端/本地两套说明)
├── .env                  # 你的配置 (不提交)
├── src/
│   ├── config.js         # 配置加载和校验
│   ├── crypto.js         # 企业微信消息加解密
│   ├── wecom-api.js      # 企业微信 API 客户端
│   ├── server.js         # Express 回调服务 + /tunnel 端点 + /internal/llm (主进程)
│   ├── tunnel-server.js  # 云端 WS 隧道端点 (转发 LLM 请求给本地客户端)
│   ├── tunnel-client.js  # 本地 Mac 客户端 (长连云端, 调本机 Ollama)
│   ├── worker.js         # 实时回复 worker (tunnel 模式走 /internal/llm)
│   └── mcp-server.js     # MCP Server (WorkBuddy 对接, 本地调试用)
├── scripts/
│   ├── start.sh          # 仅启动服务
│   ├── start-all.sh      # 一键启动 (--daemon 含隧道+服务+worker; --tunnel-only 仅本地客户端)
│   ├── supervisor.sh     # 保活 (云端: 不在此用; 本地: 仅保活 tunnel-client)
│   ├── start-container.sh# 容器内: server + worker 同跑
│   ├── stop.sh           # 停止全部
│   ├── status.sh         # 查看状态
│   ├── test-tunnel.mjs   # 本地模拟自测 (验证隧道全链路)
│   └── frps-setup.sh     # (已弃用) 早期 frp 方案脚本, 留作参考
└── README.md            # 本文件
```
