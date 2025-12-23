<template>
  <div class="home">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-container">
        <div class="logo" @click="goHome">
          <img v-if="logoUrl" :src="logoUrl" alt="数流精灵" class="logo-image" />
          <span class="logo-text">{{ siteName }}</span>
        </div>
        <nav class="nav">
          <router-link to="/editor" class="nav-link">创建内容</router-link>
          <router-link to="/my-content" class="nav-link">我的内容</router-link>
          <router-link to="/favorites" class="nav-link">我的收藏</router-link>
          <router-link v-if="isAuthenticated && isAdmin" to="/admin/users" class="nav-link">管理后台</router-link>
          <router-link v-if="isAuthenticated && !isAdmin" to="/admin/profile" class="nav-link">个人中心</router-link>
          <NotificationDropdown v-if="isAuthenticated" />
          <template v-if="!isAuthenticated">
            <router-link to="/login" class="nav-link">登录</router-link>
            <router-link to="/register" class="nav-link btn-primary">注册</router-link>
          </template>
          <!-- 用户信息下拉菜单 -->
          <el-dropdown v-else trigger="click" @command="handleUserCommand">
            <span class="user-dropdown">
              <el-avatar :size="32" :icon="UserFilled" />
              <span class="user-name">{{ userName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  <div class="user-info-item">
                    <el-icon><User /></el-icon>
                    <span>{{ userEmail }}</span>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item disabled>
                  <div class="vip-info-item" :class="getVipStatusClass(accountExpireAt)">
                    <el-icon class="vip-icon"><Trophy /></el-icon>
                    <span class="vip-label">VIP到期：</span>
                    <span class="vip-time">{{ formatExpireDate(accountExpireAt) }}</span>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </nav>
      </div>
    </header>

    <main class="main">
      <!-- Hero 区域 -->
      <section class="hero-section" :style="heroStyle">
        <h1 class="hero-title">{{ heroTitle }}</h1>
        <p class="hero-subtitle">{{ heroSubtitle }}</p>
        <p v-if="siteDescription" class="hero-description">{{ siteDescription }}</p>
      </section>

      <!-- 搜索区域 -->
      <section class="search-section">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索AI数据、知识文档..."
            size="large"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" type="primary" @click="handleSearch" />
            </template>
          </el-input>
        </div>
        <!-- 热门搜索 -->
        <div class="hot-tags">
          <span class="hot-label">热门搜索：</span>
          <el-tag 
            v-for="tag in hotTags" 
            :key="tag" 
            class="hot-tag"
            @click="searchByTag(tag)"
          >
            {{ tag }}
          </el-tag>
        </div>
      </section>

      <!-- 内容类目 -->
      <section class="category-section">
        <h3 class="section-title">内容类目</h3>
        <p class="section-desc">选择感兴趣的类目浏览相关内容</p>
        
        <div class="category-list">
          <div 
            class="category-item"
            :class="{ active: !selectedCategory }"
            @click="selectCategory(null)"
          >
            <el-icon class="category-icon"><Grid /></el-icon>
            <span class="category-name">全部</span>
            <span class="category-count">{{ totalElements }}</span>
          </div>
          <div 
            v-for="cat in categories" 
            :key="cat.id"
            class="category-item"
            :class="{ active: selectedCategory === cat.id }"
            @click="selectCategory(cat.id)"
          >
            <el-icon class="category-icon">
              <component :is="getCategoryIcon(cat.name)" />
            </el-icon>
            <span class="category-name">{{ cat.name }}</span>
            <span class="category-count">{{ cat.contentCount || 0 }}</span>
          </div>
        </div>

        <div class="category-stats">
          共 <span class="highlight">{{ categories.length }}</span> 个类目，
          <span class="highlight">{{ totalElements }}</span> 篇内容
        </div>
      </section>

      <!-- 热门内容 -->
      <section class="content-section">
        <h3 class="section-title center">热门内容</h3>
        
        <!-- Loading -->
        <div v-if="loading" class="loading-state">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <p>加载中...</p>
        </div>

        <!-- Empty -->
        <div v-else-if="contents.length === 0" class="empty-state">
          <el-empty description="暂无内容">
            <router-link v-if="isAuthenticated" to="/editor">
              <el-button type="primary">创建内容</el-button>
            </router-link>
          </el-empty>
        </div>

        <!-- Content Grid -->
        <div v-else class="content-grid">
          <div 
            v-for="content in contents" 
            :key="content.id" 
            class="content-card"
            @click="viewContent(content.id)"
          >
            <div class="card-header">
              <el-tag size="small" type="info">{{ getCategoryName(content.categoryIds) }}</el-tag>
            </div>
            <h4 class="card-title">{{ content.title }}</h4>
            <p class="card-desc">{{ getContentSummary(content) }}</p>
            <div class="card-tags">
              <span v-for="tag in getContentTags(content)" :key="tag" class="tag-item">
                #{{ tag }}
              </span>
            </div>
            <div class="card-footer">
              <span class="card-date">{{ formatDate(content.publishedAt) }}</span>
              <span class="card-author">{{ content.authorName || '匿名' }}</span>
              <div class="card-actions">
                <span class="action-item">
                  <el-icon><Star /></el-icon> 点赞
                </span>
                <span class="action-item">
                  <el-icon><Collection /></el-icon> 收藏
                </span>
                <span class="action-item">
                  <el-icon><ChatDotRound /></el-icon> 评论
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Load More -->
        <div v-if="hasMore && contents.length > 0" class="load-more">
          <el-button @click="loadMore">
            加载更多内容
          </el-button>
        </div>
      </section>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-container">
        <div class="footer-grid">
          <!-- 品牌信息 -->
          <div class="footer-brand">
            <h4>{{ siteName }}</h4>
            <p>{{ siteDescription || '专为AI优化的智能结构化数据平台' }}</p>
            <div class="social-links">
              <a href="#" class="social-link"><el-icon><Share /></el-icon></a>
              <a href="#" class="social-link"><el-icon><ChatDotRound /></el-icon></a>
              <a href="#" class="social-link"><el-icon><Link /></el-icon></a>
            </div>
          </div>
          
          <!-- 动态页脚链接组 -->
          <div v-for="group in footerLinkGroups" :key="group.group" class="footer-links">
            <h5>{{ group.group }}</h5>
            <a v-for="link in group.links" :key="link.name" :href="link.url">{{ link.name }}</a>
          </div>
        </div>
        
        <div class="footer-bottom">
          <p>© 2024 {{ siteName }}. 保留所有权利</p>
          <div class="footer-bottom-links">
            <a v-for="link in footerBottomLinks" :key="link.name" :href="link.url">{{ link.name }}</a>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getPublishedContents, getContentsByCategory, searchContents } from '@/api/content'
import { getAllCategories } from '@/api/category'
import { getPopularTags } from '@/api/tag'
import { getPublicConfig } from '@/api/system'
import NotificationDropdown from '@/components/NotificationDropdown.vue'
import { 
  Search, Grid, Cpu, DataAnalysis, Connection, SetUp, 
  Monitor, MagicStick, Cloudy, Star, Collection, 
  ChatDotRound, Loading, Share, Link, UserFilled, ArrowDown,
  User, Timer, SwitchButton, Trophy
} from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

// 状态
const logoUrl = ref('')
const contents = ref([])
const categories = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const selectedCategory = ref(null)
const searchKeyword = ref('')
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const pageSize = 8

// 站点配置
const siteName = ref('数流精灵')
const siteDescription = ref('')
const heroTitle = ref('去伪存真、建立AI秩序')
const heroSubtitle = ref('专为AI优化的结构化数据平台')
const heroBgColor = ref('')  // Hero 背景颜色
const heroTextColor = ref('')  // Hero 文字颜色

// 页脚链接配置
const footerLinkGroups = ref([
  { group: '产品功能', links: [{ name: '结构化模板', url: '#' }, { name: '内容编辑器', url: '#' }, { name: '数据探索', url: '#' }, { name: 'API文档', url: '#' }] },
  { group: '资源中心', links: [{ name: '帮助文档', url: '#' }, { name: '教程指南', url: '#' }, { name: '博客文章', url: '#' }, { name: '常见问题', url: '#' }] },
  { group: '关于我们', links: [{ name: '公司介绍', url: '#' }, { name: '联系我们', url: '#' }, { name: '隐私政策', url: '#' }, { name: '服务条款', url: '#' }] }
])

// 页脚底部链接配置
const footerBottomLinks = ref([
  { name: '网站地图', url: '#' },
  { name: 'AI数据接口', url: '#' },
  { name: '机器人协议', url: '#' }
])

// Hero 区域动态样式
const heroStyle = computed(() => {
  const style = {}
  if (heroBgColor.value) {
    style.background = heroBgColor.value
  }
  if (heroTextColor.value) {
    style.color = heroTextColor.value
  }
  return style
})

// 热门搜索标签（动态加载）
const hotTags = ref([])

// 加载热门标签
const loadHotTags = async () => {
  try {
    const response = await getPopularTags()
    const tags = Array.isArray(response) ? response : (response.data || [])
    // 取前8个热门标签的名称
    hotTags.value = tags.slice(0, 8).map(tag => tag.name)
  } catch (error) {
    console.error('加载热门标签失败:', error)
    // 失败时使用默认标签
    hotTags.value = ['人工智能', '数据结构', '机器学习', 'Web3.0', '语义网']
  }
}

const isAuthenticated = computed(() => authStore.isAuthenticated)
const isAdmin = computed(() => authStore.isAdmin)
const isVipValid = computed(() => authStore.isVipValid)
const hasMore = computed(() => currentPage.value < totalPages.value - 1)

// 用户信息
const userName = computed(() => authStore.user?.name || authStore.user?.email?.split('@')[0] || '用户')
const userEmail = computed(() => authStore.user?.email || '')
const accountExpireAt = computed(() => authStore.user?.expiresAt || null)

// 格式化过期时间（精确到秒）
const formatExpireDate = (timestamp) => {
  if (!timestamp) return '永久'
  const date = new Date(timestamp)
  const now = new Date()
  if (date < now) return '已过期'
  // 检查是否是2037年（永久有效）
  if (date.getFullYear() >= 2037) return '永久'
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 获取VIP状态样式类
const getVipStatusClass = (timestamp) => {
  if (!timestamp) return 'vip-permanent'
  const date = new Date(timestamp)
  const now = new Date()
  if (date < now) return 'vip-expired'
  if (date.getFullYear() >= 2037) return 'vip-permanent'
  // 7天内到期
  const sevenDays = 7 * 24 * 60 * 60 * 1000
  if (date.getTime() - now.getTime() < sevenDays) return 'vip-expiring'
  return 'vip-active'
}

// 用户下拉菜单命令
const handleUserCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  }
}

// 分类图标映射
const categoryIconMap = {
  '科技': Cpu,
  '数据科学': DataAnalysis,
  '区块链': Connection,
  '数据架构': SetUp,
  'Web开发': Monitor,
  '人工智能': MagicStick,
  '云计算': Cloudy,
  'default': Grid
}

const getCategoryIcon = (name) => {
  return categoryIconMap[name] || categoryIconMap['default']
}

const getCategoryName = (categoryIds) => {
  if (!categoryIds || categoryIds.length === 0) return '未分类'
  const cat = categories.value.find(c => c.id === categoryIds[0])
  return cat ? cat.name : '未分类'
}

const getContentSummary = (content) => {
  if (content.structuredData?.articleBody) {
    const text = content.structuredData.articleBody.replace(/<[^>]+>/g, '')
    return text.length > 100 ? text.substring(0, 100) + '...' : text
  }
  return content.structuredData?.description || '暂无简介'
}

const getContentTags = (content) => {
  return content.tagNames?.slice(0, 4) || []
}

const formatDate = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 加载分类
const loadCategories = async () => {
  try {
    const response = await getAllCategories()
    categories.value = Array.isArray(response) ? response : (response.data || [])
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 加载内容
const loadContents = async (page = 0, append = false) => {
  if (append) {
    loadingMore.value = true
  } else {
    loading.value = true
  }
  
  try {
    const params = { page, size: pageSize, sort: 'publishedAt,desc' }
    
    let response
    if (selectedCategory.value) {
      response = await getContentsByCategory(selectedCategory.value, params)
    } else {
      response = await getPublishedContents(params)
    }
    
    const data = response.content ? response : (response.data || response)
    const newContents = data.content || data
    
    if (append) {
      contents.value = [...contents.value, ...newContents]
    } else {
      contents.value = newContents
    }
    
    currentPage.value = data.number || page
    totalPages.value = data.totalPages || 1
    totalElements.value = data.totalElements || newContents.length
  } catch (error) {
    console.error('加载内容失败:', error)
    contents.value = []
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 选择分类
const selectCategory = (categoryId) => {
  // 清空搜索状态
  searchKeyword.value = ''
  isSearchMode.value = false
  
  selectedCategory.value = categoryId
  currentPage.value = 0
  loadContents(0)
}

// 搜索
const isSearchMode = ref(false)

const handleSearch = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    // 清空搜索，返回正常列表
    isSearchMode.value = false
    selectedCategory.value = null
    loadContents(0)
    return
  }
  
  isSearchMode.value = true
  loading.value = true
  
  try {
    const params = { page: 0, size: pageSize, sort: 'publishedAt,desc' }
    const response = await searchContents(keyword, params)
    
    const data = response.content ? response : (response.data || response)
    contents.value = data.content || data
    currentPage.value = data.number || 0
    totalPages.value = data.totalPages || 1
    totalElements.value = data.totalElements || (data.content || data).length
  } catch (error) {
    console.error('搜索失败:', error)
    contents.value = []
  } finally {
    loading.value = false
  }
}

const searchByTag = (tag) => {
  searchKeyword.value = tag
  handleSearch()
}

// 加载更多
const loadMore = () => {
  if (isSearchMode.value) {
    // 搜索模式下加载更多
    loadMoreSearch()
  } else {
    loadContents(currentPage.value + 1, true)
  }
}

const loadMoreSearch = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) return
  
  loadingMore.value = true
  
  try {
    const params = { page: currentPage.value + 1, size: pageSize, sort: 'publishedAt,desc' }
    const response = await searchContents(keyword, params)
    
    const data = response.content ? response : (response.data || response)
    const newContents = data.content || data
    
    contents.value = [...contents.value, ...newContents]
    currentPage.value = data.number || currentPage.value + 1
    totalPages.value = data.totalPages || 1
    totalElements.value = data.totalElements || contents.value.length
  } catch (error) {
    console.error('加载更多搜索结果失败:', error)
  } finally {
    loadingMore.value = false
  }
}

// 查看内容
const viewContent = (id) => {
  router.push({ name: 'content-detail', params: { id } })
}

const goHome = () => {
  // 返回首页时清空搜索
  searchKeyword.value = ''
  isSearchMode.value = false
  selectedCategory.value = null
  loadContents(0)
  router.push('/')
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

// 创建内容（检查VIP状态）
const handleCreateContent = () => {
  if (!isVipValid.value) {
    ElMessageBox.confirm(
      '您的VIP已过期，无法创建内容。请联系管理员续费。',
      'VIP已过期',
      {
        confirmButtonText: '我知道了',
        showCancelButton: false,
        type: 'warning'
      }
    )
    return
  }
  router.push('/editor')
}

const loadConfig = async () => {
  try {
    const response = await getPublicConfig()
    const config = response.data ? response.data : response
    logoUrl.value = config.logoUrl || ''
    siteName.value = config.siteName || '数流精灵'
    siteDescription.value = config.siteDescription || ''
    heroTitle.value = config.heroTitle || '去伪存真、建立AI秩序'
    heroSubtitle.value = config.heroSubtitle || '专为AI优化的结构化数据平台'
    heroBgColor.value = config.heroBgColor || ''
    heroTextColor.value = config.heroTextColor || ''
    // 加载页脚链接配置
    if (config.footerLinks) {
      try {
        const links = typeof config.footerLinks === 'string' ? JSON.parse(config.footerLinks) : config.footerLinks
        if (Array.isArray(links) && links.length > 0) {
          footerLinkGroups.value = links
        }
      } catch (e) {
        console.error('解析页脚链接配置失败:', e)
      }
    }
    // 加载页脚底部链接配置
    if (config.footerBottomLinks) {
      try {
        const links = typeof config.footerBottomLinks === 'string' ? JSON.parse(config.footerBottomLinks) : config.footerBottomLinks
        if (Array.isArray(links) && links.length > 0) {
          footerBottomLinks.value = links
        }
      } catch (e) {
        console.error('解析页脚底部链接配置失败:', e)
      }
    }
  } catch (error) {
    console.error('加载配置失败:', error)
  }
}

onMounted(() => {
  loadConfig()
  loadCategories()
  loadHotTags()
  loadContents()
})
</script>


<style scoped>
.home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

/* 顶部导航 */
.header {
  background: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.logo-image {
  height: 36px;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: #1a73e8;
}

.nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-link {
  padding: 8px 16px;
  color: #333;
  text-decoration: none;
  font-size: 14px;
  border-radius: 6px;
  transition: all 0.2s;
  background: none;
  border: none;
  cursor: pointer;
}

.nav-link:hover {
  background: #f0f5ff;
  color: #1a73e8;
}

.nav-link.btn-primary {
  background: #1a73e8;
  color: #fff;
}

.nav-link.btn-primary:hover {
  background: #1557b0;
}

/* 用户下拉菜单 */
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: all 0.2s;
}

.user-dropdown:hover {
  background: #f0f5ff;
}

.user-name {
  font-size: 14px;
  color: #333;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
}

/* VIP 信息样式 */
.vip-info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  padding: 4px 0;
}

.vip-icon {
  font-size: 18px;
}

.vip-label {
  color: #666;
}

.vip-time {
  font-weight: 600;
  font-family: 'Monaco', 'Menlo', monospace;
}

/* VIP 永久 */
.vip-permanent .vip-icon {
  color: #ffd700;
}
.vip-permanent .vip-time {
  color: #ffd700;
  text-shadow: 0 0 8px rgba(255, 215, 0, 0.5);
}

/* VIP 正常 */
.vip-active .vip-icon {
  color: #67c23a;
}
.vip-active .vip-time {
  color: #67c23a;
}

/* VIP 即将到期（7天内） */
.vip-expiring .vip-icon {
  color: #e6a23c;
  animation: pulse 1.5s infinite;
}
.vip-expiring .vip-time {
  color: #e6a23c;
  font-weight: 700;
}

/* VIP 已过期 */
.vip-expired .vip-icon {
  color: #f56c6c;
}
.vip-expired .vip-time {
  color: #f56c6c;
  text-decoration: line-through;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* 主内容区 */
.main {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
  width: 100%;
  box-sizing: border-box;
}

/* Hero 区域 */
.hero-section {
  text-align: center;
  padding: 48px 24px 32px;
  background: linear-gradient(135deg, #f0f7ff 0%, #e8f4fd 100%);
  margin-bottom: 0;
}

.hero-logo {
  margin-bottom: 20px;
}

.hero-logo-image {
  max-height: 80px;
  max-width: 300px;
  object-fit: contain;
}

.hero-title {
  font-size: 32px;
  font-weight: 700;
  color: #1a73e8;
  margin: 0 0 12px;
}

.hero-subtitle {
  font-size: 18px;
  color: #666;
  margin: 0 0 8px;
}

.hero-description {
  font-size: 14px;
  color: #999;
  margin: 0;
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
}

/* 搜索区域 */
.search-section {
  text-align: center;
  padding-top: 32px;
  margin-bottom: 48px;
}

.search-box {
  max-width: 600px;
  margin: 0 auto 20px;
}

.search-box :deep(.el-input__wrapper) {
  border-radius: 24px;
  padding: 4px 4px 4px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.search-box :deep(.el-input-group__append) {
  border-radius: 0 24px 24px 0;
  background: #1a73e8;
  border: none;
  padding: 0 20px;
}

.search-box :deep(.el-input-group__append .el-button) {
  color: #fff;
}

.hot-tags {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.hot-label {
  color: #999;
  font-size: 13px;
}

.hot-tag {
  cursor: pointer;
  background: #f0f5ff;
  border: none;
  color: #1a73e8;
  transition: all 0.2s;
}

.hot-tag:hover {
  background: #1a73e8;
  color: #fff;
}

/* 分类区域 */
.category-section {
  margin-bottom: 48px;
  text-align: center;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px;
}

.section-title.center {
  text-align: center;
}

.section-desc {
  color: #999;
  font-size: 14px;
  margin: 0 0 24px;
}

.category-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
  justify-content: center;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 120px;
  justify-content: center;
}

.category-item:hover {
  border-color: #1a73e8;
  color: #1a73e8;
}

.category-item.active {
  background: #1a73e8;
  border-color: #1a73e8;
  color: #fff;
}

.category-item.active .category-icon,
.category-item.active .category-name,
.category-item.active .category-count {
  color: #fff;
}

.category-icon {
  font-size: 18px;
  color: #666;
}

.category-name {
  font-size: 14px;
  color: #333;
}

.category-count {
  font-size: 12px;
  color: #999;
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 10px;
}

.category-item.active .category-count {
  background: rgba(255, 255, 255, 0.2);
}

.category-stats {
  font-size: 13px;
  text-align: center;
  color: #999;
}

.category-stats .highlight {
  color: #1a73e8;
  font-weight: 500;
}

/* 内容区域 */
.content-section {
  margin-bottom: 48px;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.loading-icon {
  font-size: 32px;
  color: #1a73e8;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 内容卡片网格 */
.content-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

@media (max-width: 768px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

.content-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e8e8e8;
}

.content-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.card-header {
  margin-bottom: 12px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.tag-item {
  font-size: 12px;
  color: #1a73e8;
  background: #f0f5ff;
  padding: 2px 8px;
  border-radius: 4px;
}

.card-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #999;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

.card-date {
  flex-shrink: 0;
}

.card-author {
  flex-shrink: 0;
}

.card-actions {
  display: flex;
  gap: 12px;
  margin-left: auto;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #999;
  transition: color 0.2s;
}

.action-item:hover {
  color: #1a73e8;
}

/* 加载更多 */
.load-more {
  text-align: center;
  margin-top: 32px;
}

/* 页脚 */
.footer {
  background: #f5f5f5;
  color: #333;
  padding: 48px 0 24px;
  margin-top: auto;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.footer-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 48px;
  margin-bottom: 32px;
}

@media (max-width: 768px) {
  .footer-grid {
    grid-template-columns: 1fr 1fr;
    gap: 32px;
  }
}

@media (max-width: 480px) {
  .footer-grid {
    grid-template-columns: 1fr;
  }
}

.footer-brand h4 {
  font-size: 20px;
  margin: 0 0 12px;
  font-weight: 600;
  color: #333;
}

.footer-brand p {
  color: #666;
  font-size: 14px;
  margin: 0 0 16px;
  line-height: 1.6;
}

.social-links {
  display: flex;
  gap: 12px;
}

.social-link {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e0e0e0;
  border-radius: 50%;
  color: #666;
  transition: all 0.2s;
}

.social-link:hover {
  background: #1a73e8;
  color: #fff;
}

.footer-links h5 {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 16px;
  color: #333;
}

.footer-links a {
  display: block;
  color: #666;
  font-size: 14px;
  text-decoration: none;
  margin-bottom: 10px;
  transition: color 0.2s;
}

.footer-links a:hover {
  color: #1a73e8;
}

.footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 24px;
  border-top: 1px solid #e0e0e0;
}

@media (max-width: 480px) {
  .footer-bottom {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
}

.footer-bottom p {
  color: #666;
  font-size: 13px;
  margin: 0;
}

.footer-bottom-links {
  display: flex;
  gap: 24px;
}

.footer-bottom-links a {
  color: #666;
  font-size: 13px;
  text-decoration: none;
  transition: color 0.2s;
}

.footer-bottom-links a:hover {
  color: #1a73e8;
}
</style>
