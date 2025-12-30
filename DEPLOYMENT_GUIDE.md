# 数流精灵 - 云服务器部署指南

本文档提供在单台云服务器上部署数流精灵的完整步骤。

## 环境要求

| 组件 | 版本要求 | 说明 |
|------|---------|------|
| 操作系统 | CentOS 7+ / Ubuntu 20.04+ | 推荐 Ubuntu 22.04 |
| CPU | 2核+ | 推荐 4核 |
| 内存 | 4GB+ | 推荐 8GB |
| 硬盘 | 40GB+ | SSD 推荐 |
| Java | JDK 17 | 必须 |
| Node.js | 18+ | 用于构建前端 |
| MySQL | 5.7+ | 数据库 |
| Redis | 6+ | 缓存 |
| Nginx | 1.18+ | 反向代理 |

---

## 一、服务器初始化

### 1.1 更新系统

```bash
# Ubuntu/Debian
sudo apt update && sudo apt upgrade -y

# CentOS
sudo yum update -y
```

### 1.2 安装基础工具

```bash
# Ubuntu/Debian
sudo apt install -y curl wget git vim unzip

# CentOS
sudo yum install -y curl wget git vim unzip
```

---

## 二、安装依赖服务

### 2.1 安装 JDK 17

```bash
# Ubuntu/Debian
sudo apt install -y openjdk-17-jdk

# CentOS (使用 Amazon Corretto)
sudo rpm --import https://yum.corretto.aws/corretto.key
sudo curl -L -o /etc/yum.repos.d/corretto.repo https://yum.corretto.aws/corretto.repo
sudo yum install -y java-17-amazon-corretto-devel

# 验证
java -version
```

### 2.2 安装 Node.js 18

```bash
# 使用 NodeSource
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# 或使用 nvm (推荐)
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
source ~/.bashrc
nvm install 18
nvm use 18

# 验证
node -v
npm -v
```

### 2.3 安装 MySQL 5.7

```bash
# Ubuntu
sudo apt install -y mysql-server

# 启动并设置开机自启
sudo systemctl start mysql
sudo systemctl enable mysql

# 安全配置
sudo mysql_secure_installation
```

创建数据库和用户：

```bash
sudo mysql -u root -p
```

```sql
-- 创建数据库
CREATE DATABASE ai_data_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER 'dataelf'@'localhost' IDENTIFIED BY 'YourStrongPassword123!';

-- 授权
GRANT ALL PRIVILEGES ON ai_data_platform.* TO 'dataelf'@'localhost';
FLUSH PRIVILEGES;

EXIT;
```

### 2.4 安装 Redis

```bash
# Ubuntu/Debian
sudo apt install -y redis-server

# 启动并设置开机自启
sudo systemctl start redis-server
sudo systemctl enable redis-server

# 验证
redis-cli ping
# 应返回 PONG
```

配置 Redis 密码（可选但推荐）：

```bash
sudo vim /etc/redis/redis.conf
```

找到并修改：
```
requirepass YourRedisPassword123!
```

重启 Redis：
```bash
sudo systemctl restart redis-server
```

### 2.5 安装 Nginx

```bash
# Ubuntu/Debian
sudo apt install -y nginx

# 启动并设置开机自启
sudo systemctl start nginx
sudo systemctl enable nginx
```

---

## 三、部署应用

### 3.1 创建应用目录

```bash
sudo mkdir -p /opt/dataelf
sudo mkdir -p /opt/dataelf/backend
sudo mkdir -p /opt/dataelf/frontend
sudo mkdir -p /opt/dataelf/logs
sudo mkdir -p /opt/dataelf/uploads

# 设置权限
sudo chown -R $USER:$USER /opt/dataelf
```

### 3.2 克隆代码

```bash
cd /opt/dataelf
git clone https://github.com/jingxi10/dataelf.git source
```

### 3.3 构建后端

```bash
cd /opt/dataelf/source/backend

# 构建 (跳过测试加快速度)
./mvnw clean package -DskipTests

# 或者如果没有 mvnw
# 先安装 Maven
sudo apt install -y maven
mvn clean package -DskipTests

# 复制 jar 包
cp target/ai-data-platform-1.0.0.jar /opt/dataelf/backend/app.jar
```

### 3.4 配置后端环境变量

创建环境配置文件：

```bash
vim /opt/dataelf/backend/.env
```

内容：
```bash
# 数据库配置
DB_USERNAME=dataelf
DB_PASSWORD=YourStrongPassword123!

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=YourRedisPassword123!

# JWT密钥 (生成一个随机字符串，至少32位)
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production-at-least-256-bits

# 应用配置
APP_BASE_URL=https://yourdomain.com
CORS_ORIGINS=https://yourdomain.com

# 阿里云OSS配置 (如果使用)
ALIYUN_OSS_ENDPOINT=oss-cn-hangzhou.aliyuncs.com
ALIYUN_OSS_ACCESS_KEY_ID=your-access-key-id
ALIYUN_OSS_ACCESS_KEY_SECRET=your-access-key-secret
ALIYUN_OSS_BUCKET_NAME=your-bucket-name
```

### 3.5 创建 Systemd 服务

```bash
sudo vim /etc/systemd/system/dataelf.service
```

内容：
```ini
[Unit]
Description=DataElf Backend Service
After=network.target mysql.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/dataelf/backend
EnvironmentFile=/opt/dataelf/backend/.env
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar /opt/dataelf/backend/app.jar --spring.profiles.active=prod
Restart=always
RestartSec=10
StandardOutput=append:/opt/dataelf/logs/backend.log
StandardError=append:/opt/dataelf/logs/backend-error.log

[Install]
WantedBy=multi-user.target
```

启动服务：

```bash
sudo systemctl daemon-reload
sudo systemctl start dataelf
sudo systemctl enable dataelf

# 查看状态
sudo systemctl status dataelf

# 查看日志
tail -f /opt/dataelf/logs/backend.log
```

### 3.6 构建前端

```bash
cd /opt/dataelf/source/frontend

# 创建生产环境配置
cat > .env.production << EOF
VITE_API_BASE_URL=https://yourdomain.com
VITE_APP_TITLE=数流精灵
EOF

# 安装依赖
npm install

# 构建
npm run build

# 复制构建产物
cp -r dist/* /opt/dataelf/frontend/
```

---

## 四、配置 Nginx

### 4.1 创建 Nginx 配置

```bash
sudo vim /etc/nginx/sites-available/dataelf
```

内容：
```nginx
server {
    listen 80;
    server_name yourdomain.com;
    
    # 重定向到 HTTPS (如果配置了SSL)
    # return 301 https://$server_name$request_uri;
    
    # 前端静态文件
    root /opt/dataelf/frontend;
    index index.html;
    
    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;
    gzip_min_length 1000;
    
    # 前端路由
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API 代理
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket 支持
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # 上传文件代理
    location /uploads {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    # Swagger 文档
    location /swagger-ui {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
    }
    
    location /v3/api-docs {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
    }
    
    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
```

### 4.2 启用配置

```bash
# 创建软链接
sudo ln -s /etc/nginx/sites-available/dataelf /etc/nginx/sites-enabled/

# 删除默认配置
sudo rm /etc/nginx/sites-enabled/default

# 测试配置
sudo nginx -t

# 重载 Nginx
sudo systemctl reload nginx
```

---

## 五、配置 HTTPS (推荐)

### 5.1 安装 Certbot

```bash
sudo apt install -y certbot python3-certbot-nginx
```

### 5.2 获取证书

```bash
sudo certbot --nginx -d yourdomain.com
```

### 5.3 自动续期

```bash
# 测试续期
sudo certbot renew --dry-run

# Certbot 会自动添加定时任务
```

---

## 六、防火墙配置

```bash
# Ubuntu (ufw)
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable

# CentOS (firewalld)
sudo firewall-cmd --permanent --add-service=ssh
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

---

## 七、验证部署

### 7.1 检查服务状态

```bash
# 后端服务
sudo systemctl status dataelf

# MySQL
sudo systemctl status mysql

# Redis
sudo systemctl status redis-server

# Nginx
sudo systemctl status nginx
```

### 7.2 测试接口

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 公开配置
curl http://localhost:8080/api/public/config
```

### 7.3 访问网站

打开浏览器访问：`http://yourdomain.com`

默认管理员账号：
- 邮箱：`admin@dataelf.com`
- 密码：`admin123`

---

## 八、常用运维命令

### 8.1 服务管理

```bash
# 重启后端
sudo systemctl restart dataelf

# 查看后端日志
tail -f /opt/dataelf/logs/backend.log

# 重启 Nginx
sudo systemctl restart nginx

# 查看 Nginx 日志
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log
```

### 8.2 更新部署

```bash
# 拉取最新代码
cd /opt/dataelf/source
git pull

# 重新构建后端
cd backend
./mvnw clean package -DskipTests
cp target/ai-data-platform-1.0.0.jar /opt/dataelf/backend/app.jar
sudo systemctl restart dataelf

# 重新构建前端
cd ../frontend
npm install
npm run build
cp -r dist/* /opt/dataelf/frontend/
```

### 8.3 数据库备份

```bash
# 备份
mysqldump -u dataelf -p ai_data_platform > backup_$(date +%Y%m%d).sql

# 恢复
mysql -u dataelf -p ai_data_platform < backup_20241222.sql
```

---

## 九、Docker 部署 (可选)

如果服务器已安装 Docker，可以使用 Docker Compose 快速部署基础服务：

```bash
cd /opt/dataelf/source
docker-compose up -d
```

这会启动 MySQL、Redis 服务，然后只需部署应用即可。

---

## 十、故障排查

### 问题1：后端启动失败

```bash
# 查看详细日志
journalctl -u dataelf -f

# 检查端口占用
netstat -tlnp | grep 8080
```

### 问题2：数据库连接失败

```bash
# 测试连接
mysql -u dataelf -p -h localhost ai_data_platform

# 检查 MySQL 状态
sudo systemctl status mysql
```

### 问题3：Redis 连接失败

```bash
# 测试连接
redis-cli -a YourRedisPassword123! ping

# 检查 Redis 状态
sudo systemctl status redis-server
```

### 问题4：Nginx 502 错误

```bash
# 检查后端是否运行
curl http://localhost:8080/actuator/health

# 检查 Nginx 错误日志
tail -f /var/log/nginx/error.log
```

---

## 附录：快速部署脚本

创建一键部署脚本 `deploy.sh`：

```bash
#!/bin/bash
set -e

echo "=== 数流精灵部署脚本 ==="

# 变量
APP_DIR=/opt/dataelf
SOURCE_DIR=$APP_DIR/source

# 拉取代码
echo ">>> 拉取最新代码..."
cd $SOURCE_DIR
git pull

# 构建后端
echo ">>> 构建后端..."
cd $SOURCE_DIR/backend
./mvnw clean package -DskipTests
cp target/ai-data-platform-1.0.0.jar $APP_DIR/backend/app.jar

# 重启后端
echo ">>> 重启后端服务..."
sudo systemctl restart dataelf

# 构建前端
echo ">>> 构建前端..."
cd $SOURCE_DIR/frontend
npm install
npm run build
rm -rf $APP_DIR/frontend/*
cp -r dist/* $APP_DIR/frontend/

echo "=== 部署完成 ==="
```

使用：
```bash
chmod +x deploy.sh
./deploy.sh
```
