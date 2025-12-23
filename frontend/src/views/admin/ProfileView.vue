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
                <el-icon><Edit /></el-icon>
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
        <el-descriptions-item label="角色">
          <el-tag :type="userInfo.role === 'ADMIN' ? 'danger' : 'info'">
            {{ userInfo.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import axios from '@/api/axios'

const authStore = useAuthStore()

const loading = ref(false)
const saving = ref(false)
const editingNickname = ref(false)
const newNickname = ref('')

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

const loadProfile = async () => {
  loading.value = true
  try {
    const response = await axios.get('/user/profile')
    const data = response.data || response
    Object.assign(userInfo, data)
    // 同步更新 authStore
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
    // 同步更新 authStore
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
