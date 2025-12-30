#!/usr/bin/env bash
set -euo pipefail

# One-click deploy for CentOS/RHEL/Rocky/Alma:
# - Installs nginx + Java17
# - Creates dataelf user/dirs
# - Writes backend env (/opt/dataelf/backend/.env)
# - Creates systemd service
# - Writes nginx site config (HTTP) with /api and /uploads proxy
# - Starts services
#
# Prereq:
# - Run as root on the server
# - Provide deploy.env (see deploy/deploy.env.example)
# - Upload backend jar to /opt/dataelf/backend/app.jar
# - Upload frontend dist to /opt/dataelf/frontend/dist

ENV_FILE="${1:-/opt/dataelf/deploy.env}"

if [[ "${EUID}" -ne 0 ]]; then
  echo "ERROR: run as root (e.g. sudo -i)" >&2
  exit 1
fi

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "ERROR: env file not found: ${ENV_FILE}" >&2
  echo "Create it by copying deploy/deploy.env.example to ${ENV_FILE} and filling values." >&2
  exit 1
fi

# shellcheck disable=SC1090
source "${ENV_FILE}"

require_var() {
  local name="$1"
  if [[ -z "${!name:-}" ]]; then
    echo "ERROR: required env var ${name} is empty in ${ENV_FILE}" >&2
    exit 1
  fi
}

require_var DOMAIN
require_var DB_HOST
require_var DB_PORT
require_var DB_NAME
require_var DB_USER
require_var DB_PASS
require_var REDIS_HOST
require_var REDIS_PORT
require_var JWT_SECRET

BACKEND_PORT="${BACKEND_PORT:-8080}"
FRONTEND_ROOT="${FRONTEND_ROOT:-/opt/dataelf/frontend/dist}"
REQUIRE_HTTPS="${REQUIRE_HTTPS:-false}"

echo "[1/7] Installing packages..."
if command -v dnf >/dev/null 2>&1; then
  dnf -y install nginx java-17-openjdk java-17-openjdk-headless curl tar unzip vim gettext || true
elif command -v yum >/dev/null 2>&1; then
  yum -y install nginx curl tar unzip vim gettext || true
  # Best effort: Java 17 might not exist on CentOS7 default repos.
  if ! command -v java >/dev/null 2>&1; then
    echo "WARN: java not found. Install JDK 17 first (Temurin/Corretto) then rerun." >&2
  fi
else
  echo "ERROR: neither dnf nor yum found" >&2
  exit 1
fi

echo "[2/7] Creating user and directories..."
useradd --system --home /opt/dataelf --shell /sbin/nologin dataelf 2>/dev/null || true
mkdir -p /opt/dataelf/backend/{config,logs,uploads} /opt/dataelf/frontend/dist /var/www/html
chown -R dataelf:dataelf /opt/dataelf/backend

# nginx user differs by distro; try both.
if id nginx >/dev/null 2>&1; then
  chown -R nginx:nginx /opt/dataelf/frontend
fi

echo "[3/7] Writing backend env..."
cat > /opt/dataelf/backend/.env << EOF
DB_USERNAME=${DB_USER}
DB_PASSWORD=${DB_PASS}
REDIS_HOST=${REDIS_HOST}
REDIS_PORT=${REDIS_PORT}
REDIS_PASSWORD=${REDIS_PASS:-}
JWT_SECRET=${JWT_SECRET}
APP_BASE_URL=http://${DOMAIN}
CORS_ORIGINS=http://${DOMAIN}
REQUIRE_HTTPS=${REQUIRE_HTTPS}
SERVER_FORWARD_HEADERS_STRATEGY=native
SPRING_DATASOURCE_URL=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai

MAIL_HOST=${MAIL_HOST:-}
MAIL_PORT=${MAIL_PORT:-465}
MAIL_USERNAME=${MAIL_USERNAME:-}
MAIL_PASSWORD=${MAIL_PASSWORD:-}
MAIL_SSL_ENABLE=${MAIL_SSL_ENABLE:-true}

ALIYUN_OSS_ENDPOINT=${ALIYUN_OSS_ENDPOINT:-}
ALIYUN_OSS_ACCESS_KEY_ID=${ALIYUN_OSS_ACCESS_KEY_ID:-}
ALIYUN_OSS_ACCESS_KEY_SECRET=${ALIYUN_OSS_ACCESS_KEY_SECRET:-}
ALIYUN_OSS_BUCKET_NAME=${ALIYUN_OSS_BUCKET_NAME:-}
ALIYUN_OSS_CUSTOM_DOMAIN=${ALIYUN_OSS_CUSTOM_DOMAIN:-}
ALIYUN_OSS_DIR_PREFIX=${ALIYUN_OSS_DIR_PREFIX:-dataelf/}
EOF

chmod 600 /opt/dataelf/backend/.env
chown dataelf:dataelf /opt/dataelf/backend/.env

echo "[4/7] Creating systemd service..."
cat > /etc/systemd/system/dataelf-backend.service << EOF
[Unit]
Description=DataElf Backend (Spring Boot)
After=network.target

[Service]
Type=simple
User=dataelf
WorkingDirectory=/opt/dataelf/backend
EnvironmentFile=/opt/dataelf/backend/.env
ExecStart=/usr/bin/java -Xms512m -Xmx2048m -jar /opt/dataelf/backend/app.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable dataelf-backend

echo "[5/7] Writing nginx config..."
cat > /etc/nginx/conf.d/dataelf.conf.template << 'EOF'
server {
  listen 80;
  server_name $DOMAIN;

  location /.well-known/acme-challenge/ {
    root /var/www/html;
  }

  root $FRONTEND_ROOT;
  index index.html;
  client_max_body_size 100M;

  location / {
    try_files $uri $uri/ /index.html;
  }

  location /api {
    proxy_pass http://127.0.0.1:$BACKEND_PORT;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
  }

  location /uploads {
    proxy_pass http://127.0.0.1:$BACKEND_PORT;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
  }
}
EOF

export FRONTEND_ROOT BACKEND_PORT DOMAIN
envsubst '$DOMAIN $FRONTEND_ROOT $BACKEND_PORT' \
  < /etc/nginx/conf.d/dataelf.conf.template \
  > /etc/nginx/conf.d/dataelf.conf

echo "[6/7] Starting services..."
systemctl enable --now nginx
nginx -t
systemctl reload nginx

if [[ -f /opt/dataelf/backend/app.jar ]]; then
  systemctl restart dataelf-backend
else
  echo "WARN: /opt/dataelf/backend/app.jar not found. Upload the jar then run: systemctl restart dataelf-backend" >&2
fi

echo "[7/7] Quick checks..."
echo "Nginx:  systemctl status nginx --no-pager"
echo "Backend: systemctl status dataelf-backend --no-pager"
echo "Health: curl -sS http://127.0.0.1:${BACKEND_PORT}/actuator/health"
echo "Site:   curl -I http://${DOMAIN}"


