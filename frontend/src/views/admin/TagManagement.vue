<template>
  <div class="tag-management">
    <!-- 操作栏 -->
    <el-card class="operation-card" shadow="never">
      <div class="operation-bar">
        <div class="left-actions">
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            创建标签
          </el-button>
          <el-button @click="loadTags">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
        <div class="right-actions">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索标签"
            clearable
            style="width: 300px"
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
    </el-card>

    <!-- 标签云预览 -->
    <el-card class="cloud-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>标签云预览</span>
          <el-tag type="info">共 {{ tags.length }} 个标签</el-tag>
        </div>
      </template>
      <TagCloud
        ref="tagCloudRef"
        :mode="'all'"
        :max-tags="50"
        :show-count="true"
        :clickable="true"
        @tag-click="handleTagClick"
      />
    </el-card>

    <!-- 标签列表 -->
    <el-card class="list-card" shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>标签列表</span>
          <el-radio-group v-model="sortBy" size="small" @change="handleSortChange">
            <el-radio-button label="usage">按使用量</el-radio-button>
            <el-radio-button label="name">按名称</el-radio-button>
            <el-radio-button label="time">按时间</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table
        :data="displayTags"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="标签名称" min-width="200">
          <template #default="{ row }">
            <el-tag :type="getTagType(row.usageCount)">
              {{ row.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="usageCount" label="使用次数" width="120" sortable>
          <template #default="{ row }">
            <el-badge :value="row.usageCount" :max="999" type="primary" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              @click="handleViewContents(row)"
            >
              查看内容
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              :disabled="row.usageCount > 0"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && displayTags.length === 0" description="暂无标签">
        <el-button type="primary" @click="handleCreate">创建标签</el-button>
      </el-empty>
    </el-card>

    <!-- 创建标签对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="创建标签"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="标签名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入标签名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-alert
          title="提示"
          type="info"
          :closable="false"
          show-icon
        >
          标签名称应简洁明了，建议2-10个字符
        </el-alert>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">
          创建
        </el-button>
      </template>
    </el-dialog>

    <!-- 标签内容抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="'标签「' + (selectedTag?.name || '') + '」的内容'"
      size="60%"
    >
      <div v-if="selectedTag" class="tag-contents">
        <el-alert
          :title="`共 ${selectedTag.usageCount} 篇内容使用了此标签`"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px"
        />

        <div v-loading="contentsLoading" class="contents-list">
          <el-card
            v-for="content in tagContents"
            :key="content.id"
            class="content-card"
            shadow="hover"
            @click="handleViewContent(content)"
          >
            <div class="content-info">
              <h3>{{ content.title }}</h3>
              <p class="content-summary">{{ content.summary }}</p>
              <div class="content-meta">
                <el-tag size="small">{{ content.templateType }}</el-tag>
                <span class="meta-item">
                  <el-icon><View /></el-icon>
                  {{ content.viewCount }}
                </span>
                <span class="meta-item">
                  <el-icon><Clock /></el-icon>
                  {{ formatDate(content.publishedAt) }}
                </span>
              </div>
            </div>
          </el-card>

          <el-empty
            v-if="!contentsLoading && tagContents.length === 0"
            description="暂无内容"
          />
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search, View, Clock } from '@element-plus/icons-vue'
import TagCloud from '@/components/TagCloud.vue'
import {
  getAllTags,
  createTag,
  deleteTag,
  searchTags,
  searchContentsByTag
} from '@/api/tag'

const router = useRouter()

const loading = ref(false)
const tags = ref([])
const searchKeyword = ref('')
const sortBy = ref('usage')
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const submitting = ref(false)
const contentsLoading = ref(false)
const selectedTag = ref(null)
const tagContents = ref([])
const tagCloudRef = ref(null)

const formRef = ref(null)
const form = ref({
  name: ''
})

const formRules = {
  name: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

const displayTags = computed(() => {
  let result = [...tags.value]
  
  // 排序
  if (sortBy.value === 'usage') {
    result.sort((a, b) => b.usageCount - a.usageCount)
  } else if (sortBy.value === 'name') {
    result.sort((a, b) => a.name.localeCompare(b.name))
  } else if (sortBy.value === 'time') {
    result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  }
  
  return result
})

const loadTags = async () => {
  loading.value = true
  try {
    const response = await getAllTags()
    // axios拦截器已经解包
    tags.value = Array.isArray(response) ? response : (response.data || [])
    // 刷新标签云
    if (tagCloudRef.value) {
      tagCloudRef.value.loadTags()
    }
  } catch (error) {
    console.error('Failed to load tags:', error)
    ElMessage.error('加载标签失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    await loadTags()
    return
  }
  
  loading.value = true
  try {
    const response = await searchTags(searchKeyword.value)
    // axios拦截器已经解包
    tags.value = Array.isArray(response) ? response : (response.data || [])
  } catch (error) {
    console.error('Failed to search tags:', error)
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const handleSortChange = () => {
  // 排序由computed自动处理
}

const handleCreate = () => {
  form.value = { name: '' }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      await createTag({ name: form.value.name.trim() })
      ElMessage.success('标签创建成功')
      dialogVisible.value = false
      await loadTags()
    } catch (error) {
      console.error('Failed to create tag:', error)
      const message = error.response?.data?.error?.message || '创建失败'
      ElMessage.error(message)
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (tag) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除标签"${tag.name}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteTag(tag.id)
    ElMessage.success('标签删除成功')
    await loadTags()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete tag:', error)
      const message = error.response?.data?.error?.message || '删除失败'
      ElMessage.error(message)
    }
  }
}

const handleTagClick = (tag) => {
  handleViewContents(tag)
}

const handleViewContents = async (tag) => {
  selectedTag.value = tag
  drawerVisible.value = true
  
  contentsLoading.value = true
  try {
    const response = await searchContentsByTag(tag.name, 0, 20)
    // axios拦截器已经解包
    tagContents.value = Array.isArray(response) ? response : (response.data || [])
  } catch (error) {
    console.error('Failed to load tag contents:', error)
    ElMessage.error('加载内容失败')
  } finally {
    contentsLoading.value = false
  }
}

const handleViewContent = (content) => {
  router.push(`/content/${content.id}`)
}

const getTagType = (usageCount) => {
  if (usageCount >= 50) return 'danger'
  if (usageCount >= 30) return 'warning'
  if (usageCount >= 10) return 'success'
  return 'info'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

onMounted(() => {
  loadTags()
})
</script>

<style scoped>
.tag-management {
  padding: 0;
}

.operation-card {
  margin-bottom: 20px;
}

.operation-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left-actions {
  display: flex;
  gap: 10px;
}

.right-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.cloud-card {
  margin-bottom: 20px;
}

.list-card {
  min-height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tag-contents {
  padding: 10px 0;
}

.contents-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.content-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.content-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.content-info h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
}

.content-summary {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.content-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
