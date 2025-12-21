import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || null)
  const loading = ref(false)
  const initialized = ref(false)

  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isPending = computed(() => user.value?.status === 'PENDING')
  const isExpired = computed(() => user.value?.status === 'EXPIRED')

  /**
   * 设置认证信息
   */
  function setAuth(userData, authToken) {
    user.value = userData
    token.value = authToken
    localStorage.setItem('token', authToken)
  }

  /**
   * 清除认证信息
   */
  function clearAuth() {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
    initialized.value = false
  }

  /**
   * 初始化用户信息（从token恢复）
   */
  async function initializeAuth() {
    if (!token.value || initialized.value) {
      initialized.value = true
      return
    }

    try {
      loading.value = true
      const response = await authApi.getCurrentUser()
      console.log('initializeAuth response:', response)
      user.value = response.data || response
      console.log('User set to:', user.value)
      initialized.value = true
    } catch (error) {
      console.error('初始化用户信息失败:', error)
      // Token无效，清除认证信息
      clearAuth()
      initialized.value = true
    } finally {
      loading.value = false
    }
  }

  /**
   * 用户登录
   */
  async function login(credentials) {
    try {
      loading.value = true
      const response = await authApi.login(credentials)
      
      // 处理不同的响应格式 (后端返回 accessToken)
      const authToken = response.accessToken || response.token || response.data?.accessToken || response.data?.token
      const userData = response.user || response.data?.user
      
      if (authToken && userData) {
        setAuth(userData, authToken)
        return { success: true, user: userData }
      } else {
        throw new Error('登录响应格式错误')
      }
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 用户注册
   */
  async function register(userData) {
    try {
      loading.value = true
      const response = await authApi.register(userData)
      return response
    } catch (error) {
      console.error('注册失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 用户登出
   */
  async function logout() {
    try {
      loading.value = true
      await authApi.logout()
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      clearAuth()
      loading.value = false
    }
  }

  /**
   * 刷新用户信息
   */
  async function refreshUser() {
    if (!token.value) return

    try {
      const response = await authApi.getCurrentUser()
      user.value = response.data || response
    } catch (error) {
      console.error('刷新用户信息失败:', error)
      clearAuth()
    }
  }

  return {
    user,
    token,
    loading,
    initialized,
    isAuthenticated,
    isAdmin,
    isPending,
    isExpired,
    setAuth,
    clearAuth,
    initializeAuth,
    login,
    register,
    logout,
    refreshUser
  }
})
