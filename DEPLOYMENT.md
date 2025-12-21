# 数流精灵 - 部署文档

## 目录

- [本地开发环境](#本地开发环境)
- [生产环境部署](#生产环境部署)
- [阿里云部署指南](#阿里云部署指南)
- [Docker部署](#docker部署)
- [监控和维护](#监控和维护)

---

## 本地开发环境

### 前置要求

- **Java**: JDK 17+
- **Node.js**: 18+
- **MySQL**: 5.7+
- **Redis**: 6+
- **RabbitMQ**: 3.9+
- **Maven**: 3.8+

### 1. 数据库配置

```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE ai_data_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 创建用户（可选）
CREATE USER 'dataelf'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON ai_data_platform.* TO 'dataelf'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Redis配置

```bash
# 启动Redis
redis-server

# 验证Redis运行
redis-cli ping
# 应该返回: PONG
```

### 3. RabbitMQ配置

```bash
# 启动RabbitMQ
rabbitmq-server

# 启用管理插件
rabbitmq-plugins enable rabbitmq_management

# 访问管理界面: http://localhost:15672
# 默认用户名/密码: guest/guest
```

### 4. 后端配置

```bash
cd backend

# 复制配置文件
cp src/main/resources/application.yml src/main/resources/application-dev.yml

# 编辑配置文件
vim src/main/resources/application-dev.yml
```

**application-dev.yml 配置示例:**

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_data_platform?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: dataelf
    password: your_password
    
  redis:
    host: localhost
    port: 6379
    password: # 如果有密码则填写
    
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    
  mail:
    host: smtp.example.com
    port: 587
    username: your-email@example.com
    password: your-email-password
    
server:
  port: 8080

jwt:
  secret: your-secret-key-change-this-in-production
  expiration: 3600000
```

### 5. 启动后端

```bash
cd backend

# 方式1: 使用Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 方式2: 打包后运行
mvn clean package -DskipTests
java -jar target/ai-data-platform-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

后端将在 http://localhost:8080 启动

API文档: http://localhost:8080/swagger-ui.html

### 6. 前端配置

```bash
cd frontend

# 安装依赖
npm install

# 配置环境变量
cp .env .env.local
vim .env.local
```

**.env.local 配置:**

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_TITLE=数流精灵
```

### 7. 启动前端

```bash
cd frontend

# 开发模式
npm run dev

# 前端将在 http://localhost:5173 启动
```

### 8. 验证安装

访问 http://localhost:5173，你应该能看到登录页面。

---

## 生产环境部署

### 1. 后端打包

```bash
cd backend

# 打包（跳过测试以加快速度）
mvn clean package -DskipTests

# 生成的JAR文件位于: target/ai-data-platform-0.0.1-SNAPSHOT.jar
```

### 2. 前端打包

```bash
cd frontend

# 设置生产环境变量
cat > .env.production << EOF
VITE_API_BASE_URL=https://api.yourdomain.com
VITE_APP_TITLE=数流精灵
EOF

# 打包
npm run build

# 生成的文件位于: dist/
```

### 3. 生产环境配置

**application-prod.yml:**

```yaml
spring:
  datasource:
    url: jdbc:mysql://your-db-host:3306/ai_data_platform?useSSL=true&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      
  redis:
    host: ${REDIS_HOST}
    port: 6379
    password: ${REDIS_PASSWORD}
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
    
  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: 5672
    username: ${RABBITMQ_USERNAME}
    password: ${RABBITMQ_PASSWORD}
    
server:
  port: 8080
  compression:
    enabled: true
    
logging:
  level:
    root: INFO
    com.dataelf.platform: INFO
```

### 4. 启动生产环境

```bash
# 使用环境变量启动
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_db_password
export REDIS_HOST=your_redis_host
export REDIS_PASSWORD=your_redis_password
export RABBITMQ_HOST=your_rabbitmq_host
export RABBITMQ_USERNAME=your_rabbitmq_user
export RABBITMQ_PASSWORD=your_rabbitmq_password

java -jar -Xms512m -Xmx2g \
  -Dspring.profiles.active=prod \
  target/ai-data-platform-0.0.1-SNAPSHOT.jar
```

---

## 阿里云部署指南

### 架构概览

```
┌─────────────────────────────────────────────────────┐
│                    阿里云 VPC                        │
│                                                      │
│  ┌──────────────┐         ┌──────────────┐         │
│  │   SLB负载均衡 │────────▶│   ECS实例    │         │
│  │  (公网访问)   │         │  (应用服务器) │         │
│  └──────────────┘         └──────────────┘         │
│         │                        │                  │
│         │                        ▼                  │
│         │                 ┌──────────────┐         │
│         │                 │  RDS MySQL   │         │
│         │                 └──────────────┘         │
│         │                        │                  │
│         │                        ▼                  │
│         │                 ┌──────────────┐         │
│         └────────────────▶│ Redis (ApsaraDB) │     │
│                           └──────────────┘         │
│                                  │                  │
│                                  ▼                  │
│                           ┌──────────────┐         │
│                           │   RabbitMQ   │         │
│                           │  (消息队列MQ) │         │
│                           └──────────────┘         │
│                                                      │
│  ┌──────────────┐         ┌──────────────┐         │
│  │   OSS存储    │         │   CDN加速    │         │
│  │  (静态资源)   │◀────────│  (前端资源)   │         │
│  └──────────────┘         └──────────────┘         │
└─────────────────────────────────────────────────────┘
```

### 步骤1: 购买阿里云资源

#### 1.1 ECS云服务器

```
推荐配置:
- 实例规格: ecs.c6.xlarge (4核8GB)
- 操作系统: CentOS 7.9 或 Ubuntu 20.04
- 系统盘: 40GB SSD
- 数据盘: 100GB SSD
- 带宽: 5Mbps (按需调整)
- 地域: 根据用户分布选择
```

#### 1.2 RDS MySQL数据库

```
推荐配置:
- 版本: MySQL 5.7
- 规格: mysql.n2.medium.1 (2核4GB)
- 存储: 100GB SSD
- 备份: 自动备份，保留7天
```

#### 1.3 Redis实例

```
推荐配置:
- 版本: Redis 6.0
- 规格: redis.master.small.default (1GB)
- 架构: 标准版-双副本
```

#### 1.4 消息队列RabbitMQ

```
推荐配置:
- 版本: RabbitMQ 3.8
- 规格: 专业版
- 存储: 100GB
```

#### 1.5 SLB负载均衡

```
配置:
- 类型: 应用型负载均衡(ALB)
- 规格: 简约型I
- 监听: HTTPS 443 + HTTP 80
```

#### 1.6 OSS对象存储

```
配置:
- 存储类型: 标准存储
- 读写权限: 私有
- 用途: 存储用户上传文件、日志等
```

### 步骤2: 配置ECS服务器

#### 2.1 连接到ECS

```bash
ssh root@your-ecs-ip
```

#### 2.2 安装Java

```bash
# 安装OpenJDK 17
yum install -y java-17-openjdk java-17-openjdk-devel

# 验证安装
java -version
```

#### 2.3 安装Node.js

```bash
# 安装Node.js 18
curl -fsSL https://rpm.nodesource.com/setup_18.x | bash -
yum install -y nodejs

# 验证安装
node -v
npm -v
```

#### 2.4 安装Nginx

```bash
# 安装Nginx
yum install -y nginx

# 启动Nginx
systemctl start nginx
systemctl enable nginx
```

### 步骤3: 配置RDS MySQL

#### 3.1 设置白名单

在RDS控制台添加ECS内网IP到白名单

#### 3.2 创建数据库

```bash
# 使用DMS或MySQL客户端连接
mysql -h your-rds-endpoint -u root -p

# 创建数据库
CREATE DATABASE ai_data_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 创建应用用户
CREATE USER 'dataelf'@'%' IDENTIFIED BY 'strong_password_here';
GRANT ALL PRIVILEGES ON ai_data_platform.* TO 'dataelf'@'%';
FLUSH PRIVILEGES;
```

### 步骤4: 配置Redis

在Redis控制台获取连接地址和密码，记录下来用于应用配置。

### 步骤5: 部署后端应用

#### 5.1 创建应用目录

```bash
mkdir -p /opt/ai-data-platform
cd /opt/ai-data-platform
```

#### 5.2 上传JAR文件

```bash
# 在本地打包
cd backend
mvn clean package -DskipTests

# 上传到服务器
scp target/ai-data-platform-0.0.1-SNAPSHOT.jar root@your-ecs-ip:/opt/ai-data-platform/
```

#### 5.3 创建配置文件

```bash
cat > /opt/ai-data-platform/application-prod.yml << 'EOF'
spring:
  datasource:
    url: jdbc:mysql://your-rds-endpoint:3306/ai_data_platform?useSSL=true&serverTimezone=Asia/Shanghai
    username: dataelf
    password: your_rds_password
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      
  redis:
    host: your-redis-endpoint
    port: 6379
    password: your_redis_password
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
    
  rabbitmq:
    host: your-rabbitmq-endpoint
    port: 5672
    username: your_rabbitmq_username
    password: your_rabbitmq_password
    virtual-host: /
    
  mail:
    host: smtp.aliyun.com
    port: 465
    username: your-email@aliyun.com
    password: your-email-password
    properties:
      mail:
        smtp:
          auth: true
          ssl:
            enable: true
    
server:
  port: 8080
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain
    
logging:
  level:
    root: INFO
    com.dataelf.platform: INFO
  file:
    name: /opt/ai-data-platform/logs/application.log
    max-size: 100MB
    max-history: 30
    
jwt:
  secret: your-production-secret-key-change-this
  expiration: 3600000

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
EOF
```

#### 5.4 创建systemd服务

```bash
cat > /etc/systemd/system/ai-data-platform.service << 'EOF'
[Unit]
Description=AI Data Platform Backend
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/ai-data-platform
ExecStart=/usr/bin/java -Xms512m -Xmx2g \
  -Dspring.profiles.active=prod \
  -Dspring.config.location=/opt/ai-data-platform/application-prod.yml \
  -jar /opt/ai-data-platform/ai-data-platform-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

# 重新加载systemd
systemctl daemon-reload

# 启动服务
systemctl start ai-data-platform
systemctl enable ai-data-platform

# 查看状态
systemctl status ai-data-platform

# 查看日志
journalctl -u ai-data-platform -f
```

### 步骤6: 部署前端应用

#### 6.1 构建前端

```bash
# 在本地
cd frontend

# 配置生产环境
cat > .env.production << EOF
VITE_API_BASE_URL=https://api.yourdomain.com
VITE_APP_TITLE=数流精灵
EOF

# 构建
npm run build
```

#### 6.2 上传到OSS

```bash
# 安装ossutil
wget http://gosspublic.alicdn.com/ossutil/1.7.15/ossutil64
chmod 755 ossutil64

# 配置ossutil
./ossutil64 config

# 上传前端文件到OSS
./ossutil64 cp -r dist/ oss://your-bucket-name/frontend/ --update
```

#### 6.3 配置Nginx

```bash
cat > /etc/nginx/conf.d/ai-data-platform.conf << 'EOF'
# 前端服务
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;
    
    # 重定向到HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com www.yourdomain.com;
    
    # SSL证书配置
    ssl_certificate /etc/nginx/ssl/yourdomain.com.crt;
    ssl_certificate_key /etc/nginx/ssl/yourdomain.com.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    
    root /var/www/ai-data-platform/frontend;
    index index.html;
    
    # Gzip压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
    
    # 前端路由
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}

# API服务
server {
    listen 80;
    server_name api.yourdomain.com;
    
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.yourdomain.com;
    
    ssl_certificate /etc/nginx/ssl/api.yourdomain.com.crt;
    ssl_certificate_key /etc/nginx/ssl/api.yourdomain.com.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    
    # 反向代理到后端
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket支持
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
}
EOF

# 测试配置
nginx -t

# 重启Nginx
systemctl restart nginx
```

### 步骤7: 配置SSL证书

#### 7.1 申请免费SSL证书

在阿里云SSL证书服务申请免费DV证书

#### 7.2 下载并安装证书

```bash
# 创建证书目录
mkdir -p /etc/nginx/ssl

# 上传证书文件
# yourdomain.com.crt (证书文件)
# yourdomain.com.key (私钥文件)
```

### 步骤8: 配置CDN加速

#### 8.1 添加CDN域名

在阿里云CDN控制台添加加速域名

#### 8.2 配置回源

- 回源地址: OSS bucket域名
- 回源协议: HTTPS
- 回源Host: OSS bucket域名

#### 8.3 配置缓存规则

```
静态资源 (js, css, images): 缓存30天
HTML文件: 缓存1小时
API请求: 不缓存
```

### 步骤9: 配置安全组

在ECS安全组中添加以下规则:

```
入方向:
- 80/TCP (HTTP) - 0.0.0.0/0
- 443/TCP (HTTPS) - 0.0.0.0/0
- 22/TCP (SSH) - 你的IP地址

出方向:
- 全部允许
```

### 步骤10: 配置域名解析

在阿里云DNS控制台添加解析记录:

```
记录类型: A
主机记录: @
记录值: SLB公网IP
TTL: 10分钟

记录类型: A
主机记录: www
记录值: SLB公网IP
TTL: 10分钟

记录类型: A
主机记录: api
记录值: SLB公网IP
TTL: 10分钟
```

---

## Docker部署

### 1. 创建Dockerfile

**backend/Dockerfile:**

```dockerfile
FROM openjdk:17-slim

WORKDIR /app

COPY target/ai-data-platform-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx2g"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

**frontend/Dockerfile:**

```dockerfile
FROM node:18-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

### 2. 创建docker-compose.yml

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:5.7
    container_name: ai-platform-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: ai_data_platform
      MYSQL_USER: dataelf
      MYSQL_PASSWORD: dataelf_password
    volumes:
      - mysql-data:/var/lib/mysql
      - ./backend/src/main/resources/schema.sql:/docker-entrypoint-initdb.d/schema.sql
    ports:
      - "3306:3306"
    networks:
      - ai-platform-network

  redis:
    image: redis:6-alpine
    container_name: ai-platform-redis
    command: redis-server --requirepass redis_password
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - ai-platform-network

  rabbitmq:
    image: rabbitmq:3.9-management-alpine
    container_name: ai-platform-rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin_password
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq-data:/var/lib/rabbitmq
    networks:
      - ai-platform-network

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: ai-platform-backend
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/ai_data_platform?useSSL=false&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: dataelf
      SPRING_DATASOURCE_PASSWORD: dataelf_password
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PASSWORD: redis_password
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_RABBITMQ_USERNAME: admin
      SPRING_RABBITMQ_PASSWORD: admin_password
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
      - rabbitmq
    networks:
      - ai-platform-network
    restart: unless-stopped

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: ai-platform-frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - ai-platform-network
    restart: unless-stopped

volumes:
  mysql-data:
  redis-data:
  rabbitmq-data:

networks:
  ai-platform-network:
    driver: bridge
```

### 3. 启动Docker容器

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 停止并删除数据
docker-compose down -v
```

---

## 监控和维护

### 1. 日志管理

```bash
# 查看应用日志
tail -f /opt/ai-data-platform/logs/application.log

# 查看Nginx访问日志
tail -f /var/log/nginx/access.log

# 查看Nginx错误日志
tail -f /var/log/nginx/error.log

# 查看systemd日志
journalctl -u ai-data-platform -f
```

### 2. 性能监控

访问 Prometheus metrics: `https://api.yourdomain.com/actuator/prometheus`

### 3. 健康检查

```bash
# 检查后端健康状态
curl https://api.yourdomain.com/actuator/health

# 检查前端
curl https://yourdomain.com

# 检查数据库连接
mysql -h your-rds-endpoint -u dataelf -p -e "SELECT 1"

# 检查Redis连接
redis-cli -h your-redis-endpoint -a your_redis_password ping
```

### 4. 备份策略

#### 数据库备份

```bash
# 手动备份
mysqldump -h your-rds-endpoint -u dataelf -p ai_data_platform > backup_$(date +%Y%m%d).sql

# 自动备份脚本
cat > /opt/backup/backup-db.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/opt/backup/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR

mysqldump -h your-rds-endpoint -u dataelf -pYOUR_PASSWORD ai_data_platform | gzip > $BACKUP_DIR/backup_$DATE.sql.gz

# 保留最近30天的备份
find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +30 -delete
EOF

chmod +x /opt/backup/backup-db.sh

# 添加到crontab (每天凌晨2点执行)
echo "0 2 * * * /opt/backup/backup-db.sh" | crontab -
```

### 5. 更新部署

```bash
# 停止服务
systemctl stop ai-data-platform

# 备份当前版本
cp /opt/ai-data-platform/ai-data-platform-0.0.1-SNAPSHOT.jar /opt/ai-data-platform/backup/

# 上传新版本
scp target/ai-data-platform-0.0.1-SNAPSHOT.jar root@your-ecs-ip:/opt/ai-data-platform/

# 启动服务
systemctl start ai-data-platform

# 查看日志确认启动成功
journalctl -u ai-data-platform -f
```

### 6. 故障排查

#### 后端无法启动

```bash
# 检查日志
journalctl -u ai-data-platform -n 100

# 检查端口占用
netstat -tlnp | grep 8080

# 检查数据库连接
telnet your-rds-endpoint 3306

# 检查Redis连接
telnet your-redis-endpoint 6379
```

#### 前端无法访问

```bash
# 检查Nginx状态
systemctl status nginx

# 检查Nginx配置
nginx -t

# 检查Nginx日志
tail -f /var/log/nginx/error.log
```

---

## 安全建议

1. **定期更新系统和软件包**
   ```bash
   yum update -y
   ```

2. **配置防火墙**
   ```bash
   firewall-cmd --permanent --add-service=http
   firewall-cmd --permanent --add-service=https
   firewall-cmd --reload
   ```

3. **启用fail2ban防止暴力破解**
   ```bash
   yum install -y fail2ban
   systemctl start fail2ban
   systemctl enable fail2ban
   ```

4. **定期检查安全漏洞**
   - 使用阿里云安全中心
   - 定期更新依赖包

5. **数据加密**
   - 使用HTTPS
   - 数据库连接使用SSL
   - 敏感数据加密存储

---

## 性能优化建议

1. **数据库优化**
   - 添加适当的索引
   - 定期分析和优化查询
   - 配置连接池

2. **缓存策略**
   - 使用Redis缓存热点数据
   - 配置浏览器缓存
   - 使用CDN加速静态资源

3. **应用优化**
   - 启用Gzip压缩
   - 优化图片大小
   - 使用异步处理

4. **监控告警**
   - 配置阿里云云监控
   - 设置关键指标告警
   - 定期查看性能报告

---

## 联系支持

如有问题，请联系技术支持团队。
