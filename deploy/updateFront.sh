#!/usr/bin/env bash
set -euo pipefail

# 更新前端包：本地构建 -> 打包 dist -> 上传到服务器 -> 备份旧 dist -> 覆盖 -> reload nginx
#
# 用法：
#   bash deploy/updateFront.sh
#
# 你需要先改下面这些变量（或用环境变量覆盖）：
#   SERVER_HOST / SSH_USER / SSH_PORT / REMOTE_FRONTEND_DIST

SERVER_HOST="${SERVER_HOST:-47.100.191.204}"
SSH_USER="${SSH_USER:-root}"
SSH_PORT="${SSH_PORT:-22}"

# 服务器前端目录（和你 nginx root 对应）
REMOTE_FRONTEND_DIST="${REMOTE_FRONTEND_DIST:-/opt/dataelf/frontend/dist}"

# 本地前端目录
FRONTEND_DIR="${FRONTEND_DIR:-frontend}"

# 可选：构建时写入 VITE_API_BASE_URL（同域部署建议留空）
VITE_API_BASE_URL="${VITE_API_BASE_URL:-}"
VITE_APP_TITLE="${VITE_APP_TITLE:-铼河数据}"

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || { echo "ERROR: missing command: $1" >&2; exit 1; }
}

require_cmd ssh
require_cmd tar
require_cmd npm

echo "[1/4] Build frontend..."
pushd "${FRONTEND_DIR}" >/dev/null
cat > .env.production << EOF
VITE_API_BASE_URL=${VITE_API_BASE_URL}
VITE_APP_TITLE=${VITE_APP_TITLE}
EOF

# 如果你更希望严格可复现：把 npm install 换成 npm ci
npm install
npm run build
popd >/dev/null

if [[ ! -d "${FRONTEND_DIR}/dist" ]]; then
  echo "ERROR: ${FRONTEND_DIR}/dist not found after build" >&2
  exit 1
fi

echo "[2/4] Upload dist to server (stream tar over ssh)..."
STAMP="$(date +%Y%m%d_%H%M%S)"

tar -C "${FRONTEND_DIR}/dist" -czf - . | ssh -p "${SSH_PORT}" "${SSH_USER}@${SERVER_HOST}" bash -s << EOF
set -euo pipefail

REMOTE_DIST="${REMOTE_FRONTEND_DIST}"
BACKUP_DIR="\$(dirname "\${REMOTE_DIST}")/backup"
STAMP="${STAMP}"

mkdir -p "\${REMOTE_DIST}" "\${BACKUP_DIR}"

if [[ -d "\${REMOTE_DIST}" ]] && [[ -n "\$(ls -A "\${REMOTE_DIST}" 2>/dev/null || true)" ]]; then
  cp -a "\${REMOTE_DIST}" "\${BACKUP_DIR}/dist_\${STAMP}"
fi

rm -rf "\${REMOTE_DIST:?}/"*
tar -xzf - -C "\${REMOTE_DIST}"

# 尝试设置常见权限（nginx 用户因环境不同可能是 nginx/www/www-data）
if id nginx >/dev/null 2>&1; then
  chown -R nginx:nginx "\$(dirname "\${REMOTE_DIST}")" || true
elif id www >/dev/null 2>&1; then
  chown -R www:www "\$(dirname "\${REMOTE_DIST}")" || true
elif id www-data >/dev/null 2>&1; then
  chown -R www-data:www-data "\$(dirname "\${REMOTE_DIST}")" || true
fi

chmod -R 755 "\$(dirname "\${REMOTE_DIST}")" || true

echo "[3/4] Reload nginx..."
if systemctl is-active --quiet nginx; then
  systemctl reload nginx
  systemctl is-active --quiet nginx && echo "nginx reloaded (systemd)."
elif [[ -x /www/server/nginx/sbin/nginx ]]; then
  /www/server/nginx/sbin/nginx -t -c /www/server/nginx/conf/nginx.conf
  /www/server/nginx/sbin/nginx -s reload -c /www/server/nginx/conf/nginx.conf
  echo "nginx reloaded (bt/nginx)."
else
  echo "WARN: nginx not reloaded automatically (cannot find systemd nginx or bt nginx)." >&2
fi

echo "[4/4] Done. Frontend updated at: \${REMOTE_DIST}"
EOF

echo "OK: frontend updated on ${SSH_USER}@${SERVER_HOST}:${REMOTE_FRONTEND_DIST}"


