<template>
  <div id="app">
    <router-view v-if="!loading" />
    <div v-else class="app-loading">
      <el-icon class="is-loading" :size="40">
        <Loading />
      </el-icon>
      <p>加载中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { Loading } from '@element-plus/icons-vue'

const authStore = useAuthStore()
const loading = ref(true)

onMounted(async () => {
  // 初始化认证状态
  await authStore.initializeAuth()
  loading.value = false
})
</script>

<style scoped>
#app {
  min-height: 100vh;
  background-color: var(--color-bg-primary);
}

.app-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  gap: var(--spacing-md);
}

.app-loading p {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  font-weight: var(--font-weight-medium);
}
</style>
