<template>
  <div class="my-contents">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="4">
        <el-card shadow="never" class="stat-card" @click="filterByStatus('')">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">全部内容</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="never" class="stat-card" @click="filterByStatus('DRAFT')">
          <div class="stat-value draft">{{ stats.draft }}</div>
          <div class="stat-label">草稿</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="never" class="stat-card" @click="filterByStatus('PENDING_REVIEW')">
          <div class="stat-value pending">{{ stats.pending }}</div>
          <div class="stat-label">待审核</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="never" class="stat-card" @click="filterByStatus('PUBLISHED')">
          <div class="stat-value published">{{ stats.published }}</div>
          <div class="stat-label">已发布</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="never" class="stat-card" @click="filterByStatus('REJECTED')">
          <div class="stat-value rejected">{{ stats.rejected }}</div>
          <div class="stat-label">已拒绝</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-button type="primary" @click="goToEditor" style="height: 100%; width: 100%;">
          <el-icon style="animation: none;"><Edit /></el-icon>
          写文章
        </el-button>
      </el-col>
    </el-row>

    <!-- 筛选和列表 -->
    <el-card shadow="never" class="content-card">
      <template #header>
        <div class="card-header">
          <span>我的内容</span>
          <el-select
            v-model="currentStatus"
            placeholder="全部状态"
            clearable
            @change="handleStatusChange"
            style="width: 150px;"
          >
            <el-option label="全部" value="" />
            <el-option label="草稿" value="DRAFT" />
            <el-option label="待审核" value="PENDING_REVIEW" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="contentList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="viewContent(row)">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="viewCount" label="浏览量" width="80" />

        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <!-- 草稿：编辑、提交审核、删除 -->
            <template v-if="row.status === 'DRAFT'">
              <el-button type="primary" size="small" @click="editContent(row)">编辑</el-button>
              <el-button type="success" size="small" @click="submitForReview(row)">提交审核</el-button>
              <el-button type="danger" size="small" @click="deleteContent(row)">删除</el-button>
            </template>
            
            <!-- 待审核：查看、删除 -->
            <template v-else-if="row.status === 'PENDING_REVIEW'">
              <el-button type="info" size="small" @click="viewContent(row)">查看</el-button>
              <el-button type="danger" size="small" @click="deleteContent(row)">删除</el-button>
            </template>
            
            <!-- 已通过：发布、编辑、删除 -->
            <template v-else-if="row.status === 'APPROVED'">
              <el-button type="success" size="small" @click="publishContent(row)">发布</el-button>
              <el-button type="primary" size="small" @click="editContent(row)">编辑</el-button>
              <el-button type="danger" size="small" @click="deleteContent(row)">删除</el-button>
            </template>
            
            <!-- 已发布：查看、下架、删除 -->
            <template v-else-if="row.status === 'PUBLISHED'">
              <el-button type="success" size="small" @click="viewContent(row)">查看</el-button>
              <el-button type="warning" size="small" @click="unpublishContent(row)">下架</el-button>
              <el-button type="danger" size="small" @click="deleteContent(row)">删除</el-button>
            </template>
            
            <!-- 已拒绝：编辑、查看原因、删除 -->
            <template v-else-if="row.status === 'REJECTED'">
              <el-button type="primary" size="small" @click="editContent(row)">编辑</el-button>
              <el-button type="info" size="small" @click="showRejectReason(row)">原因</el-button>
              <el-button type="danger" size="small" @click="deleteContent(row)">删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 拒绝原因对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝原因"
      width="500px"
    >
      <p>{{ currentRejectReason || '未提供拒绝原因' }}</p>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import axios from '@/api/axios'
import { useAuthStore } from '@/stores/auth'
import { submitForReview as submitContentForReview, publishContent as publishContentApi, deleteContent as deleteContentApi, adminDeleteContent as adminDeleteContentApi, unpublishContent as unpublishContentApi } from '@/api/content'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const contentList = ref([])
const currentStatus = ref('')
const rejectDialogVisible = ref(false)
const currentRejectReason = ref('')

const stats = reactive({
  total: 0,
  draft: 0,
  pending: 0,
  published: 0,
  rejected: 0
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const loadStats = async () => {
  try {
    const response = await axios.get('/user/contents/stats')
    const data = response.data || response
    Object.assign(stats, data)
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

const loadContents = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size
    }
    if (currentStatus.value) {
      params.status = currentStatus.value
    }
    
    const response = await axios.get('/user/contents', { params })
    const data = response.data || response
    contentList.value = data.content || []
    pagination.total = data.totalElements || 0
  } catch (error) {
    console.error('加载内容失败:', error)
    ElMessage.error('加载内容失败')
  } finally {
    loading.value = false
  }
}

const filterByStatus = (status) => {
  currentStatus.value = status
  pagination.page = 1
  loadContents()
}

const handleStatusChange = () => {
  pagination.page = 1
  loadContents()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadContents()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadContents()
}

const goToEditor = () => {
  router.push({ name: 'admin-editor' })
}

const editContent = (row) => {
  // 跳转到编辑页面，带上内容ID
  router.push({ path: '/editor/' + row.id })
}

const viewContent = (row) => {
  router.push({ name: 'content-detail', params: { id: row.id } })
}

const showRejectReason = (row) => {
  currentRejectReason.value = row.rejectReason
  rejectDialogVisible.value = true
}

// 提交审核
const submitForReview = async (row) => {
  try {
    await ElMessageBox.confirm('确认提交此内容进行审核？', '提交审核', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await submitContentForReview(row.id)
    ElMessage.success('提交成功，请等待审核')
    loadContents()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交审核失败:', error)
      ElMessage.error('提交审核失败')
    }
  }
}

// 发布内容
const publishContent = async (row) => {
  try {
    await ElMessageBox.confirm('确认发布此内容？发布后将公开显示。', '发布内容', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'success'
    })
    
    await publishContentApi(row.id)
    ElMessage.success('发布成功')
    loadContents()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布失败:', error)
      ElMessage.error('发布失败')
    }
  }
}

// 下架内容
const unpublishContent = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确认下架此内容？下架后内容将不再公开显示。',
      '下架内容',
      {
        confirmButtonText: '确认下架',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await unpublishContentApi(row.id)
    ElMessage.success('内容已下架')
    loadContents()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('下架失败:', error)
      const errorMessage = error.response?.data?.error?.message || error.response?.data?.message || error.message || '下架失败'
      ElMessage.error(errorMessage)
    }
  }
}

// 删除内容（会员可以删除自己发布的内容）
const deleteContent = async (row) => {
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
    
    // 会员可以删除自己发布的内容（所有状态）
    await deleteContentApi(row.id)
    
    ElMessage.success('删除成功')
    loadContents()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      const errorMessage = error.response?.data?.error?.message || error.response?.data?.message || error.message || '删除失败'
      ElMessage.error(errorMessage)
    }
  }
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'DRAFT': '草稿',
    'PENDING_REVIEW': '待审核',
    'APPROVED': '已审核通过',
    'PUBLISHED': '已发布',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status
}

const getStatusType = (status) => {
  const types = {
    DRAFT: 'info',
    PENDING_REVIEW: 'warning',
    APPROVED: 'success',
    PUBLISHED: 'success',
    REJECTED: 'danger'
  }
  return types[status] || 'info'
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

onMounted(() => {
  loadStats()
  loadContents()
})
</script>

<style scoped>
.my-contents {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-row {
  margin-bottom: 0;
}

.stat-card {
  cursor: pointer;
  text-align: center;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.stat-value.draft {
  color: #909399;
}

.stat-value.pending {
  color: #e6a23c;
}

.stat-value.published {
  color: #67c23a;
}

.stat-value.rejected {
  color: #f56c6c;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.content-card {
  flex: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
