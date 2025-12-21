# 通知系统使用说明

## 概述

通知系统为用户提供实时的站内通知功能，包括账号审核、内容审核、账号到期提醒等通知类型。

## 组件说明

### 1. NotificationBadge（通知徽章）

显示未读通知数量的徽章按钮。

**使用方式：**
```vue
<NotificationBadge @click="handleClick" />
```

**特性：**
- 显示未读通知数量
- 超过99条显示"99+"
- 无未读通知时隐藏徽章
- 点击时触发click事件

### 2. NotificationItem（通知项）

单个通知的展示组件。

**使用方式：**
```vue
<NotificationItem :notification="notification" />
```

**Props：**
- `notification`: 通知对象，包含以下字段：
  - `id`: 通知ID
  - `type`: 通知类型（ACCOUNT_APPROVED, CONTENT_APPROVED, CONTENT_REJECTED, ACCOUNT_EXPIRING, ACCOUNT_EXTENDED）
  - `title`: 通知标题
  - `message`: 通知内容
  - `isRead`: 是否已读
  - `createdAt`: 创建时间
  - `relatedContentId`: 关联内容ID（可选）

**特性：**
- 根据通知类型显示不同图标和颜色
- 未读通知有蓝色背景高亮
- 显示相对时间（刚刚、X分钟前、X小时前等）
- 点击后自动标记为已读
- 如有关联内容，点击后跳转到内容详情页

### 3. NotificationDropdown（通知下拉框）

包含通知徽章和下拉列表的完整组件。

**使用方式：**
```vue
<NotificationDropdown />
```

**特性：**
- 集成NotificationBadge
- 下拉显示最近10条通知
- 支持"全部已读"操作
- 支持跳转到通知中心
- 每30秒自动更新未读数量

### 4. NotificationsView（通知中心页面）

完整的通知列表页面。

**路由：** `/notifications`

**特性：**
- 显示所有通知
- 支持"全部通知"和"未读通知"标签切换
- 支持批量标记已读
- 显示未读通知数量徽章

## API接口

### 通知Store（useNotificationStore）

**状态：**
- `notifications`: 通知列表
- `unreadCount`: 未读通知数量
- `loading`: 加载状态
- `unreadNotifications`: 未读通知列表（计算属性）

**方法：**
- `loadNotifications()`: 加载所有通知
- `loadUnreadNotifications()`: 加载未读通知
- `updateUnreadCount()`: 更新未读数量
- `markAsRead(notificationId)`: 标记单个通知为已读
- `markAllAsRead()`: 标记所有通知为已读
- `clearNotifications()`: 清空通知（用于登出）

**使用示例：**
```javascript
import { useNotificationStore } from '@/stores/notification'

const notificationStore = useNotificationStore()

// 加载通知
await notificationStore.loadNotifications()

// 标记已读
await notificationStore.markAsRead(notificationId)

// 获取未读数量
const count = notificationStore.unreadCount
```

## 后端API

### 获取用户通知列表
```
GET /api/notifications
Authorization: Bearer {token}
```

### 获取未读通知
```
GET /api/notifications/unread
Authorization: Bearer {token}
```

### 获取未读通知数量
```
GET /api/notifications/unread/count
Authorization: Bearer {token}
```

### 标记通知为已读
```
PUT /api/notifications/{id}/read
Authorization: Bearer {token}
```

### 标记所有通知为已读
```
PUT /api/notifications/read-all
Authorization: Bearer {token}
```

## 通知类型

| 类型 | 说明 | 图标 | 颜色 |
|------|------|------|------|
| ACCOUNT_APPROVED | 账号审核通过 | SuccessFilled | 绿色 |
| CONTENT_APPROVED | 内容审核通过 | SuccessFilled | 绿色 |
| CONTENT_REJECTED | 内容审核被拒 | WarningFilled | 红色 |
| ACCOUNT_EXPIRING | 账号即将到期 | Clock | 橙色 |
| ACCOUNT_EXTENDED | 账号已延长 | InfoFilled | 蓝色 |

## 集成到页面

### 在导航栏中添加通知下拉框

```vue
<template>
  <nav class="nav">
    <!-- 其他导航项 -->
    <NotificationDropdown v-if="isAuthenticated" />
  </nav>
</template>

<script setup>
import NotificationDropdown from '@/components/NotificationDropdown.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const isAuthenticated = computed(() => authStore.isAuthenticated)
</script>
```

## 自动更新机制

通知系统会自动：
1. 在NotificationDropdown组件挂载时更新未读数量
2. 每30秒自动刷新未读数量
3. 打开下拉框时加载最新通知列表

## 测试

运行通知组件测试：
```bash
npm test -- --run NotificationComponents.spec.js
```

## 注意事项

1. 通知功能需要用户登录认证
2. 通知数据会在用户登出时清空
3. 点击通知项会自动标记为已读
4. 通知下拉框只显示最近10条通知，完整列表请访问通知中心页面
5. 时间显示为相对时间，超过7天显示具体日期
