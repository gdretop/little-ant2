#!/bin/bash
# 启动钉钉入口 (Stream 长连接, 免域名)
# 用法: 在终端里执行  bash scripts/start-dingtalk.sh
# 保持终端窗口开着即可常驻; 想让它开机自启请用 launchd (见 README / 下方注释)。
#
# 开机自启 (在「系统自带终端」里执行一次即可, 不要在本沙箱里执行):
#   launchctl bootstrap gui/$(id -u) ~/Library/LaunchAgents/com.littleant.dingtalk-bot.plist
# 停止/卸载:
#   launchctl bootout gui/$(id -u) ~/Library/LaunchAgents/com.littleant.dingtalk-bot.plist

cd "$(dirname "$0")/.." || exit 1

# 优先用 WorkBuddy 托管的 node, 其次用系统 node
NODE_BIN="/Users/littleant/.workbuddy/binaries/node/versions/22.22.2/bin/node"
if [ ! -x "$NODE_BIN" ]; then
  NODE_BIN="$(command -v node || echo /usr/local/bin/node)"
fi

echo "[start-dingtalk] node: $NODE_BIN"
echo "[start-dingtalk] 工作目录: $(pwd)"
echo "[start-dingtalk] 启动中... (Ctrl+C 停止)"

exec "$NODE_BIN" src/dingtalk-ingest.js
