<template>
  <div class="data-source-management">
    <!-- 操作栏 -->
    <el-card class="operation-card" shadow="never">
      <div class="operation-bar">
        <div class="left-actions">
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            添加数据源
          </el-button>
          <el-button @click="loadDataSources">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
        <div class="right-actions">
          <el-tag type="info">共 {{ dataSources.length }} 个数据源</el-tag>
        </div>
      </div>
    </el-card>

    <!-- 数据源列表 -->
    <el-card class="list-card" shadow="never" v-loading="loading">
      <el-table
        :data="dataSources"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="数据源名称" min-width="150" />
        <el-table-column prop="sourceType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getSourceTypeColor(row.sourceType)" size="small">
              {{ row.sourceType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fetchInterval" label="抓取间隔" width="100">
          <template #default="{ row }">
            {{ row.fetchInterval }}小时
          </template>
        </el-table-column>
        <el-table-column prop="fetchCount" label="抓取次数" width="100" />
        <el-table-column prop="successCount" label="成功" width="80">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.successCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorCount" label="失败" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.errorCount > 0" type="danger" size="small">
              {{ row.errorCount }}
            </el-tag>
            <span v-else>0</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastFetchTime" label="最后抓取" width="160">
          <template #default="{ row }">
            {{ formatDate(row.lastFetchTime) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              @click="handleView(row)"
            >
              查看
            </el-button>
            <el-button
              link
              type="primary"
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              link
              type="success"
              size="small"
              :disabled="!row.enabled"
              @click="handleFetch(row)"
            >
              抓取
            </el-button>
            <el-button
              link
              :type="row.enabled ? 'warning' : 'success'"
              size="small"
              @click="handleToggle(row)"
            >
              {{ row.enabled ? '禁用' : '启用' }}
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && dataSources.length === 0" description="暂无数据源">
        <el-button type="primary" @click="handleCreate">添加数据源</el-button>
      </el-empty>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="数据源名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入数据源名称"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="数据源URL" prop="url">
          <el-input
            v-model="form.url"
            placeholder="请输入数据源URL"
            maxlength="500"
          />
        </el-form-item>

        <el-form-item label="数据源类型" prop="sourceType">
          <el-select v-model="form.sourceType" placeholder="请选择数据源类型">
            <el-option label="RSS订阅" value="RSS" />
            <el-option label="HTML网页" value="HTML" />
            <el-option label="API接口" value="API" />
            <el-option label="JSON数据" value="JSON" />
            <el-option label="XML数据" value="XML" />
          </el-select>
        </el-form-item>

        <el-form-item label="抓取间隔" prop="fetchInterval">
          <el-input-number
            v-model="form.fetchInterval"
            :min="1"
            :max="168"
            placeholder="小时"
          />
          <span class="form-tip">（1-168小时，建议6-24小时）</span>
        </el-form-item>

        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入数据源描述"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-divider content-position="left">高级配置（可选）</el-divider>

        <el-form-item label="选择器配置">
          <el-input
            v-model="form.selectorConfig"
            type="textarea"
            :rows="4"
            placeholder='JSON格式，例如：{"title": ".article-title", "content": ".article-content"}'
          />
        </el-form-item>

        <el-form-item label="清洗规则">
          <el-input
            v-model="form.cleaningRules"
            type="textarea"
            :rows="4"
            placeholder='JSON格式，例如：{"removeHtml": true, "trimSpaces": true}'
          />
        </el-form-item>

        <el-form-item label="模板映射">
          <el-input
            v-model="form.templateMapping"
            type="textarea"
            :rows="4"
            placeholder='JSON格式，例如：{"templateType": "TECH_ARTICLE", "fields": {...}}'
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="数据源详情"
      size="600px"
    >
      <div v-if="selectedDataSource" class="data-source-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="ID">
            {{ selectedDataSource.id }}
          </el-descriptions-item>
          <el-descriptions-item label="名称">
            {{ selectedDataSource.name }}
          </el-descriptions-item>
          <el-descriptions-item label="URL">
            <el-link :href="selectedDataSource.url" target="_blank" type="primary">
              {{ selectedDataSource.url }}
            </el-link>
          </el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="getSourceTypeColor(selectedDataSource.sourceType)">
              {{ selectedDataSource.sourceType }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusColor(selectedDataSource.status)">
              {{ getStatusText(selectedDataSource.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="抓取间隔">
            {{ selectedDataSource.fetchInterval }}小时
          </el-descriptions-item>
          <el-descriptions-item label="抓取统计">
            总计: {{ selectedDataSource.fetchCount }} | 
            成功: {{ selectedDataSource.successCount }} | 
            失败: {{ selectedDataSource.errorCount }}
          </el-descriptions-item>
          <el-descriptions-item label="最后抓取">
            {{ formatDate(selectedDataSource.lastFetchTime) || '从未抓取' }}
          </el-descriptions-item>
          <el-descriptions-item label="下次抓取">
            {{ formatDate(selectedDataSource.nextFetchTime) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(selectedDataSource.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDate(selectedDataSource.updatedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="描述" v-if="selectedDataSource.description">
            {{ selectedDataSource.description }}
          </el-descriptions-item>
          <el-descriptions-item label="最后错误" v-if="selectedDataSource.lastError">
            <el-alert
              :title="selectedDataSource.lastError"
              type="error"
              :closable="false"
            />
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-actions">
          <el-button type="primary" @click="handleEdit(selectedDataSource)">
            编辑
          </el-button>
          <el-button
            type="success"
            :disabled="!selectedDataSource.enabled"
            @click="handleFetch(selectedDataSource)"
          >
            立即抓取
          </el-button>
          <el-button
            :type="selectedDataSource.enabled ? 'warning' : 'success'"
            @click="handleToggle(selectedDataSource)"
          >
            {{ selectedDataSource.enabled ? '禁用' : '启用' }}
          </el-button>
          <el-button type="danger" @click="handleDelete(selectedDataSource)">
            删除
          </el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import {
  getAllDataSources,
  createDataSource,
  updateDataSource,
  deleteDataSource,
  toggleDataSource,
  triggerFetch
} from '@/api/dataSource'

const loading = ref(false)
const dataSources = ref([])
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const selectedDataSource = ref(null)

const formRef = ref(null)
const form = ref({
  name: '',
  url: '',
  description: '',
  sourceType: 'RSS',
  fetchInterval: 24,
  selectorConfig: '',
  cleaningRules: '',
  templateMapping: ''
})

const formRules = {
  name: [
    { required: true, message: '请输入数据源名称', trigger: 'blur' }
  ],
  url: [
    { required: true, message: '请输入数据源URL', trigger: 'blur' },
    { type: 'url', message: '请输入有效的URL', trigger: 'blur' }
  ],
  sourceType: [
    { required: true, message: '请选择数据源类型', trigger: 'change' }
  ],
  fetchInterval: [
    { required: true, message: '请输入抓取间隔', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => {
  return isEdit.value ? '编辑数据源' : '添加数据源'
})

const loadDataSources = async () => {
  loading.value = true
  try {
    const response = await getAllDataSources()
    // axios拦截器已经解包
    dataSources.value = Array.isArray(response) ? response : (response.data || [])
  } catch (error) {
    console.error('Failed to load data sources:', error)
    ElMessage.error('加载数据源失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  isEdit.value = false
  form.value = {
    name: '',
    url: '',
    description: '',
    sourceType: 'RSS',
    fetchInterval: 24,
    selectorConfig: '',
    cleaningRules: '',
    templateMapping: ''
  }
  dialogVisible.value = true
}

const handleEdit = (dataSource) => {
  isEdit.value = true
  form.value = {
    id: dataSource.id,
    name: dataSource.name,
    url: dataSource.url,
    description: dataSource.description || '',
    sourceType: dataSource.sourceType,
    fetchInterval: dataSource.fetchInterval,
    selectorConfig: dataSource.selectorConfig || '',
    cleaningRules: dataSource.cleaningRules || '',
    templateMapping: dataSource.templateMapping || ''
  }
  dialogVisible.value = true
  drawerVisible.value = false
}

const handleView = (dataSource) => {
  selectedDataSource.value = dataSource
  drawerVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const data = {
        name: form.value.name,
        url: form.value.url,
        description: form.value.description,
        sourceType: form.value.sourceType,
        fetchInterval: form.value.fetchInterval,
        selectorConfig: form.value.selectorConfig || null,
        cleaningRules: form.value.cleaningRules || null,
        templateMapping: form.value.templateMapping || null
      }

      if (isEdit.value) {
        await updateDataSource(form.value.id, data)
        ElMessage.success('数据源更新成功')
      } else {
        await createDataSource(data)
        ElMessage.success('数据源创建成功')
      }

      dialogVisible.value = false
      await loadDataSources()
    } catch (error) {
      console.error('Failed to save data source:', error)
      const message = error.response?.data?.error?.message || '保存失败'
      ElMessage.error(message)
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (dataSource) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除数据源"${dataSource.name}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteDataSource(dataSource.id)
    ElMessage.success('数据源删除成功')
    await loadDataSources()
    drawerVisible.value = false
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete data source:', error)
      const message = error.response?.data?.error?.message || '删除失败'
      ElMessage.error(message)
    }
  }
}

const handleToggle = async (dataSource) => {
  try {
    const enabled = !dataSource.enabled
    await toggleDataSource(dataSource.id, enabled)
    ElMessage.success(enabled ? '数据源已启用' : '数据源已禁用')
    await loadDataSources()
    if (drawerVisible.value) {
      selectedDataSource.value = dataSources.value.find(ds => ds.id === dataSource.id)
    }
  } catch (error) {
    console.error('Failed to toggle data source:', error)
    const message = error.response?.data?.error?.message || '操作失败'
    ElMessage.error(message)
  }
}

const handleFetch = async (dataSource) => {
  try {
    await triggerFetch(dataSource.id)
    ElMessage.success('抓取任务已触发')
    await loadDataSources()
  } catch (error) {
    console.error('Failed to trigger fetch:', error)
    const message = error.response?.data?.error?.message || '触发失败'
    ElMessage.error(message)
  }
}

const getSourceTypeColor = (type) => {
  const colors = {
    RSS: 'warning',
    HTML: 'primary',
    API: 'success',
    JSON: 'info',
    XML: 'danger'
  }
  return colors[type] || 'info'
}

const getStatusColor = (status) => {
  const colors = {
    ACTIVE: 'success',
    PAUSED: 'warning',
    ERROR: 'danger',
    DISABLED: 'info'
  }
  return colors[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    ACTIVE: '活跃',
    PAUSED: '暂停',
    ERROR: '错误',
    DISABLED: '禁用'
  }
  return texts[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
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
  loadDataSources()
})
</script>

<style scoped>
.data-source-management {
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

.list-card {
  min-height: 400px;
}

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.data-source-detail {
  padding: 10px 0;
}

.detail-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  justify-content: center;
}
</style>
