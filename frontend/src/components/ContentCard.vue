<template>
  <div class="content-card" @click="navigateToContent">
    <div class="card-header">
      <h3 class="card-title">{{ content.title }}</h3>
      <span v-if="content.isOriginal" class="badge original">原创</span>
      <span v-else class="badge">转载</span>
    </div>
    
    <div class="card-meta">
      <span v-if="content.authorName" class="author">
        <el-icon><User /></el-icon>
        {{ content.authorName }}
      </span>
      <span v-if="content.publishedAt" class="date">
        <el-icon><Calendar /></el-icon>
        {{ formatDate(content.publishedAt) }}
      </span>
    </div>
    
    <div v-if="content.structuredData && content.structuredData.description" class="card-description">
      {{ truncateText(content.structuredData.description, 150) }}
    </div>
    
    <div class="card-footer">
      <div class="tags" v-if="content.tagNames && content.tagNames.length > 0">
        <span v-for="tag in content.tagNames.slice(0, 3)" :key="tag" class="tag">
          {{ tag }}
        </span>
      </div>
      <div class="card-stats">
        <span class="stat-item" @click.stop>
          <el-icon><View /></el-icon>
          {{ content.viewCount || 0 }}
        </span>
        <span class="stat-item" @click.stop>
          <el-icon><Star /></el-icon>
          {{ content.likeCount || 0 }}
        </span>
        <span class="stat-item" @click.stop>
          <el-icon><ChatDotRound /></el-icon>
          {{ content.commentCount || 0 }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { User, Calendar, View, Star, ChatDotRound } from '@element-plus/icons-vue'

const props = defineProps({
  content: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const navigateToContent = () => {
  router.push(`/content/${props.content.id}`)
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const truncateText = (text, maxLength) => {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}
</script>

<style scoped>
.content-card {
  background: #FFFFFF;
  border: 1px solid #E8EAED;
  border-radius: 8px;
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.content-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
  border-color: #1A73E8;
}

.card-header {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.card-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #333;
  margin: 0;
  flex: 1;
  line-height: 1.4;
}

.badge {
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  background: #F8F9FA;
  color: #666;
  white-space: nowrap;
}

.badge.original {
  background: #E8F5E9;
  color: #2E7D32;
}

.card-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.875rem;
  color: #666;
  margin-bottom: 0.75rem;
  flex-wrap: wrap;
}

.card-meta span {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.card-description {
  color: #555;
  font-size: 0.9375rem;
  line-height: 1.6;
  margin-bottom: 1rem;
  flex: 1;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 0.75rem;
  border-top: 1px solid #F0F0F0;
}

.tags {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  flex: 1;
}

.tag {
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  background: #F0F7FF;
  color: #1A73E8;
  border-radius: 4px;
}

.card-stats {
  display: flex;
  gap: 12px;
  color: #999;
  font-size: 0.8125rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: default;
}

.stat-item .el-icon {
  font-size: 14px;
}

.stat-item:hover {
  color: #1A73E8;
}

.card-meta .el-icon {
  font-size: 14px;
  color: #999;
}

@media (max-width: 768px) {
  .content-card {
    padding: 1rem;
  }
  
  .card-title {
    font-size: 1rem;
  }
  
  .card-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}
</style>
