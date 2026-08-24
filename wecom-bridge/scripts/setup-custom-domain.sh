#!/bin/bash
# 生成企业域名的受信任 TLS 证书 (Let's Encrypt, 免费), 用于云托管「自定义域名」HTTPS。
#
# 前置 (需你自行准备):
#   1. 一个已「企业主体备案」的域名, 例如 wecom.littleant.com (或 littleant.com)
#   2. 该域名的 DNS 解析托管在 腾讯云 DNSPod (默认) 或 Cloudflare
#   3. 对应的 API 凭据 (仅用于 DNS-01 挑战验证, 不会触碰你的证书私钥以外内容)
#
# 用法:
#   # 腾讯云 DNSPod (推荐, 与你现有腾讯云账号一致):
#   DP_Id=你的DNSPod_ID DP_Key=你的DNSPod_Key DOMAIN=wecom.littleant.com bash scripts/setup-custom-domain.sh
#
#   # Cloudflare:
#   CF_Token=xxx CF_Zone_ID=xxx CF_Account_ID=xxx DOMAIN=wecom.littleant.com bash scripts/setup-custom-domain.sh
#
# 产出 (用于云托管自定义域名 SSL 上传):
#   ./certs/<DOMAIN>/fullchain.pem   <- 证书 (含链)
#   ./certs/<DOMAIN>/privkey.pem     <- 私钥
#
# 注意: 自签名证书 (Desktop/wecom.crt) 不能用于公网, 企微/浏览器均不信任。必须用这里签发的真实证书。

set -euo pipefail

DOMAIN="${DOMAIN:?请设置 DOMAIN, 例如 DOMAIN=wecom.littleant.com}"
OUT_DIR="$(cd "$(dirname "$0")/.." && pwd)/certs/${DOMAIN}"
mkdir -p "$OUT_DIR"

# ---- 安装 acme.sh (若未安装) ----
if ! command -v acme.sh >/dev/null 2>&1; then
  echo "[cert] 未找到 acme.sh, 正在安装到 ~/.acme.sh ..."
  curl -fsSL https://get.acme.sh | sh
  # 让当前 shell 能找到 acme.sh
  export PATH="$HOME/.acme.sh:$PATH"
fi
# 确保 acme.sh 在 PATH (已安装时)
export PATH="$HOME/.acme.sh:$PATH"

echo "[cert] 为 $DOMAIN 签发 Let's Encrypt 证书 (DNS-01 挑战)..."

# 选择 DNS 提供商
if [ -n "${DP_Id:-}" ] && [ -n "${DP_Key:-}" ]; then
  DNS_MODE="dns_dp"
  export DP_Id DP_Key
  echo "[cert] 使用 DNSPod (腾讯云 DNS) 验证"
elif [ -n "${CF_Token:-}" ]; then
  DNS_MODE="dns_cf"
  export CF_Token CF_Zone_ID CF_Account_ID
  echo "[cert] 使用 Cloudflare 验证"
else
  echo "[cert][ERROR] 未提供 DNS 凭据。DNSPod 需 DP_Id+DP_Key; Cloudflare 需 CF_Token+CF_Zone_ID+CF_Account_ID" >&2
  exit 1
fi

# 签发 (RSA 2048, 与云托管兼容最好)
acme.sh --issue --domain "$DOMAIN" --dns "$DNS_MODE" --keylength 2048

# 导出为 PEM (云托管上传格式)
acme.sh --install-cert -d "$DOMAIN" \
  --fullchain-file "$OUT_DIR/fullchain.pem" \
  --key-file "$OUT_DIR/privkey.pem"

echo ""
echo "=========================================="
echo " 证书已生成 ✅"
echo "   证书: $OUT_DIR/fullchain.pem"
echo "   私钥: $OUT_DIR/privkey.pem"
echo "=========================================="
echo "下一步: 云托管控制台 → 服务设置 → 自定义域名 → 添加 $DOMAIN"
echo "        → 上传上面两个文件 → DNS 加 CNAME 指向你的云托管默认域名"
echo "        → 企微后台回调 URL 填: https://$DOMAIN/callback"
