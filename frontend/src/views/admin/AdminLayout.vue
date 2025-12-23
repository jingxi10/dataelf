<template>
  <div class="admin-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px" class="admin-sidebar">
        <div class="admin-logo">
          <h2>{{ isAdmin ? '管理后台' : '个人中心' }}</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          class="admin-menu"
        >
          <!-- 所有用户可见 -->
          <el-menu-item index="/admin/profile">
            <el-icon><UserFilled /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="/admin/my-contents">
            <el-icon><Document /></el-icon>
            <span>内容管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/editor">
            <el-icon><Edit /></el-icon>
            <span>写文章</span>
          </el-menu-item>
          
          <!-- 仅管理员可见 -->
          <template v-if="isAdmin">
            <el-divider style="margin: 12px 0; border-color: #4a5568;" />
            <el-menu-item index="/admin/users">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/templates">
              <el-icon><Files /></el-icon>
              <span>模板管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/review">
              <el-icon><Check /></el-icon>
              <span>内容审核</span>
            </el-menu-item>
            <el-menu-item index="/admin/categories">
              <el-icon><Folder /></el-icon>
              <span>分类管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/tags">
              <el-icon><PriceTag /></el-icon>
              <span>标签管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/data-sources">
              <el-icon><Connection /></el-icon>
              <span>数据源管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/settings">
              <el-icon><Setting /></el-icon>
              <span>系统设置</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <el-header class="admin-header">
          <div class="header-left">
            <span class="page-title">{{ pageTitle }}</span>
          </div>
          <div class="header-right">
            <NotificationDropdown style="margin-right: 16px;" />
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-icon><Avatar /></el-icon>
                {{ authStore.user?.nickname || authStore.user?.email || '用户' }}
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="home">返回首页</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <el-main class="admin-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { User, UserFilled, Document, Check, Avatar, Setting, Folder, PriceTag, Connection, Edit, Files } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import NotificationDropdown from '@/components/NotificationDropdown.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeMenu = computed(() => route.path)
const isAdmin = computed(() => authStore.isAdmin)

const pageTitle = computed(() => {
  const titles = {
    '/admin/profile': '个人信息',
    '/admin/my-contents': '内容管理',
    '/admin/users': '用户管理',
    '/admin/editor': '写文章',
    '/admin/templates': '模板管理',
    '/admin/review': '内容审核',
    '/admin/categories': '分类管理',
    '/admin/tags': '标签管理',
    '/admin/data-sources': '数据源管理',
    '/admin/settings': '系统设置'
  }
  return titles[route.path] || (isAdmin.value ? '管理后台' : '个人中心')
})

const handleCommand = (command) => {
  if (command === 'logout') {
    authStore.clearAuth()
    ElMessage.success('已退出登录')
    router.push('/login')
  } else if (command === 'home') {
    router.push('/')
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
}

.el-container {
  height: 100%;
}

.admin-sidebar {
  background-color: #304156;
  color: #fff;
}

.admin-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #263445;
}

.admin-logo h2 {
  margin: 0;
  font-size: 18px;
  color: #fff;
}

.admin-menu {
  border-right: none;
  background-color: #304156;
}

.admin-menu .el-menu-item {
  color: #bfcbd9;
}

.admin-menu .el-menu-item:hover,
.admin-menu .el-menu-item.is-active {
  background-color: #263445;
  color: #409eff;
}

.admin-header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #606266;
}

.user-info:hover {
  color: #409eff;
}

.admin-main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
