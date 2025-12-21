# 日志和监控配置文档

## 概述

本文档描述了数流精灵平台的日志和监控配置，包括结构化日志、审计日志、Prometheus指标和Spring Boot Actuator配置。

## 日志配置

### 日志格式

系统支持多种日志格式：

1. **控制台日志** - 开发环境使用，带颜色高亮
2. **JSON格式日志** - 生产环境使用，便于日志聚合和分析
3. **标准文本日志** - 用于快速查看
4. **错误日志** - 单独记录ERROR级别日志
5. **审计日志** - 记录所有敏感操作

### 日志文件

所有日志文件存储在 `logs/` 目录下：

- `ai-data-platform.log` - 标准应用日志
- `ai-data-platform.json` - JSON格式日志
- `ai-data-platform-error.log` - 错误日志
- `ai-data-platform-audit.log` - 审计日志

### 日志轮转策略

- **按天轮转** - 每天生成新的日志文件
- **压缩归档** - 旧日志文件自动压缩为 `.gz` 格式
- **保留期限**：
  - 标准日志：30天
  - 错误日志：90天
  - 审计日志：365天
- **总大小限制**：
  - 标准日志：1GB
  - 错误日志：2GB
  - 审计日志：5GB

### MDC上下文

系统自动在MDC中记录以下信息：

- `requestId` - 唯一请求ID
- `userId` - 当前用户ID
- `ipAddress` - 客户端IP地址
- `operation` - 执行的操作
- `resourceType` - 资源类型（USER、CONTENT、TEMPLATE等）
- `resourceId` - 资源ID
- `action` - 操作动作（REGISTER、LOGIN、APPROVE等）
- `result` - 操作结果（SUCCESS、FAILURE）

### JSON日志示例

```json
{
  "@timestamp": "2024-01-01T12:00:00.000+08:00",
  "level": "INFO",
  "logger_name": "AUDIT",
  "message": "User registration completed",
  "application": "ai-data-platform",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "user@example.com",
  "ipAddress": "192.168.1.100",
  "operation": "UserService.register()",
  "resourceType": "USER",
  "action": "REGISTER",
  "result": "SUCCESS"
}
```

## 审计日志

### 审计的操作

系统自动审计以下敏感操作：

1. **用户操作**
   - 用户注册
   - 用户登录（成功和失败）
   - 账号批准
   - 账号延长

2. **内容操作**
   - 内容提交审核
   - 内容批准
   - 内容拒绝
   - 内容发布

3. **模板操作**
   - 模板删除

### 审计日志查询

审计日志以JSON格式存储，可以使用以下工具查询：

```bash
# 查询特定用户的操作
cat logs/ai-data-platform-audit.log | jq 'select(.userId == "user@example.com")'

# 查询特定时间范围的操作
cat logs/ai-data-platform-audit.log | jq 'select(."@timestamp" >= "2024-01-01" and ."@timestamp" <= "2024-01-31")'

# 查询失败的操作
cat logs/ai-data-platform-audit.log | jq 'select(.result == "FAILURE")'

# 统计各类操作的数量
cat logs/ai-data-platform-audit.log | jq -r '.action' | sort | uniq -c
```

## Prometheus指标

### 自定义业务指标

系统提供以下自定义Prometheus指标：

#### 计数器（Counter）

- `user.registration.total` - 用户注册总数
- `user.login.total` - 用户登录总数
- `user.login.failure.total` - 登录失败总数
- `content.creation.total` - 内容创建总数
- `content.publish.total` - 内容发布总数
- `content.approval.total` - 内容批准总数
- `content.rejection.total` - 内容拒绝总数
- `ai.api.call.total` - AI API调用总数
- `user.api.call.total` - 用户API调用总数
- `email.sent.total` - 邮件发送总数
- `email.failure.total` - 邮件发送失败总数
- `cache.hit.total` - 缓存命中总数
- `cache.miss.total` - 缓存未命中总数
- `application.errors.total` - 应用错误总数（按错误类型分类）

#### 计时器（Timer）

- `jsonld.generation.time` - JSON-LD生成时间
- `content.export.time` - 内容导出时间（按格式分类）
- `database.query.time` - 数据库查询时间
- `ai.api.response.time` - AI API响应时间
- `user.api.response.time` - 用户API响应时间
- `http.server.requests` - HTTP请求时间（Spring Boot自动提供）

### 指标端点

访问 `http://localhost:8080/actuator/prometheus` 获取所有Prometheus指标。

### Prometheus配置示例

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'ai-data-platform'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
    scrape_interval: 15s
```

### Grafana仪表板

推荐的Grafana面板：

1. **系统概览**
   - 请求速率
   - 错误率
   - 响应时间（P50、P95、P99）
   - JVM内存使用

2. **业务指标**
   - 用户注册趋势
   - 内容发布趋势
   - API调用量
   - 审核通过率

3. **性能指标**
   - 数据库查询时间
   - 缓存命中率
   - JSON-LD生成时间
   - 导出操作时间

4. **错误监控**
   - 错误类型分布
   - 错误趋势
   - 失败的登录尝试

## Spring Boot Actuator

### 启用的端点

- `/actuator/health` - 健康检查
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 指标查询
- `/actuator/prometheus` - Prometheus指标
- `/actuator/loggers` - 日志级别管理
- `/actuator/httptrace` - HTTP请求追踪
- `/actuator/threaddump` - 线程转储
- `/actuator/heapdump` - 堆转储

### 健康检查

健康检查包含以下组件：

- 数据库连接
- Redis连接
- 磁盘空间
- 邮件服务
- RabbitMQ连接

访问 `http://localhost:8080/actuator/health` 查看健康状态。

### 动态调整日志级别

```bash
# 查看当前日志级别
curl http://localhost:8080/actuator/loggers/com.dataelf.platform

# 调整日志级别为DEBUG
curl -X POST http://localhost:8080/actuator/loggers/com.dataelf.platform \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'

# 恢复默认日志级别
curl -X POST http://localhost:8080/actuator/loggers/com.dataelf.platform \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": null}'
```

## 告警规则

### Prometheus告警规则示例

```yaml
# alerts.yml
groups:
  - name: ai-data-platform
    interval: 30s
    rules:
      # API错误率告警
      - alert: HighErrorRate
        expr: rate(application_errors_total[5m]) > 0.05
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }} errors/sec"

      # API响应时间告警
      - alert: SlowApiResponse
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Slow API response time"
          description: "P95 response time is {{ $value }}s"

      # 数据库连接池告警
      - alert: DatabaseConnectionPoolExhausted
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.9
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Database connection pool nearly exhausted"
          description: "{{ $value }}% of connections are in use"

      # 磁盘空间告警
      - alert: LowDiskSpace
        expr: disk_free_bytes / disk_total_bytes < 0.2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Low disk space"
          description: "Only {{ $value }}% disk space remaining"

      # 登录失败告警
      - alert: HighLoginFailureRate
        expr: rate(user_login_failure_total[5m]) > 10
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High login failure rate"
          description: "{{ $value }} login failures per second"
```

## 日志聚合

### ELK Stack集成

1. **Filebeat配置**

```yaml
# filebeat.yml
filebeat.inputs:
  - type: log
    enabled: true
    paths:
      - /app/logs/ai-data-platform.json
    json.keys_under_root: true
    json.add_error_key: true

output.elasticsearch:
  hosts: ["elasticsearch:9200"]
  index: "ai-data-platform-%{+yyyy.MM.dd}"

setup.kibana:
  host: "kibana:5601"
```

2. **Logstash配置**

```ruby
# logstash.conf
input {
  file {
    path => "/app/logs/ai-data-platform.json"
    codec => json
  }
}

filter {
  if [level] == "ERROR" {
    mutate {
      add_tag => ["error"]
    }
  }
  
  if [logger_name] == "AUDIT" {
    mutate {
      add_tag => ["audit"]
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "ai-data-platform-%{+YYYY.MM.dd}"
  }
}
```

## 最佳实践

### 开发环境

- 使用控制台日志，便于实时查看
- 日志级别设置为DEBUG
- 启用SQL日志查看数据库操作

### 生产环境

- 使用JSON格式日志，便于日志聚合
- 日志级别设置为INFO
- 关闭SQL日志，避免性能影响
- 启用所有Actuator端点，但配置访问控制
- 配置Prometheus和Grafana监控
- 设置告警规则，及时发现问题

### 日志记录建议

1. **使用合适的日志级别**
   - ERROR：系统错误，需要立即处理
   - WARN：警告信息，可能影响功能
   - INFO：重要的业务操作
   - DEBUG：详细的调试信息

2. **记录关键信息**
   - 用户操作
   - 业务流程关键节点
   - 外部服务调用
   - 异常和错误

3. **避免敏感信息**
   - 不记录密码
   - 不记录完整的信用卡号
   - 不记录个人隐私信息

4. **使用结构化日志**
   - 使用MDC记录上下文信息
   - 使用JSON格式便于解析
   - 包含请求ID便于追踪

## 故障排查

### 查看实时日志

```bash
# 查看所有日志
tail -f logs/ai-data-platform.log

# 查看错误日志
tail -f logs/ai-data-platform-error.log

# 查看审计日志
tail -f logs/ai-data-platform-audit.log

# 过滤特定用户的日志
tail -f logs/ai-data-platform.json | jq 'select(.userId == "user@example.com")'
```

### 查看应用健康状态

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 查看指标
curl http://localhost:8080/actuator/metrics

# 查看特定指标
curl http://localhost:8080/actuator/metrics/user.login.total
```

### 性能分析

```bash
# 获取线程转储
curl http://localhost:8080/actuator/threaddump > threaddump.txt

# 获取堆转储（大文件）
curl http://localhost:8080/actuator/heapdump > heapdump.hprof
```

## 安全考虑

### Actuator端点保护

在生产环境中，应该保护Actuator端点：

```yaml
# application-prod.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
```

### 日志脱敏

系统自动对敏感信息进行脱敏：

- 密码：不记录
- 手机号：部分脱敏（138****8000）
- 邮箱：部分脱敏（u***@example.com）
- IP地址：完整记录（用于安全审计）

## 总结

本配置提供了完整的日志和监控解决方案，包括：

- ✅ 结构化JSON日志
- ✅ 审计日志记录
- ✅ Prometheus指标收集
- ✅ Spring Boot Actuator端点
- ✅ 错误日志记录
- ✅ MDC上下文追踪
- ✅ 日志轮转和归档
- ✅ 健康检查
- ✅ 性能监控

通过这些配置，可以实现：

1. 全面的操作审计
2. 实时的性能监控
3. 快速的故障定位
4. 有效的容量规划
5. 主动的告警通知
