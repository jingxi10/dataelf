<template>
  <div class="user-management">
    <!-- 搜索和筛选区域 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="搜索">
          <el-input
            v-model="filterForm.search"
            placeholder="邮箱或手机号"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
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
          >
            <el-option label="待审核" value="PENDING" />
            <el-option label="已批准" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已过期" value="EXPIRED" />
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

    <!-- 用户列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <div>
            <el-button
              type="success"
              size="small"
              @click="handleCreateAdmin"
            >
              <el-icon style="animation: none;"><Plus /></el-icon>
              新增管理员
            </el-button>
            <el-button
              type="primary"
              size="small"
              @click="loadUsers"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="userList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column prop="email" label="邮箱" min-width="200" />
        
        <el-table-column prop="phone" label="手机号" width="130" />
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'">
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column prop="expiresAt" label="到期时间" width="180">
          <template #default="{ row }">
            <span v-if="row.expiresAt" :class="getExpiryClass(row.expiresAt)">
              {{ formatDate(row.expiresAt) }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>

        <el-table-column label="剩余天数" width="100">
          <template #default="{ row }">
            <span v-if="row.expiresAt" :class="getExpiryClass(row.expiresAt)">
              {{ getRemainingDays(row.expiresAt) }}天
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              type="success"
              size="small"
              @click="handleApprove(row)"
            >
              批准
            </el-button>

            <el-button
              v-if="row.status === 'APPROVED'"
              type="primary"
              size="small"
              @click="handleExtend(row)"
            >
              延长时长
            </el-button>

            <el-button
              type="info"
              size="small"
              @click="handleViewDetail(row)"
            >
              详情
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

    <!-- 批准用户对话框 -->
    <el-dialog
      v-model="approveDialogVisible"
      title="批准用户"
      width="500px"
    >
      <el-form :model="approveForm" label-width="100px">
        <el-form-item label="用户邮箱">
          <el-input v-model="currentUser.email" disabled />
        </el-form-item>
        
        <el-form-item label="有效天数" required>
          <el-input-number
            v-model="approveForm.validDays"
            :min="1"
            :max="3650"
            placeholder="请输入有效天数"
          />
          <span class="form-tip">天</span>
        </el-form-item>

        <el-form-item label="到期时间">
          <span class="text-info">
            {{ getExpireDate(approveForm.validDays) }}
          </span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="confirmApprove"
        >
          确认批准
        </el-button>
      </template>
    </el-dialog>

    <!-- 延长时长对话框 -->
    <el-dialog
      v-model="extendDialogVisible"
      title="延长账号时长"
      width="500px"
    >
      <el-form :model="extendForm" label-width="100px">
        <el-form-item label="用户邮箱">
          <el-input v-model="currentUser.email" disabled />
        </el-form-item>

        <el-form-item label="当前到期">
          <span class="text-info">
            {{ formatDate(currentUser.expiresAt) }}
          </span>
        </el-form-item>
        
        <el-form-item label="延长天数" required>
          <el-input-number
            v-model="extendForm.days"
            :min="1"
            :max="3650"
            placeholder="请输入延长天数"
          />
          <span class="form-tip">天</span>
        </el-form-item>

        <el-form-item label="新到期时间">
          <span class="text-info">
            {{ getNewExpireDate(currentUser.expiresAt, extendForm.days) }}
          </span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="extendDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="confirmExtend"
        >
          确认延长
        </el-button>
      </template>
    </el-dialog>

    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="用户详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户ID">
          {{ currentUser.id }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentUser.status)">
            {{ getStatusText(currentUser.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="邮箱" :span="2">
          {{ currentUser.email }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号" :span="2">
          {{ currentUser.phone }}
        </el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="2">
          {{ formatDate(currentUser.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="批准时间" :span="2">
          {{ currentUser.approvedAt ? formatDate(currentUser.approvedAt) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="到期时间" :span="2">
          <span v-if="currentUser.expiresAt" :class="getExpiryClass(currentUser.expiresAt)">
            {{ formatDate(currentUser.expiresAt) }}
            (剩余 {{ getRemainingDays(currentUser.expiresAt) }} 天)
          </span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="批准人" :span="2">
          {{ currentUser.approvedBy || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增管理员对话框 -->
    <el-dialog
      v-model="createAdminDialogVisible"
      title="新增管理员"
      width="500px"
    >
      <el-form :model="createAdminForm" :rules="createAdminRules" ref="createAdminFormRef" label-width="100px">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="createAdminForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="createAdminForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input v-model="createAdminForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="createAdminForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="createAdminDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="confirmCreateAdmin"
        >
          确认创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getUserList,
  approveUser,
  extendAccount,
  createAdmin
} from '@/api/admin'

// 数据状态
const loading = ref(false)
const submitting = ref(false)
const userList = ref([])

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 筛选表单
const filterForm = reactive({
  search: '',
  status: ''
})

// 对话框状态
const approveDialogVisible = ref(false)
const extendDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const createAdminDialogVisible = ref(false)

// 当前操作的用户
const currentUser = ref({})

// 表单数据
const approveForm = reactive({
  validDays: 30
})

const extendForm = reactive({
  days: 30
})

// 新增管理员表单
const createAdminFormRef = ref(null)
const createAdminForm = reactive({
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

// 新增管理员表单验证规则
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== createAdminForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const createAdminRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度必须在6-50位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1, // 后端从0开始
      size: pagination.size,
      status: filterForm.status || undefined,
      search: filterForm.search || undefined
    }
    
    const response = await getUserList(params)
    
    // 处理响应数据结构
    const data = response.data || response
    userList.value = data.content || []
    pagination.total = data.totalElements || 0
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadUsers()
}

// 重置
const handleReset = () => {
  filterForm.search = ''
  filterForm.status = ''
  pagination.page = 1
  loadUsers()
}

// 分页变化
const handlePageChange = (page) => {
  pagination.page = page
  loadUsers()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadUsers()
}

// 批准用户
const handleApprove = (user) => {
  currentUser.value = { ...user }
  approveForm.validDays = 30
  approveDialogVisible.value = true
}

const confirmApprove = async () => {
  if (!approveForm.validDays || approveForm.validDays < 1) {
    ElMessage.warning('请输入有效天数')
    return
  }

  submitting.value = true
  try {
    await approveUser(currentUser.value.id, approveForm.validDays)
    ElMessage.success('用户批准成功')
    approveDialogVisible.value = false
    loadUsers()
  } catch (error) {
    console.error('批准用户失败:', error)
    ElMessage.error('批准用户失败')
  } finally {
    submitting.value = false
  }
}

// 延长时长
const handleExtend = (user) => {
  currentUser.value = { ...user }
  extendForm.days = 30
  extendDialogVisible.value = true
}

const confirmExtend = async () => {
  if (!extendForm.days || extendForm.days < 1) {
    ElMessage.warning('请输入延长天数')
    return
  }

  submitting.value = true
  try {
    await extendAccount(currentUser.value.id, extendForm.days)
    ElMessage.success('账号时长延长成功')
    extendDialogVisible.value = false
    loadUsers()
  } catch (error) {
    console.error('延长账号失败:', error)
    ElMessage.error('延长账号失败')
  } finally {
    submitting.value = false
  }
}

// 查看详情
const handleViewDetail = (user) => {
  currentUser.value = { ...user }
  detailDialogVisible.value = true
}

// 新增管理员
const handleCreateAdmin = () => {
  createAdminForm.email = ''
  createAdminForm.phone = ''
  createAdminForm.password = ''
  createAdminForm.confirmPassword = ''
  createAdminDialogVisible.value = true
}

const confirmCreateAdmin = async () => {
  if (!createAdminFormRef.value) return
  
  await createAdminFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await createAdmin({
        email: createAdminForm.email,
        phone: createAdminForm.phone,
        password: createAdminForm.password
      })
      ElMessage.success('管理员创建成功')
      createAdminDialogVisible.value = false
      loadUsers()
    } catch (error) {
      console.error('创建管理员失败:', error)
      ElMessage.error(error.response?.data?.message || '创建管理员失败')
    } finally {
      submitting.value = false
    }
  })
}

// 工具函数
const getStatusType = (status) => {
  const types = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    EXPIRED: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    PENDING: '待审核',
    APPROVED: '已批准',
    REJECTED: '已拒绝',
    EXPIRED: '已过期'
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
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const getRemainingDays = (expiresAt) => {
  if (!expiresAt) return 0
  const now = new Date()
  const expiry = new Date(expiresAt)
  const diff = expiry - now
  const days = Math.ceil(diff / (1000 * 60 * 60 * 24))
  return days > 0 ? days : 0
}

const getExpiryClass = (expiresAt) => {
  const days = getRemainingDays(expiresAt)
  if (days <= 0) return 'text-danger'
  if (days <= 7) return 'text-warning'
  return 'text-success'
}

const getExpireDate = (days) => {
  if (!days) return '-'
  const date = new Date()
  date.setDate(date.getDate() + days)
  return formatDate(date)
}

const getNewExpireDate = (currentExpiry, days) => {
  if (!currentExpiry || !days) return '-'
  const date = new Date(currentExpiry)
  date.setDate(date.getDate() + days)
  return formatDate(date)
}

// 初始化
onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-management {
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

.form-tip {
  margin-left: 10px;
  color: #909399;
}

.text-muted {
  color: #909399;
}

.text-info {
  color: #409eff;
}

.text-success {
  color: #67c23a;
}

.text-warning {
  color: #e6a23c;
}

.text-danger {
  color: #f56c6c;
}
</style>
