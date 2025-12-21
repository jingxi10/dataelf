import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as notificationApi from '@/api/notification'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([])
  const unreadCount = ref(0)
  const loading = ref(false)

  // 计算未读通知
  const unreadNotifications = computed(() => 
    notifications.value.filter(n => !n.isRead)
  )

  /**
   * 加载所有通知
   */
  async function loadNotifications() {
    try {
      loading.value = true
      const response = await notificationApi.getUserNotifications()
      notifications.value = response.data || response || []
      updateUnreadCount()
    } catch (error) {
      console.error('加载通知失败:', error)
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载未读通知
   */
  async function loadUnreadNotifications() {
    try {
      const response = await notificationApi.getUnreadNotifications()
      const unreadList = response.data || response || []
      // 合并到现有通知列表
      unreadList.forEach(newNotif => {
        const index = notifications.value.findIndex(n => n.id === newNotif.id)
        if (index === -1) {
          notifications.value.unshift(newNotif)
        }
      })
      updateUnreadCount()
    } catch (error) {
      console.error('加载未读通知失败:', error)
    }
  }

  /**
   * 更新未读通知数量
   */
  async function updateUnreadCount() {
    try {
      const response = await notificationApi.getUnreadCount()
      unreadCount.value = response.count || 0
    } catch (error) {
      console.error('更新未读数量失败:', error)
    }
  }

  /**
   * 标记通知为已读
   */
  async function markAsRead(notificationId) {
    try {
      await notificationApi.markAsRead(notificationId)
      
      // 更新本地状态
      const notification = notifications.value.find(n => n.id === notificationId)
      if (notification) {
        notification.isRead = true
      }
      
      updateUnreadCount()
    } catch (error) {
      console.error('标记已读失败:', error)
      throw error
    }
  }

  /**
   * 标记所有通知为已读
   */
  async function markAllAsRead() {
    try {
      await notificationApi.markAllAsRead()
      
      // 更新本地状态
      notifications.value.forEach(n => {
        n.isRead = true
      })
      
      unreadCount.value = 0
    } catch (error) {
      console.error('标记所有已读失败:', error)
      throw error
    }
  }

  /**
   * 清空通知
   */
  function clearNotifications() {
    notifications.value = []
    unreadCount.value = 0
  }

  return {
    notifications,
    unreadCount,
    loading,
    unreadNotifications,
    loadNotifications,
    loadUnreadNotifications,
    updateUnreadCount,
    markAsRead,
    markAllAsRead,
    clearNotifications
  }
})
