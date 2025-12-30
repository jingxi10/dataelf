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
    // 确保视频标签正确显示
    nextTick(() => {
      const videos = editorRef.value?.querySelectorAll('video')
      videos?.forEach(video => {
        if (!video.hasAttribute('controls')) {
          video.setAttribute('controls', '')
        }
        if (!video.style.maxWidth) {
          video.style.maxWidth = '100%'
        }
        if (!video.style.height) {
          video.style.height = 'auto'
        }
      })
    })
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
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
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
  // 检查文件大小（100MB）
  if (file.size > 100 * 1024 * 1024) {
    ElMessage.error('视频大小不能超过100MB')
    return
  }
  
  // 检查文件类型
  const allowedTypes = ['video/mp4', 'video/webm', 'video/ogg', 'video/quicktime', 'video/x-msvideo', 'video/avi']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('不支持的视频格式，请使用 MP4、WebM、OGG 格式')
    return
  }
  
  uploadingVisible.value = true
  uploadProgress.value = 0
  uploadStatus.value = ''
  uploadTip.value = `正在上传视频... (${(file.size / 1024 / 1024).toFixed(2)}MB)`
  
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
    
    // 处理响应数据 - axios拦截器已经解包，返回的是 response.data.data 或 response.data
    console.log('视频上传响应:', response)
    
    // 尝试多种可能的响应结构
    let url = null
    if (typeof response === 'string') {
      // 如果响应是字符串URL
      url = response
    } else if (response?.url) {
      // 直接有url字段
      url = response.url
    } else if (response?.data?.url) {
      // 嵌套在data中
      url = response.data.url
    } else if (response?.data && typeof response.data === 'object') {
      // 尝试从data对象中获取url
      url = response.data.url
    }
    
    if (url) {
      console.log('视频上传成功，URL:', url)
      insertVideo(url)
      ElMessage.success('视频插入成功')
    } else {
      console.error('响应中没有找到视频URL，完整响应:', JSON.stringify(response, null, 2))
      ElMessage.error('视频上传成功，但无法获取URL。响应: ' + JSON.stringify(response))
    }
    
    setTimeout(() => {
      uploadingVisible.value = false
    }, 500)
  } catch (error) {
    console.error('视频上传失败:', error)
    uploadStatus.value = 'exception'
    
    // 显示详细错误信息
    let errorMessage = '视频上传失败'
    if (error.response?.data?.error?.message) {
      errorMessage = error.response.data.error.message
    } else if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.message) {
      errorMessage = error.message
    }
    
    uploadTip.value = errorMessage
    ElMessage.error(errorMessage)
    
    setTimeout(() => {
      uploadingVisible.value = false
    }, 2000)
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
  // 确保URL是完整的
  if (!url) {
    console.error('视频URL为空')
    ElMessage.error('视频URL无效')
    return
  }
  
  // URL处理：如果已经是完整URL（http/https开头），直接使用；否则可能需要添加域名
  let videoUrl = url.trim()
  
  // 如果URL不是以http开头，可能需要添加基础URL
  if (!videoUrl.startsWith('http://') && !videoUrl.startsWith('https://')) {
    // 如果是OSS URL（通常以bucket名开头），添加https://
    if (videoUrl.startsWith('/')) {
      // 相对路径，尝试添加基础URL
      const baseUrl = import.meta.env.VITE_API_BASE_URL || window.location.origin
      videoUrl = baseUrl + videoUrl
    } else {
      // 可能是OSS bucket路径，添加https://
      videoUrl = 'https://' + videoUrl
    }
  }
  
  console.log('插入视频，URL:', videoUrl)
  
  // 创建video标签
  const videoHtml = `<video src="${videoUrl}" controls preload="metadata" style="max-width: 100%; height: auto; display: block; margin: 16px 0; border-radius: 8px; background-color: #000;"></video>`
  
  // 确保编辑器获得焦点
  editorRef.value?.focus()
  
  // 使用 insertHTML 插入视频
  try {
    document.execCommand('insertHTML', false, videoHtml)
    handleInput()
    
    // 验证视频是否成功插入
    nextTick(() => {
      const videos = editorRef.value?.querySelectorAll('video')
      if (videos && videos.length > 0) {
        const lastVideo = videos[videos.length - 1]
        console.log('视频已插入，src:', lastVideo.src)
        
        // 添加错误处理
        lastVideo.addEventListener('error', (e) => {
          console.error('视频加载失败:', e)
          ElMessage.error('视频加载失败，请检查视频URL是否正确')
        })
        
        // 添加加载成功事件
        lastVideo.addEventListener('loadedmetadata', () => {
          console.log('视频元数据加载成功，时长:', lastVideo.duration)
        })
      } else {
        console.warn('视频插入后未找到video元素')
      }
    })
  } catch (error) {
    console.error('插入视频失败:', error)
    ElMessage.error('插入视频失败')
  }
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
  height: auto;
  margin: 16px 0;
  border-radius: 8px;
  display: block;
  background-color: #000;
}

.editor-content .video-container {
  margin: 16px 0;
  max-width: 100%;
  position: relative;
}

.editor-content .video-container video {
  width: 100%;
  max-width: 100%;
  height: auto;
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
