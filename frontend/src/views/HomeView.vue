<template>
  <div class="home">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-container">
        <div class="logo" @click="goHome">
          <img v-if="logoUrl" :src="logoUrl" alt="数流精灵" class="logo-image" />
          <span v-else class="logo-text">数流精灵</span>
        </div>
        <nav class="nav">
          <router-link v-if="isAuthenticated" to="/editor" class="nav-link">创建内容</router-link>
          <router-link v-if="isAuthenticated" to="/my-content" class="nav-link">我的内容</router-link>
          <router-link v-if="isAuthenticated" to="/favorites" class="nav-link">我的收藏</router-link>
          <router-link v-if="isAuthenticated && isAdmin" to="/admin/users" class="nav-link">管理后台</router-link>
          <NotificationDropdown v-if="isAuthenticated" />
          <template v-if="!isAuthenticated">
            <router-link to="/login" class="nav-link">登录</router-link>
            <router-link to="/register" class="nav-link btn-primary">注册</router-link>
          </template>
          <button v-else @click="handleLogout" class="nav-link">退出</button>
        </nav>
      </div>
    </header>

    <main class="main">
      <!-- Hero 区域 -->
      <section class="hero-section">
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
          <el-button @click="loadMore" :loading="loadingMore">
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
            <h4>数流精灵</h4>
            <p>专为AI优化的智能结构化数据平台</p>
            <div class="social-links">
              <a href="#" class="social-link"><el-icon><Share /></el-icon></a>
              <a href="#" class="social-link"><el-icon><ChatDotRound /></el-icon></a>
              <a href="#" class="social-link"><el-icon><Link /></el-icon></a>
            </div>
          </div>
          
          <!-- 产品功能 -->
          <div class="footer-links">
            <h5>产品功能</h5>
            <a href="#">结构化模板</a>
            <a href="#">内容编辑器</a>
            <a href="#">数据探索</a>
            <a href="#">API文档</a>
          </div>
          
          <!-- 资源中心 -->
          <div class="footer-links">
            <h5>资源中心</h5>
            <a href="#">帮助文档</a>
            <a href="#">教程指南</a>
            <a href="#">博客文章</a>
            <a href="#">常见问题</a>
          </div>
          
          <!-- 关于我们 -->
          <div class="footer-links">
            <h5>关于我们</h5>
            <a href="#">公司介绍</a>
            <a href="#">联系我们</a>
            <a href="#">隐私政策</a>
            <a href="#">服务条款</a>
          </div>
        </div>
        
        <div class="footer-bottom">
          <p>© 2024 数流精灵. 保留所有权利</p>
          <div class="footer-bottom-links">
            <a href="#">网站地图</a>
            <a href="#">AI数据接口</a>
            <a href="#">机器人协议</a>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getPublishedContents, getContentsByCategory, searchContents } from '@/api/content'
import { getAllCategories } from '@/api/category'
import { getPopularTags } from '@/api/tag'
import { getPublicConfig } from '@/api/system'
import NotificationDropdown from '@/components/NotificationDropdown.vue'
import { 
  Search, Grid, Cpu, DataAnalysis, Connection, SetUp, 
  Monitor, MagicStick, Cloudy, Star, Collection, 
  ChatDotRound, Loading, Share, Link
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
const hasMore = computed(() => currentPage.value < totalPages.value - 1)

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

const loadConfig = async () => {
  try {
    const response = await getPublicConfig()
    const config = response.data ? response.data : response
    logoUrl.value = config.logoUrl || ''
    siteName.value = config.siteName || '数流精灵'
    siteDescription.value = config.siteDescription || ''
    heroTitle.value = config.heroTitle || '去伪存真、建立AI秩序'
    heroSubtitle.value = config.heroSubtitle || '专为AI优化的结构化数据平台'
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
}

.category-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.2s;
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
  background: #1a1a2e;
  color: #fff;
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
}

.footer-brand p {
  color: #999;
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
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  color: #fff;
  transition: all 0.2s;
}

.social-link:hover {
  background: #1a73e8;
}

.footer-links h5 {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 16px;
  color: #fff;
}

.footer-links a {
  display: block;
  color: #999;
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
  border-top: 1px solid rgba(255, 255, 255, 0.1);
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
