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
            <!-- 只有主管理员可以新增管理员 -->
            <el-button
              v-if="isMainAdmin"
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

        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.role === 'USER'" type="info">会员</el-tag>
            <el-tag v-else-if="row.adminType === 'MAIN_ADMIN'" type="danger">主管理员</el-tag>
            <el-tag v-else-if="row.adminType === 'NORMAL_ADMIN'" type="warning">普通管理员</el-tag>
            <el-tag v-else type="info">普通用户</el-tag>
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

        <el-table-column label="操作" width="420" fixed="right">
          <template #default="{ row }">
            <!-- 批准按钮 - 需要user_approve权限或主管理员 -->
            <el-button
              v-if="row.status === 'PENDING' && (isMainAdmin || hasPermission('user_approve'))"
              type="success"
              size="small"
              @click="handleApprove(row)"
            >
              批准
            </el-button>

            <!-- 延长时长按钮 - 需要user_approve权限或主管理员 -->
            <el-button
              v-if="row.status === 'APPROVED' && (isMainAdmin || hasPermission('user_approve'))"
              type="primary"
              size="small"
              @click="handleExtend(row)"
            >
              延长时长
            </el-button>

            <!-- 编辑权限按钮 - 只有主管理员可以编辑普通管理员的权限 -->
            <el-button
              v-if="isMainAdmin && row.role === 'ADMIN' && row.adminType === 'NORMAL_ADMIN'"
              type="warning"
              size="small"
              @click="handleEditPermissions(row)"
            >
              编辑权限
            </el-button>

            <!-- 删除会员按钮 - 需要user_delete权限或主管理员，且不能删除管理员账号（普通管理员） -->
            <el-button
              v-if="(isMainAdmin || hasPermission('user_delete')) && 
                     (row.role !== 'ADMIN' || (row.role === 'ADMIN' && isMainAdmin))"
              type="danger"
              size="small"
              @click="handleDeleteUser(row)"
            >
              删除会员
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
          {{ currentUser.approvedByName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentUser.role === 'ADMIN' && currentUser.adminType === 'NORMAL_ADMIN'" label="权限配置" :span="2">
          <div v-if="currentUser.adminPermissions">
            <el-tag 
              v-for="perm in (typeof currentUser.adminPermissions === 'string' ? JSON.parse(currentUser.adminPermissions) : currentUser.adminPermissions)" 
              :key="perm" 
              size="small" 
              style="margin-right: 8px; margin-bottom: 4px;"
            >
              {{ getPermissionLabel(perm) }}
            </el-tag>
          </div>
          <span v-else class="text-muted">暂无权限配置</span>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 编辑权限对话框 -->
    <el-dialog
      v-model="editPermissionsDialogVisible"
      title="编辑管理员权限"
      width="600px"
    >
      <el-form :model="editPermissionsForm" ref="editPermissionsFormRef" label-width="120px">
        <el-form-item label="管理员邮箱">
          <el-input v-model="currentUser.email" disabled />
        </el-form-item>
        
        <el-form-item label="管理员类型">
          <el-tag type="warning">普通管理员</el-tag>
        </el-form-item>
        
        <el-form-item label="权限菜单">
          <el-checkbox-group v-model="editPermissionsForm.permissions">
            <el-checkbox label="user_approve">审核会员（延长时长）</el-checkbox>
            <el-checkbox label="user_delete">删减会员（删除会员）</el-checkbox>
            <el-checkbox label="content_review">审核内容</el-checkbox>
            <el-checkbox label="content_delete">删减内容</el-checkbox>
            <el-checkbox label="content_view_own">查看自己审核的内容</el-checkbox>
            <el-checkbox label="content_unpublish_own">下架或删除自己审核的内容</el-checkbox>
            <el-checkbox label="template_manage">模板管理</el-checkbox>
            <el-checkbox label="tag_manage">标签管理</el-checkbox>
            <el-checkbox label="category_manage">分类管理</el-checkbox>
            <el-checkbox label="data_source_manage">数据源管理</el-checkbox>
            <el-checkbox label="system_settings">系统设置</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editPermissionsDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="confirmEditPermissions"
        >
          确认保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 新增管理员对话框 -->
    <el-dialog
      v-model="createAdminDialogVisible"
      title="新增管理员"
      width="600px"
    >
      <el-form :model="createAdminForm" :rules="createAdminRules" ref="createAdminFormRef" label-width="120px">
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
        
        <el-form-item label="管理员类型">
          <el-radio-group v-model="createAdminForm.adminType">
            <el-radio label="NORMAL_ADMIN">普通管理员</el-radio>
            <el-radio label="MAIN_ADMIN">主管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item v-if="createAdminForm.adminType === 'NORMAL_ADMIN'" label="权限菜单">
          <el-checkbox-group v-model="createAdminForm.permissions">
            <el-checkbox label="user_approve">审核会员（延长时长）</el-checkbox>
            <el-checkbox label="user_delete">删减会员（删除会员）</el-checkbox>
            <el-checkbox label="content_review">审核内容</el-checkbox>
            <el-checkbox label="content_delete">删减内容</el-checkbox>
            <el-checkbox label="content_view_own">查看自己审核的内容</el-checkbox>
            <el-checkbox label="content_unpublish_own">下架或删除自己审核的内容</el-checkbox>
            <el-checkbox label="template_manage">模板管理</el-checkbox>
            <el-checkbox label="tag_manage">标签管理</el-checkbox>
            <el-checkbox label="category_manage">分类管理</el-checkbox>
            <el-checkbox label="data_source_manage">数据源管理</el-checkbox>
            <el-checkbox label="system_settings">系统设置</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="createAdminDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="confirmCreateAdmin"
        >
          确认创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import {
  getUserList,
  approveUser,
  extendAccount,
  createAdmin,
  deleteUser,
  updateAdminPermissions
} from '@/api/admin'

const authStore = useAuthStore()

// 权限检查
const isMainAdmin = computed(() => authStore.isMainAdmin)
const hasPermission = (permission) => authStore.hasPermission(permission)

// 数据状态
const loading = ref(false)
const submitting = ref(false)
const deletingUserId = ref(null) // 正在删除的用户ID
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
const editPermissionsDialogVisible = ref(false)

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
  confirmPassword: '',
  adminType: 'NORMAL_ADMIN',
  permissions: []
})

// 编辑权限表单
const editPermissionsFormRef = ref(null)
const editPermissionsForm = reactive({
  permissions: []
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
    approveForm.validDays = 30 // 重置表单
    await loadUsers()
  } catch (error) {
    console.error('批准用户失败:', error)
    ElMessage.error(error.response?.data?.message || '批准用户失败')
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
    extendForm.days = 30 // 重置表单
    await loadUsers()
  } catch (error) {
    console.error('延长账号失败:', error)
    ElMessage.error(error.response?.data?.message || '延长账号失败')
  } finally {
    submitting.value = false
  }
}

// 查看详情
const handleViewDetail = (user) => {
  currentUser.value = { ...user }
  detailDialogVisible.value = true
}

// 删除用户
const handleDeleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确认删除用户 "${user.email}"？此操作不可恢复！`,
      '删除用户',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    deletingUserId.value = user.id
    try {
      await deleteUser(user.id)
      ElMessage.success('用户删除成功')
      loadUsers()
    } catch (error) {
      console.error('删除用户失败:', error)
      ElMessage.error(error.response?.data?.message || '删除用户失败')
    } finally {
      deletingUserId.value = null
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
    }
  }
}

// 编辑权限
const handleEditPermissions = (user) => {
  currentUser.value = { ...user }
  // 解析权限列表
  if (user.adminPermissions) {
    try {
      editPermissionsForm.permissions = typeof user.adminPermissions === 'string' 
        ? JSON.parse(user.adminPermissions) 
        : user.adminPermissions
    } catch (e) {
      console.error('解析权限失败:', e)
      editPermissionsForm.permissions = []
    }
  } else {
    editPermissionsForm.permissions = []
  }
  editPermissionsDialogVisible.value = true
}

const confirmEditPermissions = async () => {
  if (!editPermissionsFormRef.value) return
  
  submitting.value = true
  try {
    await updateAdminPermissions(currentUser.value.id, {
      permissions: editPermissionsForm.permissions
    })
    ElMessage.success('权限更新成功')
    editPermissionsDialogVisible.value = false
    loadUsers()
  } catch (error) {
    console.error('更新权限失败:', error)
    ElMessage.error(error.response?.data?.message || '更新权限失败')
  } finally {
    submitting.value = false
  }
}

// 新增管理员
const handleCreateAdmin = () => {
  createAdminForm.email = ''
  createAdminForm.phone = ''
  createAdminForm.password = ''
  createAdminForm.confirmPassword = ''
  createAdminForm.adminType = 'NORMAL_ADMIN'
  createAdminForm.permissions = []
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
        password: createAdminForm.password,
        adminType: createAdminForm.adminType,
        permissions: createAdminForm.permissions
      })
      ElMessage.success('管理员创建成功')
      createAdminDialogVisible.value = false
      // 重置表单
      createAdminForm.email = ''
      createAdminForm.phone = ''
      createAdminForm.password = ''
      createAdminForm.confirmPassword = ''
      createAdminForm.adminType = 'NORMAL_ADMIN'
      createAdminForm.permissions = []
      await loadUsers()
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

// 权限标签映射
const getPermissionLabel = (permission) => {
  const labels = {
    'user_approve': '审核会员',
    'user_delete': '删减会员',
    'content_review': '审核内容',
    'content_delete': '删减内容',
    'content_view_own': '查看自己审核的内容',
    'content_unpublish_own': '下架或删除自己审核的内容',
    'template_manage': '模板管理',
    'tag_manage': '标签管理',
    'category_manage': '分类管理',
    'data_source_manage': '数据源管理',
    'system_settings': '系统设置'
  }
  return labels[permission] || permission
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
