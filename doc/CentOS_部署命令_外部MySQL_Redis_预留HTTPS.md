# CentOS 部署命令（前端 + 后端，外部 MySQL/Redis，预留 HTTPS）

> 适用：你有一台 CentOS 服务器（7/8/9 均可按命令微调），**MySQL 和 Redis 已经在别处部署好**；本机只部署 **后端 JAR + Nginx 静态前端 + 反向代理**。  
> 目标：HTTP 先跑通，并把 HTTPS 改造位点（证书、Nginx、后端 `REQUIRE_HTTPS`）提前预留好。

---

## 0. 你需要准备的值（先填好）

先在服务器上设置这些变量（后面的命令会直接用到）：

```bash
export DOMAIN="your-domain.com"

export DB_HOST="your-mysql-host-or-ip"
export DB_PORT="3306"
export DB_NAME="ai_data_platform"
export DB_USER="your_db_user"
export DB_PASS="your_db_password"

export REDIS_HOST="your-redis-host-or-ip"
export REDIS_PORT="6379"
export REDIS_PASS=""   # 无密码就留空
```

### 0.1（推荐）使用一键脚本自动部署

仓库已提供脚本：`deploy/centos_autodeploy.sh`，它会在服务器上：

- 安装 Nginx + Java17（尽力而为，CentOS7 需你提前装好 JDK17）
- 创建 `dataelf` 用户与目录
- 写入 `/opt/dataelf/backend/.env`
- 创建 `dataelf-backend` systemd 服务
- 写入 Nginx（HTTP）配置：`/` 静态站点 + `/api` 反代 + `/uploads` 透传
- 启动/重载服务

你只需要：

- 上传后端 JAR 到 `/opt/dataelf/backend/app.jar`
- 上传前端 `dist/` 到 `/opt/dataelf/frontend/dist/`
- 在服务器创建 `/opt/dataelf/deploy.env`（不要提交到仓库）

创建 `deploy.env`（在服务器执行）：

```bash
sudo mkdir -p /opt/dataelf
sudo cp -f ./deploy/deploy.env.example /opt/dataelf/deploy.env
sudo vim /opt/dataelf/deploy.env
sudo chmod 600 /opt/dataelf/deploy.env
```

运行脚本（在服务器执行）：

```bash
sudo bash ./deploy/centos_autodeploy.sh /opt/dataelf/deploy.env
```

> 说明：前端会通过同域名的 `/api` 访问后端；后端监听 `127.0.0.1:8080`（仅本机），由 Nginx 对外提供 80/443。

---

## 1) CentOS 基础环境（一次性）

### 1.1 安装依赖（Java 17 + Nginx + 常用工具）

> **推荐 CentOS 8/9（或 Rocky/Alma 8/9）**：仓库里后端需要 Java 17。  
> CentOS 7 已 EOL，系统源里常见只有 Java 8；如果你必须用 7，建议用 Temurin/Corretto 之类的 JDK 17 安装包。

```bash
# CentOS 8/9 / Rocky / Alma（推荐）
sudo dnf -y update
sudo dnf -y install nginx java-17-openjdk java-17-openjdk-headless unzip tar curl vim

# （可选）如果你后面要用 certbot 配 HTTPS：
sudo dnf -y install epel-release
sudo dnf -y install certbot python3-certbot-nginx
```

```bash
# CentOS 7（不推荐；示例：装 Temurin 17）
sudo yum -y update
sudo yum -y install nginx unzip tar curl vim

sudo tee /etc/yum.repos.d/adoptium.repo >/dev/null << 'EOF'
[Adoptium]
name=Eclipse Adoptium
baseurl=https://packages.adoptium.net/artifactory/rpm/centos/7/$basearch
enabled=1
gpgcheck=0
EOF

sudo yum -y install temurin-17-jdk
```

验证：

```bash
java -version
nginx -v
```

### 1.2 防火墙放行（HTTP 先放行，HTTPS 预留）

```bash
sudo systemctl enable --now firewalld
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

### 1.3 SELinux（如果开启了，允许 Nginx 反代）

如果你的系统 SELinux 是 `Enforcing`，需要允许 Nginx 访问网络：

```bash
sudo setsebool -P httpd_can_network_connect 1
```

---

## 2) 部署后端（JAR + systemd）

### 2.1 创建运行用户与目录

```bash
sudo useradd --system --home /opt/dataelf --shell /sbin/nologin dataelf || true

sudo mkdir -p /opt/dataelf/backend/{config,logs,uploads}
sudo chown -R dataelf:dataelf /opt/dataelf/backend
sudo chmod 750 /opt/dataelf/backend
```

### 2.2 上传后端 JAR

在你的构建机（本地或 CI）打包：

```bash
cd backend
mvn clean package -DskipTests
ls -lh target/ai-data-platform-1.0.0.jar
```

上传到服务器：

```bash
scp backend/target/ai-data-platform-1.0.0.jar root@YOUR_SERVER_IP:/opt/dataelf/backend/app.jar
```

服务器上修正权限：

```bash
sudo chown dataelf:dataelf /opt/dataelf/backend/app.jar
sudo chmod 640 /opt/dataelf/backend/app.jar
```

### 2.3 写入后端环境变量（对接外部 MySQL/Redis）

```bash
sudo tee /opt/dataelf/backend/.env >/dev/null << EOF
# ===== MySQL =====
SPRING_DATASOURCE_URL=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
DB_USERNAME=${DB_USER}
DB_PASSWORD=${DB_PASS}

# ===== Redis =====
REDIS_HOST=${REDIS_HOST}
REDIS_PORT=${REDIS_PORT}
REDIS_PASSWORD=${REDIS_PASS}

# ===== JWT（必须改成强随机）=====
JWT_SECRET=$(openssl rand -hex 32)

# ===== 站点域名（先用 http，后续切 https）=====
APP_BASE_URL=http://${DOMAIN}
CORS_ORIGINS=http://${DOMAIN}

# ===== HTTPS 强制（先关闭，HTTPS 配好后再改 true）=====
REQUIRE_HTTPS=false

# ===== 让后端识别反向代理的 X-Forwarded-*（为 HTTPS 切换预留）=====
SERVER_FORWARD_HEADERS_STRATEGY=native
EOF
```
收紧权限：

```bash
sudo chmod 600 /opt/dataelf/backend/.env
sudo chown dataelf:dataelf /opt/dataelf/backend/.env
```

> 如果你的 Redis 有密码：把 `.env` 里的 `REDIS_PASSWORD=` 填上即可。无密码就留空。

### 2.4 systemd 服务（开机自启）

```bash
sudo tee /etc/systemd/system/dataelf-backend.service >/dev/null << 'EOF'
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

# 日志建议走 journald（更好维护）；也可在应用侧写 logs/ 目录
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable --now dataelf-backend
```

检查状态与日志：

```bash
sudo systemctl status dataelf-backend --no-pager
sudo journalctl -u dataelf-backend -f
```

本机健康检查：

```bash
curl -sS http://127.0.0.1:8080/actuator/health
```

---

## 3) 部署前端（构建 dist + Nginx 静态托管）

### 3.1 构建前端 dist

> 推荐在你的构建机（本地/CI）构建，然后上传 `dist/`。  
> 关键：**必须提供 `VITE_API_BASE_URL`**（否则个别页面会拼出 `undefined/api/...`）。

```bash
cd frontend

cat > .env.production << 'EOF'
# 同域名部署：留空字符串即可，拼接时会得到 /api/xxx
VITE_API_BASE_URL=
VITE_APP_TITLE=数流精灵
EOF

npm install
npm run build
ls -lh dist/
```

### 3.2 上传 dist 到服务器

服务器创建目录：

```bash
sudo mkdir -p /opt/dataelf/frontend/dist
sudo chown -R nginx:nginx /opt/dataelf/frontend
sudo chmod -R 755 /opt/dataelf/frontend
```

上传（在构建机执行）：

```bash
scp -r frontend/dist/* root@YOUR_SERVER_IP:/opt/dataelf/frontend/dist/
```

---

## 4) Nginx（HTTP 先上线，HTTPS 预留）

### 4.1 写入 Nginx 配置（HTTP）

```bash
sudo tee /etc/nginx/conf.d/dataelf.conf.template >/dev/null << 'EOF'
server {
    listen 80;
    server_name $DOMAIN;

    # 给未来 HTTPS/Certbot 预留（不会影响 HTTP）
    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }

    root /opt/dataelf/frontend/dist;
    index index.html;

    client_max_body_size 100M;

    # 前端 SPA 路由
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 反代（同域名 /api）
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket（如果后端未来用到）
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # 上传文件透传
    location /uploads {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF
```

生成最终 Nginx 配置（推荐用 `envsubst`，避免手改出错）：

```bash
sudo dnf -y install gettext || true
sudo yum -y install gettext || true
sudo envsubst '$DOMAIN' < /etc/nginx/conf.d/dataelf.conf.template | sudo tee /etc/nginx/conf.d/dataelf.conf >/dev/null
```

> 如果你没有 `dnf`（或上面命令失败），就直接手改 `server_name`：`sudo vim /etc/nginx/conf.d/dataelf.conf`。

启用并验证：

```bash
sudo nginx -t
sudo systemctl enable --now nginx
sudo systemctl reload nginx
```

验证：

```bash
curl -I "http://$DOMAIN"
curl -I "http://$DOMAIN/api/actuator/health"
```

---

## 5) HTTPS 改造（预留位点，随时切换）

你现在可以先不做；等域名解析好/备案好/证书准备好之后再执行本节。

### 5.1 方式 A：Certbot（Let's Encrypt，推荐）

```bash
# 先确保 DNS 已指向本机公网 IP，且 80/443 放行
sudo mkdir -p /var/www/html
sudo certbot --nginx -d "$DOMAIN"
```

Certbot 成功后会自动：

- 生成证书到 `/etc/letsencrypt/live/your-domain.com/`
- 修改 Nginx 配置启用 `443 ssl`
- 配置 HTTP→HTTPS 跳转（视你的选择）

测试：

```bash
sudo nginx -t
sudo systemctl reload nginx
curl -I "https://$DOMAIN"
```

### 5.2 切换后端强制 HTTPS（可选，但建议）

当你确认 Nginx 已经是 HTTPS 对外后，再把后端 `.env` 改成：

```bash
sudo sed -i \
  -e "s|APP_BASE_URL=http://|APP_BASE_URL=https://|g" \
  -e "s|CORS_ORIGINS=http://|CORS_ORIGINS=https://|g" \
  -e "s|REQUIRE_HTTPS=false|REQUIRE_HTTPS=true|g" \
  /opt/dataelf/backend/.env

sudo systemctl restart dataelf-backend
```

> 如果你开启了 `REQUIRE_HTTPS=true`，务必保留 `.env` 里的 `SERVER_FORWARD_HEADERS_STRATEGY=native`，否则后端可能认为请求非 HTTPS 并不断重定向。

---

## 6) 常用运维命令

```bash
# 后端
sudo systemctl restart dataelf-backend
sudo journalctl -u dataelf-backend -n 200 --no-pager

# Nginx
sudo nginx -t
sudo systemctl reload nginx
sudo tail -n 200 /var/log/nginx/error.log
```


