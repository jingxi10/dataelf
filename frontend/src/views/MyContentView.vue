<template>
  <div class="my-content-view">
    <header class="header">
      <div class="container">
        <div class="logo" @click="goHome">
          <img v-if="logoUrl" :src="logoUrl" alt="数流精灵" class="logo-image" />
          <h1 v-else class="logo-text">数流精灵</h1>
        </div>
        <nav class="nav">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/editor" class="nav-link">创建内容</router-link>
          <router-link to="/my-content" class="nav-link active">我的内容</router-link>
          <router-link to="/favorites" class="nav-link">我的收藏</router-link>
          <router-link v-if="isAdmin" to="/admin/users" class="nav-link">管理后台</router-link>
          <router-link v-if="!isAdmin" to="/admin/profile" class="nav-link">个人中心</router-link>
          <NotificationDropdown />
          <button @click="handleLogout" class="nav-link">退出</button>
        </nav>
      </div>
    </header>

    <main class="main">
      <div class="container">
        <div class="page-header">
          <h2 class="page-title">我的内容</h2>
          <router-link to="/editor" class="create-btn">
            <span class="icon">+</span>
            创建新内容
          </router-link>
        </div>

        <!-- 标签页 -->
        <div class="tabs">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            class="tab"
            :class="{ active: activeTab === tab.key }"
            @click="changeTab(tab.key)"
          >
            {{ tab.label }}
            <span v-if="tab.count !== null" class="count">{{ tab.count }}</span>
          </button>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <p>加载中...</p>
        </div>

        <!-- 空状态 -->
        <div v-else-if="contents.length === 0" class="empty-state">
          <div class="empty-icon">📝</div>
          <p class="empty-text">{{ emptyText }}</p>
          <router-link v-if="activeTab === 'all'" to="/editor" class="empty-action">
            创建第一篇内容
          </router-link>
        </div>

        <!-- 内容列表 -->
        <div v-else class="content-list">
          <div
            v-for="content in contents"
            :key="content.id"
            class="content-item"
          >
            <div class="content-main">
              <h3 class="content-title" @click="viewContent(content.id)">
                {{ content.title }}
              </h3>
              <div class="content-meta">
                <span class="meta-item">
                  <span class="icon">📄</span>
                  {{ content.templateName }}
                </span>
                <span class="meta-item">
                  <span class="icon">📅</span>
                  {{ formatDate(content.createdAt) }}
                </span>
                <span class="meta-item">
                  <span class="icon">📊</span>
                  完整度: {{ content.integrityScore }}%
                </span>
                <span class="status-badge" :class="getStatusClass(content.status)">
                  {{ getStatusText(content.status) }}
                </span>
              </div>
            </div>
            <div class="content-actions">
              <button
                v-if="content.status === 'DRAFT'"
                @click="editContent(content.id)"
                class="action-btn primary"
              >
                编辑
              </button>
              <button
                v-if="content.status === 'DRAFT'"
                @click="submitContent(content.id)"
                class="action-btn"
              >
                提交审核
              </button>
              <button
                v-if="content.status === 'APPROVED'"
                @click="publishContent(content.id)"
                class="action-btn success"
              >
                发布
              </button>
              <button
                v-if="content.status === 'PUBLISHED'"
                @click="viewContent(content.id)"
                class="action-btn"
              >
                查看
              </button>
              <button
                v-if="content.status === 'PUBLISHED'"
                @click="unpublishContent(content.id)"
                class="action-btn warning"
              >
                下架
              </button>
              <button
                @click="deleteContent(content.id)"
                class="action-btn danger"
              >
                删除
              </button>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="totalPages > 1" class="pagination">
          <button
            class="page-btn"
            :disabled="currentPage === 0"
            @click="goToPage(currentPage - 1)"
          >
            上一页
          </button>

          <div class="page-numbers">
            <button
              v-for="page in visiblePages"
              :key="page"
              class="page-number"
              :class="{ active: page === currentPage }"
              @click="goToPage(page)"
            >
              {{ page + 1 }}
            </button>
          </div>

          <button
            class="page-btn"
            :disabled="currentPage === totalPages - 1"
            @click="goToPage(currentPage + 1)"
          >
            下一页
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getMyContents,
  getMyDrafts,
  getMyPublishedContents,
  getMyPendingContents,
  submitForReview,
  publishContent as publishContentApi,
  deleteContent as deleteContentApi,
  unpublishContent as unpublishContentApi
} from '@/api/content'
import { getPublicConfig } from '@/api/system'
import NotificationDropdown from '@/components/NotificationDropdown.vue'

const router = useRouter()
const authStore = useAuthStore()

const logoUrl = ref('')
const loading = ref(false)
const contents = ref([])
const activeTab = ref('all')
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const pageSize = 20

const isAdmin = computed(() => authStore.isAdmin)

const tabs = ref([
  { key: 'all', label: '全部', count: null },
  { key: 'drafts', label: '草稿', count: null },
  { key: 'pending', label: '待审核', count: null },
  { key: 'published', label: '已发布', count: null }
])

const emptyText = computed(() => {
  const texts = {
    all: '还没有任何内容',
    drafts: '没有草稿',
    pending: '没有待审核的内容',
    published: '还没有发布任何内容'
  }
  return texts[activeTab.value] || '暂无内容'
})

const visiblePages = computed(() => {
  const pages = []
  const maxVisible = 5
  let start = Math.max(0, currentPage.value - Math.floor(maxVisible / 2))
  let end = Math.min(totalPages.value - 1, start + maxVisible - 1)

  if (end - start < maxVisible - 1) {
    start = Math.max(0, end - maxVisible + 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})

const loadContents = async (page = 0) => {
  loading.value = true
  try {
    const params = {
      page,
      size: pageSize,
      sort: 'createdAt,desc'
    }

    let response
    switch (activeTab.value) {
      case 'drafts':
        response = await getMyDrafts(params)
        break
      case 'pending':
        response = await getMyPendingContents(params)
        break
      case 'published':
        response = await getMyPublishedContents(params)
        break
      default:
        response = await getMyContents(params)
    }

    // axios拦截器已经解包
    const data = response.content ? response : (response.data || response)
    contents.value = data.content || []
    currentPage.value = data.number || page
    totalPages.value = data.totalPages || 1
    totalElements.value = data.totalElements || 0
  } catch (error) {
    console.error('Failed to load contents:', error)
    ElMessage.error('加载内容失败')
  } finally {
    loading.value = false
  }
}

const changeTab = (tab) => {
  activeTab.value = tab
  currentPage.value = 0
  loadContents(0)
}

const goToPage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
    loadContents(page)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

const editContent = (id) => {
  router.push({ name: 'editor-edit', params: { id } })
}

const viewContent = (id) => {
  router.push({ name: 'content-detail', params: { id } })
}

const submitContent = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要提交此内容进行审核吗？',
      '提交审核',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await submitForReview(id)
    ElMessage.success('提交成功，请等待审核')
    loadContents(currentPage.value)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to submit content:', error)
      ElMessage.error('提交失败')
    }
  }
}

const publishContent = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要发布此内容吗？',
      '发布内容',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    await publishContentApi(id)
    ElMessage.success('发布成功')
    loadContents(currentPage.value)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to publish content:', error)
      ElMessage.error('发布失败')
    }
  }
}

// 下架内容
const unpublishContent = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要下架此内容吗？下架后内容将不再公开显示。',
      '下架确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await unpublishContentApi(id)
    ElMessage.success('下架成功')
    loadContents(currentPage.value)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to unpublish content:', error)
      ElMessage.error(error.response?.data?.error?.message || error.response?.data?.message || '下架失败')
    }
  }
}

const deleteContent = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除此内容吗？此操作不可恢复。',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    await deleteContentApi(id)
    ElMessage.success('删除成功')
    loadContents(currentPage.value)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete content:', error)
      ElMessage.error(error.response?.data?.error?.message || error.response?.data?.message || '删除失败')
    }
  }
}

const getStatusClass = (status) => {
  const classes = {
    DRAFT: 'draft',
    PENDING_REVIEW: 'pending',
    APPROVED: 'approved',
    PUBLISHED: 'published',
    REJECTED: 'rejected'
  }
  return classes[status] || ''
}

const getStatusText = (status) => {
  const texts = {
    DRAFT: '草稿',
    PENDING_REVIEW: '待审核',
    APPROVED: '已批准',
    PUBLISHED: '已发布',
    REJECTED: '已拒绝'
  }
  return texts[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const goHome = () => {
  router.push('/')
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

const loadConfig = async () => {
  try {
    const response = await getPublicConfig()
    // axios拦截器已经解包
    const config = response.data ? response.data : response
    logoUrl.value = config.logoUrl || ''
  } catch (error) {
    console.error('Failed to load config:', error)
  }
}

onMounted(() => {
  loadConfig()
  loadContents()
})
</script>

<style scoped>
.my-content-view {
  min-height: 100vh;
  background: #FAFAFA;
}

.header {
  background: #FFFFFF;
  border-bottom: 1px solid #E8EAED;
  padding: 1rem 0;
  position: sticky;
  top: 0;
  z-index: 200;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.header .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  cursor: pointer;
  transition: opacity 0.3s;
}

.logo:hover {
  opacity: 0.8;
}

.logo-text {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1A73E8;
  margin: 0;
}

.logo-image {
  height: 40px;
  object-fit: contain;
}

.nav {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.nav-link {
  padding: 0.5rem 1rem;
  text-decoration: none;
  color: #333;
  border-radius: 4px;
  transition: all 0.3s;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 0.9375rem;
  font-weight: 500;
}

.nav-link:hover {
  background: #F8F9FA;
}

.nav-link.active {
  background: #E8F0FE;
  color: #1A73E8;
}

.main {
  padding: 2rem 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  margin: 0;
  color: #333;
  font-weight: 600;
}

.create-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: #1A73E8;
  color: #FFFFFF;
  text-decoration: none;
  border-radius: 4px;
  font-weight: 500;
  transition: all 0.3s;
}

.create-btn:hover {
  background: #4285F4;
}

.create-btn .icon {
  font-size: 1.25rem;
  font-weight: 600;
}

.tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  border-bottom: 2px solid #E8EAED;
}

.tab {
  padding: 0.75rem 1.5rem;
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  color: #666;
  cursor: pointer;
  font-size: 0.9375rem;
  font-weight: 500;
  transition: all 0.3s;
  margin-bottom: -2px;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.tab:hover {
  color: #1A73E8;
}

.tab.active {
  color: #1A73E8;
  border-bottom-color: #1A73E8;
}

.tab .count {
  background: #E8EAED;
  color: #666;
  padding: 0.125rem 0.5rem;
  border-radius: 12px;
  font-size: 0.75rem;
}

.tab.active .count {
  background: #E8F0FE;
  color: #1A73E8;
}

.loading-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #666;
}

.spinner {
  width: 40px;
  height: 40px;
  margin: 0 auto 1rem;
  border: 3px solid #E8EAED;
  border-top-color: #1A73E8;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: #FFFFFF;
  border-radius: 8px;
  border: 1px solid #E8EAED;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-text {
  font-size: 1.125rem;
  color: #666;
  margin: 0 0 1.5rem 0;
}

.empty-action {
  display: inline-block;
  padding: 0.75rem 1.5rem;
  background: #1A73E8;
  color: #FFFFFF;
  text-decoration: none;
  border-radius: 4px;
  font-weight: 500;
  transition: all 0.3s;
}

.empty-action:hover {
  background: #4285F4;
}

.content-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.content-item {
  background: #FFFFFF;
  border: 1px solid #E8EAED;
  border-radius: 8px;
  padding: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s;
}

.content-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.content-main {
  flex: 1;
}

.content-title {
  font-size: 1.125rem;
  margin: 0 0 0.75rem 0;
  color: #333;
  cursor: pointer;
  transition: color 0.3s;
}

.content-title:hover {
  color: #1A73E8;
}

.content-meta {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
  align-items: center;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.875rem;
  color: #666;
}

.meta-item .icon {
  font-size: 1rem;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-badge.draft {
  background: #F8F9FA;
  color: #666;
}

.status-badge.pending {
  background: #FFF3E0;
  color: #F57C00;
}

.status-badge.approved {
  background: #E8F5E9;
  color: #2E7D32;
}

.status-badge.published {
  background: #E3F2FD;
  color: #1976D2;
}

.status-badge.rejected {
  background: #FFEBEE;
  color: #C62828;
}

.content-actions {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  padding: 0.5rem 1rem;
  border: 1px solid #E8EAED;
  background: #FFFFFF;
  color: #333;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.3s;
}

.action-btn:hover {
  background: #F8F9FA;
}

.action-btn.primary {
  background: #1A73E8;
  color: #FFFFFF;
  border-color: #1A73E8;
}

.action-btn.primary:hover {
  background: #4285F4;
}

.action-btn.success {
  background: #34A853;
  color: #FFFFFF;
  border-color: #34A853;
}

.action-btn.success:hover {
  background: #46B864;
}

.action-btn.warning {
  color: #FF9800;
  border-color: #FF9800;
}

.action-btn.warning:hover {
  background: #FFF3E0;
}

.action-btn.danger {
  color: #EA4335;
  border-color: #EA4335;
}

.action-btn.danger:hover {
  background: #FFEBEE;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  margin-top: 2rem;
}

.page-btn {
  padding: 0.5rem 1rem;
  border: 1px solid #E8EAED;
  background: #FFFFFF;
  color: #333;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.9375rem;
}

.page-btn:hover:not(:disabled) {
  background: #F8F9FA;
  border-color: #1A73E8;
  color: #1A73E8;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 0.25rem;
}

.page-number {
  min-width: 40px;
  padding: 0.5rem;
  border: 1px solid #E8EAED;
  background: #FFFFFF;
  color: #333;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.9375rem;
}

.page-number:hover {
  background: #F8F9FA;
  border-color: #1A73E8;
  color: #1A73E8;
}

.page-number.active {
  background: #1A73E8;
  color: #FFFFFF;
  border-color: #1A73E8;
}

@media (max-width: 768px) {
  .container {
    padding: 0 1rem;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .tabs {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .content-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .content-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .action-btn {
    flex: 1;
  }

  .page-numbers {
    display: none;
  }
}
</style>
