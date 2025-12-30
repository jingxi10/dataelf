<template>
  <div class="content-review">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ total }}</div>
          <div class="stat-label">待审核</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value total">{{ stats.totalReviewed }}</div>
          <div class="stat-label">已审核总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value approved">{{ stats.approved }}</div>
          <div class="stat-label">已通过</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value rejected">{{ stats.rejected }}</div>
          <div class="stat-label">已拒绝</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 审核队列列表 -->
    <el-card class="review-queue-card">
      <template #header>
        <div class="card-header">
          <span>审核队列</span>
          <el-button 
            type="primary" 
            :icon="Refresh" 
            @click="loadReviewQueue"
          >
            刷新
          </el-button>
        </div>
      </template>

      <!-- 队列列表 -->
      <el-table
        v-loading="loading"
        :data="reviewQueue"
        style="width: 100%"
        @row-click="handleRowClick"
        class="review-table"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column label="模板" width="150">
          <template #default="{ row }">
            <el-tag type="info" size="small">
              {{ row.templateName || '未知模板' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="作者" width="150">
          <template #default="{ row }">
            {{ row.authorName || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="完整性评分" width="120" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="getScoreType(row.integrityScore)"
              effect="dark"
            >
              {{ formatScore(row.integrityScore) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.submittedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="reviewedByName" label="审批人" width="150">
          <template #default="{ row }">
            <span v-if="row.reviewedByName">{{ row.reviewedByName }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="440" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small"
              @click.stop="handlePreview(row)"
            >
              预览
            </el-button>
            <el-button 
              type="success" 
              size="small"
              @click.stop="handleApprove(row.id)"
            >
              批准
            </el-button>
            <el-button 
              type="danger" 
              size="small"
              @click.stop="handleReject(row.id)"
            >
              拒绝
            </el-button>
            <el-button 
              type="warning" 
              size="small"
              @click.stop="handleDirectPublish(row.id)"
            >
              直接发布
            </el-button>
            <!-- 主管理员可以下架和删除任何内容 -->
            <template v-if="isMainAdmin">
              <el-button 
                v-if="row.status !== 'REJECTED'"
                type="warning" 
                size="small"
                @click.stop="handleUnpublish(row)"
              >
                下架
              </el-button>
              <el-button 
                type="danger" 
                size="small"
                @click.stop="handleDelete(row.id)"
              >
                删除
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 内容预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="内容预览"
      width="80%"
      :close-on-click-modal="false"
    >
      <div v-if="selectedContent" class="preview-content">
        <!-- 基本信息 -->
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题">
            {{ selectedContent.title }}
          </el-descriptions-item>
          <el-descriptions-item label="作者">
            {{ selectedContent.authorName || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="内容来源">
            {{ selectedContent.contentSource || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="原创标识">
            <el-tag :type="selectedContent.isOriginal ? 'success' : 'info'">
              {{ selectedContent.isOriginal ? '原创' : '转载' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">
            {{ formatDateTime(selectedContent.submittedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="完整性评分">
            <el-tag :type="getScoreType(selectedContent.integrityScore)">
              {{ formatScore(selectedContent.integrityScore) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedContent.reviewedByName" label="审批人">
            {{ selectedContent.reviewedByName }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedContent.reviewedAt" label="审批时间">
            {{ formatDateTime(selectedContent.reviewedAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 版权说明 -->
        <div v-if="selectedContent.copyrightNotice" class="copyright-section">
          <h4>版权说明</h4>
          <p>{{ selectedContent.copyrightNotice }}</p>
        </div>

        <!-- 文章正文内容 -->
        <div class="article-body-section">
          <h4>文章正文</h4>
          <div class="article-body-content" v-html="getArticleBody(selectedContent)"></div>
        </div>

        <!-- 完整性详情 -->
        <div v-if="integrityDetails" class="integrity-section">
          <h4>完整性详情</h4>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="总字段数">
              {{ integrityDetails.totalFields }}
            </el-descriptions-item>
            <el-descriptions-item label="已填字段数">
              {{ integrityDetails.filledFields }}
            </el-descriptions-item>
            <el-descriptions-item label="评分">
              {{ formatScore(integrityDetails.score) }}
            </el-descriptions-item>
          </el-descriptions>
          <div v-if="integrityDetails.missingFields && integrityDetails.missingFields.length > 0" class="missing-fields">
            <el-alert
              title="缺失字段"
              type="warning"
              :closable="false"
            >
              <template #default>
                <el-tag 
                  v-for="field in integrityDetails.missingFields" 
                  :key="field"
                  type="warning"
                  size="small"
                  style="margin-right: 8px; margin-bottom: 8px;"
                >
                  {{ field }}
                </el-tag>
              </template>
            </el-alert>
          </div>
        </div>

        <!-- 结构化数据预览 -->
        <div class="structured-data-section">
          <h4>结构化数据</h4>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="JSON-LD" name="jsonld">
              <pre class="code-preview">{{ formatJson(selectedContent.jsonLd) }}</pre>
            </el-tab-pane>
            <el-tab-pane label="HTML" name="html">
              <pre class="code-preview">{{ selectedContent.htmlOutput }}</pre>
            </el-tab-pane>
            <el-tab-pane label="Markdown" name="markdown">
              <pre class="code-preview">{{ selectedContent.markdownOutput }}</pre>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="previewDialogVisible = false">关闭</el-button>
          <el-button 
            type="success" 
            @click="handleApproveFromPreview"
          >
            批准
          </el-button>
          <el-button 
            type="danger" 
            @click="handleRejectFromPreview"
          >
            拒绝
          </el-button>
          <el-button 
            type="warning" 
            @click="handleDirectPublishFromPreview"
          >
            直接发布
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 下架对话框 -->
    <el-dialog
      v-model="unpublishDialogVisible"
      title="下架内容"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="unpublishForm" label-width="100px">
        <el-form-item label="下架原因">
          <el-input
            v-model="unpublishForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入下架原因（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="unpublishDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="loading"
          @click="confirmUnpublish"
        >
          确认下架
        </el-button>
      </template>
    </el-dialog>

    <!-- 拒绝原因对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝内容"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因" required>
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因，将发送给用户"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button 
          type="danger" 
          @click="confirmReject"
          :disabled="!rejectForm.reason"
        >
          确认拒绝
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { 
  getReviewQueue, 
  approveContent, 
  rejectContent, 
  directPublish,
  checkIntegrity,
  adminDeleteContent,
  adminUnpublishContent,
  getReviewStatistics
} from '@/api/admin'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const isMainAdmin = computed(() => authStore.isMainAdmin)

// 统计数据
const stats = ref({
  totalReviewed: 0,
  approved: 0,
  rejected: 0,
  published: 0
})

// 数据状态
const loading = ref(false)
const reviewQueue = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 预览对话框
const previewDialogVisible = ref(false)
const selectedContent = ref(null)
const integrityDetails = ref(null)
const activeTab = ref('jsonld')

// 拒绝对话框
const rejectDialogVisible = ref(false)
const rejectForm = ref({
  contentId: null,
  reason: ''
})

// 下架对话框
const unpublishDialogVisible = ref(false)
const unpublishForm = ref({
  contentId: null,
  reason: ''
})

// 加载统计数据
const loadStats = async () => {
  try {
    const response = await getReviewStatistics()
    const data = response.data || response
    stats.value = {
      totalReviewed: data.totalReviewed || 0,
      approved: data.approved || 0,
      rejected: data.rejected || 0,
      published: data.published || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载审核队列
const loadReviewQueue = async () => {
  loading.value = true
  try {
    const response = await getReviewQueue({
      page: currentPage.value - 1,
      size: pageSize.value
    })
    
    // axios拦截器已经解包
    const data = response.content ? response : (response.data || response)
    reviewQueue.value = data.content || data
    total.value = data.totalElements || 0
  } catch (error) {
    console.error('加载审核队列失败:', error)
    ElMessage.error('加载审核队列失败')
  } finally {
    loading.value = false
  }
}

// 格式化评分
const formatScore = (score) => {
  if (score === null || score === undefined) return 'N/A'
  return (score * 100).toFixed(0) + '%'
}

// 获取评分类型
const getScoreType = (score) => {
  if (score === null || score === undefined) return 'info'
  if (score >= 0.9) return 'success'
  if (score >= 0.7) return 'warning'
  return 'danger'
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return 'N/A'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 格式化JSON
const formatJson = (jsonStr) => {
  if (!jsonStr) return ''
  try {
    const obj = typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr
    return JSON.stringify(obj, null, 2)
  } catch (e) {
    return jsonStr
  }
}

// 获取文章正文内容
const getArticleBody = (content) => {
  if (!content) return ''
  
  // 尝试从 structuredData 中获取 articleBody
  if (content.structuredData) {
    const data = typeof content.structuredData === 'string' 
      ? JSON.parse(content.structuredData) 
      : content.structuredData
    if (data.articleBody) {
      return data.articleBody
    }
  }
  
  // 尝试从 htmlOutput 中获取
  if (content.htmlOutput) {
    return content.htmlOutput
  }
  
  return '<p style="color: #999;">暂无正文内容</p>'
}

// 处理行点击
const handleRowClick = (row) => {
  handlePreview(row)
}

// 预览内容
const handlePreview = async (content) => {
  selectedContent.value = content
  previewDialogVisible.value = true
  activeTab.value = 'jsonld'
  
  // 加载完整性详情
  try {
    const response = await checkIntegrity(content.id)
    // axios拦截器已经解包
    integrityDetails.value = response.data ? response.data : response
  } catch (error) {
    console.error('加载完整性详情失败:', error)
  }
}

// 批准内容
const handleApprove = async (contentId) => {
  try {
    await ElMessageBox.confirm(
      '确认批准此内容？批准后将发送通知给用户。',
      '确认批准',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    
    loading.value = true
    await approveContent(contentId)
    
    ElMessage.success('内容已批准')
    await loadStats()
    await loadReviewQueue()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批准内容失败:', error)
      ElMessage.error('批准内容失败')
    }
  } finally {
    loading.value = false
  }
}

// 从预览对话框批准
const handleApproveFromPreview = async () => {
  if (selectedContent.value) {
    await handleApprove(selectedContent.value.id)
    previewDialogVisible.value = false
  }
}

// 拒绝内容
const handleReject = (contentId) => {
  rejectForm.value = {
    contentId,
    reason: ''
  }
  rejectDialogVisible.value = true
}

// 从预览对话框拒绝
const handleRejectFromPreview = () => {
  if (selectedContent.value) {
    handleReject(selectedContent.value.id)
  }
}

// 确认拒绝
const confirmReject = async () => {
  if (!rejectForm.value.reason) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  loading.value = true
  try {
    await rejectContent(
      rejectForm.value.contentId,
      rejectForm.value.reason
    )
    
    ElMessage.success('内容已拒绝')
    rejectDialogVisible.value = false
    previewDialogVisible.value = false
    await loadStats()
    await loadReviewQueue()
  } catch (error) {
    console.error('拒绝内容失败:', error)
    ElMessage.error('拒绝内容失败')
  } finally {
    loading.value = false
  }
}

// 直接发布
const handleDirectPublish = async (contentId) => {
  try {
    await ElMessageBox.confirm(
      '确认直接发布此内容？将跳过用户确认步骤，内容将立即公开。',
      '确认直接发布',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    await directPublish(contentId)
    
    ElMessage.success('内容已直接发布')
    await loadStats()
    await loadReviewQueue()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('直接发布失败:', error)
      ElMessage.error('直接发布失败')
    }
  } finally {
    loading.value = false
  }
}

// 下架内容
const handleUnpublish = (row) => {
  unpublishForm.value = {
    contentId: row.id,
    reason: ''
  }
  unpublishDialogVisible.value = true
}

const confirmUnpublish = async () => {
  loading.value = true
  try {
    await adminUnpublishContent(unpublishForm.value.contentId, {
      reason: unpublishForm.value.reason || '管理员下架'
    })
    
    ElMessage.success('内容已下架')
    unpublishDialogVisible.value = false
    await loadStats()
    await loadReviewQueue()
  } catch (error) {
    console.error('下架内容失败:', error)
    ElMessage.error(error.response?.data?.message || '下架内容失败')
  } finally {
    loading.value = false
  }
}

// 删除内容（主管理员可以删除任何状态的内容）
const handleDelete = async (contentId) => {
  try {
    await ElMessageBox.confirm(
      '确认删除此内容？删除后无法恢复。',
      '删除内容',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    await adminDeleteContent(contentId)
    ElMessage.success('删除成功')
    await loadStats()
    await loadReviewQueue()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      const errorMessage = error.response?.data?.message || error.response?.data?.error?.message || error.message || '删除失败'
      ElMessage.error(errorMessage)
    }
  } finally {
    loading.value = false
  }
}

// 从预览对话框直接发布
const handleDirectPublishFromPreview = async () => {
  if (selectedContent.value) {
    await handleDirectPublish(selectedContent.value.id)
    previewDialogVisible.value = false
  }
}

// 分页处理
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
  loadReviewQueue()
}

const handlePageChange = (newPage) => {
  currentPage.value = newPage
  loadReviewQueue()
}

// 组件挂载时加载数据
onMounted(() => {
  loadStats()
  loadReviewQueue()
})
</script>

<style scoped>
.content-review {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-row {
  margin-bottom: 0;
}

.stat-card {
  text-align: center;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.stat-value.total {
  color: #409eff;
}

.stat-value.approved {
  color: #67c23a;
}

.stat-value.rejected {
  color: #f56c6c;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.review-queue-card {
  flex: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.review-table {
  margin-bottom: 20px;
}

.review-table :deep(.el-table__row) {
  cursor: pointer;
}

.review-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.preview-content {
  max-height: 70vh;
  overflow-y: auto;
}

.preview-content h4 {
  margin: 20px 0 10px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.copyright-section {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.copyright-section p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.article-body-section {
  margin-top: 20px;
  padding: 15px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.article-body-content {
  padding: 15px;
  background-color: #fafafa;
  border-radius: 4px;
  line-height: 1.8;
  color: #303133;
  max-height: 400px;
  overflow-y: auto;
}

.article-body-content img {
  max-width: 100%;
  height: auto;
  margin: 10px 0;
  border-radius: 4px;
}

.article-body-content video {
  max-width: 100%;
  margin: 10px 0;
}

.integrity-section {
  margin-top: 20px;
}

.missing-fields {
  margin-top: 10px;
}

.structured-data-section {
  margin-top: 20px;
}

.code-preview {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
  color: #303133;
  max-height: 400px;
  overflow-y: auto;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
