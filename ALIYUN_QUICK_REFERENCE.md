# 阿里云部署快速参考

## 资源清单

### 必需资源

| 资源类型 | 推荐配置 | 月费用估算 |
|---------|---------|-----------|
| ECS云服务器 | ecs.c6.xlarge (4核8GB) | ¥300-500 |
| RDS MySQL | mysql.n2.medium.1 (2核4GB) | ¥200-300 |
| Redis | redis.master.small.default (1GB) | ¥100-150 |
| RabbitMQ | 专业版 | ¥200-300 |
| SLB负载均衡 | 简约型I | ¥50-100 |
| OSS对象存储 | 标准存储 100GB | ¥10-20 |
| **总计** | | **¥860-1370/月** |

### 可选资源

| 资源类型 | 用途 | 月费用估算 |
|---------|------|-----------|
| CDN | 加速静态资源 | 按流量计费 |
| SSL证书 | HTTPS加密 | 免费DV证书 |
| 云监控 | 监控告警 | 免费版 |
| 日志服务 | 日志分析 | 按量付费 |

## 快速部署步骤

### 1. 购买资源（10分钟）

```bash
# 登录阿里云控制台
https://www.aliyun.com

# 按顺序购买:
1. ECS云服务器 (选择CentOS 7.9)
2. RDS MySQL 5.7
3. Redis 6.0
4. RabbitMQ 3.8
5. SLB负载均衡
6. OSS存储桶
```

### 2. 配置ECS（15分钟）

```bash
# SSH连接到ECS
ssh root@your-ecs-ip

# 安装Java 17
yum install -y java-17-openjdk java-17-openjdk-devel

# 安装Nginx
yum install -y nginx

# 创建应用目录
mkdir -p /opt/ai-data-platform
```

### 3. 配置数据库（5分钟）

```bash
# 在RDS控制台:
1. 设置白名单 (添加ECS内网IP)
2. 创建数据库: ai_data_platform
3. 创建用户: dataelf
4. 记录连接地址
```

### 4. 部署后端（10分钟）

```bash
# 上传JAR文件
scp target/ai-data-platform-0.0.1-SNAPSHOT.jar root@your-ecs-ip:/opt/ai-data-platform/

# 创建配置文件
cat > /opt/ai-data-platform/application-prod.yml << 'EOF'
spring:
  datasource:
    url: jdbc:mysql://your-rds-endpoint:3306/ai_data_platform
    username: dataelf
    password: YOUR_PASSWORD
  redis:
    host: your-redis-endpoint
    password: YOUR_PASSWORD
  rabbitmq:
    host: your-rabbitmq-endpoint
    username: YOUR_USERNAME
    password: YOUR_PASSWORD
EOF

# 创建systemd服务
cat > /etc/systemd/system/ai-data-platform.service << 'EOF'
[Unit]
Description=AI Data Platform
After=network.target

[Service]
Type=simple
WorkingDirectory=/opt/ai-data-platform
ExecStart=/usr/bin/java -Xms512m -Xmx2g \
  -Dspring.profiles.active=prod \
  -jar /opt/ai-data-platform/ai-data-platform-0.0.1-SNAPSHOT.jar
Restart=always

[Install]
WantedBy=multi-user.target
EOF

# 启动服务
systemctl daemon-reload
systemctl start ai-data-platform
systemctl enable ai-data-platform
```

### 5. 部署前端（10分钟）

```bash
# 配置Nginx
cat > /etc/nginx/conf.d/ai-platform.conf << 'EOF'
server {
    listen 80;
    server_name yourdomain.com;
    
    location / {
        root /var/www/ai-platform;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
EOF

# 上传前端文件
scp -r dist/* root@your-ecs-ip:/var/www/ai-platform/

# 重启Nginx
nginx -t
systemctl restart nginx
```

### 6. 配置域名（5分钟）

```bash
# 在阿里云DNS控制台添加A记录:
记录类型: A
主机记录: @
记录值: your-ecs-ip
TTL: 10分钟
```

### 7. 配置SSL（10分钟）

```bash
# 在SSL证书服务申请免费证书
# 下载证书文件到 /etc/nginx/ssl/

# 更新Nginx配置支持HTTPS
# 参考 DEPLOYMENT.md 中的完整配置
```

## 常用命令速查

### 服务管理

```bash
# 查看后端状态
systemctl status ai-data-platform

# 重启后端
systemctl restart ai-data-platform

# 查看后端日志
journalctl -u ai-data-platform -f

# 重启Nginx
systemctl restart nginx

# 查看Nginx日志
tail -f /var/log/nginx/error.log
```

### 数据库操作

```bash
# 连接RDS
mysql -h your-rds-endpoint -u dataelf -p

# 备份数据库
mysqldump -h your-rds-endpoint -u dataelf -p ai_data_platform > backup.sql

# 恢复数据库
mysql -h your-rds-endpoint -u dataelf -p ai_data_platform < backup.sql
```

### Redis操作

```bash
# 连接Redis
redis-cli -h your-redis-endpoint -a your-password

# 清空缓存
redis-cli -h your-redis-endpoint -a your-password FLUSHALL

# 查看所有键
redis-cli -h your-redis-endpoint -a your-password KEYS '*'
```

## 监控检查清单

### 每日检查

- [ ] 检查应用健康状态: `curl http://localhost:8080/actuator/health`
- [ ] 检查磁盘使用: `df -h`
- [ ] 检查内存使用: `free -h`
- [ ] 查看错误日志: `tail -100 /opt/ai-data-platform/logs/application.log`

### 每周检查

- [ ] 检查数据库备份
- [ ] 检查系统更新: `yum check-update`
- [ ] 查看访问日志分析
- [ ] 检查SSL证书有效期

### 每月检查

- [ ] 审查安全组规则
- [ ] 检查资源使用趋势
- [ ] 更新依赖包
- [ ] 性能优化评估

## 故障排查速查

### 后端无法启动

```bash
# 1. 检查日志
journalctl -u ai-data-platform -n 100

# 2. 检查端口
netstat -tlnp | grep 8080

# 3. 检查数据库连接
telnet your-rds-endpoint 3306

# 4. 检查配置文件
cat /opt/ai-data-platform/application-prod.yml
```

### 前端无法访问

```bash
# 1. 检查Nginx状态
systemctl status nginx

# 2. 检查Nginx配置
nginx -t

# 3. 检查文件权限
ls -la /var/www/ai-platform/

# 4. 查看错误日志
tail -f /var/log/nginx/error.log
```

### 数据库连接失败

```bash
# 1. 检查RDS白名单
# 在RDS控制台确认ECS IP在白名单中

# 2. 测试连接
mysql -h your-rds-endpoint -u dataelf -p -e "SELECT 1"

# 3. 检查网络
ping your-rds-endpoint

# 4. 检查密码
# 确认application-prod.yml中的密码正确
```

## 性能优化建议

### 数据库优化

```sql
-- 添加索引
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_content_status ON contents(status);
CREATE INDEX idx_content_published ON contents(published_at);

-- 分析表
ANALYZE TABLE users;
ANALYZE TABLE contents;
```

### Redis优化

```bash
# 设置最大内存
redis-cli -h your-redis-endpoint -a your-password CONFIG SET maxmemory 1gb

# 设置淘汰策略
redis-cli -h your-redis-endpoint -a your-password CONFIG SET maxmemory-policy allkeys-lru
```

### Nginx优化

```nginx
# 在nginx.conf中添加
worker_processes auto;
worker_connections 2048;

gzip on;
gzip_types text/plain text/css application/json application/javascript;
gzip_min_length 1000;

client_max_body_size 10M;
```

## 安全加固清单

- [ ] 修改SSH默认端口
- [ ] 禁用root远程登录
- [ ] 配置防火墙规则
- [ ] 启用fail2ban
- [ ] 定期更新系统补丁
- [ ] 配置自动备份
- [ ] 启用云监控告警
- [ ] 定期审查访问日志

## 成本优化建议

1. **使用预留实例**: ECS和RDS可购买包年包月，节省30%
2. **合理配置规格**: 根据实际负载调整资源规格
3. **使用OSS生命周期**: 自动转换冷数据到低频存储
4. **CDN按需开启**: 流量大时再启用CDN
5. **定期清理**: 删除不用的快照和备份

## 扩容方案

### 垂直扩容（升级配置）

```bash
# 在阿里云控制台:
1. ECS: 升级到更高规格
2. RDS: 升级到更大内存
3. Redis: 升级到更大容量
```

### 水平扩容（增加实例）

```bash
# 1. 增加ECS实例
# 2. 配置SLB负载均衡
# 3. 配置RDS读写分离
# 4. 配置Redis集群
```

## 联系支持

- 阿里云工单: https://workorder.console.aliyun.com
- 技术支持: 95187
- 文档中心: https://help.aliyun.com

---

**提示**: 建议先在测试环境完整部署一遍，熟悉流程后再部署生产环境。
