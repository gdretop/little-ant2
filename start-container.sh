#!/bin/bash
# 容器启动脚本: 同容器拉起 Java 应用(8080) + WeCom 反向隧道桥(80)
# 云托管只暴露一个端口(80), 桥作为边缘:
#   - 自己处理 /callback /tunnel /internal /health
#   - 其余路径反向代理到同容器的 Java 应用 (JAVA_UPSTREAM, 默认 127.0.0.1:8080)
# 这样微信回调/隧道和原有 Java Web 服务共用同一个公网域名, 互不干扰。

# 1) Java 应用 (后台, 端口 8080)
JAR=$(ls /home/root/java/*.jar 2>/dev/null | head -1)
if [ -n "$JAR" ]; then
  echo "[start] Java 应用: $JAR (port 8080)"
  java -Duser.timezone=GMT+08 -jar "$JAR" &
  JAVA_PID=$!
else
  echo "[start] 未找到 /home/root/java/*.jar, 跳过 Java (仅运行桥)"
fi

# 2) WeCom 桥 (server + worker, 监听 PORT, 默认 80)
cd /home/root/wecom-bridge
echo "[start] WeCom 桥 (port ${PORT:-80}, LLM_MODE=${LLM_MODE:-direct})"
node src/server.js &
BRIDGE_SRV=$!
node src/worker.js &
BRIDGE_WRK=$!

# 3) 飞书入口 (长连接收消息, 免域名) —— 仅当配置了 FEISHU_APP_ID 才启动
FEISHU_PID=""
if [ -n "$FEISHU_APP_ID" ] && [ -n "$FEISHU_APP_SECRET" ]; then
  echo "[start] 飞书入口 (长连接模式, 免域名)"
  node src/feishu-ingest.js &
  FEISHU_PID=$!
fi

# 转发终止信号给子进程
trap "kill -TERM $JAVA_PID $BRIDGE_SRV $BRIDGE_WRK $FEISHU_PID 2>/dev/null" TERM INT

# 保持容器存活: 任一进程退出即结束, 交由平台重启
wait -n
