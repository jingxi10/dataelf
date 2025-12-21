<template>
  <div class="favorites-view">
    <el-page-header @back="goBack" title="返回">
      <template #content>
        <span class="page-title">我的收藏</span>
      </template>
    </el-page-header>

    <div class="favorites-content">
      <el-card v-loading="loading">
        <div v-if="favorites.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无收藏内容">
            <el-button type="primary" @click="goHome">去首页看看</el-button>
          </el-empty>
        </div>

        <div v-else class="favorites-grid">
          <ContentCard
            v-for="content in favorites"
            :key="content.id"
            :content="content"
            @click="viewContent(content.id)"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFavorites } from '@/api/interaction'
import { ElMessage } from 'element-plus'
import ContentCard from '@/components/ContentCard.vue'

const router = useRouter()
const favorites = ref([])
const loading = ref(false)

// 加载收藏列表
const loadFavorites = async () => {
  loading.value = true
  try {
    const response = await getFavorites()
    // axios interceptor已经解包了response，直接就是数组或包含data的对象
    if (Array.isArray(response)) {
      favorites.value = response
    } else if (response && response.data) {
      favorites.value = response.data
    } else {
      favorites.value = []
    }
  } catch (error) {
    console.error('加载收藏列表失败:', error)
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

// 查看内容详情
const viewContent = (contentId) => {
  router.push({ name: 'content-detail', params: { id: contentId } })
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 去首页
const goHome = () => {
  router.push({ name: 'home' })
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.favorites-view {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
}

.favorites-content {
  margin-top: 20px;
}

.empty-state {
  padding: 60px 0;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

@media (max-width: 768px) {
  .favorites-grid {
    grid-template-columns: 1fr;
  }
}
</style>
