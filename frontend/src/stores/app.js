import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 全局加载状态
  const globalLoading = ref(false)
  
  // 侧边栏折叠状态（用于管理后台）
  const sidebarCollapsed = ref(false)
  
  // 移动端侧边栏显示状态
  const mobileSidebarVisible = ref(false)
  
  // 系统配置
  const systemConfig = ref({
    siteName: '数流精灵',
    logo: null,
    description: '专为AI优化的结构化数据平台'
  })

  /**
   * 设置全局加载状态
   */
  function setGlobalLoading(loading) {
    globalLoading.value = loading
  }

  /**
   * 切换侧边栏折叠状态
   */
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  /**
   * 设置侧边栏折叠状态
   */
  function setSidebarCollapsed(collapsed) {
    sidebarCollapsed.value = collapsed
  }

  /**
   * 切换移动端侧边栏显示
   */
  function toggleMobileSidebar() {
    mobileSidebarVisible.value = !mobileSidebarVisible.value
  }

  /**
   * 设置移动端侧边栏显示状态
   */
  function setMobileSidebarVisible(visible) {
    mobileSidebarVisible.value = visible
  }

  /**
   * 更新系统配置
   */
  function updateSystemConfig(config) {
    systemConfig.value = { ...systemConfig.value, ...config }
  }

  return {
    globalLoading,
    sidebarCollapsed,
    mobileSidebarVisible,
    systemConfig,
    setGlobalLoading,
    toggleSidebar,
    setSidebarCollapsed,
    toggleMobileSidebar,
    setMobileSidebarVisible,
    updateSystemConfig
  }
})
