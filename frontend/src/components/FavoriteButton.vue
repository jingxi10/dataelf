<template>
  <el-button
    :type="hasFavorited ? 'warning' : 'default'"
    :loading="loading"
    @click="handleFavorite"
    size="small"
  >
    <el-icon><Collection /></el-icon>
    <span class="count">{{ favoriteCount }}</span>
  </el-button>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Collection } from '@element-plus/icons-vue'
import { interact, removeInteraction } from '@/api/interaction'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({
  contentId: {
    type: Number,
    required: true
  },
  initialFavorited: {
    type: Boolean,
    default: false
  },
  initialCount: {
    type: Number,
    default: 0
  }
})

const authStore = useAuthStore()
const hasFavorited = ref(props.initialFavorited)
const favoriteCount = ref(props.initialCount)
const loading = ref(false)

// 监听props变化
watch(() => props.initialFavorited, (newVal) => {
  hasFavorited.value = newVal
})

watch(() => props.initialCount, (newVal) => {
  favoriteCount.value = newVal
})

const handleFavorite = async () => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    if (hasFavorited.value) {
      // 取消收藏
      await removeInteraction('favorite', props.contentId)
      hasFavorited.value = false
      favoriteCount.value = Math.max(0, favoriteCount.value - 1)
      ElMessage.success('已取消收藏')
    } else {
      // 收藏
      await interact('favorite', props.contentId)
      hasFavorited.value = true
      favoriteCount.value += 1
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.count {
  margin-left: 4px;
}
</style>
