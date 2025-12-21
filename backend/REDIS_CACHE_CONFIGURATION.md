# Redis缓存配置文档

## 概述

本文档描述了AI数据平台的Redis缓存配置，包括缓存策略、TTL设置和缓存失效机制。

## 配置的缓存

根据任务28的要求，系统配置了以下Redis缓存：

### 1. 内容JSON-LD缓存 (`ai-content-jsonld`)
- **用途**: 缓存已发布内容的JSON-LD结构化数据
- **TTL**: 1小时
- **缓存键**: 内容ID
- **使用位置**: `AiApiController.getData()`
- **失效时机**: 
  - 内容发布时
  - 内容更新时（仅已发布内容）
  - 管理员直接发布时

### 2. 用户会话缓存 (`user-sessions`)
- **用途**: 缓存用户账号有效性检查结果
- **TTL**: 24小时
- **缓存键**: `validity_{userId}`
- **使用位置**: `UserService.isAccountValid()`
- **失效时机**:
  - 管理员批准用户账号时
  - 延长账号时长时

### 3. 模板定义缓存 (`template-definitions`)
- **用途**: 缓存模板定义数据
- **TTL**: 365天（永久，手动失效）
- **缓存键**: 
  - `all` - 所有模板列表
  - `{templateId}` - 单个模板
- **使用位置**: 
  - `TemplateService.getAllTemplates()`
  - `TemplateService.getTemplate()`
- **失效时机**:
  - 创建新模板时
  - 更新模板时
  - 删除模板时
  - 导入模板时

### 4. 首页内容列表缓存 (`homepage-contents`)
- **用途**: 缓存首页和分类页面的内容列表
- **TTL**: 5分钟
- **缓存键**: 
  - `{page}_{size}_{sort}` - 首页内容列表
  - `category_{categoryId}_{page}_{size}_{sort}` - 分类内容列表
- **使用位置**: 
  - `PublicController.getPublishedContents()`
  - `PublicController.getContentsByCategory()`
- **失效时机**:
  - 内容发布时
  - 内容更新时（仅已发布内容）
  - 管理员直接发布时

### 5. AI搜索结果缓存 (`ai-search-results`)
- **用途**: 缓存AI搜索API的结果
- **TTL**: 5分钟
- **缓存键**: `{query}_{category}_{tag}_{page}_{size}`
- **使用位置**: `AiApiController.search()`
- **失效时机**:
  - 内容发布时
  - 内容更新时（仅已发布内容）
  - 管理员直接发布时

### 6. AI网站地图缓存 (`ai-sitemap`)
- **用途**: 缓存AI网站地图数据
- **TTL**: 1小时
- **缓存键**: `sitemap`
- **使用位置**: `AiApiController.getSitemap()`
- **失效时机**:
  - 内容发布时
  - 内容更新时（仅已发布内容）
  - 管理员直接发布时

## 缓存配置详情

### 序列化配置
- **键序列化器**: `StringRedisSerializer`
- **值序列化器**: `GenericJackson2JsonRedisSerializer`
- **类型信息**: 使用Jackson的默认类型信息（`LaissezFaireSubTypeValidator`）

### 默认配置
- **默认TTL**: 1小时
- **空值缓存**: 禁用（`disableCachingNullValues()`）
- **事务支持**: 启用（`transactionAware()`）

## 缓存失效策略

### 自动失效
所有缓存都配置了TTL，到期后自动失效。

### 手动失效
使用Spring的`@CacheEvict`注解在以下场景手动失效缓存：

1. **内容相关操作**:
   - `ContentService.publishContent()` - 失效所有内容相关缓存
   - `ContentService.directPublish()` - 失效所有内容相关缓存
   - `ContentService.updateContent()` - 条件失效（仅已发布内容）

2. **模板相关操作**:
   - `TemplateService.createTemplate()` - 失效所有模板缓存
   - `TemplateService.updateTemplate()` - 失效所有模板缓存
   - `TemplateService.deleteTemplate()` - 失效所有模板缓存
   - `TemplateService.importTemplate()` - 失效所有模板缓存

3. **用户相关操作**:
   - `UserService.approveUser()` - 失效特定用户的会话缓存
   - `UserService.extendAccount()` - 失效特定用户的会话缓存

## 性能优化建议

### 缓存命中率监控
建议通过Spring Boot Actuator和Prometheus监控以下指标：
- 缓存命中率
- 缓存大小
- 缓存失效频率

### TTL调优
根据实际使用情况调整TTL：
- 高频访问、低更新频率的数据可以增加TTL
- 低频访问、高更新频率的数据可以减少TTL

### 缓存预热
对于首页内容等高频访问的数据，可以在系统启动时进行缓存预热。

## 配置文件

### application.yml
```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
```

## 测试

缓存配置的单元测试位于：
- `backend/src/test/java/com/dataelf/platform/config/CacheConfigTest.java`

运行测试：
```bash
mvn test -Dtest=CacheConfigTest
```

## 依赖

Redis缓存功能依赖以下Maven依赖：
- `spring-boot-starter-data-redis`
- `spring-boot-starter-cache`

## 故障排查

### Redis连接失败
检查Redis服务是否运行：
```bash
redis-cli ping
```

### 缓存未生效
1. 确认`@EnableCaching`注解已添加到配置类
2. 检查方法是否被Spring代理（不能是private方法）
3. 查看日志中的缓存相关信息

### 缓存数据不一致
1. 检查缓存失效逻辑是否正确
2. 确认事务配置是否正确
3. 考虑减少TTL或增加手动失效点

## 相关文档

- [Spring Cache文档](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache)
- [Spring Data Redis文档](https://docs.spring.io/spring-data/redis/docs/current/reference/html/)
- [Redis官方文档](https://redis.io/documentation)
