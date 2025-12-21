<template>
  <div class="tag-cloud">
    <div v-if="loading" class="loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <div v-else-if="tags.length > 0" class="tags-container">
      <el-tag
        v-for="tag in displayTags"
        :key="tag.id"
        :size="getTagSize(tag.usageCount)"
        :type="getTagType(tag.usageCount)"
        :effect="effect"
        class="tag-item"
        :class="{ 'tag-clickable': clickable }"
        @click="handleTagClick(tag)"
      >
        {{ tag.name }}
        <span v-if="showCount" class="tag-count">({{ tag.usageCount }})</span>
      </el-tag>
    </div>

    <el-empty v-else description="暂无标签" :image-size="80" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { getPopularTags, getAllTags } from '@/api/tag'

const props = defineProps({
  // 显示模式：popular（热门）或 all（全部）
  mode: {
    type: String,
    default: 'popular',
    validator: (value) => ['popular', 'all'].includes(value)
  },
  // 最大显示数量
  maxTags: {
    type: Number,
    default: 30
  },
  // 是否显示使用次数
  showCount: {
    type: Boolean,
    default: true
  },
  // 是否可点击
  clickable: {
    type: Boolean,
    default: true
  },
  // 标签效果
  effect: {
    type: String,
    default: 'light',
    validator: (value) => ['light', 'dark', 'plain'].includes(value)
  }
})

const emit = defineEmits(['tag-click'])

const loading = ref(false)
const tags = ref([])

const displayTags = computed(() => {
  return tags.value.slice(0, props.maxTags)
})

const loadTags = async () => {
  loading.value = true
  try {
    const response = props.mode === 'popular' 
      ? await getPopularTags() 
      : await getAllTags()
    
    // axios拦截器已经解包
    tags.value = Array.isArray(response) ? response : (response.data || [])
  } catch (error) {
    console.error('Failed to load tags:', error)
  } finally {
    loading.value = false
  }
}

const getTagSize = (usageCount) => {
  if (usageCount >= 50) return 'large'
  if (usageCount >= 20) return 'default'
  return 'small'
}

const getTagType = (usageCount) => {
  if (usageCount >= 50) return 'danger'
  if (usageCount >= 30) return 'warning'
  if (usageCount >= 10) return 'success'
  return 'info'
}

const handleTagClick = (tag) => {
  if (props.clickable) {
    emit('tag-click', tag)
  }
}

onMounted(() => {
  loadTags()
})

defineExpose({
  loadTags
})
</script>

<style scoped>
.tag-cloud {
  width: 100%;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  color: #909399;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 8px 0;
}

.tag-item {
  transition: all 0.3s ease;
}

.tag-clickable {
  cursor: pointer;
}

.tag-clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.tag-count {
  margin-left: 4px;
  font-size: 0.9em;
  opacity: 0.8;
}

:deep(.el-tag--large) {
  font-size: 16px;
  padding: 8px 16px;
}

:deep(.el-tag--default) {
  font-size: 14px;
  padding: 6px 12px;
}

:deep(.el-tag--small) {
  font-size: 12px;
  padding: 4px 8px;
}
</style>
