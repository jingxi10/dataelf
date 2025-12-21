<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <el-icon :size="120" class="not-found-icon">
        <WarningFilled />
      </el-icon>
      <h1 class="not-found-title">404</h1>
      <p class="not-found-message">抱歉，您访问的页面不存在</p>
      <div class="not-found-actions">
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goBack">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { WarningFilled } from '@element-plus/icons-vue'

const router = useRouter()

function goHome() {
  router.push({ name: 'home' })
}

function goBack() {
  router.back()
}
</script>

<style scoped>
.not-found-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.not-found-content {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  max-width: 500px;
}

.not-found-icon {
  color: #667eea;
  margin-bottom: 20px;
}

.not-found-title {
  font-size: 72px;
  font-weight: bold;
  color: #333;
  margin: 0 0 16px 0;
}

.not-found-message {
  font-size: 18px;
  color: #666;
  margin: 0 0 32px 0;
}

.not-found-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}
</style>
