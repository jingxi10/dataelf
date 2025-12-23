import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue'),
      meta: { title: '首页' }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { title: '登录', guest: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/auth/RegisterView.vue'),
      meta: { title: '注册', guest: true }
    },
    {
      path: '/content/:id',
      name: 'content-detail',
      component: () => import('@/views/content/ContentDetailView.vue'),
      meta: { title: '内容详情' }
    },
    {
      path: '/favorites',
      name: 'favorites',
      component: () => import('@/views/FavoritesView.vue'),
      meta: { requiresAuth: true, title: '我的收藏' }
    },
    {
      path: '/my-content',
      name: 'my-content',
      component: () => import('@/views/MyContentView.vue'),
      meta: { requiresAuth: true, title: '我的内容' }
    },
    {
      path: '/notifications',
      name: 'notifications',
      component: () => import('@/views/NotificationsView.vue'),
      meta: { requiresAuth: true, title: '通知中心' }
    },
    {
      path: '/editor',
      name: 'editor',
      component: () => import('@/views/editor/EditorView.vue'),
      meta: { requiresAuth: true, title: '内容编辑器' }
    },
    {
      path: '/editor/:id',
      name: 'editor-edit',
      component: () => import('@/views/editor/EditorView.vue'),
      meta: { requiresAuth: true, title: '编辑内容' }
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/admin/AdminLayout.vue'),
      meta: { requiresAuth: true, title: '管理后台' },
      redirect: { name: 'admin-profile' },
      children: [
        {
          path: 'profile',
          name: 'admin-profile',
          component: () => import('@/views/admin/ProfileView.vue'),
          meta: { title: '个人信息' }
        },
        {
          path: 'my-contents',
          name: 'admin-my-contents',
          component: () => import('@/views/admin/MyContentsManagement.vue'),
          meta: { title: '内容管理' }
        },
        {
          path: 'users',
          name: 'admin-users',
          component: () => import('@/views/admin/UserManagement.vue'),
          meta: { title: '用户管理', requiresAdmin: true }
        },
        {
          path: 'editor',
          name: 'admin-editor',
          component: () => import('@/views/editor/EditorView.vue'),
          meta: { title: '写文章' }
        },
        {
          path: 'templates',
          name: 'admin-templates',
          component: () => import('@/views/admin/TemplateManagement.vue'),
          meta: { title: '模板管理', requiresAdmin: true }
        },
        {
          path: 'review',
          name: 'admin-review',
          component: () => import('@/views/admin/ContentReview.vue'),
          meta: { title: '内容审核', requiresAdmin: true }
        },
        {
          path: 'settings',
          name: 'admin-settings',
          component: () => import('@/views/admin/SystemSettings.vue'),
          meta: { title: '系统设置', requiresAdmin: true }
        },
        {
          path: 'categories',
          name: 'admin-categories',
          component: () => import('@/views/admin/CategoryManagement.vue'),
          meta: { title: '分类管理', requiresAdmin: true }
        },
        {
          path: 'tags',
          name: 'admin-tags',
          component: () => import('@/views/admin/TagManagement.vue'),
          meta: { title: '标签管理', requiresAdmin: true }
        },
        {
          path: 'data-sources',
          name: 'admin-data-sources',
          component: () => import('@/views/admin/DataSourceManagement.vue'),
          meta: { title: '数据源管理', requiresAdmin: true }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue'),
      meta: { title: '页面未找到' }
    }
  ]
})

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 等待认证初始化完成
  if (!authStore.initialized) {
    await authStore.initializeAuth()
  }
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 数流精灵`
  } else {
    document.title = '数流精灵'
  }
  
  // 已登录用户访问登录/注册页面，重定向到首页
  if (to.meta.guest && authStore.isAuthenticated) {
    next({ name: 'home' })
    return
  }
  
  // 需要认证的路由
  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) {
      ElMessage.warning('请先登录')
      next({ 
        name: 'login', 
        query: { redirect: to.fullPath } 
      })
      return
    }
    
    // 检查账号状态
    if (authStore.isPending) {
      ElMessage.warning('您的账号正在审核中，请耐心等待')
      next({ name: 'home' })
      return
    }
    
    if (authStore.isExpired) {
      ElMessage.error('您的账号已过期，请联系管理员')
      next({ name: 'home' })
      return
    }
  }
  
  // 需要管理员权限的路由
  if (to.meta.requiresAdmin) {
    if (!authStore.isAdmin) {
      ElMessage.error('您没有权限访问此页面')
      next({ name: 'admin-profile' })
      return
    }
  }
  
  next()
})

// 全局后置守卫
router.afterEach((to, from) => {
  // 页面切换后滚动到顶部
  window.scrollTo(0, 0)
})

// 路由错误处理
router.onError((error) => {
  console.error('路由错误:', error)
  ElMessage.error('页面加载失败，请刷新重试')
})

export default router
