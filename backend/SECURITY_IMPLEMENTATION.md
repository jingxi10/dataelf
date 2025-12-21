# 安全功能实现文档

本文档描述了数流精灵平台实现的安全功能。

## 1. 密码哈希（BCrypt）

### 实现位置
- `UserService.java` - 用户注册和登录

### 配置
```yaml
app:
  password:
    bcrypt-strength: 12  # BCrypt强度因子
```

### 功能说明
- 使用BCrypt算法对用户密码进行哈希
- 强度因子设置为12，提供良好的安全性和性能平衡
- 注册时自动哈希密码
- 登录时使用BCrypt的matches方法验证密码

### 代码示例
```java
private BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(bcryptStrength);
}

// 注册时哈希密码
user.setPasswordHash(passwordEncoder().encode(request.getPassword()));

// 登录时验证密码
passwordEncoder().matches(request.getPassword(), user.getPasswordHash())
```

## 2. 登录失败锁定

### 实现位置
- `LoginAttemptService.java` - 登录尝试跟踪服务
- `LoginAttempt.java` - 登录尝试实体
- `LoginAttemptRepository.java` - 登录尝试数据访问

### 配置
```yaml
app:
  login:
    max-attempts: 5           # 最大失败尝试次数
    lockout-duration: 900000  # 锁定时长（15分钟，毫秒）
```

### 功能说明
- 记录每次登录尝试（成功或失败）
- 记录IP地址用于审计
- 在锁定时间窗口内失败次数超过最大尝试次数时锁定账号
- 锁定期间拒绝登录并显示剩余锁定时间
- 成功登录后重置失败计数
- 定时清理30天前的旧记录

### 工作流程
1. 用户尝试登录
2. 检查账号是否被锁定
3. 如果锁定，返回错误和剩余锁定时间
4. 验证凭证
5. 记录登录尝试（成功或失败）
6. 如果失败次数达到阈值，后续登录将被锁定

### 数据库表结构
```sql
CREATE TABLE login_attempts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    successful BOOLEAN NOT NULL,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_created_at (created_at)
);
```

## 3. CORS策略配置

### 实现位置
- `SecurityConfig.java` - Spring Security配置

### 配置
```yaml
app:
  cors:
    allowed-origins: ${CORS_ORIGINS:http://localhost:3000,http://localhost:5173}
```

### 功能说明
- 配置允许的源（origins）
- 支持多个源，用逗号分隔
- 允许的HTTP方法：GET, POST, PUT, DELETE, OPTIONS, PATCH
- 允许的请求头：Authorization, Content-Type等
- 允许凭证（credentials）
- 预检请求缓存1小时

### 安全特性
- 明确指定允许的源，不使用通配符
- 只暴露必要的响应头
- 限制允许的HTTP方法
- 支持凭证传递（用于JWT认证）

## 4. HTTPS强制

### 实现位置
- `SecurityConfig.java` - Spring Security配置

### 配置
```yaml
app:
  security:
    require-https: ${REQUIRE_HTTPS:false}  # 生产环境设置为true
```

### 功能说明
- 开发环境默认关闭（false）
- 生产环境通过环境变量启用（REQUIRE_HTTPS=true）
- 启用后，所有HTTP请求自动重定向到HTTPS
- 配置HSTS（HTTP Strict Transport Security）头部
  - 包含子域名
  - 最大年龄：1年（31536000秒）

### 部署建议
```bash
# 生产环境启动命令
export REQUIRE_HTTPS=true
java -jar ai-data-platform.jar
```

## 5. XSS防护

### 实现位置
- `SecurityConfig.java` - 安全头部配置
- Spring Security默认XSS保护

### 功能说明
- 启用XSS保护头部：`X-XSS-Protection: 1; mode=block`
- 内容类型嗅探防护：`X-Content-Type-Options: nosniff`
- 内容安全策略（CSP）：限制脚本和样式来源
- 点击劫持防护：`X-Frame-Options: DENY`

### 安全头部配置
```java
.headers()
    .xssProtection()
        .and()
    .contentTypeOptions()
        .and()
    .frameOptions()
        .deny()
        .and()
    .contentSecurityPolicy("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'")
```

### 额外保护
- Spring MVC自动转义HTML输出
- Jackson JSON库防止JSON注入
- Thymeleaf模板引擎自动转义

## 6. SQL注入防护

### 实现位置
- JPA/Hibernate - 所有数据访问层
- Spring Data JPA - Repository接口

### 功能说明
- 使用JPA参数化查询，自动防止SQL注入
- 所有用户输入通过参数绑定传递
- 不使用字符串拼接构建SQL查询
- JPQL查询使用命名参数或位置参数

### 代码示例
```java
// 安全的参数化查询
@Query("SELECT la FROM LoginAttempt la WHERE la.email = :email " +
       "AND la.successful = false AND la.createdAt > :since")
List<LoginAttempt> findFailedAttemptsSince(
    @Param("email") String email, 
    @Param("since") LocalDateTime since
);

// Spring Data JPA方法名查询（自动参数化）
Optional<User> findByEmail(String email);
```

### 验证层保护
- Bean Validation注解验证输入格式
- 自定义验证器检查业务规则
- 在Service层进行二次验证

## 7. 其他安全特性

### 7.1 JWT认证
- 无状态会话管理
- 访问令牌：1小时有效期
- 刷新令牌：7天有效期
- 使用HS256算法签名

### 7.2 安全审计
- 记录所有登录尝试（成功和失败）
- 记录IP地址用于追踪
- 结构化日志记录敏感操作

### 7.3 输入验证
- 前端验证：实时字段验证
- 后端验证：Bean Validation + 自定义验证器
- 邮箱格式验证
- 密码强度验证
- 长度限制

### 7.4 会话管理
- 无状态会话（JWT）
- 不使用服务器端会话
- 令牌过期自动失效

### 7.5 错误处理
- 统一错误响应格式
- 不泄露内部实现细节
- 敏感操作失败时记录日志

## 8. 安全配置检查清单

### 开发环境
- [ ] BCrypt强度设置为12
- [ ] HTTPS强制关闭（require-https: false）
- [ ] CORS允许本地开发端口
- [ ] 登录失败锁定启用
- [ ] 日志级别设置为DEBUG

### 生产环境
- [ ] BCrypt强度保持12或更高
- [ ] HTTPS强制启用（REQUIRE_HTTPS=true）
- [ ] CORS只允许生产域名
- [ ] 登录失败锁定启用
- [ ] 日志级别设置为INFO
- [ ] JWT密钥使用强随机密钥（至少256位）
- [ ] 数据库凭证使用环境变量
- [ ] 邮件服务凭证使用环境变量
- [ ] 启用HSTS头部
- [ ] 配置防火墙规则
- [ ] 定期更新依赖库

## 9. 安全测试建议

### 9.1 密码安全测试
- 测试弱密码被拒绝
- 测试密码哈希不可逆
- 测试相同密码产生不同哈希

### 9.2 登录锁定测试
- 测试5次失败后锁定
- 测试锁定期间无法登录
- 测试锁定时间过后可以登录
- 测试成功登录重置失败计数

### 9.3 CORS测试
- 测试允许的源可以访问
- 测试不允许的源被拒绝
- 测试预检请求正确处理

### 9.4 XSS测试
- 测试脚本标签被转义
- 测试事件处理器被转义
- 测试JSON响应正确编码

### 9.5 SQL注入测试
- 测试特殊字符输入
- 测试SQL关键字输入
- 测试参数化查询正确工作

## 10. 监控和告警

### 10.1 监控指标
- 登录失败率
- 账号锁定次数
- 异常IP地址访问
- API错误率

### 10.2 告警规则
- 登录失败率 > 10%
- 单个IP短时间内多次失败
- 异常的API访问模式
- 数据库连接异常

## 11. 安全更新和维护

### 11.1 定期任务
- 每天清理旧的登录尝试记录
- 每周检查依赖库安全更新
- 每月审查安全日志
- 每季度进行安全审计

### 11.2 应急响应
- 发现安全漏洞立即修复
- 记录安全事件
- 通知受影响用户
- 更新安全文档

## 12. 参考资源

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security文档](https://spring.io/projects/spring-security)
- [BCrypt算法](https://en.wikipedia.org/wiki/Bcrypt)
- [CORS规范](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)
- [HSTS规范](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Strict-Transport-Security)
