<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <h1>登录</h1>
        <p>欢迎回到数流精灵</p>
      </div>

      <!-- 账号状态提示 -->
      <el-alert
        v-if="accountStatus"
        :title="accountStatus.title"
        :type="accountStatus.type"
        :description="accountStatus.description"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        size="large"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="loginForm.email"
            placeholder="请输入邮箱地址"
            :prefix-icon="Message"
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            style="width: 100%"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="auth-footer">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const loginFormRef = ref(null)
const loading = ref(false)
const accountStatus = ref(null)

const loginForm = reactive({
  email: '',
  password: ''
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

const loginRules = {
  email: [
    { required: true, validator: validateEmail, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true
    accountStatus.value = null

    const response = await login(loginForm)
    
    console.log('Login response:', response)
    
    // 保存认证信息 (response 结构: { accessToken, refreshToken, user })
    const token = response.accessToken || response.token
    const userData = response.user
    
    console.log('Token:', token)
    console.log('User:', userData)
    
    if (token && userData) {
      authStore.setAuth(userData, token)
      console.log('Auth set, isAuthenticated:', authStore.isAuthenticated)
      ElMessage.success('登录成功')
      
      // 重定向到目标页面或首页
      const redirect = route.query.redirect || '/'
      router.push(redirect)
    } else {
      ElMessage.error('登录响应格式错误')
    }
  } catch (error) {
    if (error.response?.data?.error) {
      const errorData = error.response.data.error
      const errorCode = errorData.code
      
      // 根据错误代码显示不同的账号状态提示
      if (errorCode === 'ACCOUNT_PENDING') {
        accountStatus.value = {
          title: '账号审核中',
          type: 'warning',
          description: '您的账号正在审核中，请耐心等待。审核通过后我们会发送邮件通知您。'
        }
      } else if (errorCode === 'ACCOUNT_EXPIRED') {
        accountStatus.value = {
          title: '账号已到期',
          type: 'error',
          description: '您的账号已到期，请联系管理员延长使用时长。'
        }
      } else if (errorCode === 'ACCOUNT_REJECTED') {
        accountStatus.value = {
          title: '账号审核未通过',
          type: 'error',
          description: '您的账号审核未通过，如有疑问请联系管理员。'
        }
      } else if (errorCode === 'INVALID_CREDENTIALS') {
        accountStatus.value = {
          title: '登录失败',
          type: 'error',
          description: '邮箱或密码错误，请检查后重试。'
        }
      } else {
        ElMessage.error(errorData.message || '登录失败，请稍后重试')
      }
    }
  } finally {
    loading.value = false
  }
}

// 如果已登录，重定向到首页
onMounted(() => {
  if (authStore.isAuthenticated) {
    router.push('/')
  }
})
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

:deep(.el-alert) {
  border-radius: 8px;
}
</style>
