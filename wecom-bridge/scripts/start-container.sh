#!/usr/bin/env bash
# 容器内启动脚本 (微信云托管 / CloudBase 用)
# 同容器跑 bridge 服务 + worker, 通过反向隧道调用你本地的 Ollama
set -e

echo "[container] 启动 WeCom Bridge (server + worker)..."
node src/server.js &
SERVER_PID=$!
node src/worker.js &
WORKER_PID=$!

trap "kill -TERM $SERVER_PID $WORKER_PID 2>/dev/null" SIGTERM SIGINT

# 等任一子进程退出则整体退出, 由云平台重启
wait -n
kill -TERM $SERVER_PID $WORKER_PID 2>/dev/null
