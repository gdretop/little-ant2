#!/bin/bash
# 查看 WeCom Bridge 运行状态和公网 URL
# 用法: ./scripts/status.sh

CURL="curl -s -m 2 --noproxy *"

echo "=== WeCom Bridge 状态 ==="

# Bridge 服务
if $CURL -o /dev/null -w '' http://localhost:3000/health 2>/dev/null && \
   $CURL -o /dev/null -w '%{http_code}' http://localhost:3000/health 2>/dev/null | grep -q 200; then
  echo "[bridge] 运行中 ✓  $($CURL http://localhost:3000/health)"
else
  echo "[bridge] 未运行 ✗"
fi

# 隧道 (优先读 start-all.sh 保存的 URL, 兼容 ngrok 本地 API)
if [ -f /tmp/wecom-tunnel.url ]; then
  echo "[tunnel] 运行中 ✓"
  echo "  公网回调 URL: $(cat /tmp/wecom-tunnel.url)/callback"
elif $CURL http://localhost:4040/api/tunnels 2>/dev/null | grep -q "ngrok"; then
  PUBLIC_URL=$($CURL http://localhost:4040/api/tunnels | node -e "
    let d='';process.stdin.on('data',c=>d+=c).on('end',()=>{
      try{const t=JSON.parse(d).tunnels||[];
      const p=t.find(x=>x.proto==='https');
      if(p)console.log(p.public_url);}catch(e){}});" 2>/dev/null)
  echo "[tunnel] 运行中 ✓ (ngrok)"
  if [ -n "$PUBLIC_URL" ]; then
    echo "  公网回调 URL: ${PUBLIC_URL}/callback"
  fi
else
  echo "[tunnel] 未运行 ✗  (企业微信回调不可达)"
fi

# 待处理消息数 (从 /health 读取, 不消费队列)
HEALTH=$($CURL http://localhost:3000/health 2>/dev/null)
if [ -n "$HEALTH" ]; then
  COUNT=$(echo "$HEALTH" | node -e "
    let d='';process.stdin.on('data',c=>d+=c).on('end',()=>{
      try{const r=JSON.parse(d);console.log(r.pendingMessages||0);}catch(e){console.log('?')}});" 2>/dev/null)
  echo "[queue]  待处理消息: ${COUNT}"
fi

# Worker (实时处理进程)
if pgrep -f "node src/worker.js" >/dev/null 2>&1; then
  echo "[worker] 运行中 ✓  (实时调用 hy3 处理微信消息)"
else
  echo "[worker] 未运行 ✗  (需求②实时回复需启动: ./scripts/start-all.sh --daemon)"
fi
