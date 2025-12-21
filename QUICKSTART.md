# 数流精灵 - 快速启动指南

## 5分钟快速启动

### 前置条件

确保你已安装:
- Docker & Docker Compose
- Git

### 步骤1: 克隆项目

```bash
git clone <your-repo-url>
cd ai-data-platform
```

### 步骤2: 启动所有服务

```bash
docker-compose up -d
```

这将启动:
- MySQL 数据库 (端口 3306)
- Redis 缓存 (端口 6379)
- RabbitMQ 消息队列 (端口 5672, 管理界面 15672)
- 后端服务 (端口 8080)
- 前端服务 (端口 80)

### 步骤3: 等待服务启动

```bash
# 查看启动日志
docker-compose logs -f backend

# 等待看到 "Started AiDataPlatformApplication" 消息
```

### 步骤4: 访问应用

打开浏览器访问: http://localhost

默认管理员账号:
- 邮箱: admin@example.com
- 密码: admin123

### 步骤5: 查看API文档

访问 Swagger UI: http://localhost:8080/swagger-ui.html

---

## 本地开发模式

如果你想在本地开发环境运行（不使用Docker）:

### 1. 启动依赖服务

```bash
# 只启动MySQL, Redis, RabbitMQ
docker-compose up -d mysql redis rabbitmq
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

后端将在 http://localhost:8080 启动

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端将在 http://localhost:5173 启动

---

## 常用命令

### Docker Compose

```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f [service-name]

# 重启服务
docker-compose restart [service-name]

# 重新构建并启动
docker-compose up -d --build
```

### 数据库管理

```bash
# 连接到MySQL
docker exec -it ai-platform-mysql mysql -u dataelf -pdataelf_password ai_data_platform

# 导入SQL文件
docker exec -i ai-platform-mysql mysql -u dataelf -pdataelf_password ai_data_platform < backup.sql

# 导出数据库
docker exec ai-platform-mysql mysqldump -u dataelf -pdataelf_password ai_data_platform > backup.sql
```

### Redis管理

```bash
# 连接到Redis
docker exec -it ai-platform-redis redis-cli -a redis_password

# 查看所有键
docker exec -it ai-platform-redis redis-cli -a redis_password KEYS '*'

# 清空缓存
docker exec -it ai-platform-redis redis-cli -a redis_password FLUSHALL
```

### RabbitMQ管理

访问管理界面: http://localhost:15672
- 用户名: admin
- 密码: admin_password

---

## 测试

### 运行后端测试

```bash
cd backend
mvn test
```

### 运行前端测试

```bash
cd frontend
npm test
```

---

## 故障排查

### 后端无法启动

1. 检查MySQL是否已启动:
   ```bash
   docker-compose ps mysql
   ```

2. 查看后端日志:
   ```bash
   docker-compose logs backend
   ```

3. 检查端口占用:
   ```bash
   lsof -i :8080
   ```

### 前端无法访问

1. 检查前端容器状态:
   ```bash
   docker-compose ps frontend
   ```

2. 查看前端日志:
   ```bash
   docker-compose logs frontend
   ```

3. 检查端口占用:
   ```bash
   lsof -i :80
   ```

### 数据库连接失败

1. 确认MySQL容器运行:
   ```bash
   docker-compose ps mysql
   ```

2. 测试数据库连接:
   ```bash
   docker exec -it ai-platform-mysql mysql -u dataelf -pdataelf_password -e "SELECT 1"
   ```

3. 检查数据库日志:
   ```bash
   docker-compose logs mysql
   ```

---

## 清理环境

### 停止并删除所有容器

```bash
docker-compose down
```

### 删除所有数据（包括数据库）

```bash
docker-compose down -v
```

### 删除所有镜像

```bash
docker-compose down --rmi all
```

---

## 下一步

- 阅读 [完整部署文档](DEPLOYMENT.md) 了解生产环境部署
- 查看 [API文档](backend/API_DOCUMENTATION.md) 了解接口详情
- 阅读 [设计文档](.kiro/specs/ai-data-platform/design.md) 了解系统架构

---

## 获取帮助

如遇到问题:
1. 查看日志: `docker-compose logs -f`
2. 检查 [故障排查](#故障排查) 部分
3. 提交 Issue 到项目仓库
