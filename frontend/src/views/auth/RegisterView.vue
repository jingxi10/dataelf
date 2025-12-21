<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <h1>注册账号</h1>
        <p>创建您的数流精灵账号</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-position="top"
        size="large"
        @submit.prevent="handleRegister"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱地址"
            :prefix-icon="Message"
            clearable
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            :prefix-icon="Phone"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（至少8个字符）"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            style="width: 100%"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="auth-footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>

    <!-- 注册成功提示对话框 -->
    <el-dialog
      v-model="showSuccessDialog"
      title="注册成功"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="success-content">
        <el-icon :size="60" color="#67C23A" style="margin-bottom: 20px">
          <SuccessFilled />
        </el-icon>
        <p class="success-message">
          注册成功！您的账号正在审核中，请留意查看邮箱。
        </p>
        <p class="success-tip">
          审核通过后，我们会发送邮件通知您。
        </p>
      </div>
      <template #footer>
        <el-button type="primary" @click="goToLogin">
          前往登录
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Phone, Lock, SuccessFilled } from '@element-plus/icons-vue'
import { register } from '@/api/auth'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)
const showSuccessDialog = ref(false)

const registerForm = reactive({
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

// 邮箱验证规则
const validateEmail = (rule, value, callback) => {
  const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/
  if (!value) {
    callback(new Error('请输入邮箱地址'))
  } else if (!emailRegex.test(value)) {
    callback(new Error('邮箱格式无效'))
  } else {
    callback()
  }
}

// 手机号验证规则
const validatePhone = (rule, value, callback) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!phoneRegex.test(value)) {
    callback(new Error('手机号格式无效'))
  } else {
    callback()
  }
}

// 密码强度验证规则
const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 8) {
    callback(new Error('密码强度不足，需要至少8个字符'))
  } else if (!/[A-Z]/.test(value)) {
    callback(new Error('密码需要包含至少一个大写字母'))
  } else if (!/[a-z]/.test(value)) {
    callback(new Error('密码需要包含至少一个小写字母'))
  } else if (!/[0-9]/.test(value)) {
    callback(new Error('密码需要包含至少一个数字'))
  } else {
    callback()
  }
}

// 确认密码验证规则
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  email: [
    { required: true, validator: validateEmail, trigger: 'blur' }
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
    loading.value = true

    const { email, phone, password } = registerForm
    await register({ email, phone, password })

    showSuccessDialog.value = true
  } catch (error) {
    if (error.response?.data?.error) {
      const errorData = error.response.data.error
      if (errorData.details && Array.isArray(errorData.details)) {
        // 显示字段级错误
        errorData.details.forEach(detail => {
          ElMessage.error(`${detail.field}: ${detail.message}`)
        })
      } else {
        ElMessage.error(errorData.message || '注册失败，请稍后重试')
      }
    }
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  showSuccessDialog.value = false
  router.push('/login')
}
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.auth-card {
  width: 100%;
  max-width: 450px;
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.auth-header {
  text-align: center;
  margin-bottom: 30px;
}

.auth-header h1 {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.auth-header p {
  font-size: 14px;
  color: #666;
}

.auth-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.auth-footer a {
  color: #667eea;
  text-decoration: none;
  margin-left: 5px;
  font-weight: 500;
}

.auth-footer a:hover {
  text-decoration: underline;
}

.success-content {
  text-align: center;
  padding: 20px 0;
}

.success-message {
  font-size: 16px;
  color: #333;
  margin-bottom: 10px;
  font-weight: 500;
}

.success-tip {
  font-size: 14px;
  color: #666;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #667eea inset;
}

:deep(.el-button--primary) {
  background-color: #667eea;
  border-color: #667eea;
}

:deep(.el-button--primary:hover) {
  background-color: #7c8ff0;
  border-color: #7c8ff0;
}
</style>
