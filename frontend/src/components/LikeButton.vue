<template>
  <el-button
    :type="hasLiked ? 'primary' : 'default'"
    :icon="hasLiked ? 'el-icon-star-on' : 'el-icon-star-off'"
    @click="handleLike"
    size="small"
  >
    <el-icon><Star /></el-icon>
    <span class="count">{{ likeCount }}</span>
  </el-button>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Star } from '@element-plus/icons-vue'
import { interact, removeInteraction } from '@/api/interaction'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({
  contentId: {
    type: Number,
    required: true
  },
  initialLiked: {
    type: Boolean,
    default: false
  },
  initialCount: {
    type: Number,
    default: 0
  }
})

const authStore = useAuthStore()
const hasLiked = ref(props.initialLiked)
const likeCount = ref(props.initialCount)
const loading = ref(false)

// 监听props变化
watch(() => props.initialLiked, (newVal) => {
  hasLiked.value = newVal
})

watch(() => props.initialCount, (newVal) => {
  likeCount.value = newVal
})

const handleLike = async () => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    if (hasLiked.value) {
      // 取消点赞
      await removeInteraction('like', props.contentId)
      hasLiked.value = false
      likeCount.value = Math.max(0, likeCount.value - 1)
      ElMessage.success('已取消点赞')
    } else {
      // 点赞
      await interact('like', props.contentId)
      hasLiked.value = true
      likeCount.value += 1
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
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
