<template>
  <el-badge 
    :value="displayCount" 
    :hidden="unreadCount === 0"
    :max="99"
    class="notification-badge"
  >
    <el-button 
      :icon="Bell" 
      circle
      @click="handleClick"
      :class="{ 'has-unread': unreadCount > 0 }"
    />
  </el-badge>
</template>

<script setup>
import { computed } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import { useNotificationStore } from '@/stores/notification'

const notificationStore = useNotificationStore()

const emit = defineEmits(['click'])

const unreadCount = computed(() => notificationStore.unreadCount)

const displayCount = computed(() => {
  return unreadCount.value > 99 ? '99+' : unreadCount.value
})

const handleClick = () => {
  emit('click')
}
</script>

<style scoped>
.notification-badge {
  cursor: pointer;
}

.notification-badge :deep(.el-badge__content) {
  background-color: #f56c6c;
  border: 2px solid #fff;
}

.notification-badge .el-button.has-unread {
  color: #409eff;
}

.notification-badge .el-button:hover {
  background-color: #ecf5ff;
}
</style>
