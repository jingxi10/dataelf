# Docker 一键部署指南

## 前置要求

服务器安装 Docker 和 Docker Compose：

```bash
# 安装 Docker (Ubuntu)
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 重新登录使 docker 组生效
exit
```

## 快速部署

### 1. 克隆代码

```bash
git clone https://github.com/jingxi10/dataelf.git
cd dataelf
```

### 2. 配置环境变量

```bash
cp .env.example .env
vim .env
```

修改以下配置：
```bash
DB_PASSWORD=你的数据库密码
JWT_SECRET=随机字符串至少32位
APP_BASE_URL=https://你的域名
CORS_ORIGINS=https://你的域名
```

### 3. 修改前端 API 地址

```bash
# 创建前端生产环境配置
cat > frontend/.env.production << EOF
VITE_API_BASE_URL=https://你的域名
VITE_APP_TITLE=数流精灵
EOF
```

### 4. 启动服务

```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看日志
docker-compose logs -f

# 查看服务状态
docker-compose ps
```

### 5. 验证部署

```bash
# 检查后端健康状态
curl http://localhost:8080/actuator/health

# 访问前端
curl http://localhost
```

浏览器访问 `http://服务器IP` 即可看到网站。

---

## 配置域名和 HTTPS

### 方案一：使用 Nginx 反向代理 (推荐)

在宿主机安装 Nginx：

```bash
sudo apt install -y nginx certbot python3-certbot-nginx
```

创建 Nginx 配置：

```bash
sudo vim /etc/nginx/sites-available/dataelf
```

```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;

    location / {
        proxy_pass http://127.0.0.1:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

启用配置：

```bash
sudo ln -s /etc/nginx/sites-available/dataelf /etc/nginx/sites-enabled/
sudo rm /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl reload nginx
```

配置 HTTPS：

```bash
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com
```

### 方案二：修改 docker-compose 端口

如果不用宿主机 Nginx，修改 `docker-compose.yml` 中前端端口：

```yaml
frontend:
  ports:
    - "80:80"
    - "443:443"
```

---

## 常用命令

```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 重启服务
docker-compose restart

# 查看日志
docker-compose logs -f backend
docker-compose logs -f frontend

# 重新构建并启动
docker-compose up -d --build

# 进入容器
docker exec -it dataelf-backend sh
docker exec -it dataelf-mysql mysql -u dataelf -p

# 查看资源占用
docker stats
```

---

## 数据备份

```bash
# 备份 MySQL
docker exec dataelf-mysql mysqldump -u dataelf -p ai_data_platform > backup.sql

# 恢复 MySQL
docker exec -i dataelf-mysql mysql -u dataelf -p ai_data_platform < backup.sql

# 备份所有数据卷
docker run --rm -v dataelf_mysql-data:/data -v $(pwd):/backup alpine tar czf /backup/mysql-backup.tar.gz /data
```

---

## 更新部署

```bash
# 拉取最新代码
git pull

# 重新构建并启动
docker-compose up -d --build
```

---

## 故障排查

### 后端启动失败

```bash
# 查看后端日志
docker-compose logs backend

# 检查 MySQL 是否就绪
docker-compose logs mysql
```

### 数据库连接失败

```bash
# 等待 MySQL 完全启动后再启动后端
docker-compose up -d mysql redis
sleep 30
docker-compose up -d backend frontend
```

### 清理重建

```bash
# 停止并删除所有容器和数据
docker-compose down -v

# 重新构建
docker-compose up -d --build
```

---

## 默认账号

- 邮箱：`admin@dataelf.com`
- 密码：`admin123`

首次登录后请修改密码！
