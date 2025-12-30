# 快速部署指南

## 前提条件

- ✅ 数据库已准备好（MySQL）
- ✅ Redis已准备好
- ✅ 服务器已安装Java 17和Nginx
- ✅ 域名已解析到服务器IP

---

## 一、后端快速部署

### 1. 创建目录结构

```bash
sudo mkdir -p /opt/dataelf/backend/{logs,config}
sudo chown -R $USER:$USER /opt/dataelf
```

### 2. 上传JAR文件

```bash
# 在本地构建
cd backend
mvn clean package -DskipTests

# 上传到服务器
scp target/ai-data-platform-1.0.0.jar user@your-server:/opt/dataelf/backend/app.jar
```

### 3. 创建配置文件

```bash
cat > /opt/dataelf/backend/config/application-prod.yml << 'EOF'
spring:
  datasource:
    url: jdbc:mysql://YOUR_DB_HOST:3306/ai_data_platform?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0

server:
  port: 8080

app:
  base-url: https://your-domain.com
  cors:
    allowed-origins: https://your-domain.com
  security:
    require-https: true
EOF
```

### 4. 创建环境变量文件

```bash
cat > /opt/dataelf/backend/.env << 'EOF'
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
REDIS_HOST=your_redis_host
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password
JWT_SECRET=your-super-secret-jwt-key-must-be-at-least-256-bits-long
ALIYUN_OSS_ENDPOINT=oss-cn-hangzhou.aliyuncs.com
ALIYUN_OSS_ACCESS_KEY_ID=your_key_id
ALIYUN_OSS_ACCESS_KEY_SECRET=your_key_secret
ALIYUN_OSS_BUCKET_NAME=your_bucket_name
MAIL_HOST=smtp.qq.com
MAIL_PORT=465
MAIL_USERNAME=your_email@qq.com
MAIL_PASSWORD=your_email_password
EOF

chmod 600 /opt/dataelf/backend/.env
```

### 5. 创建Systemd服务

```bash
sudo tee /etc/systemd/system/dataelf-backend.service > /dev/null << 'EOF'
[Unit]
Description=DataElf Backend Service
After=network.target

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/dataelf/backend
EnvironmentFile=/opt/dataelf/backend/.env
ExecStart=/usr/bin/java -Xms512m -Xmx2048m -jar /opt/dataelf/backend/app.jar --spring.profiles.active=prod --spring.config.additional-location=file:/opt/dataelf/backend/config/
Restart=always
RestartSec=10
StandardOutput=append:/opt/dataelf/backend/logs/stdout.log
StandardError=append:/opt/dataelf/backend/logs/stderr.log

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable dataelf-backend
sudo systemctl start dataelf-backend
```

### 6. 验证后端

```bash
# 查看状态
sudo systemctl status dataelf-backend

# 查看日志
tail -f /opt/dataelf/backend/logs/application.log

# 测试接口
curl http://localhost:8080/actuator/health
```

---

## 二、前端快速部署

### 1. 构建前端

```bash
cd frontend
npm install
npm run build
```

### 2. 上传文件

```bash
sudo mkdir -p /opt/dataelf/frontend/dist
scp -r dist/* user@your-server:/opt/dataelf/frontend/dist/
```

---

## 三、Nginx + HTTPS配置

### 选择SSL证书方案

**方案A：Let's Encrypt（推荐，自动续期）**
**方案B：阿里云免费证书（推荐，中文支持）**

详细申请流程请参考：`ALIYUN_SSL_CERTIFICATE_GUIDE.md`

### 1. 安装Certbot（如果使用Let's Encrypt）

```bash
sudo apt update
sudo apt install certbot python3-certbot-nginx -y
```

### 2. 配置Nginx（先配置HTTP）

```bash
sudo tee /etc/nginx/sites-available/dataelf > /dev/null << 'EOF'
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;
    
    root /opt/dataelf/frontend/dist;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        client_max_body_size 100M;
    }
}
EOF

sudo ln -s /etc/nginx/sites-available/dataelf /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 3. 获取SSL证书

**使用Let's Encrypt:**

```bash
sudo certbot --nginx -d your-domain.com -d www.your-domain.com
```

Certbot会自动：
- 获取SSL证书
- 配置Nginx使用HTTPS
- 设置HTTP到HTTPS的重定向
- 配置自动续期

**使用阿里云证书:**

1. 在阿里云控制台申请并下载证书（参考 `ALIYUN_SSL_CERTIFICATE_GUIDE.md`）
2. 上传证书到服务器：
```bash
sudo mkdir -p /etc/nginx/ssl
sudo chmod 700 /etc/nginx/ssl
scp your-domain.com.pem user@server:/tmp/
scp your-domain.com.key user@server:/tmp/
sudo mv /tmp/your-domain.com.* /etc/nginx/ssl/
sudo chmod 644 /etc/nginx/ssl/your-domain.com.pem
sudo chmod 600 /etc/nginx/ssl/your-domain.com.key
```
3. 修改Nginx配置中的证书路径（见步骤2的配置）
4. 重新加载Nginx：`sudo systemctl reload nginx`

### 4. 验证HTTPS

```bash
# 测试配置
sudo nginx -t

# 访问网站
curl https://your-domain.com
```

---

## 四、防火墙配置

```bash
# Ubuntu/Debian
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable

# CentOS/RHEL
sudo firewall-cmd --permanent --add-service=ssh
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

---

## 五、验证部署

### 检查后端

```bash
# 服务状态
sudo systemctl status dataelf-backend

# 健康检查
curl https://your-domain.com/api/actuator/health

# 查看日志
tail -f /opt/dataelf/backend/logs/application.log
```

### 检查前端

```bash
# 访问网站
curl -I https://your-domain.com

# 检查Nginx日志
sudo tail -f /var/log/nginx/dataelf-access.log
```

---

## 六、更新部署

### 更新后端

```bash
# 1. 停止服务
sudo systemctl stop dataelf-backend

# 2. 备份旧版本
sudo cp /opt/dataelf/backend/app.jar /opt/dataelf/backend/app.jar.backup

# 3. 上传新版本
scp target/ai-data-platform-1.0.0.jar user@server:/opt/dataelf/backend/app.jar

# 4. 启动服务
sudo systemctl start dataelf-backend

# 5. 检查状态
sudo systemctl status dataelf-backend
```

### 更新前端

```bash
# 1. 备份
sudo cp -r /opt/dataelf/frontend/dist /opt/dataelf/frontend/dist.backup

# 2. 上传新版本
scp -r dist/* user@server:/opt/dataelf/frontend/dist/

# 3. 设置权限
sudo chown -R www-data:www-data /opt/dataelf/frontend/dist

# 4. 重新加载Nginx
sudo systemctl reload nginx
```

---

## 七、常见问题

### 后端无法启动

```bash
# 查看日志
tail -50 /opt/dataelf/backend/logs/application.log
tail -50 /opt/dataelf/backend/logs/stderr.log

# 检查配置
cat /opt/dataelf/backend/.env
```

### SSL证书问题

```bash
# 测试证书
sudo certbot certificates

# 手动续期
sudo certbot renew

# 测试续期（不实际续期）
sudo certbot renew --dry-run
```

### Nginx配置错误

```bash
# 测试配置
sudo nginx -t

# 查看错误日志
sudo tail -f /var/log/nginx/error.log
```

---

## 八、一键部署脚本

创建 `deploy.sh`：

```bash
#!/bin/bash
set -e

DOMAIN="your-domain.com"
DB_HOST="your_db_host"
DB_USER="your_db_user"
DB_PASS="your_db_pass"
REDIS_HOST="your_redis_host"
REDIS_PASS="your_redis_pass"

echo "开始部署..."

# 创建目录
sudo mkdir -p /opt/dataelf/{backend/{logs,config},frontend/dist}
sudo chown -R $USER:$USER /opt/dataelf

# 配置后端环境变量
cat > /opt/dataelf/backend/.env << EOF
DB_USERNAME=$DB_USER
DB_PASSWORD=$DB_PASS
REDIS_HOST=$REDIS_HOST
REDIS_PORT=6379
REDIS_PASSWORD=$REDIS_PASS
JWT_SECRET=$(openssl rand -base64 32)
APP_BASE_URL=https://$DOMAIN
EOF

# 创建Systemd服务
sudo tee /etc/systemd/system/dataelf-backend.service > /dev/null << 'EOF'
[Unit]
Description=DataElf Backend Service
After=network.target

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/dataelf/backend
EnvironmentFile=/opt/dataelf/backend/.env
ExecStart=/usr/bin/java -Xms512m -Xmx2048m -jar /opt/dataelf/backend/app.jar --spring.profiles.active=prod --spring.config.additional-location=file:/opt/dataelf/backend/config/
Restart=always
RestartSec=10
StandardOutput=append:/opt/dataelf/backend/logs/stdout.log
StandardError=append:/opt/dataelf/backend/logs/stderr.log

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable dataelf-backend

echo "部署完成！"
echo "请执行以下步骤："
echo "1. 上传后端jar文件到 /opt/dataelf/backend/app.jar"
echo "2. 上传前端文件到 /opt/dataelf/frontend/dist/"
echo "3. 配置Nginx并获取SSL证书"
echo "4. 启动服务: sudo systemctl start dataelf-backend"
```

---

部署完成后，访问 `https://your-domain.com` 即可使用应用！
