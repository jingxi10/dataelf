<template>
  <el-dialog
    v-model="dialogVisible"
    title="导出内容"
    width="500px"
    :before-close="handleClose"
  >
    <div class="export-options">
      <p class="export-description">
        选择导出格式，系统将生成对应格式的文件供您下载。
      </p>

      <el-radio-group v-model="selectedFormat" class="format-options">
        <el-radio label="jsonld" size="large">
          <div class="format-option">
            <div class="format-title">JSON-LD</div>
            <div class="format-desc">符合Schema.org标准的结构化数据</div>
          </div>
        </el-radio>
        
        <el-radio label="html" size="large">
          <div class="format-option">
            <div class="format-title">HTML</div>
            <div class="format-desc">包含完整语义化标记的网页文件</div>
          </div>
        </el-radio>
        
        <el-radio label="markdown" size="large">
          <div class="format-option">
            <div class="format-title">Markdown</div>
            <div class="format-desc">标准Markdown格式文本文件</div>
          </div>
        </el-radio>
        
        <el-radio label="csv" size="large">
          <div class="format-option">
            <div class="format-title">CSV</div>
            <div class="format-desc">结构化字段转换为表格数据</div>
          </div>
        </el-radio>
        
        <el-radio label="word" size="large">
          <div class="format-option">
            <div class="format-title">Word文档</div>
            <div class="format-desc">导出为Microsoft Word格式(.docx)</div>
          </div>
        </el-radio>
      </el-radio-group>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          :disabled="!selectedFormat"
          @click="handleExport"
        >
          {{ exporting ? '导出中...' : '导出' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  exportAsJsonLd,
  exportAsHtml,
  exportAsMarkdown,
  exportAsCsv,
  exportAsWord
} from '@/api/content'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  contentId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'exported'])

const dialogVisible = ref(props.modelValue)
const selectedFormat = ref('jsonld')
const exporting = ref(false)

// 监听modelValue变化
watch(() => props.modelValue, (newVal) => {
  dialogVisible.value = newVal
})

// 监听dialogVisible变化
watch(dialogVisible, (newVal) => {
  emit('update:modelValue', newVal)
})

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
}

// 下载文件
const downloadFile = (blob, filename) => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

// 导出处理
const handleExport = async () => {
  if (!selectedFormat.value) {
    ElMessage.warning('请选择导出格式')
    return
  }

  exporting.value = true
  
  try {
    let response
    let filename
    
    switch (selectedFormat.value) {
      case 'jsonld':
        response = await exportAsJsonLd(props.contentId)
        filename = `content-${props.contentId}.jsonld`
        break
      case 'html':
        response = await exportAsHtml(props.contentId)
        filename = `content-${props.contentId}.html`
        break
      case 'markdown':
        response = await exportAsMarkdown(props.contentId)
        filename = `content-${props.contentId}.md`
        break
      case 'csv':
        response = await exportAsCsv(props.contentId)
        filename = `content-${props.contentId}.csv`
        break
      case 'word':
        response = await exportAsWord(props.contentId)
        filename = `content-${props.contentId}.docx`
        break
      default:
        throw new Error('不支持的导出格式')
    }

    // 下载文件
    downloadFile(response.data || response, filename)
    
    ElMessage.success('导出成功')
    emit('exported', selectedFormat.value)
    handleClose()
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error(error.response?.data?.message || '导出失败，请稍后重试')
  } finally {
    exporting.value = false
  }
}
</script>

<style scoped>
.export-options {
  padding: 10px 0;
}

.export-description {
  color: #606266;
  font-size: 14px;
  margin-bottom: 20px;
  line-height: 1.6;
}

.format-options {
  width: 100%;
}

.format-options :deep(.el-radio) {
  width: 100%;
  margin-right: 0;
  margin-bottom: 12px;
  padding: 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: all 0.3s;
}

.format-options :deep(.el-radio:hover) {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.format-options :deep(.el-radio.is-checked) {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.format-option {
  margin-left: 8px;
}

.format-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.format-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.4;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
