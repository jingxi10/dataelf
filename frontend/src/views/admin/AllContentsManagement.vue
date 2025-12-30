<template>
  <div class="all-contents-management">
    <!-- 搜索和筛选区域 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="搜索">
          <el-input
            v-model="filterForm.search"
            placeholder="标题或作者名"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
            style="width: 300px;"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select
            v-model="filterForm.status"
            placeholder="全部状态"
            clearable
            @change="handleSearch"
            style="width: 150px;"
          >
            <el-option label="全部" value="" />
            <el-option label="草稿" value="DRAFT" />
            <el-option label="待审核" value="PENDING_REVIEW" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 内容列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>所有内容管理</span>
          <el-button
            type="primary"
            size="small"
            @click="loadContents"
          >
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
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
        
        <el-table-column prop="authorName" label="作者" width="150">
          <template #default="{ row }">
            {{ row.authorName || '未知' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="reviewedByName" label="审批人" width="150">
          <template #default="{ row }">
            <span v-if="row.reviewedByName">{{ row.reviewedByName }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>

        <el-table-column prop="viewCount" label="浏览量" width="80" />
        
        <el-table-column prop="likeCount" label="点赞" width="80" />
        
        <el-table-column prop="favoriteCount" label="收藏" width="80" />
        
        <el-table-column prop="commentCount" label="评论" width="80" />

        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column prop="publishedAt" label="发布时间" width="160">
          <template #default="{ row }">
            {{ row.publishedAt ? formatDate(row.publishedAt) : '-' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button
              type="info"
              size="small"
              @click="viewContent(row)"
            >
              查看
            </el-button>
            
            <!-- 下架按钮 - 可以下架任何状态的内容 -->
            <el-button
              v-if="row.status !== 'REJECTED'"
              type="warning"
              size="small"
              @click="handleUnpublish(row)"
            >
              下架
            </el-button>
            
            <!-- 删除按钮 - 可以删除任何状态的内容 -->
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 下架原因对话框 -->
    <el-dialog
      v-model="unpublishDialogVisible"
      title="下架内容"
      width="500px"
    >
      <el-form :model="unpublishForm" label-width="100px">
        <el-form-item label="内容标题">
          <el-input v-model="currentContent.title" disabled />
        </el-form-item>
        
        <el-form-item label="下架原因">
          <el-input
            v-model="unpublishForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入下架原因（可选）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="unpublishDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="confirmUnpublish"
        >
          确认下架
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getAllContents, adminDeleteContent, adminUnpublishContent } from '@/api/admin'

const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const contentList = ref([])

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const filterForm = reactive({
  search: '',
  status: ''
})

const unpublishDialogVisible = ref(false)
const currentContent = ref({})
const unpublishForm = reactive({
  reason: ''
})

// 加载内容列表
const loadContents = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size
    }
    if (filterForm.status) {
      params.status = filterForm.status
    }
    if (filterForm.search) {
      params.search = filterForm.search
    }
    
    const response = await getAllContents(params)
    const data = response.data || response
    contentList.value = data.content || data || []
    pagination.total = data.totalElements || 0
  } catch (error) {
    console.error('加载内容失败:', error)
    ElMessage.error(error.response?.data?.message || '加载内容失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadContents()
}

// 重置
const handleReset = () => {
  filterForm.search = ''
  filterForm.status = ''
  pagination.page = 1
  loadContents()
}

// 分页变化
const handlePageChange = (page) => {
  pagination.page = page
  loadContents()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadContents()
}

// 查看内容
const viewContent = (row) => {
  router.push({ name: 'content-detail', params: { id: row.id } })
}

// 下架内容
const handleUnpublish = (row) => {
  currentContent.value = { ...row }
  unpublishForm.reason = ''
  unpublishDialogVisible.value = true
}

const confirmUnpublish = async () => {
  submitting.value = true
  try {
    await adminUnpublishContent(currentContent.value.id, {
      reason: unpublishForm.reason || '管理员下架'
    })
    ElMessage.success('内容已下架')
    unpublishDialogVisible.value = false
    loadContents()
  } catch (error) {
    console.error('下架内容失败:', error)
    ElMessage.error(error.response?.data?.message || '下架内容失败')
  } finally {
    submitting.value = false
  }
}

// 删除内容
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认删除内容《${row.title}》？此操作不可恢复！`,
      '删除内容',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    submitting.value = true
    try {
      await adminDeleteContent(row.id)
      ElMessage.success('内容删除成功')
      loadContents()
    } catch (error) {
      console.error('删除内容失败:', error)
      ElMessage.error(error.response?.data?.message || '删除内容失败')
    } finally {
      submitting.value = false
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除内容失败:', error)
    }
  }
}

// 工具函数
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

const getStatusText = (status) => {
  const texts = {
    DRAFT: '草稿',
    PENDING_REVIEW: '待审核',
    APPROVED: '已通过',
    PUBLISHED: '已发布',
    REJECTED: '已拒绝'
  }
  return texts[status] || status
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

// 初始化
onMounted(() => {
  loadContents()
})
</script>

<style scoped>
.all-contents-management {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-card {
  margin-bottom: 0;
}

.filter-form {
  margin-bottom: 0;
}

.table-card {
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
