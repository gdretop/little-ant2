#!/bin/bash
# 停止 WeCom Bridge 相关进程
# 用法: ./scripts/stop.sh

echo "[stop] 停止 Bridge 服务..."
pkill -f "node src/server.js" 2>/dev/null && echo "  已停止" || echo "  未在运行"
echo "[stop] 停止 Worker..."
pkill -f "node src/worker.js" 2>/dev/null && echo "  已停止" || echo "  未在运行"
echo "[stop] 停止 隧道客户端..."
pkill -f "node src/tunnel-client.js" 2>/dev/null && echo "  已停止" || echo "  未在运行"
echo "完成"
