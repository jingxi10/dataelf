# 数流精灵 - 完整部署步骤

## 第一步：本地准备

### 1.1 确保代码已提交到 Git

```bash
# 在本地项目目录执行
git add .
git commit -m "准备部署"
git push origin main
```

---

## 第二步：服务器环境准备

### 2.1 登录服务器

```bash
ssh root@你的服务器IP
# 或
ssh -i 你的密钥.pem root@你的服务器IP
```

### 2.2 安装 Docker

```bash
# 一键安装 Docker
curl -fsSL https://get.docker.com | sh

# 启动 Docker
systemctl start docker
systemctl enable docker

# 验证安装
docker --version
```

### 2.3 安装 Docker Compose

```bash
# 下载 Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# 添加执行权限
chmod +x /usr/local/bin/docker-compose

# 验证安装
docker-compose --version
```

### 2.4 安装 Git

```bash
# Ubuntu/Debian
apt update && apt install -y git

# CentOS
yum install -y git
```

---

## 第三步：上传项目到服务器

### 方式一：通过 Git 克隆（推荐）

```bash
# 创建项目目录
mkdir -p /opt/dataelf
cd /opt/dataelf

# 克隆代码
git clone https://github.com/jingxi10/dataelf.git .
```

### 方式二：通过 SCP 上传

在**本地电脑**执行：

```bash
# 打包项目（排除不需要的文件）
cd 你的项目目录
tar --exclude='node_modules' --exclude='target' --exclude='.git' --exclude='logs' -czvf dataelf.tar.gz .

# 上传到服务器
scp dataelf.tar.gz root@你的服务器IP:/opt/

# 或使用密钥
scp -i 你的密钥.pem dataelf.tar.gz root@你的服务器IP:/opt/
```

在**服务器**执行：

```bash
# 创建目录并解压
mkdir -p /opt/dataelf
cd /opt/dataelf
tar -xzvf /opt/dataelf.tar.gz
rm /opt/dataelf.tar.gz
```

### 方式三：通过 SFTP 工具上传

使用 FileZilla、WinSCP 等工具：
1. 连接服务器
2. 上传整个项目到 `/opt/dataelf/`
3. 排除 `node_modules`、`target`、`.git` 目录

---

## 第四步：配置项目

### 4.1 创建环境变量文件

```bash
cd /opt/dataelf

# 创建 .env 文件
cat > .env << 'EOF'
# 数据库配置
DB_ROOT_PASSWORD=RootPass123!
DB_USERNAME=dataelf
DB_PASSWORD=DataElf123!

# JWT 密钥（请修改为随机字符串）
JWT_SECRET=your-super-secret-jwt-key-change-this-must-be-at-least-256-bits

# 应用地址（改成你的域名或IP）
APP_BASE_URL=http://你的服务器IP
CORS_ORIGINS=http://你的服务器IP
EOF
```

### 4.2 配置前端 API 地址

```bash
# 创建前端生产环境配置
cat > frontend/.env.production << 'EOF'
VITE_API_BASE_URL=http://你的服务器IP
VITE_APP_TITLE=数流精灵
EOF
```

**注意**：将 `你的服务器IP` 替换为实际的服务器公网 IP 或域名。

---

## 第五步：启动服务

### 5.1 构建并启动

```bash
cd /opt/dataelf

# 构建并启动所有服务（首次需要较长时间）
docker-compose up -d --build
```

### 5.2 查看启动状态

```bash
# 查看容器状态
docker-compose ps

# 查看启动日志
docker-compose logs -f

# 单独查看后端日志
docker-compose logs -f backend
```

### 5.3 等待服务就绪

首次启动需要等待：
- MySQL 初始化：约 30 秒
- 后端构建：约 2-5 分钟
- 前端构建：约 1-2 分钟

看到以下日志说明启动成功：
```
backend    | Started AiDataPlatformApplication in X seconds
frontend   | nginx: started
```

---

## 第六步：配置防火墙

```bash
# Ubuntu (ufw)
ufw allow 22    # SSH
ufw allow 80    # HTTP
ufw allow 443   # HTTPS
ufw enable

# CentOS (firewalld)
firewall-cmd --permanent --add-port=22/tcp
firewall-cmd --permanent --add-port=80/tcp
firewall-cmd --permanent --add-port=443/tcp
firewall-cmd --reload

# 云服务器还需要在控制台安全组开放 80、443 端口
```

---

## 第七步：验证部署

### 7.1 检查服务

```bash
# 检查后端健康状态
curl http://localhost:8080/actuator/health

# 检查前端
curl http://localhost
```

### 7.2 浏览器访问

打开浏览器访问：`http://你的服务器IP`

### 7.3 登录系统

默认管理员账号：
- 邮箱：`admin@dataelf.com`
- 密码：`admin123`

---

## 第八步：配置域名（可选）

### 8.1 DNS 解析

在域名服务商后台添加 A 记录：
- 主机记录：`@` 和 `www`
- 记录值：你的服务器 IP

### 8.2 修改配置

```bash
cd /opt/dataelf

# 修改 .env
sed -i 's|http://你的服务器IP|https://你的域名|g' .env

# 修改前端配置
echo "VITE_API_BASE_URL=https://你的域名" > frontend/.env.production

# 重新构建
docker-compose up -d --build
```

### 8.3 配置 HTTPS

安装 Nginx 和 Certbot：

```bash
apt install -y nginx certbot python3-certbot-nginx
```

创建 Nginx 配置：

```bash
cat > /etc/nginx/sites-available/dataelf << 'EOF'
server {
    listen 80;
    server_name 你的域名 www.你的域名;

    location / {
        proxy_pass http://127.0.0.1:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF

ln -s /etc/nginx/sites-available/dataelf /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default
nginx -t && systemctl reload nginx
```

申请 SSL 证书：

```bash
certbot --nginx -d 你的域名 -d www.你的域名
```

---

## 常用运维命令

```bash
cd /opt/dataelf

# 查看状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 重启服务
docker-compose restart

# 停止服务
docker-compose down

# 更新代码后重新部署
git pull
docker-compose up -d --build

# 进入后端容器
docker exec -it dataelf-backend sh

# 进入数据库
docker exec -it dataelf-mysql mysql -u dataelf -p
```

---

## 故障排查

### 问题：容器启动失败

```bash
# 查看详细日志
docker-compose logs backend
docker-compose logs mysql
```

### 问题：数据库连接失败

```bash
# 等待 MySQL 完全启动
docker-compose up -d mysql redis
sleep 60
docker-compose up -d backend frontend
```

### 问题：端口被占用

```bash
# 查看端口占用
netstat -tlnp | grep 80
netstat -tlnp | grep 8080

# 停止占用进程或修改 docker-compose.yml 端口
```

### 问题：内存不足

```bash
# 查看内存
free -h

# 创建 swap（如果内存小于 4G）
fallocate -l 2G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab
```

---

## 完整命令汇总（复制粘贴版）

```bash
# === 服务器执行 ===

# 1. 安装 Docker
curl -fsSL https://get.docker.com | sh
systemctl start docker && systemctl enable docker

# 2. 安装 Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# 3. 安装 Git
apt update && apt install -y git

# 4. 克隆项目
mkdir -p /opt/dataelf && cd /opt/dataelf
git clone https://github.com/jingxi10/dataelf.git .

# 5. 创建配置（记得修改 IP/域名 和密码）
cat > .env << 'EOF'
DB_ROOT_PASSWORD=RootPass123!
DB_USERNAME=dataelf
DB_PASSWORD=DataElf123!
JWT_SECRET=change-this-to-random-string-at-least-32-chars
APP_BASE_URL=http://你的服务器IP
CORS_ORIGINS=http://你的服务器IP
EOF

cat > frontend/.env.production << 'EOF'
VITE_API_BASE_URL=http://你的服务器IP
VITE_APP_TITLE=数流精灵
EOF

# 6. 启动服务
docker-compose up -d --build

# 7. 开放防火墙
ufw allow 80 && ufw allow 443 && ufw enable

# 8. 查看状态
docker-compose ps
docker-compose logs -f
```
