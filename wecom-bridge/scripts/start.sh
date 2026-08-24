#!/bin/bash
# WeCom Bridge 启动脚本
# 用法: ./scripts/start.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_DIR"

# 检查 .env 是否存在
if [ ! -f .env ]; then
  echo "============================================"
  echo "  未找到 .env 文件!"
  echo "  请先复制 .env.example 并填写配置:"
  echo ""
  echo "  cp .env.example .env"
  echo "  vim .env"
  echo ""
  echo "  配置完成后重新运行此脚本"
  echo "============================================"
  exit 1
fi

# 检查 node_modules 是否存在
if [ ! -d node_modules ]; then
  echo "[setup] 安装依赖..."
  npm install
fi

# 检查 ngrok 是否在运行
NGROK_RUNNING=$(pgrep -f "ngrok" 2>/dev/null || true)
if [ -z "$NGROK_RUNNING" ]; then
  echo ""
  echo "  [警告] ngrok 未运行!"
  echo "  企业微信回调需要公网可达的 URL"
  echo "  请在另一个终端运行:"
  echo ""
  echo "    ngrok http 3000"
  echo ""
  echo "  然后将 https://xxx.ngrok.io/callback 填入企业微信回调URL"
  echo ""
fi

echo ""
echo "========================================"
echo "  启动 WeCom Bridge..."
echo "========================================"
echo ""

# 启动服务
exec node src/server.js
