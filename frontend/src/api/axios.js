import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const instance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
instance.interceptors.response.use(
  (response) => {
    // 如果响应包含 success 字段，返回 data 部分
    if (response.data && response.data.success !== undefined) {
      return response.data.data || response.data
    }
    return response.data
  },
  (error) => {
    if (error.response) {
      const { status } = error.response
      
      // 只处理401未授权，自动跳转登录页
      // 其他错误由各组件自行处理，避免双重弹窗
      if (status === 401) {
        const authStore = useAuthStore()
        authStore.clearAuth()
        // 避免重复跳转
        if (window.location.pathname !== '/login') {
          ElMessage.error('登录已过期，请重新登录')
          window.location.href = '/login'
        }
      }
    }
    
    return Promise.reject(error)
  }
)

export default instance
