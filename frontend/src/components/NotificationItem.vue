<template>
  <div 
    class="notification-item" 
    :class="{ 'unread': !notification.isRead }"
    @click="handleClick"
  >
    <div class="notification-icon">
      <el-icon :size="20" :color="getIconColor()">
        <component :is="getIcon()" />
      </el-icon>
    </div>
    
    <div class="notification-content">
      <div class="notification-title">{{ notification.title }}</div>
      <div class="notification-message">{{ notification.message }}</div>
      <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
    </div>
    
    <div class="notification-status">
      <span v-if="!notification.isRead" class="unread-dot"></span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { 
  SuccessFilled, 
  WarningFilled, 
  InfoFilled,
  Clock
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notification'

const props = defineProps({
  notification: {
    type: Object,
    required: true
  }
})

const router = useRouter()
const notificationStore = useNotificationStore()

const getIcon = () => {
  const typeMap = {
    'ACCOUNT_APPROVED': SuccessFilled,
    'CONTENT_APPROVED': SuccessFilled,
    'CONTENT_REJECTED': WarningFilled,
    'ACCOUNT_EXPIRING': Clock,
    'ACCOUNT_EXTENDED': InfoFilled
  }
  return typeMap[props.notification.type] || InfoFilled
}

const getIconColor = () => {
  const colorMap = {
    'ACCOUNT_APPROVED': '#67c23a',
    'CONTENT_APPROVED': '#67c23a',
    'CONTENT_REJECTED': '#f56c6c',
    'ACCOUNT_EXPIRING': '#e6a23c',
    'ACCOUNT_EXTENDED': '#409eff'
  }
  return colorMap[props.notification.type] || '#909399'
}

const formatTime = (dateTime) => {
  if (!dateTime) return ''
  
  const date = new Date(dateTime)
  const now = new Date()
  const diff = now - date
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return date.toLocaleDateString('zh-CN')
}

const handleClick = async () => {
  // 标记为已读
  if (!props.notification.isRead) {
    try {
      await notificationStore.markAsRead(props.notification.id)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  
  // 如果有关联内容，跳转到内容详情页
  if (props.notification.relatedContentId) {
    router.push(`/content/${props.notification.relatedContentId}`)
  }
}
</script>

<style scoped>
.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #ecf5ff;
}

.notification-item.unread:hover {
  background-color: #d9ecff;
}

.notification-icon {
  flex-shrink: 0;
  margin-right: 12px;
  margin-top: 2px;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.notification-message {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 4px;
  word-wrap: break-word;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.notification-status {
  flex-shrink: 0;
  margin-left: 8px;
  margin-top: 6px;
}

.unread-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #409eff;
}
</style>
