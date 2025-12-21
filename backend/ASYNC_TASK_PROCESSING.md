# 异步任务处理实现文档

## 概述

本文档描述了数流精灵平台的异步任务处理系统实现，包括RabbitMQ消息队列配置、邮件发送队列、通知创建队列和定时任务。

## 架构组件

### 1. RabbitMQ配置 (RabbitMQConfig.java)

配置了两个主要队列：
- **email.queue**: 处理异步邮件发送
- **notification.queue**: 处理异步站内通知创建

**关键配置：**
```java
- 持久化队列 (durable = true)
- JSON消息转换器 (Jackson2JsonMessageConverter)
- 并发消费者配置 (3-10个并发消费者)
```

### 2. 消息监听器 (NotificationMessageListener.java)

实现了两个RabbitMQ监听器：

#### 邮件队列监听器
- 监听队列: `email.queue`
- 处理: EmailMessage 消息
- 功能: 从队列中取出邮件消息并发送
- 错误处理: 失败时抛出异常触发重试机制

#### 通知队列监听器
- 监听队列: `notification.queue`
- 处理: NotificationMessage 消息
- 功能: 从队列中取出通知消息并创建站内通知
- 错误处理: 失败时抛出异常触发重试机制

### 3. 通知服务 (NotificationService.java)

提供同步和异步两种方式处理邮件和通知：

#### 邮件发送方法
- `sendEmail()`: 同步发送邮件，失败时自动转为异步
- `sendEmailAsync()`: 异步发送邮件，将消息放入队列

#### 通知创建方法
- `createNotification()`: 同步创建站内通知
- `createNotificationAsync()`: 异步创建通知，将消息放入队列

#### 业务通知方法
- `sendAccountApprovedNotification()`: 账号批准通知（邮件+站内）
- `sendContentApprovedNotification()`: 内容批准通知（邮件+站内）
- `sendContentRejectedNotification()`: 内容拒绝通知（邮件+站内）
- `sendAccountExtendedNotification()`: 账号延长通知（邮件+站内）
- `sendExpiryReminder()`: 账号到期提醒

### 4. 定时任务服务 (ScheduledTaskService.java)

实现账号到期检查和提醒功能：

#### 定时任务配置
- **执行时间**: 每天凌晨2点 (cron: "0 0 2 * * ?")
- **检查范围**: 7天内到期的账号
- **提醒时机**: 到期前7天、3天、1天

#### 执行流程
1. 查询所有已批准且在7天内到期的账号
2. 计算剩余天数
3. 在特定时间点（7天、3天、1天）发送提醒
4. 同时发送邮件和站内通知

## 消息格式

### EmailMessage
```java
{
  "to": "user@example.com",
  "subject": "邮件主题",
  "content": "邮件内容"
}
```

### NotificationMessage
```java
{
  "userId": 1,
  "type": "ACCOUNT_APPROVED",
  "title": "通知标题",
  "message": "通知内容",
  "relatedContentId": 123
}
```

## 配置说明

### application.yml 配置

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    virtual-host: /
```

### Docker Compose 配置

```yaml
rabbitmq:
  image: rabbitmq:3-management-alpine
  ports:
    - "5672:5672"   # AMQP端口
    - "15672:15672" # 管理界面端口
```

## 使用示例

### 1. 发送异步邮件

```java
@Autowired
private NotificationService notificationService;

// 异步发送邮件
notificationService.sendEmailAsync(
    "user@example.com",
    "欢迎使用数流精灵",
    "您的账号已创建成功！"
);
```

### 2. 创建异步通知

```java
// 异步创建站内通知
notificationService.createNotificationAsync(
    userId,
    NotificationType.CONTENT_APPROVED,
    "您的内容已通过审核"
);
```

### 3. 发送业务通知

```java
// 发送账号批准通知（自动发送邮件+站内通知）
notificationService.sendAccountApprovedNotification(
    userEmail,
    userId,
    30 // 有效天数
);
```

## 错误处理和重试机制

### 邮件发送失败处理
1. 同步发送失败时，自动转为异步发送
2. 异步发送失败时，RabbitMQ会根据配置进行重试
3. 多次重试失败后，消息会进入死信队列（需配置）

### 通知创建失败处理
1. 异步创建失败时，回退到同步创建
2. 确保重要通知不会丢失

### 定时任务错误处理
- 任务执行失败会记录日志
- 不影响下次定时执行
- 建议配置监控告警

## 性能优化

### 并发处理
- 邮件队列: 3-10个并发消费者
- 通知队列: 3-10个并发消费者
- 可根据负载调整并发数

### 消息持久化
- 队列持久化确保消息不丢失
- 重启后消息仍然存在

### 异步优势
- 不阻塞主业务流程
- 提高系统响应速度
- 支持削峰填谷

## 监控和日志

### 日志记录
- 邮件发送成功/失败日志
- 通知创建成功/失败日志
- 定时任务执行日志

### RabbitMQ管理界面
- 访问地址: http://localhost:15672
- 默认账号: guest/guest
- 可查看队列状态、消息数量、消费速率等

### 监控指标
- 队列消息积压数量
- 消息处理速率
- 失败消息数量
- 定时任务执行时间

## 测试

### 单元测试
参见 `NotificationServiceTest.java`，包含：
- 邮件发送测试
- 通知创建测试
- 异步队列测试
- 业务通知测试

### 集成测试
需要启动RabbitMQ服务：
```bash
docker-compose up -d rabbitmq
```

## 相关需求

本实现满足以下需求：
- **需求 1.3**: 管理员批准用户账号时发送通知
- **需求 5.4**: 账号即将到期时发送提醒
- **需求 12.1**: 内容审核通过时创建站内通知
- **需求 12.2**: 内容审核被拒绝时创建站内通知

## 未来改进

1. **死信队列配置**: 处理多次重试失败的消息
2. **消息优先级**: 为重要通知设置更高优先级
3. **延迟队列**: 支持延迟发送功能
4. **消息追踪**: 记录消息的完整生命周期
5. **告警机制**: 队列积压或处理失败时发送告警
