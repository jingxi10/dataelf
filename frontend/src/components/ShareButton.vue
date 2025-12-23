<template>
  <el-button
    type="default"
    @click="handleShare"
    size="small"
  >
    <el-icon><Share /></el-icon>
    分享
  </el-button>

  <!-- 分享对话框 -->
  <ShareDialog
    v-model="dialogVisible"
    :content-id="contentId"
    @shared="handleShared"
  />
</template>

<script setup>
import { ref } from 'vue'
import { Share } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import ShareDialog from './ShareDialog.vue'

const props = defineProps({
  contentId: {
    type: Number,
    required: true
  }
})

const loading = ref(false)
const dialogVisible = ref(false)

const handleShare = () => {
  dialogVisible.value = true
}

const handleShared = (platform) => {
  console.log('Shared to:', platform)
}
</script>

<style scoped>
/* 样式已移至ShareDialog组件 */
</style>
