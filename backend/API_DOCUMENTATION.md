# 数流精灵 API 文档

## 概述

数流精灵提供完整的RESTful API，支持AI数据抓取、用户内容管理和管理员操作。所有API遵循统一的响应格式和错误处理机制。

## 访问API文档

启动应用后，可以通过以下方式访问交互式API文档：

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

## API分类

### 1. AI API (`/api/ai/*`)

**特点：**
- ✅ 无需认证
- ✅ 提供纯净的结构化数据
- ✅ 符合Schema.org标准
- ✅ 专为AI系统优化

**主要接口：**
- `GET /api/ai/data/{id}` - 获取纯JSON-LD结构化数据
- `GET /api/ai/search` - AI优化搜索
- `GET /api/ai/sitemap` - 获取结构化网站地图
- `GET /api/ai/page/{id}` - 获取AI友好的HTML页面

### 2. 用户API (`/api/user/*`)

**特点：**
- 🔒 需要JWT认证
- ✅ 提供完整的用户交互功能
- ✅ 包含个性化内容和操作

**主要接口：**
- `POST /api/user/submit` - 提交内容
- `GET /api/user/feed` - 获取个性化内容流
- `POST /api/user/interact/{action}` - 用户交互（点赞、收藏等）

### 3. 管理员API (`/api/admin/*`)

**特点：**
- 🔒 需要JWT认证
- 🔒 需要管理员权限
- ✅ 用户和内容管理

**主要接口：**
- `POST /api/admin/users/approve` - 批准用户账号
- `POST /api/admin/users/extend` - 延长账号时长
- `GET /api/admin/users/{userId}` - 查看用户详情
- `GET /api/admin/contents/review-queue` - 获取审核队列
- `POST /api/admin/contents/{id}/approve` - 批准内容
- `POST /api/admin/contents/{id}/reject` - 拒绝内容

### 4. 公开API (`/api/public/*`)

**特点：**
- ✅ 无需认证
- ✅ 公开内容浏览
- ✅ 分类和标签查询

**主要接口：**
- `GET /api/public/contents` - 获取已发布内容列表
- `GET /api/public/contents/{id}` - 获取内容详情
- `GET /api/public/categories` - 获取分类列表
- `GET /api/public/tags` - 获取标签列表

### 5. 认证API (`/api/auth/*`)

**特点：**
- ✅ 无需认证（登录和注册接口本身）
- ✅ 用户注册和登录

**主要接口：**
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录

## 认证机制

### JWT令牌认证

需要认证的接口需要在请求头中包含JWT令牌：

```http
Authorization: Bearer <your-jwt-token>
```

### 获取令牌流程

1. **注册账号**
   ```bash
   POST /api/auth/register
   {
     "email": "user@example.com",
     "phone": "13800138000",
     "password": "StrongPass123"
   }
   ```

2. **等待管理员审核**
   - 账号状态：PENDING
   - 收到审核通过邮件后可登录

3. **登录获取令牌**
   ```bash
   POST /api/auth/login
   {
     "email": "user@example.com",
     "password": "StrongPass123"
   }
   ```
   
   响应：
   ```json
   {
     "success": true,
     "data": {
       "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
       "user": {
         "id": 1,
         "email": "user@example.com",
         "role": "USER"
       }
     }
   }
   ```

4. **使用令牌访问受保护接口**
   ```bash
   curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
        http://localhost:8080/api/user/feed
   ```

## 统一响应格式

### 成功响应

```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    // 响应数据
  }
}
```

### 错误响应

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "错误描述",
    "details": [
      {
        "field": "fieldName",
        "message": "字段错误信息"
      }
    ]
  },
  "timestamp": "2024-01-01T12:00:00Z"
}
```

## HTTP状态码

| 状态码 | 说明 | 使用场景 |
|--------|------|----------|
| 200 | OK | 请求成功 |
| 201 | Created | 资源创建成功 |
| 400 | Bad Request | 请求参数验证失败 |
| 401 | Unauthorized | 未认证或令牌无效 |
| 403 | Forbidden | 无权限访问 |
| 404 | Not Found | 资源不存在 |
| 409 | Conflict | 业务逻辑冲突 |
| 500 | Internal Server Error | 服务器内部错误 |

## 常见错误码

| 错误码 | 说明 |
|--------|------|
| VALIDATION_ERROR | 输入验证失败 |
| AUTHENTICATION_ERROR | 认证失败 |
| AUTHORIZATION_ERROR | 授权失败 |
| ACCOUNT_PENDING | 账号待审核 |
| ACCOUNT_EXPIRED | 账号已过期 |
| ACCOUNT_LOCKED | 账号已锁定 |
| RESOURCE_NOT_FOUND | 资源不存在 |
| DUPLICATE_RESOURCE | 资源重复 |
| BUSINESS_LOGIC_ERROR | 业务逻辑错误 |

## 分页参数

所有支持分页的接口使用统一的分页参数：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | int | 0 | 页码（从0开始） |
| size | int | 20 | 每页数量 |
| sort | string | - | 排序字段（格式：字段名,方向） |

分页响应格式：
```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 5,
  "currentPage": 0,
  "pageSize": 20
}
```

## 缓存策略

| 接口 | 缓存时间 | 说明 |
|------|----------|------|
| `/api/ai/data/{id}` | 1小时 | 内容JSON-LD数据 |
| `/api/ai/search` | 5分钟 | AI搜索结果 |
| `/api/ai/sitemap` | 1小时 | 网站地图 |
| `/api/public/contents` | 5分钟 | 首页内容列表 |

## 速率限制

为保护系统资源，API实施以下速率限制：

| 接口类型 | 限制 |
|----------|------|
| AI API | 1000次/小时 |
| 用户API | 500次/小时 |
| 管理员API | 无限制 |
| 公开API | 2000次/小时 |

超过限制将返回 `429 Too Many Requests` 状态码。

## 示例代码

### JavaScript/Axios

```javascript
// 登录
const loginResponse = await axios.post('http://localhost:8080/api/auth/login', {
  email: 'user@example.com',
  password: 'StrongPass123'
});

const token = loginResponse.data.data.token;

// 使用令牌访问受保护接口
const feedResponse = await axios.get('http://localhost:8080/api/user/feed', {
  headers: {
    'Authorization': `Bearer ${token}`
  },
  params: {
    page: 0,
    size: 20
  }
});
```

### Python/Requests

```python
import requests

# 登录
login_response = requests.post('http://localhost:8080/api/auth/login', json={
    'email': 'user@example.com',
    'password': 'StrongPass123'
})

token = login_response.json()['data']['token']

# 使用令牌访问受保护接口
feed_response = requests.get('http://localhost:8080/api/user/feed', 
    headers={'Authorization': f'Bearer {token}'},
    params={'page': 0, 'size': 20}
)
```

### cURL

```bash
# 登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"StrongPass123"}'

# 使用令牌访问受保护接口
curl -X GET http://localhost:8080/api/user/feed?page=0&size=20 \
  -H "Authorization: Bearer <your-token>"
```

## AI系统集成指南

### 1. 获取所有内容

```bash
# 获取网站地图
curl http://localhost:8080/api/ai/sitemap

# 遍历所有内容
for id in $(jq -r '.entries[].id' sitemap.json); do
  curl http://localhost:8080/api/ai/data/$id > content_$id.json
done
```

### 2. 搜索特定内容

```bash
# 搜索关键词
curl "http://localhost:8080/api/ai/search?query=人工智能&page=0&size=20"
```

### 3. 获取HTML页面（包含JSON-LD）

```bash
# 获取AI友好的HTML页面
curl http://localhost:8080/api/ai/page/1
```

## 数据格式

### JSON-LD示例

```json
{
  "@context": "https://schema.org",
  "@type": "Article",
  "headline": "人工智能在医疗领域的应用",
  "author": {
    "@type": "Person",
    "name": "张三"
  },
  "datePublished": "2024-01-01T12:00:00Z",
  "dateModified": "2024-01-02T12:00:00Z",
  "articleBody": "文章内容...",
  "keywords": ["人工智能", "医疗", "机器学习"],
  "copyrightHolder": {
    "@type": "Organization",
    "name": "数流精灵"
  },
  "copyrightNotice": "版权所有 © 2024",
  "sourceOrganization": {
    "@type": "Organization",
    "name": "原始来源"
  }
}
```

## 安全最佳实践

1. **HTTPS**: 生产环境必须使用HTTPS
2. **令牌存储**: 不要在客户端代码中硬编码令牌
3. **令牌刷新**: 令牌过期前及时刷新
4. **密码强度**: 使用强密码（至少8个字符，包含大小写字母和数字）
5. **速率限制**: 遵守API速率限制
6. **错误处理**: 妥善处理所有错误响应

## 支持与反馈

- **技术支持**: support@dataelf.com
- **问题反馈**: https://github.com/dataelf/platform/issues
- **文档更新**: 本文档随API版本更新

## 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0.0 | 2024-01-01 | 初始版本 |

---

**注意**: 本文档描述的是API v1.0.0版本。请确保使用与您的应用版本匹配的API文档。
