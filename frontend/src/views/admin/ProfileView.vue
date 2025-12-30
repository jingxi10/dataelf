<template>
  <div class="profile-view">
    <el-card class="profile-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>

      <el-descriptions :column="1" border v-loading="loading">
        <el-descriptions-item label="昵称">
          <div class="nickname-row">
            <template v-if="!editingNickname">
              <span>{{ userInfo.nickname || '未设置' }}</span>
              <el-button type="primary" link @click="startEditNickname">
                <el-icon style="animation: none;"><Edit /></el-icon>
                修改
              </el-button>
            </template>
            <template v-else>
              <el-input
                v-model="newNickname"
                placeholder="请输入昵称"
                maxlength="50"
                show-word-limit
                style="width: 200px; margin-right: 10px;"
              />
              <el-button type="primary" size="small" @click="saveNickname" :loading="saving">
                保存
              </el-button>
              <el-button size="small" @click="cancelEditNickname">
                取消
              </el-button>
            </template>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="邮箱">
          {{ userInfo.email }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号">
          {{ userInfo.phone }}
        </el-descriptions-item>
        <el-descriptions-item label="密码">
          <el-button type="primary" link @click="showPasswordDialog">
            <el-icon style="animation: none;"><Lock /></el-icon>
            修改密码
          </el-button>
        </el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag v-if="userInfo.role === 'USER'" type="info">会员</el-tag>
          <el-tag v-else-if="userInfo.adminType === 'MAIN_ADMIN'" type="danger">主管理员</el-tag>
          <el-tag v-else-if="userInfo.adminType === 'NORMAL_ADMIN'" type="warning">普通管理员</el-tag>
          <el-tag v-else type="info">普通用户</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="账号状态">
          <el-tag :type="getStatusType(userInfo.status)">
            {{ getStatusText(userInfo.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">
          {{ formatDate(userInfo.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="到期时间">
          <span :class="getExpiryClass(userInfo.expiresAt)">
            {{ formatDate(userInfo.expiresAt) }}
            <template v-if="userInfo.expiresAt">
              (剩余 {{ getRemainingDays(userInfo.expiresAt) }} 天)
            </template>
          </span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="450px"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPassword" :loading="savingPassword">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import axios from '@/api/axios'

const authStore = useAuthStore()

const loading = ref(false)
const saving = ref(false)
const editingNickname = ref(false)
const newNickname = ref('')
const passwordDialogVisible = ref(false)
const savingPassword = ref(false)
const passwordFormRef = ref(null)

const userInfo = reactive({
  id: '',
  email: '',
  phone: '',
  nickname: '',
  role: '',
  status: '',
  createdAt: '',
  expiresAt: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度必须在6-50位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const loadProfile = async () => {
  loading.value = true
  try {
    const response = await axios.get('/user/profile')
    const data = response.data || response
    Object.assign(userInfo, data)
    if (authStore.user) {
      authStore.user.nickname = data.nickname
    }
  } catch (error) {
    console.error('加载个人信息失败:', error)
    ElMessage.error('加载个人信息失败')
  } finally {
    loading.value = false
  }
}

const startEditNickname = () => {
  newNickname.value = userInfo.nickname || ''
  editingNickname.value = true
}

const cancelEditNickname = () => {
  editingNickname.value = false
  newNickname.value = ''
}

const saveNickname = async () => {
  saving.value = true
  try {
    const response = await axios.put('/user/nickname', { nickname: newNickname.value })
    const data = response.data || response
    userInfo.nickname = data.nickname
    if (authStore.user) {
      authStore.user.nickname = data.nickname
    }
    ElMessage.success('昵称更新成功')
    editingNickname.value = false
  } catch (error) {
    console.error('更新昵称失败:', error)
    ElMessage.error('更新昵称失败')
  } finally {
    saving.value = false
  }
}

const showPasswordDialog = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
}

const submitPassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    savingPassword.value = true
    try {
      await axios.put('/user/password', {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      ElMessage.success('密码修改成功')
      passwordDialogVisible.value = false
    } catch (error) {
      console.error('修改密码失败:', error)
      ElMessage.error(error.response?.data?.message || '修改密码失败')
    } finally {
      savingPassword.value = false
    }
  })
}

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
  return `${year}-${month}-${day} ${hours}:${minutes}`
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

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-view {
  width: 100%;
}

.profile-card {
  margin-bottom: 20px;
  width: 100%;
}

.card-header {
  font-size: 16px;
  font-weight: 500;
}

.nickname-row {
  display: flex;
  align-items: center;
  gap: 10px;
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
