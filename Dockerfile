# 二开推荐阅读[如何提高项目构建效率](https://developers.weixin.qq.com/miniprogram/dev/wxcloudrun/src/scene/build/speed.html)
# 选择构建用基础镜像。如需更换，请到[dockerhub官方仓库](https://hub.docker.com/_/java?tab=tags)自行选择后替换。
FROM maven:3.6.0-jdk-8-slim as build

# 指定构建过程中的工作目录
WORKDIR /app

# 将src目录下所有文件，拷贝到工作目录中src目录下（.gitignore/.dockerignore中文件除外）
COPY java /app/java

# 将pom.xml文件，拷贝到工作目录下
COPY settings.xml pom.xml /app/

# 执行代码编译命令
# 自定义settings.xml, 选用国内镜像源以提高下载速度
RUN mvn -s /app/settings.xml -f /app/pom.xml clean package

# 自定义镜像 (基于你已有的基础镜像, 内含 JRE8 + python 环境)
FROM ccr.ccs.tencentyun.com/little-ant2/little-ant:basic1.0_221228

# ===== 新增: 安装 Node.js (官方二进制, 免 apt, 兼容任意 glibc Linux) =====
# 用二进制 tarball 而非 apt, 避免依赖基础镜像的包管理器, 构建更稳。
ENV NODE_VERSION=18.20.4
RUN curl -fsSL "https://nodejs.org/dist/v${NODE_VERSION}/node-v${NODE_VERSION}-linux-x64.tar.gz" -o /tmp/node.tar.gz \
    && tar -xzf /tmp/node.tar.gz -C /usr/local --strip-components=1 \
    && rm -f /tmp/node.tar.gz \
    && node --version && npm --version

COPY python /home/root/python
COPY --from=build /app/target/*.jar /home/root/java

# ===== 新增: WeCom 反向隧道桥 (Node.js) =====
COPY wecom-bridge /home/root/wecom-bridge
WORKDIR /home/root/wecom-bridge
# --omit=dev 只装运行时依赖; 这里仍装全部以便后续排障/热更
RUN npm install --omit=dev
WORKDIR /

# 容器启动脚本: 同容器拉起 Java(8080) + 桥 server/worker(80, 边缘反向代理到 Java)
COPY start-container.sh /home/root/start-container.sh
RUN chmod +x /home/root/start-container.sh

# 云托管只暴露一个端口: 80 (= 桥, 同时代理原 Java 服务)
EXPOSE 80

# 注意: 不要写多行 CMD, 只有最后一行生效。统一用启动脚本。
CMD ["/home/root/start-container.sh"]
