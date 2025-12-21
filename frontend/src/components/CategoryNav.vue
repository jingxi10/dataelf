<template>
  <nav class="category-nav">
    <div class="nav-container">
      <button 
        class="nav-item" 
        :class="{ active: selectedCategory === null }"
        @click="selectCategory(null)"
      >
        <el-icon><Grid /></el-icon>
        <span>全部</span>
      </button>
      
      <button
        v-for="category in categories"
        :key="category.id"
        class="nav-item"
        :class="{ active: selectedCategory === category.id }"
        @click="selectCategory(category.id)"
      >
        <el-icon><component :is="getCategoryIcon(category.name)" /></el-icon>
        <span>{{ category.name }}</span>
      </button>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllCategories } from '@/api/category'
import { 
  Grid, Cpu, DataAnalysis, Connection, SetUp, 
  Monitor, MagicStick, Cloudy, Document, Folder
} from '@element-plus/icons-vue'

// 分类名称到图标的映射
const categoryIconMap = {
  '科技': Cpu,
  '数据科学': DataAnalysis,
  '区块链': Connection,
  '数据架构': SetUp,
  'Web开发': Monitor,
  '人工智能': MagicStick,
  '云计算': Cloudy,
  '文档': Document
}

const getCategoryIcon = (categoryName) => {
  return categoryIconMap[categoryName] || Folder
}

const emit = defineEmits(['category-change'])

const categories = ref([])
const selectedCategory = ref(null)

const loadCategories = async () => {
  try {
    const response = await getAllCategories()
    // axios拦截器已经解包
    const data = Array.isArray(response) ? response : (response.data || [])
    // Only show top-level categories (level 1)
    categories.value = data.filter(cat => cat.level === 1)
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

const selectCategory = (categoryId) => {
  selectedCategory.value = categoryId
  emit('category-change', categoryId)
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.category-nav {
  background: #FFFFFF;
  border-bottom: 1px solid #E8EAED;
  padding: 1rem 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  gap: 0.5rem;
  overflow-x: auto;
  scrollbar-width: thin;
}

.nav-container::-webkit-scrollbar {
  height: 4px;
}

.nav-container::-webkit-scrollbar-track {
  background: #F8F9FA;
}

.nav-container::-webkit-scrollbar-thumb {
  background: #CCC;
  border-radius: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0.5rem 1rem;
  border: 1px solid #E8EAED;
  background: #FFFFFF;
  color: #333;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  font-size: 0.9375rem;
  font-weight: 500;
}

.nav-item .el-icon {
  font-size: 16px;
}

.nav-item:hover {
  background: #F8F9FA;
  border-color: #1A73E8;
  color: #1A73E8;
}

.nav-item.active {
  background: #1A73E8;
  color: #FFFFFF;
  border-color: #1A73E8;
}

@media (max-width: 768px) {
  .nav-container {
    padding: 0 1rem;
  }
  
  .nav-item {
    font-size: 0.875rem;
    padding: 0.4rem 0.8rem;
  }
}
</style>
