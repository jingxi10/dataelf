<template>
  <div class="rich-text-editor">
    <!-- 工具栏 -->
    <div class="editor-toolbar">
      <el-button-group>
        <el-tooltip content="加粗" placement="top">
          <el-button size="small" @click="execCommand('bold')">
            <el-icon><EditPen /></el-icon>
            <strong>B</strong>
          </el-button>
        </el-tooltip>
        <el-tooltip content="斜体" placement="top">
          <el-button size="small" @click="execCommand('italic')">
            <em>I</em>
          </el-button>
        </el-tooltip>
        <el-tooltip content="下划线" placement="top">
          <el-button size="small" @click="execCommand('underline')">
            <u>U</u>
          </el-button>
        </el-tooltip>
      </el-button-group>
      
      <el-divider direction="vertical" />
      
      <el-button-group>
        <el-tooltip content="标题1" placement="top">
          <el-button size="small" @click="execCommand('formatBlock', 'h1')">H1</el-button>
        </el-tooltip>
        <el-tooltip content="标题2" placement="top">
          <el-button size="small" @click="execCommand('formatBlock', 'h2')">H2</el-button>
        </el-tooltip>
        <el-tooltip content="标题3" placement="top">
          <el-button size="small" @click="execCommand('formatBlock', 'h3')">H3</el-button>
        </el-tooltip>
        <el-tooltip content="段落" placement="top">
          <el-button size="small" @click="execCommand('formatBlock', 'p')">P</el-button>
        </el-tooltip>
      </el-button-group>
      
      <el-divider direction="vertical" />
      
      <el-button-group>
        <el-tooltip content="无序列表" placement="top">
          <el-button size="small" @click="execCommand('insertUnorderedList')">
            <el-icon><List /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="有序列表" placement="top">
          <el-button size="small" @click="execCommand('insertOrderedList')">
            <el-icon><Memo /></el-icon>
          </el-button>
        </el-tooltip>
      </el-button-group>
      
      <el-divider direction="vertical" />
      
      <el-button-group>
        <el-tooltip content="插入链接" placement="top">
          <el-button size="small" @click="showLinkDialog">
            <el-icon><Link /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="插入图片" placement="top">
          <el-button size="small" @click="triggerImageUpload">
            <el-icon><Picture /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="插入视频" placement="top">
          <el-button size="small" @click="triggerVideoUpload">
            <el-icon><VideoCamera /></el-icon>
          </el-button>
        </el-tooltip>
      </el-button-group>
      
      <el-divider direction="vertical" />
      
      <el-button-group>
        <el-tooltip content="撤销" placement="top">
          <el-button size="small" @click="execCommand('undo')">
            <el-icon><RefreshLeft /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="重做" placement="top">
          <el-button size="small" @click="execCommand('redo')">
            <el-icon><RefreshRight /></el-icon>
          </el-button>
        </el-tooltip>
      </el-button-group>
    </div>
    
    <!-- 编辑区域 -->
    <div
      ref="editorRef"
      class="editor-content"
      contenteditable="true"
      :placeholder="placeholder"
      @input="handleInput"
      @paste="handlePaste"
      @drop="handleDrop"
      @dragover.prevent
    ></div>
    
    <!-- 隐藏的文件输入 -->
    <input
      ref="imageInputRef"
      type="file"
      accept="image/*"
      style="display: none"
      @change="handleImageSelect"
    />
    <input
      ref="videoInputRef"
      type="file"
      accept="video/*"
      style="display: none"
      @change="handleVideoSelect"
    />
    
    <!-- 链接对话框 -->
    <el-dialog v-model="linkDialogVisible" title="插入链接" width="400px">
      <el-form :model="linkForm" label-width="80px">
        <el-form-item label="链接文字">
          <el-input v-model="linkForm.text" placeholder="请输入链接文字" />
        </el-form-item>
        <el-form-item label="链接地址">
          <el-input v-model="linkForm.url" placeholder="https://" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="linkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="insertLink">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 上传进度 -->
    <el-dialog v-model="uploadingVisible" title="上传中" width="300px" :close-on-click-modal="false">
      <el-progress :percentage="uploadProgress" :status="uploadStatus" />
      <p class="upload-tip">{{ uploadTip }}</p>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  EditPen, List, Memo, Link, Picture, VideoCamera,
  RefreshLeft, RefreshRight
} from '@element-plus/icons-vue'
import { uploadImage, uploadVideo } from '@/api/upload'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入内容...'
  }
})

const emit = defineEmits(['update:modelValue'])

const editorRef = ref(null)
const imageInputRef = ref(null)
const videoInputRef = ref(null)

// 链接对话框
const linkDialogVisible = ref(false)
const linkForm = ref({ text: '', url: '' })

// 上传状态
const uploadingVisible = ref(false)
const uploadProgress = ref(0)
const uploadStatus = ref('')
const uploadTip = ref('')

// 初始化内容
onMounted(() => {
  if (props.modelValue && editorRef.value) {
    editorRef.value.innerHTML = props.modelValue
  }
})

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  if (editorRef.value && editorRef.value.innerHTML !== newVal) {
    editorRef.value.innerHTML = newVal || ''
  }
})

// 执行编辑命令
function execCommand(command, value = null) {
  document.execCommand(command, false, value)
  editorRef.value?.focus()
  handleInput()
}

// 处理输入
function handleInput() {
  const html = editorRef.value?.innerHTML || ''
  emit('update:modelValue', html)
}

// 处理粘贴
function handlePaste(e) {
  // 检查是否有图片
  const items = e.clipboardData?.items
  if (items) {
    for (const item of items) {
      if (item.type.startsWith('image/')) {
        e.preventDefault()
        const file = item.getAsFile()
        if (file) {
          handleImageUpload(file)
        }
        return
      }
    }
  }
  
  // 普通文本粘贴，清除格式
  e.preventDefault()
  const text = e.clipboardData?.getData('text/plain') || ''
  document.execCommand('insertText', false, text)
}

// 处理拖放
function handleDrop(e) {
  e.preventDefault()
  const files = e.dataTransfer?.files
  if (files && files.length > 0) {
    const file = files[0]
    if (file.type.startsWith('image/')) {
      handleImageUpload(file)
    } else if (file.type.startsWith('video/')) {
      handleVideoUpload(file)
    }
  }
}

// 触发图片上传
function triggerImageUpload() {
  imageInputRef.value?.click()
}

// 触发视频上传
function triggerVideoUpload() {
  videoInputRef.value?.click()
}

// 处理图片选择
function handleImageSelect(e) {
  const file = e.target.files?.[0]
  if (file) {
    handleImageUpload(file)
  }
  e.target.value = ''
}

// 处理视频选择
function handleVideoSelect(e) {
  const file = e.target.files?.[0]
  if (file) {
    handleVideoUpload(file)
  }
  e.target.value = ''
}

// 上传图片
async function handleImageUpload(file) {
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过10MB')
    return
  }
  
  uploadingVisible.value = true
  uploadProgress.value = 0
  uploadStatus.value = ''
  uploadTip.value = '正在上传图片...'
  
  try {
    // 模拟进度
    const progressInterval = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += 10
      }
    }, 100)
    
    const response = await uploadImage(file)
    
    clearInterval(progressInterval)
    uploadProgress.value = 100
    uploadStatus.value = 'success'
    uploadTip.value = '上传成功！'
    
    // 获取URL
    const url = response.url || response.data?.url
    if (url) {
      insertImage(url, file.name)
    }
    
    setTimeout(() => {
      uploadingVisible.value = false
    }, 500)
  } catch (error) {
    console.error('图片上传失败:', error)
    uploadStatus.value = 'exception'
    uploadTip.value = '上传失败'
    ElMessage.error('图片上传失败')
    
    setTimeout(() => {
      uploadingVisible.value = false
    }, 1000)
  }
}

// 上传视频
async function handleVideoUpload(file) {
  if (file.size > 100 * 1024 * 1024) {
    ElMessage.error('视频大小不能超过100MB')
    return
  }
  
  uploadingVisible.value = true
  uploadProgress.value = 0
  uploadStatus.value = ''
  uploadTip.value = '正在上传视频...'
  
  try {
    const progressInterval = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += 5
      }
    }, 200)
    
    const response = await uploadVideo(file)
    
    clearInterval(progressInterval)
    uploadProgress.value = 100
    uploadStatus.value = 'success'
    uploadTip.value = '上传成功！'
    
    const url = response.url || response.data?.url
    if (url) {
      insertVideo(url)
    }
    
    setTimeout(() => {
      uploadingVisible.value = false
    }, 500)
  } catch (error) {
    console.error('视频上传失败:', error)
    uploadStatus.value = 'exception'
    uploadTip.value = '上传失败'
    ElMessage.error('视频上传失败')
    
    setTimeout(() => {
      uploadingVisible.value = false
    }, 1000)
  }
}

// 插入图片
function insertImage(url, alt = '') {
  const img = `<img src="${url}" alt="${alt}" style="max-width: 100%; height: auto;" />`
  document.execCommand('insertHTML', false, img)
  handleInput()
}

// 插入视频
function insertVideo(url) {
  const video = `<video src="${url}" controls style="max-width: 100%;"></video>`
  document.execCommand('insertHTML', false, video)
  handleInput()
}

// 显示链接对话框
function showLinkDialog() {
  const selection = window.getSelection()
  linkForm.value.text = selection?.toString() || ''
  linkForm.value.url = ''
  linkDialogVisible.value = true
}

// 插入链接
function insertLink() {
  if (!linkForm.value.url) {
    ElMessage.warning('请输入链接地址')
    return
  }
  
  const text = linkForm.value.text || linkForm.value.url
  const url = linkForm.value.url.startsWith('http') ? linkForm.value.url : 'https://' + linkForm.value.url
  const link = `<a href="${url}" target="_blank" rel="noopener noreferrer">${text}</a>`
  
  editorRef.value?.focus()
  document.execCommand('insertHTML', false, link)
  
  linkDialogVisible.value = false
  handleInput()
}

// 暴露方法
defineExpose({
  getContent: () => editorRef.value?.innerHTML || '',
  setContent: (html) => {
    if (editorRef.value) {
      editorRef.value.innerHTML = html
      handleInput()
    }
  },
  focus: () => editorRef.value?.focus()
})
</script>

<style scoped>
.rich-text-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  padding: 8px;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
}

.editor-toolbar .el-button {
  padding: 5px 10px;
}

.editor-content {
  min-height: 200px;
  max-height: 500px;
  overflow-y: auto;
  padding: 15px;
  outline: none;
  line-height: 1.8;
}

.editor-content:empty::before {
  content: attr(placeholder);
  color: #c0c4cc;
  pointer-events: none;
}

.editor-content:focus {
  background: #fafafa;
}

.editor-content img {
  max-width: 100%;
  height: auto;
  margin: 10px 0;
  border-radius: 4px;
}

.editor-content video {
  max-width: 100%;
  margin: 10px 0;
  border-radius: 4px;
}

.editor-content a {
  color: #409eff;
  text-decoration: underline;
}

.upload-tip {
  text-align: center;
  margin-top: 10px;
  color: #606266;
}
</style>
