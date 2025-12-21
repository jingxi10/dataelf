<template>
  <el-popover
    :visible="visible"
    placement="bottom-end"
    :width="400"
    trigger="click"
    popper-class="notification-popover"
    @show="handleShow"
    @hide="handleHide"
  >
    <template #reference>
      <NotificationBadge @click="toggleDropdown" />
    </template>
    
    <div class="notification-dropdown">
      <div class="notification-header">
        <span class="notification-title">通知</span>
        <el-button 
          v-if="unreadCount > 0"
          text 
          size="small"
          @click="handleMarkAllRead"
        >
          全部已读
        </el-button>
      </div>
      
      <el-divider style="margin: 0" />
      
      <div class="notification-list" v-loading="loading">
        <template v-if="notifications.length > 0">
          <NotificationItem 
            v-for="notification in displayNotifications" 
            :key="notification.id"
            :notification="notification"
            @click="handleNotificationClick"
          />
        </template>
        
        <el-empty 
          v-else
          description="暂无通知"
          :image-size="80"
        />
      </div>
      
      <el-divider style="margin: 0" />
      
      <div class="notification-footer">
        <el-button text @click="handleViewAll">
          查看全部通知
        </el-button>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useNotificationStore } from '@/stores/notification'
import NotificationBadge from './NotificationBadge.vue'
import NotificationItem from './NotificationItem.vue'

const router = useRouter()
const notificationStore = useNotificationStore()

const visible = ref(false)

const notifications = computed(() => notificationStore.notifications)
const unreadCount = computed(() => notificationStore.unreadCount)
const loading = computed(() => notificationStore.loading)

// 只显示最近的10条通知
const displayNotifications = computed(() => 
  notifications.value.slice(0, 10)
)

const toggleDropdown = () => {
  visible.value = !visible.value
}

const handleShow = async () => {
  // 加载通知
  await notificationStore.loadNotifications()
}

const handleHide = () => {
  visible.value = false
}

const handleMarkAllRead = async () => {
  try {
    await notificationStore.markAllAsRead()
    ElMessage.success('已标记所有通知为已读')
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

const handleNotificationClick = () => {
  visible.value = false
}

const handleViewAll = () => {
  visible.value = false
  router.push('/notifications')
}

// 定期更新未读数量
onMounted(() => {
  notificationStore.updateUnreadCount()
  
  // 每30秒更新一次未读数量
  const interval = setInterval(() => {
    notificationStore.updateUnreadCount()
  }, 30000)
  
  // 组件卸载时清除定时器
  return () => clearInterval(interval)
})
</script>

<style>
.notification-popover {
  padding: 0 !important;
}
</style>

<style scoped>
.notification-dropdown {
  display: flex;
  flex-direction: column;
  max-height: 500px;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
}

.notification-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.notification-list {
  flex: 1;
  overflow-y: auto;
  max-height: 400px;
}

.notification-list::-webkit-scrollbar {
  width: 6px;
}

.notification-list::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

.notification-list::-webkit-scrollbar-thumb:hover {
  background-color: #c0c4cc;
}

.notification-footer {
  padding: 12px;
  text-align: center;
}
</style>
