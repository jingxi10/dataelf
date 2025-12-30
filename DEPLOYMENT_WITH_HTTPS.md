# 完整部署方案（含HTTPS）

本文档提供前端和后端的完整部署方案，包括HTTPS配置。

## 目录结构

```
/opt/dataelf/
├── backend/              # 后端应用目录
│   ├── app.jar          # Spring Boot应用
│   ├── logs/            # 日志目录
│   └── config/          # 配置文件目录
├── frontend/            # 前端静态文件目录
│   └── dist/           # 构建后的前端文件
├── nginx/              # Nginx配置目录
│   ├── nginx.conf      # Nginx主配置
│   ├── ssl/            # SSL证书目录
│   │   ├── your-domain.crt
│   │   └── your-domain.key
│   └── conf.d/
│       └── dataelf.conf # 站点配置
└── scripts/            # 部署脚本目录
    ├── deploy-backend.sh
    ├── deploy-frontend.sh
    └── setup-ssl.sh
```

---

## 一、准备工作

### 1.1 服务器要求

- **操作系统**: Ubuntu 20.04+ / CentOS 7+ / Debian 10+
- **内存**: 至少 2GB
- **磁盘**: 至少 20GB
- **网络**: 公网IP，已配置域名（用于HTTPS）

### 1.2 软件依赖

```bash
# 安装Java 17
sudo apt update
sudo apt install openjdk-17-jdk -y

# 安装Nginx
sudo apt install nginx -y

# 安装必要的工具
sudo apt install curl wget unzip -y
```

### 1.3 SSL证书准备

**方式一：使用Let's Encrypt（推荐，免费，自动续期）**

```bash
# 安装Certbot
sudo apt install certbot python3-certbot-nginx -y

# 获取证书（替换为你的域名）
sudo certbot --nginx -d your-domain.com -d www.your-domain.com
```

**方式二：使用阿里云免费证书（推荐，中文支持）**

详细申请流程请参考：`ALIYUN_SSL_CERTIFICATE_GUIDE.md`

简要步骤：
1. 登录阿里云控制台 → SSL证书 → 免费证书
2. 购买免费证书（0元）
3. 申请证书，选择DNS验证或文件验证
4. 完成域名验证
5. 下载证书（选择Nginx格式）
6. 上传到服务器 `/etc/nginx/ssl/` 目录

**方式三：使用已有证书**

将证书文件上传到服务器：
- `your-domain.crt` 或 `your-domain.pem` - 证书文件
- `your-domain.key` - 私钥文件

---

## 二、后端部署

### 2.1 构建后端应用

在开发机器上构建：

```bash
cd backend
mvn clean package -DskipTests
# 生成的jar文件在: backend/target/ai-data-platform-1.0.0.jar
```

### 2.2 创建部署目录

```bash
sudo mkdir -p /opt/dataelf/backend/{logs,config}
sudo chown -R $USER:$USER /opt/dataelf
```

### 2.3 上传文件

```bash
# 上传jar文件
scp backend/target/ai-data-platform-1.0.0.jar user@your-server:/opt/dataelf/backend/app.jar

# 创建配置文件
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
  compression:
    enabled: true

# JWT配置
jwt:
  secret: ${JWT_SECRET}

# 应用配置
app:
  base-url: https://your-domain.com
  cors:
    allowed-origins: https://your-domain.com
  security:
    require-https: true

# 阿里云OSS配置
aliyun:
  oss:
    endpoint: ${ALIYUN_OSS_ENDPOINT}
    access-key-id: ${ALIYUN_OSS_ACCESS_KEY_ID}
    access-key-secret: ${ALIYUN_OSS_ACCESS_KEY_SECRET}
    bucket-name: ${ALIYUN_OSS_BUCKET_NAME}
    custom-domain: ${ALIYUN_OSS_CUSTOM_DOMAIN:}
    dir-prefix: ${ALIYUN_OSS_DIR_PREFIX:dataelf/}

# 邮件配置
spring:
  mail:
    host: ${MAIL_HOST}
    port: ${MAIL_PORT:465}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}

logging:
  level:
    root: INFO
    com.dataelf.platform: INFO
  file:
    name: /opt/dataelf/backend/logs/application.log
EOF
```

### 2.4 创建环境变量文件

```bash
cat > /opt/dataelf/backend/.env << 'EOF'
# 数据库配置
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# Redis配置
REDIS_HOST=your_redis_host
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# JWT密钥（必须修改，至少32个字符）
JWT_SECRET=your-super-secret-jwt-key-must-be-at-least-256-bits-long-change-this

# 阿里云OSS配置
ALIYUN_OSS_ENDPOINT=oss-cn-hangzhou.aliyuncs.com
ALIYUN_OSS_ACCESS_KEY_ID=your_access_key_id
ALIYUN_OSS_ACCESS_KEY_SECRET=your_access_key_secret
ALIYUN_OSS_BUCKET_NAME=your_bucket_name
ALIYUN_OSS_CUSTOM_DOMAIN=
ALIYUN_OSS_DIR_PREFIX=dataelf/

# 邮件配置
MAIL_HOST=smtp.qq.com
MAIL_PORT=465
MAIL_USERNAME=your_email@qq.com
MAIL_PASSWORD=your_email_password
MAIL_SSL_ENABLE=true
EOF

chmod 600 /opt/dataelf/backend/.env
```

### 2.5 创建Systemd服务

```bash
sudo cat > /etc/systemd/system/dataelf-backend.service << 'EOF'
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
sudo systemctl status dataelf-backend
```

### 2.6 验证后端

```bash
# 检查日志
tail -f /opt/dataelf/backend/logs/application.log

# 检查服务状态
curl http://localhost:8080/actuator/health
```

---

## 三、前端部署

### 3.1 构建前端应用

在开发机器上构建：

```bash
cd frontend
npm install
npm run build
# 生成的文件在: frontend/dist/
```

### 3.2 上传前端文件

```bash
# 创建目录
sudo mkdir -p /opt/dataelf/frontend/dist
sudo chown -R $USER:$USER /opt/dataelf/frontend

# 上传文件
scp -r frontend/dist/* user@your-server:/opt/dataelf/frontend/dist/
```

---

## 四、Nginx配置（含HTTPS）

### 4.1 创建Nginx配置

```bash
sudo cat > /etc/nginx/sites-available/dataelf << 'EOF'
# HTTP重定向到HTTPS
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;
    
    # Let's Encrypt验证
    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }
    
    # 其他请求重定向到HTTPS
    location / {
        return 301 https://$server_name$request_uri;
    }
}

# HTTPS配置
server {
    listen 443 ssl http2;
    server_name your-domain.com www.your-domain.com;
    
    # SSL证书配置
    ssl_certificate /etc/nginx/ssl/your-domain.crt;
    ssl_certificate_key /etc/nginx/ssl/your-domain.key;
    
    # SSL优化配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers 'ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384';
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    
    # 安全头
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    
    # 前端静态文件
    root /opt/dataelf/frontend/dist;
    index index.html;
    
    # Gzip压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/x-javascript application/xml+rss application/json application/javascript;
    
    # 前端路由（SPA）
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API代理到后端
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # 文件上传大小限制
        client_max_body_size 100M;
    }
    
    # 上传文件代理
    location /uploads {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-Proto $scheme;
        client_max_body_size 100M;
    }
    
    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
    
    # 禁止访问隐藏文件
    location ~ /\. {
        deny all;
    }
}
EOF

# 创建SSL证书目录
sudo mkdir -p /etc/nginx/ssl
sudo chmod 700 /etc/nginx/ssl

# 如果使用Let's Encrypt，证书会自动配置
# 如果使用自己的证书，上传到 /etc/nginx/ssl/

# 启用站点
sudo ln -s /etc/nginx/sites-available/dataelf /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 4.2 配置SSL证书

**使用Let's Encrypt（推荐，自动配置）:**

```bash
sudo certbot --nginx -d your-domain.com -d www.your-domain.com
# 按照提示操作，Certbot会自动配置Nginx
```

**使用阿里云证书:**

```bash
# 1. 创建SSL目录
sudo mkdir -p /etc/nginx/ssl
sudo chmod 700 /etc/nginx/ssl

# 2. 上传证书文件（从阿里云下载的证书）
# 上传 your-domain.com.pem 和 your-domain.com.key 到服务器
scp your-domain.com.pem user@server:/tmp/
scp your-domain.com.key user@server:/tmp/

# 3. 在服务器上移动文件并设置权限
sudo mv /tmp/your-domain.com.pem /etc/nginx/ssl/
sudo mv /tmp/your-domain.com.key /etc/nginx/ssl/
sudo chmod 644 /etc/nginx/ssl/your-domain.com.pem
sudo chmod 600 /etc/nginx/ssl/your-domain.com.key
sudo chown root:root /etc/nginx/ssl/*

# 4. 修改Nginx配置中的证书路径
sudo nano /etc/nginx/sites-available/dataelf
# 修改以下两行：
# ssl_certificate /etc/nginx/ssl/your-domain.com.pem;
# ssl_certificate_key /etc/nginx/ssl/your-domain.com.key;

# 5. 测试并重载配置
sudo nginx -t
sudo systemctl reload nginx
```

**使用自己的证书:**

```bash
# 上传证书文件
sudo cp your-domain.crt /etc/nginx/ssl/
sudo cp your-domain.key /etc/nginx/ssl/
sudo chmod 600 /etc/nginx/ssl/your-domain.key
sudo chmod 644 /etc/nginx/ssl/your-domain.crt

# 测试配置
sudo nginx -t
sudo systemctl reload nginx
```

---

## 五、部署脚本

### 5.1 后端部署脚本

```bash
cat > /opt/dataelf/scripts/deploy-backend.sh << 'EOF'
#!/bin/bash
set -e

BACKEND_DIR="/opt/dataelf/backend"
SERVICE_NAME="dataelf-backend"

echo "开始部署后端..."

# 备份旧版本
if [ -f "$BACKEND_DIR/app.jar" ]; then
    echo "备份旧版本..."
    cp "$BACKEND_DIR/app.jar" "$BACKEND_DIR/app.jar.backup.$(date +%Y%m%d_%H%M%S)"
fi

# 停止服务
echo "停止服务..."
sudo systemctl stop $SERVICE_NAME

# 更新jar文件（需要手动上传）
# scp app.jar user@server:$BACKEND_DIR/app.jar

# 启动服务
echo "启动服务..."
sudo systemctl start $SERVICE_NAME

# 等待服务启动
sleep 10

# 检查服务状态
if sudo systemctl is-active --quiet $SERVICE_NAME; then
    echo "后端部署成功！"
    sudo systemctl status $SERVICE_NAME
else
    echo "后端部署失败，查看日志："
    tail -50 $BACKEND_DIR/logs/application.log
    exit 1
fi
EOF

chmod +x /opt/dataelf/scripts/deploy-backend.sh
```

### 5.2 前端部署脚本

```bash
cat > /opt/dataelf/scripts/deploy-frontend.sh << 'EOF'
#!/bin/bash
set -e

FRONTEND_DIR="/opt/dataelf/frontend/dist"
BACKUP_DIR="/opt/dataelf/frontend/backup"

echo "开始部署前端..."

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份旧版本
if [ -d "$FRONTEND_DIR" ] && [ "$(ls -A $FRONTEND_DIR)" ]; then
    echo "备份旧版本..."
    BACKUP_NAME="backup_$(date +%Y%m%d_%H%M%S)"
    cp -r $FRONTEND_DIR $BACKUP_DIR/$BACKUP_NAME
fi

# 更新文件（需要手动上传）
# scp -r dist/* user@server:$FRONTEND_DIR/

# 设置权限
sudo chown -R www-data:www-data $FRONTEND_DIR

# 重新加载Nginx
echo "重新加载Nginx..."
sudo nginx -t && sudo systemctl reload nginx

echo "前端部署成功！"
EOF

chmod +x /opt/dataelf/scripts/deploy-frontend.sh
```

---

## 六、防火墙配置

```bash
# Ubuntu/Debian
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable

# CentOS/RHEL
sudo firewall-cmd --permanent --add-service=ssh
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

---

## 七、监控和维护

### 7.1 日志查看

```bash
# 后端日志
tail -f /opt/dataelf/backend/logs/application.log

# Nginx访问日志
sudo tail -f /var/log/nginx/access.log

# Nginx错误日志
sudo tail -f /var/log/nginx/error.log

# 系统服务日志
sudo journalctl -u dataelf-backend -f
```

### 7.2 服务管理

```bash
# 后端服务
sudo systemctl status dataelf-backend
sudo systemctl restart dataelf-backend
sudo systemctl stop dataelf-backend
sudo systemctl start dataelf-backend

# Nginx服务
sudo systemctl status nginx
sudo systemctl restart nginx
```

### 7.3 健康检查

```bash
# 检查后端健康状态
curl https://your-domain.com/api/actuator/health

# 检查前端
curl -I https://your-domain.com
```

---

## 八、常见问题

### 8.1 后端无法连接数据库

检查：
- 数据库地址和端口是否正确
- 防火墙是否允许访问数据库端口
- 数据库用户权限是否正确

### 8.2 后端无法连接Redis

检查：
- Redis地址和端口是否正确
- Redis密码是否正确
- Redis是否允许远程连接

### 8.3 HTTPS证书问题

检查：
- 证书文件路径是否正确
- 证书文件权限是否正确（key文件应该是600）
- 证书是否过期
- 域名是否匹配

### 8.4 前端API请求失败

检查：
- Nginx配置中的proxy_pass地址是否正确
- 后端服务是否正常运行
- CORS配置是否正确

---

## 九、快速部署检查清单

- [ ] 服务器已安装Java 17和Nginx
- [ ] 数据库和Redis已配置并可访问
- [ ] SSL证书已准备（Let's Encrypt或自有证书）
- [ ] 后端jar文件已上传
- [ ] 后端配置文件已创建并配置正确
- [ ] 环境变量文件已创建并配置正确
- [ ] Systemd服务已创建并启动
- [ ] 前端文件已上传
- [ ] Nginx配置已创建并配置SSL
- [ ] 防火墙已配置
- [ ] 服务健康检查通过

---

## 十、更新部署流程

### 更新后端：

```bash
# 1. 构建新版本
cd backend
mvn clean package -DskipTests

# 2. 上传到服务器
scp target/ai-data-platform-1.0.0.jar user@server:/tmp/

# 3. 在服务器上执行
ssh user@server
sudo /opt/dataelf/scripts/deploy-backend.sh
# 或者手动：
sudo systemctl stop dataelf-backend
sudo cp /tmp/ai-data-platform-1.0.0.jar /opt/dataelf/backend/app.jar
sudo systemctl start dataelf-backend
```

### 更新前端：

```bash
# 1. 构建新版本
cd frontend
npm run build

# 2. 上传到服务器
scp -r dist/* user@server:/tmp/dist/

# 3. 在服务器上执行
ssh user@server
sudo cp -r /tmp/dist/* /opt/dataelf/frontend/dist/
sudo chown -R www-data:www-data /opt/dataelf/frontend/dist
sudo systemctl reload nginx
```

---

## 十一、安全建议

1. **定期更新系统和软件**
2. **使用强密码和JWT密钥**
3. **定期备份数据库**
4. **监控日志文件**
5. **限制SSH访问**
6. **使用fail2ban防止暴力破解**
7. **定期更新SSL证书**
8. **配置日志轮转**

---

## 十二、性能优化

1. **启用Nginx缓存**
2. **配置CDN加速静态资源**
3. **优化数据库查询**
4. **配置Redis缓存策略**
5. **调整JVM参数**
6. **启用HTTP/2**

---

部署完成后，访问 `https://your-domain.com` 即可使用应用。
