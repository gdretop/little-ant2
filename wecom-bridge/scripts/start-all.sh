#!/bin/bash
# ============================================================
# WeCom Bridge 本地启动 (云端反向隧道方案)
# 真实部署: bridge 在微信云托管运行, 本机只跑隧道客户端。
# 本脚本用于本机调试: 可一并拉起 本地 bridge + worker + 隧道客户端 做全链路自测。
# 用法:
#   ./scripts/start-all.sh              # 后台拉起 本地bridge+worker+隧道客户端, 然后 tail 日志
#   ./scripts/start-all.sh --daemon     # 同上但不 tail (用于 launchd/supervisor 场景)
#   ./scripts/start-all.sh --tunnel-only # 仅拉起隧道客户端 (生产用, bridge 在云上)
# ============================================================
set -u
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_DIR"
[ ! -d node_modules ] && { echo "[setup] 安装依赖..."; npm install; }

TUNNEL_ONLY=0
DAEMON=0
for a in "$@"; do
  [ "$a" = "--tunnel-only" ] && TUNNEL_ONLY=1
  [ "$a" = "--daemon" ] && DAEMON=1
done

PIDS=()
start_proc() {
  local tag="$1"; shift
  nohup "$@" > "/tmp/wecom-${tag}.log" 2>&1 &
  PIDS+=($!)
  echo "[$tag] 已启动 PID $!"
}

if [ "$TUNNEL_ONLY" = "1" ]; then
  start_proc tunnel-client node src/tunnel-client.js
else
  start_proc bridge node src/server.js
  start_proc worker node src/worker.js
  start_proc tunnel-client node src/tunnel-client.js
fi

if [ "$DAEMON" = "1" ]; then
  echo "[done] 后台运行中, 停止: ./scripts/stop.sh"
  exit 0
fi

echo "[info] tail 日志中 (Ctrl+C 退出, 会结束本机进程)..."
trap 'echo; echo "[stop] 结束本机进程"; kill "${PIDS[@]}" 2>/dev/null; exit 0' SIGINT
sleep 1
tail -f /tmp/wecom-bridge.log /tmp/wecom-worker.log /tmp/wecom-tunnel-client.log
