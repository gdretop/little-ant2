#!/bin/bash
# WeCom Bridge 本地常驻监管脚本 (运行在你的 Mac 上)
# 由 launchd (~/Library/LaunchAgents/com.wecombridge.plist) 拉起
# 负责保活: 本地隧道客户端 (连云端 bridge, 把本机 Ollama 暴露给云端用)
# 注意: bridge 服务本身运行在微信云托管(CloudBase), 不在本机。
set -u

BRIDGE_DIR="/Users/littleant/WorkBuddy/2026-08-23-21-40-18/wecom-bridge"
LOG_DIR="/tmp"
export PATH="/Users/littleant/.workbuddy/binaries/node/versions/22.22.2/bin:/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:$HOME/bin:$PATH"

cd "$BRIDGE_DIR" || exit 1

ensure() {
  local pattern="$1"; local cmd="$2"; local tag="$3"
  if ! pgrep -f "$pattern" >/dev/null 2>&1; then
    echo "[supervisor $(date '+%H:%M:%S')] 启动 $tag"
    nohup bash -c "$cmd" >>"$LOG_DIR/wecom-${tag}.log" 2>&1 &
  fi
}

cleanup() {
  echo "[supervisor $(date '+%H:%M:%S')] 收到退出信号, 清理子进程"
  pkill -f "node src/tunnel-client.js" 2>/dev/null
  exit 0
}
trap cleanup SIGTERM SIGINT SIGQUIT

echo "[supervisor] 启动 WeCom Bridge 隧道客户端监管 (PID $$)"
while true; do
  ensure "node src/tunnel-client.js" "node src/tunnel-client.js" tunnel-client
  sleep 30
done
