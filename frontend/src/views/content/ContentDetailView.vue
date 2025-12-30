<template>
  <div class="content-detail">
    <el-page-header @back="goBack" title="返回">
      <template #content>
        <span class="page-title">内容详情</span>
      </template>
    </el-page-header>

    <div v-loading="loading" class="content-container">
      <el-card v-if="content" class="content-card">
        <!-- 内容头部 -->
        <template #header>
          <div class="content-header">
            <h1 class="content-title">{{ content.title }}</h1>
            <div class="content-meta">
              <el-tag v-if="content.status" :type="getStatusType(content.status)">
                {{ getStatusText(content.status) }}
              </el-tag>
              <span class="meta-item">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(content.createdAt) }}
              </span>
              <span v-if="content.userName" class="meta-item">
                <el-icon><User /></el-icon>
                {{ content.userName }}
              </span>
            </div>
          </div>
        </template>

        <!-- 内容主体 -->
        <div class="content-body">
          <!-- 正文内容（富文本） -->
          <div v-if="content.structuredData && content.structuredData.articleBody" class="article-body">
            <div class="rich-content" v-html="content.structuredData.articleBody"></div>
          </div>

          <!-- 其他结构化数据预览 -->
          <div v-if="hasOtherStructuredData" class="structured-data">
            <el-divider content-position="left">详细信息</el-divider>
            <el-descriptions :column="1" border>
              <el-descriptions-item
                v-for="(value, key) in filteredStructuredData"
                :key="key"
                :label="getFieldLabel(key)"
              >
                <template v-if="isHtmlContent(value)">
                  <div class="rich-content-small" v-html="value"></div>
                </template>
                <template v-else>
                  {{ formatValue(value) }}
                </template>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 版权信息 -->
          <div v-if="hasCopyrightInfo" class="copyright-section">
            <el-divider content-position="left">版权信息</el-divider>
            <div class="copyright-info">
              <p v-if="content.authorName">
                <strong>作者：</strong>{{ content.authorName }}
              </p>
              <p v-if="content.contentSource">
                <strong>来源：</strong>{{ content.contentSource }}
              </p>
              <p v-if="content.isOriginal !== undefined">
                <strong>类型：</strong>{{ content.isOriginal ? '原创' : '转载' }}
              </p>
              <p v-if="content.copyrightNotice">
                <strong>版权声明：</strong>{{ content.copyrightNotice }}
              </p>
            </div>
          </div>

          <!-- 审核信息 -->
          <div v-if="content.reviewedByName || content.reviewedAt" class="review-section">
            <el-divider content-position="left">审核信息</el-divider>
            <div class="review-info">
              <p v-if="content.reviewedByName">
                <strong>审批人：</strong>{{ content.reviewedByName }}
              </p>
              <p v-if="content.reviewedAt">
                <strong>审批时间：</strong>{{ formatDate(content.reviewedAt) }}
              </p>
            </div>
          </div>
        </div>

        <!-- 交互按钮区域 -->
        <div class="interaction-section">
          <el-divider />
          <div class="interaction-buttons">
            <LikeButton
              :content-id="content.id"
              :initial-liked="interactionStatus.hasLiked"
              :initial-count="interactionStatus.likeCount"
            />
            <FavoriteButton
              :content-id="content.id"
              :initial-favorited="interactionStatus.hasFavorited"
              :initial-count="interactionStatus.favoriteCount"
            />
            <ShareButton :content-id="content.id" />
            <el-button
              type="primary"
              :icon="Download"
              @click="showExportDialog"
            >
              导出
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 评论区 -->
      <el-card v-if="content" class="comment-card">
        <CommentSection :content-id="content.id" />
      </el-card>
    </div>

    <!-- 导出对话框 -->
    <ExportDialog
      v-model="exportDialogVisible"
      :content-id="parseInt(route.params.id)"
      @exported="handleExported"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Calendar, User, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from '@/api/axios'
import { getInteractionStatus } from '@/api/interaction'
import { useAuthStore } from '@/stores/auth'
import LikeButton from '@/components/LikeButton.vue'
import FavoriteButton from '@/components/FavoriteButton.vue'
import ShareButton from '@/components/ShareButton.vue'
import CommentSection from '@/components/CommentSection.vue'
import ExportDialog from '@/components/ExportDialog.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const content = ref(null)
const loading = ref(false)
const exportDialogVisible = ref(false)
const interactionStatus = ref({
  hasLiked: false,
  hasFavorited: false,
  likeCount: 0,
  favoriteCount: 0,
  commentCount: 0
})

// 是否有版权信息
const hasCopyrightInfo = computed(() => {
  return content.value && (
    content.value.authorName ||
    content.value.contentSource ||
    content.value.copyrightNotice ||
    content.value.isOriginal !== undefined
  )
})

// 是否有其他结构化数据（除了articleBody）
const hasOtherStructuredData = computed(() => {
  if (!content.value?.structuredData) return false
  const keys = Object.keys(content.value.structuredData).filter(k => k !== 'articleBody')
  return keys.length > 0
})

// 过滤掉articleBody的结构化数据
const filteredStructuredData = computed(() => {
  if (!content.value?.structuredData) return {}
  const result = {}
  for (const [key, value] of Object.entries(content.value.structuredData)) {
    if (key !== 'articleBody' && value) {
      result[key] = value
    }
  }
  return result
})

// 字段标签映射
const fieldLabelMap = {
  name: '名称',
  description: '描述',
  headline: '标题',
  datePublished: '发布日期',
  dateModified: '修改日期',
  author: '作者',
  publisher: '发布者',
  image: '图片',
  url: '链接',
  keywords: '关键词'
}

// 获取字段标签
const getFieldLabel = (key) => {
  return fieldLabelMap[key] || key
}

// 判断是否是HTML内容
const isHtmlContent = (value) => {
  if (typeof value !== 'string') return false
  return /<[^>]+>/.test(value)
}

// 加载内容详情
const loadContent = async () => {
  loading.value = true
  try {
    const contentId = route.params.id
    const response = await axios.get(`/public/content/${contentId}`)
    content.value = response.data || response
    
    // 如果用户已登录，加载交互状态
    if (authStore.isAuthenticated) {
      await loadInteractionStatus()
    }
  } catch (error) {
    console.error('加载内容失败:', error)
    ElMessage.error('加载内容失败')
  } finally {
    loading.value = false
  }
}

// 加载交互状态
const loadInteractionStatus = async () => {
  try {
    const response = await getInteractionStatus(content.value.id)
    // axios拦截器已经解包，response可能是对象或包含data的对象
    interactionStatus.value = response.data ? response.data : (response || {})
  } catch (error) {
    console.error('加载交互状态失败:', error)
  }
}

// 格式化日期
const formatDate = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 格式化值
const formatValue = (value) => {
  if (typeof value === 'object') {
    return JSON.stringify(value, null, 2)
  }
  // 检查是否是ISO日期格式
  if (typeof value === 'string' && isISODateString(value)) {
    return formatDate(value)
  }
  return value
}

// 判断是否是ISO日期字符串
const isISODateString = (str) => {
  if (typeof str !== 'string') return false
  // 匹配 ISO 8601 日期格式: 2025-12-20T16:00:00.000Z
  const isoPattern = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/
  return isoPattern.test(str)
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'DRAFT': 'info',
    'PENDING_REVIEW': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'PUBLISHED': 'success'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'DRAFT': '草稿',
    'PENDING_REVIEW': '待审核',
    'APPROVED': '已批准',
    'REJECTED': '已拒绝',
    'PUBLISHED': '已发布'
  }
  return textMap[status] || status
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 显示导出对话框
const showExportDialog = () => {
  exportDialogVisible.value = true
}

// 导出完成后的处理
const handleExported = (format) => {
  console.log('导出完成，格式:', format)
}

onMounted(() => {
  loadContent()
})
</script>

<style scoped>
.content-detail {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
}

.content-container {
  margin-top: 20px;
}

.content-card {
  margin-bottom: 20px;
}

.content-header {
  padding: 10px 0;
}

.content-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  line-height: 1.4;
}

.content-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 14px;
}

.content-body {
  padding: 20px 0;
}

.structured-data {
  margin-bottom: 20px;
}

.copyright-section {
  margin-top: 30px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.copyright-info p {
  margin: 8px 0;
  color: #606266;
  line-height: 1.6;
}

.copyright-info strong {
  color: #303133;
  margin-right: 8px;
}

.review-section {
  margin-top: 30px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.review-info p {
  margin: 8px 0;
  color: #606266;
  line-height: 1.6;
}

.review-info strong {
  color: #303133;
  margin-right: 8px;
}

/* 富文本内容样式 */
.article-body {
  margin-bottom: 30px;
}

.rich-content {
  line-height: 1.8;
  font-size: 16px;
  color: #303133;
}

.rich-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 16px 0;
  display: block;
}

.rich-content :deep(video) {
  max-width: 100%;
  border-radius: 8px;
  margin: 16px 0;
  display: block;
}

.rich-content :deep(h1),
.rich-content :deep(h2),
.rich-content :deep(h3) {
  margin: 24px 0 16px;
  font-weight: 600;
  color: #303133;
}

.rich-content :deep(h1) {
  font-size: 24px;
}

.rich-content :deep(h2) {
  font-size: 20px;
}

.rich-content :deep(h3) {
  font-size: 18px;
}

.rich-content :deep(p) {
  margin: 12px 0;
}

.rich-content :deep(a) {
  color: #409eff;
  text-decoration: none;
}

.rich-content :deep(a:hover) {
  text-decoration: underline;
}

.rich-content :deep(ul),
.rich-content :deep(ol) {
  padding-left: 24px;
  margin: 12px 0;
}

.rich-content :deep(li) {
  margin: 8px 0;
}

.rich-content :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding-left: 16px;
  margin: 16px 0;
  color: #606266;
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 0 4px 4px 0;
}

.rich-content-small {
  line-height: 1.6;
  font-size: 14px;
}

.rich-content-small :deep(img) {
  max-width: 100%;
  height: auto;
  max-height: 200px;
  object-fit: contain;
}

.rich-content-small :deep(video) {
  max-width: 100%;
  max-height: 200px;
}

.interaction-section {
  margin-top: 20px;
}

.interaction-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
}

.comment-card {
  margin-top: 20px;
}

@media (max-width: 768px) {
  .content-detail {
    padding: 10px;
  }

  .content-title {
    font-size: 22px;
  }

  .content-meta {
    font-size: 12px;
  }

  .interaction-buttons {
    flex-wrap: wrap;
  }
}
</style>
