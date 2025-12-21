# 路由和状态管理实现文档

## 概述

本文档描述了数流精灵平台前端的路由配置和状态管理实现。系统使用 Vue Router 进行路由管理，使用 Pinia 进行全局状态管理。

## 路由配置

### 路由结构

系统包含以下主要路由：

#### 公开路由
- `/` - 首页
- `/login` - 登录页
- `/register` - 注册页
- `/content/:id` - 内容详情页

#### 需要认证的路由
- `/favorites` - 我的收藏
- `/notifications` - 通知中心
- `/editor` - 内容编辑器（新建）
- `/editor/:id` - 内容编辑器（编辑）

#### 管理员路由
- `/admin` - 管理后台（重定向到用户管理）
  - `/admin/users` - 用户管理
  - `/admin/templates` - 模板管理
  - `/admin/review` - 内容审核

#### 错误页面
- `/:pathMatch(.*)*` - 404 页面

### 路由守卫

系统实现了全局前置守卫，提供以下功能：

1. **认证初始化**
   - 在首次路由前初始化用户认证状态
   - 从 localStorage 恢复 token 并验证

2. **页面标题设置**
   - 根据路由 meta.title 自动设置页面标题
   - 格式：`{页面标题} - 数流精灵`

3. **访客路由保护**
   - 已登录用户访问登录/注册页面时重定向到首页

4. **认证检查**
   - 需要认证的路由检查用户登录状态
   - 未登录用户重定向到登录页，并保存原始目标路径

5. **账号状态检查**
   - 检查账号是否处于审核中（PENDING）
   - 检查账号是否已过期（EXPIRED）
   - 不符合条件的用户无法访问需要认证的页面

6. **管理员权限检查**
   - 需要管理员权限的路由检查用户角色
   - 非管理员用户重定向到首页

### 路由配置示例

```javascript
{
  path: '/editor',
  name: 'editor',
  component: () => import('@/views/editor/EditorView.vue'),
  meta: { 
    requiresAuth: true,  // 需要认证
    title: '内容编辑器'   // 页面标题
  }
}
```

## 状态管理

系统使用 Pinia 进行状态管理，包含以下 Store：

### 1. Auth Store (`stores/auth.js`)

管理用户认证状态和用户信息。

#### State
- `user` - 当前用户信息
- `token` - JWT 认证令牌
- `loading` - 加载状态
- `initialized` - 是否已初始化

#### Getters
- `isAuthenticated` - 是否已认证
- `isAdmin` - 是否是管理员
- `isPending` - 账号是否在审核中
- `isExpired` - 账号是否已过期

#### Actions
- `initializeAuth()` - 初始化认证状态（从 token 恢复）
- `login(credentials)` - 用户登录
- `register(userData)` - 用户注册
- `logout()` - 用户登出
- `refreshUser()` - 刷新用户信息
- `setAuth(userData, token)` - 设置认证信息
- `clearAuth()` - 清除认证信息

#### 使用示例

```javascript
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// 登录
await authStore.login({ email, password })

// 检查认证状态
if (authStore.isAuthenticated) {
  // 用户已登录
}

// 登出
await authStore.logout()
```

### 2. Notification Store (`stores/notification.js`)

管理用户通知。

#### State
- `notifications` - 通知列表
- `unreadCount` - 未读通知数量
- `loading` - 加载状态

#### Getters
- `unreadNotifications` - 未读通知列表

#### Actions
- `loadNotifications()` - 加载所有通知
- `loadUnreadNotifications()` - 加载未读通知
- `updateUnreadCount()` - 更新未读数量
- `markAsRead(notificationId)` - 标记为已读
- `markAllAsRead()` - 标记所有为已读
- `clearNotifications()` - 清空通知

#### 使用示例

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

### 3. Content Store (`stores/content.js`)

管理内容数据。

#### State
- `contents` - 内容列表
- `currentContent` - 当前内容
- `loading` - 加载状态
- `pagination` - 分页信息

#### Actions
- `loadContents(page, size)` - 加载内容列表
- `loadContent(contentId)` - 加载单个内容
- `createContent(contentData)` - 创建内容
- `updateContent(contentId, contentData)` - 更新内容
- `submitForReview(contentId)` - 提交审核
- `publishContent(contentId)` - 发布内容
- `clearCurrentContent()` - 清空当前内容
- `clearContents()` - 清空所有内容

#### 使用示例

```javascript
import { useContentStore } from '@/stores/content'

const contentStore = useContentStore()

// 加载内容列表
await contentStore.loadContents(0, 20)

// 创建内容
const content = await contentStore.createContent({
  templateId: 1,
  title: '标题',
  structuredData: {}
})

// 提交审核
await contentStore.submitForReview(contentId)
```

### 4. App Store (`stores/app.js`)

管理应用全局状态。

#### State
- `globalLoading` - 全局加载状态
- `sidebarCollapsed` - 侧边栏折叠状态
- `mobileSidebarVisible` - 移动端侧边栏显示状态
- `systemConfig` - 系统配置

#### Actions
- `setGlobalLoading(loading)` - 设置全局加载状态
- `toggleSidebar()` - 切换侧边栏
- `setSidebarCollapsed(collapsed)` - 设置侧边栏状态
- `toggleMobileSidebar()` - 切换移动端侧边栏
- `setMobileSidebarVisible(visible)` - 设置移动端侧边栏状态
- `updateSystemConfig(config)` - 更新系统配置

#### 使用示例

```javascript
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

// 显示全局加载
appStore.setGlobalLoading(true)

// 切换侧边栏
appStore.toggleSidebar()
```

## 导航辅助函数

系统提供了 `useNavigation` composable 用于简化路由导航。

### 使用示例

```javascript
import { useNavigation } from '@/composables/useNavigation'

const { goHome, goLogin, goContentDetail, goEditor } = useNavigation()

// 导航到首页
goHome()

// 导航到登录页（带重定向）
goLogin('/editor')

// 导航到内容详情
goContentDetail(123)

// 导航到编辑器
goEditor() // 新建
goEditor(456) // 编辑
```

### 可用方法

- `goHome()` - 导航到首页
- `goLogin(redirect)` - 导航到登录页
- `goRegister()` - 导航到注册页
- `goContentDetail(contentId)` - 导航到内容详情
- `goEditor(contentId)` - 导航到编辑器
- `goFavorites()` - 导航到收藏页
- `goNotifications()` - 导航到通知页
- `goAdmin(section)` - 导航到管理后台
- `goBack()` - 返回上一页

## 应用初始化流程

1. **创建 Vue 应用**
   ```javascript
   const app = createApp(App)
   ```

2. **注册 Pinia**
   ```javascript
   const pinia = createPinia()
   app.use(pinia)
   ```

3. **注册 Router**
   ```javascript
   app.use(router)
   ```

4. **挂载应用**
   ```javascript
   app.mount('#app')
   ```

5. **初始化认证**（在 App.vue 的 onMounted 中）
   ```javascript
   await authStore.initializeAuth()
   ```

## 认证流程

### 登录流程

1. 用户在登录页输入邮箱和密码
2. 调用 `authStore.login(credentials)`
3. 后端验证成功后返回 token 和用户信息
4. Store 保存 token 到 localStorage 和内存
5. 重定向到原始目标页面或首页

### 认证恢复流程

1. 应用启动时检查 localStorage 中的 token
2. 如果存在 token，调用 `authStore.initializeAuth()`
3. 使用 token 请求用户信息
4. 成功则恢复用户状态，失败则清除 token

### 登出流程

1. 调用 `authStore.logout()`
2. 清除 localStorage 中的 token
3. 清除内存中的用户信息
4. 重定向到登录页

## 错误处理

### Axios 拦截器

系统在 `api/axios.js` 中配置了全局拦截器：

#### 请求拦截器
- 自动添加 Authorization header
- 格式：`Bearer {token}`

#### 响应拦截器
- 统一处理错误响应
- 401: 清除认证信息，重定向到登录页
- 403: 显示权限错误提示
- 404: 显示资源不存在提示
- 500: 显示服务器错误提示

### 路由错误处理

```javascript
router.onError((error) => {
  console.error('路由错误:', error)
  ElMessage.error('页面加载失败，请刷新重试')
})
```

## 最佳实践

### 1. Store 使用

```javascript
// ✅ 推荐：在 setup 中使用
import { useAuthStore } from '@/stores/auth'

export default {
  setup() {
    const authStore = useAuthStore()
    return { authStore }
  }
}

// ❌ 不推荐：在 setup 外使用
const authStore = useAuthStore() // 可能导致问题
```

### 2. 路由导航

```javascript
// ✅ 推荐：使用 composable
import { useNavigation } from '@/composables/useNavigation'
const { goHome } = useNavigation()
goHome()

// ✅ 也可以：使用 router
import { useRouter } from 'vue-router'
const router = useRouter()
router.push({ name: 'home' })
```

### 3. 认证检查

```javascript
// ✅ 推荐：使用 computed
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const canEdit = computed(() => authStore.isAuthenticated && !authStore.isPending)

// ❌ 不推荐：直接访问
if (authStore.user && authStore.user.status !== 'PENDING') {
  // ...
}
```

### 4. 异步操作

```javascript
// ✅ 推荐：使用 try-catch
try {
  await authStore.login(credentials)
  ElMessage.success('登录成功')
  goHome()
} catch (error) {
  ElMessage.error('登录失败')
}

// ❌ 不推荐：不处理错误
await authStore.login(credentials)
goHome()
```

## 测试

所有路由和状态管理功能都有对应的单元测试：

- `frontend/src/views/__tests__/` - 视图组件测试
- `frontend/src/components/__tests__/` - 组件测试

运行测试：

```bash
npm test
```

## 相关文件

### 路由
- `frontend/src/router/index.js` - 路由配置
- `frontend/src/views/NotFoundView.vue` - 404 页面
- `frontend/src/composables/useNavigation.js` - 导航辅助函数

### 状态管理
- `frontend/src/stores/auth.js` - 认证 Store
- `frontend/src/stores/notification.js` - 通知 Store
- `frontend/src/stores/content.js` - 内容 Store
- `frontend/src/stores/app.js` - 应用 Store
- `frontend/src/stores/index.js` - Store 统一导出

### API
- `frontend/src/api/axios.js` - Axios 配置和拦截器
- `frontend/src/api/auth.js` - 认证 API
- `frontend/src/api/content.js` - 内容 API
- `frontend/src/api/notification.js` - 通知 API

### 应用入口
- `frontend/src/main.js` - 应用入口
- `frontend/src/App.vue` - 根组件
