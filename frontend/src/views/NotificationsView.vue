<template>
  <div class="notifications-view">
    <div class="container">
      <div class="notifications-header">
        <h1>通知中心</h1>
        <div class="header-actions">
          <el-button 
            v-if="unreadCount > 0"
            type="primary"
            @click="handleMarkAllRead"
          >
            全部标记为已读
          </el-button>
        </div>
      </div>
      
      <el-card class="notifications-card">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="全部通知" name="all">
            <div class="notification-list" v-loading="loading">
              <template v-if="displayNotifications.length > 0">
                <NotificationItem 
                  v-for="notification in displayNotifications" 
                  :key="notification.id"
                  :notification="notification"
                />
              </template>
              
              <el-empty 
                v-else
                description="暂无通知"
                :image-size="120"
              />
            </div>
          </el-tab-pane>
          
          <el-tab-pane name="unread">
            <template #label>
              <span>
                未读通知
                <el-badge 
                  v-if="unreadCount > 0" 
                  :value="unreadCount" 
                  :max="99"
                  style="margin-left: 8px"
                />
              </span>
            </template>
            
            <div class="notification-list" v-loading="loading">
              <template v-if="unreadNotifications.length > 0">
                <NotificationItem 
                  v-for="notification in unreadNotifications" 
                  :key="notification.id"
                  :notification="notification"
                />
              </template>
              
              <el-empty 
                v-else
                description="暂无未读通知"
                :image-size="120"
              />
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useNotificationStore } from '@/stores/notification'
import NotificationItem from '@/components/NotificationItem.vue'

const notificationStore = useNotificationStore()

const activeTab = ref('all')

const notifications = computed(() => notificationStore.notifications)
const unreadNotifications = computed(() => notificationStore.unreadNotifications)
const unreadCount = computed(() => notificationStore.unreadCount)
const loading = computed(() => notificationStore.loading)

const displayNotifications = computed(() => {
  if (activeTab.value === 'unread') {
    return unreadNotifications.value
  }
  return notifications.value
})

const handleTabChange = (tabName) => {
  if (tabName === 'unread') {
    notificationStore.loadUnreadNotifications()
  }
}

const handleMarkAllRead = async () => {
  try {
    await notificationStore.markAllAsRead()
    ElMessage.success('已标记所有通知为已读')
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

onMounted(() => {
  notificationStore.loadNotifications()
})
</script>

<style scoped>
.notifications-view {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 24px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.notifications-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.notifications-header h1 {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.notifications-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.notification-list {
  min-height: 300px;
}

.notification-list :deep(.notification-item:last-child) {
  border-bottom: none;
}

@media (max-width: 768px) {
  .notifications-view {
    padding: 16px 0;
  }
  
  .container {
    padding: 0 16px;
  }
  
  .notifications-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .notifications-header h1 {
    font-size: 24px;
  }
}
</style>
