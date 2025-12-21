# 数流精灵 - AI数据清洗和结构化平台

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![License](https://img.shields.io/badge/license-MIT-blue)]()
[![Java](https://img.shields.io/badge/Java-17-orange)]()
[![Vue](https://img.shields.io/badge/Vue-3-green)]()

## 项目概述

数流精灵是一个专为AI优化的结构化数据平台，通过模板化编辑器将任意内容转化为AI易于理解和引用的标准化格式。平台采用"内容与交互分离"架构，确保核心数据以最纯净的语义化格式（JSON-LD+语义HTML）呈现。

### 核心特性

- 🎯 **模板化编辑器**: 6种预设模板（技术文章、案例研究、数据报告、产品评测、调查报告、行业分析）
- 🤖 **AI优化输出**: 自动生成Schema.org标准的JSON-LD和语义化HTML
- 📊 **多格式导出**: 支持JSON-LD、HTML、Markdown、CSV四种格式
- 🔐 **完整的权限系统**: 用户注册审核、账号时长管理、内容审核工作流
- 📧 **通知系统**: 邮件+站内通知双重提醒
- 🚀 **高性能**: Redis缓存、RabbitMQ异步处理、响应式设计
- 🖼️ **系统配置**: 支持Logo上传、网站信息自定义
- 💬 **评论系统**: 完整的评论功能，支持分页和实时更新
- 📝 **内容管理**: 我的内容页面，支持草稿、待审核、已发布内容管理
- 🔍 **SEO优化**: robots.txt配置、AI友好的API接口

## 技术栈

### 后端
- Spring Boot 2.7.18
- MySQL 5.7
- Redis 6+
- RabbitMQ
- JWT认证

### 前端
- Vue 3
- Vite
- Element Plus
- Pinia
- Vue Router

## 快速开始

### 🚀 5分钟快速启动（推荐）

使用Docker Compose一键启动所有服务：

```bash
# 克隆项目
git clone <your-repo-url>
cd ai-data-platform

# 启动所有服务
docker-compose up -d

# 访问应用
# 前端: http://localhost
# 后端API: http://localhost:8080
# API文档: http://localhost:8080/swagger-ui.html
# RabbitMQ管理: http://localhost:15672 (admin/admin_password)
```

详细说明请查看 [快速启动指南](QUICKSTART.md)

### 📖 本地开发环境

#### 前置要求

- Java 17+
- Node.js 18+
- MySQL 5.7+
- Redis 6+
- RabbitMQ 3.9+
- Maven 3.8+

#### 1. 启动依赖服务

```bash
# 使用Docker启动MySQL、Redis、RabbitMQ
docker-compose up -d mysql redis rabbitmq
```

#### 2. 启动后端

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

后端服务将在 http://localhost:8080 启动

#### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端应用将在 http://localhost:5173 启动

### 🌐 生产环境部署

详细的生产环境部署指南（包括阿里云部署）请查看 [部署文档](DEPLOYMENT.md)

## 项目结构

```
.
├── backend/                 # Spring Boot后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── frontend/               # Vue 3前端
│   ├── src/
│   │   ├── api/
│   │   ├── components/
│   │   ├── router/
│   │   ├── stores/
│   │   ├── views/
│   │   └── main.js
│   ├── package.json
│   └── vite.config.js
└── README.md
```

## 环境变量

### 后端环境变量

在 `backend/src/main/resources/application.yml` 中配置或通过环境变量设置：

- `DB_USERNAME`: 数据库用户名
- `DB_PASSWORD`: 数据库密码
- `REDIS_HOST`: Redis主机地址
- `REDIS_PORT`: Redis端口
- `JWT_SECRET`: JWT密钥
- `MAIL_HOST`: 邮件服务器地址
- `MAIL_USERNAME`: 邮件用户名
- `MAIL_PASSWORD`: 邮件密码

### 前端环境变量

在 `frontend/.env` 中配置：

- `VITE_API_BASE_URL`: 后端API地址

## API文档

启动后端服务后，访问 http://localhost:8080/swagger-ui.html 查看API文档

## 📚 文档

- [快速启动指南](QUICKSTART.md) - 5分钟快速上手
- [部署文档](DEPLOYMENT.md) - 完整的部署指南（包括阿里云）
- [API文档](backend/API_DOCUMENTATION.md) - REST API接口说明
- [需求文档](.kiro/specs/ai-data-platform/requirements.md) - 功能需求详细说明
- [设计文档](.kiro/specs/ai-data-platform/design.md) - 系统架构和设计
- [任务列表](.kiro/specs/ai-data-platform/tasks.md) - 开发任务清单

### 技术文档

- [安全实现](backend/SECURITY_IMPLEMENTATION.md)
- [缓存配置](backend/REDIS_CACHE_CONFIGURATION.md)
- [异步任务处理](backend/ASYNC_TASK_PROCESSING.md)
- [日志监控](backend/LOGGING_MONITORING.md)
- [Swagger配置](backend/SWAGGER_SETUP.md)

## 🧪 测试

### 后端测试

```bash
cd backend
mvn test

# 运行特定测试
mvn test -Dtest=UserServiceTest

# 生成测试报告
mvn test jacoco:report
```

测试覆盖率: **105个测试全部通过** ✅

### 前端测试

```bash
cd frontend
npm run test

# 运行特定测试
npm run test -- NotificationComponents.spec.js

# 生成覆盖率报告
npm run test:coverage
```

测试覆盖率: **44个测试全部通过** ✅

## 🔧 开发工具

### 推荐IDE

- **后端**: IntelliJ IDEA
- **前端**: VS Code + Volar插件

### 代码规范

```bash
# 后端代码格式化
cd backend
mvn spotless:apply

# 前端代码格式化
cd frontend
npm run lint
npm run format
```

## 🚀 性能指标

- API响应时间: < 200ms (P95)
- 页面首次内容绘制: < 1.5s
- 页面完全加载: < 3s
- 并发用户支持: 1000+

## 🔐 安全特性

- JWT令牌认证
- BCrypt密码加密
- 登录失败锁定（5次失败锁定15分钟）
- XSS防护
- SQL注入防护
- HTTPS强制
- CORS策略配置

## 📊 监控

- Spring Boot Actuator健康检查
- Prometheus指标收集
- 结构化日志（JSON格式）
- 操作审计日志

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📄 许可证

Copyright © 2024 数流精灵

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 提交Issue: [GitHub Issues]
- 邮箱: support@dataelf.com

---

**快速链接:**
- [快速启动](QUICKSTART.md) | [部署指南](DEPLOYMENT.md) | [API文档](backend/API_DOCUMENTATION.md)
