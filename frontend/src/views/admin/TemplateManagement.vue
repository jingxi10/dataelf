<template>
  <div class="template-management">
    <!-- 操作栏 -->
    <el-card class="operation-card" shadow="never">
      <div class="operation-bar">
        <div class="left-actions">
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            创建模板
          </el-button>
          <el-button @click="handleImport">
            <el-icon><Upload /></el-icon>
            导入模板
          </el-button>
        </div>
        <div class="right-actions">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索模板名称或类型"
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

    <!-- 模板列表 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="filteredTemplates"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="模板名称" min-width="150" />
        <el-table-column prop="type" label="模板类型" width="150" />
        <el-table-column prop="schemaOrgType" label="Schema.org类型" width="180" />
        <el-table-column label="系统模板" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isSystem" type="info" size="small">系统</el-tag>
            <el-tag v-else type="success" size="small">自定义</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button 
              link 
              type="primary" 
              size="small" 
              @click="handleEdit(row)"
              :disabled="row.isSystem"
            >
              编辑
            </el-button>
            <el-button link type="primary" size="small" @click="handleExport(row)">
              导出
            </el-button>
            <el-button 
              link 
              type="danger" 
              size="small" 
              @click="handleDelete(row)"
              :disabled="row.isSystem"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑模板对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="templateFormRef"
        :model="templateForm"
        :rules="templateRules"
        label-width="120px"
      >
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="templateForm.name" placeholder="请输入模板名称" />
        </el-form-item>
        
        <el-form-item label="模板类型" prop="type">
          <el-input v-model="templateForm.type" placeholder="例如: TechArticle" />
        </el-form-item>
        
        <el-form-item label="Schema.org类型" prop="schemaOrgType">
          <el-select v-model="templateForm.schemaOrgType" placeholder="请选择Schema.org类型" style="width: 100%">
            <el-option label="Article" value="Article" />
            <el-option label="Report" value="Report" />
            <el-option label="Review" value="Review" />
            <el-option label="AnalysisNewsArticle" value="AnalysisNewsArticle" />
            <el-option label="Product" value="Product" />
            <el-option label="Event" value="Event" />
            <el-option label="Organization" value="Organization" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="模板描述" prop="description">
          <el-input
            v-model="templateForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入模板描述"
          />
        </el-form-item>
        
        <el-form-item label="Schema定义" prop="schemaDefinition">
          <el-input
            v-model="templateForm.schemaDefinition"
            type="textarea"
            :rows="12"
            placeholder="请输入JSON格式的Schema定义"
          />
          <div class="form-tip">
            <el-icon><InfoFilled /></el-icon>
            必须包含@context、@type和fields字段
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button @click="handleValidateSchema">
            <el-icon><Check /></el-icon>
            验证Schema
          </el-button>
          <span v-if="validationResult" class="validation-result">
            <el-icon v-if="validationResult.valid" color="#67c23a"><CircleCheck /></el-icon>
            <el-icon v-else color="#f56c6c"><CircleClose /></el-icon>
            {{ validationResult.message }}
          </span>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看模板对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="查看模板详情"
      width="800px"
    >
      <el-descriptions :column="1" border v-if="currentTemplate">
        <el-descriptions-item label="模板ID">{{ currentTemplate.id }}</el-descriptions-item>
        <el-descriptions-item label="模板名称">{{ currentTemplate.name }}</el-descriptions-item>
        <el-descriptions-item label="模板类型">{{ currentTemplate.type }}</el-descriptions-item>
        <el-descriptions-item label="Schema.org类型">{{ currentTemplate.schemaOrgType }}</el-descriptions-item>
        <el-descriptions-item label="系统模板">
          <el-tag v-if="currentTemplate.isSystem" type="info">是</el-tag>
          <el-tag v-else type="success">否</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述">{{ currentTemplate.description }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(currentTemplate.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDate(currentTemplate.updatedAt) }}</el-descriptions-item>
        <el-descriptions-item label="Schema定义">
          <pre class="schema-preview">{{ formatJson(currentTemplate.schemaDefinition) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 导入模板对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入模板"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-upload
        class="upload-demo"
        drag
        :auto-upload="false"
        :on-change="handleFileChange"
        :limit="1"
        accept=".json"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将JSON文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传JSON格式的模板文件
          </div>
        </template>
      </el-upload>
      
      <div v-if="importFileContent" class="import-preview">
        <el-divider>文件预览</el-divider>
        <pre class="schema-preview">{{ importFileContent }}</pre>
      </div>
      
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleImportSubmit" 
          :disabled="!importFileContent"
        >
          导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Upload,
  Search,
  Check,
  InfoFilled,
  CircleCheck,
  CircleClose,
  UploadFilled
} from '@element-plus/icons-vue'
import {
  getTemplateList,
  createTemplate,
  updateTemplate,
  deleteTemplate,
  exportTemplate,
  importTemplate,
  validateTemplateSchema
} from '@/api/template'

// 数据
const loading = ref(false)
const templates = ref([])
const searchKeyword = ref('')

// 对话框
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const importDialogVisible = ref(false)
const isEdit = ref(false)
const currentTemplate = ref(null)

// 表单
const templateFormRef = ref(null)
const templateForm = ref({
  name: '',
  type: '',
  schemaOrgType: '',
  description: '',
  schemaDefinition: ''
})

const templateRules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  type: [{ required: true, message: '请输入模板类型', trigger: 'blur' }],
  schemaOrgType: [{ required: true, message: '请选择Schema.org类型', trigger: 'change' }],
  schemaDefinition: [{ required: true, message: '请输入Schema定义', trigger: 'blur' }]
}

// 验证
const validating = ref(false)
const validationResult = ref(null)

// 提交
const submitting = ref(false)

// 导入
const importing = ref(false)
const importFileContent = ref('')

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑模板' : '创建模板')

const filteredTemplates = computed(() => {
  if (!searchKeyword.value) {
    return templates.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return templates.value.filter(template =>
    template.name.toLowerCase().includes(keyword) ||
    template.type.toLowerCase().includes(keyword)
  )
})

// 方法
const loadTemplates = async () => {
  loading.value = true
  try {
    const response = await getTemplateList()
    // axios拦截器已经解包，response直接就是数据数组
    if (Array.isArray(response)) {
      templates.value = response
    } else if (response && response.data) {
      // 兼容未解包的情况
      templates.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载模板列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 搜索由计算属性自动处理
}

const handleCreate = () => {
  isEdit.value = false
  currentTemplate.value = null
  templateForm.value = {
    name: '',
    type: '',
    schemaOrgType: '',
    description: '',
    schemaDefinition: getDefaultSchemaDefinition()
  }
  validationResult.value = null
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentTemplate.value = row
  templateForm.value = {
    name: row.name,
    type: row.type,
    schemaOrgType: row.schemaOrgType,
    description: row.description,
    schemaDefinition: row.schemaDefinition
  }
  validationResult.value = null
  dialogVisible.value = true
}

const handleView = (row) => {
  currentTemplate.value = row
  viewDialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除模板"${row.name}"吗？如果该模板正在被内容使用，将无法删除。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteTemplate(row.id)
    ElMessage.success('模板删除成功')
    await loadTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      const message = error.response?.data?.error?.message || '删除失败'
      ElMessage.error(message)
    }
  }
}

const handleExport = async (row) => {
  try {
    const response = await exportTemplate(row.id)
    // axios拦截器已经解包
    const jsonStr = typeof response === 'string' ? response : (response.data || JSON.stringify(response))
    const blob = new Blob([jsonStr], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `template_${row.name}_${row.id}.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    ElMessage.success('模板导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
    console.error(error)
  }
}

const handleImport = () => {
  importFileContent.value = ''
  importDialogVisible.value = true
}

const handleFileChange = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const content = e.target.result
      JSON.parse(content) // 验证是否为有效JSON
      importFileContent.value = content
    } catch (error) {
      ElMessage.error('文件格式无效，请上传有效的JSON文件')
      importFileContent.value = ''
    }
  }
  reader.readAsText(file.raw)
}

const handleImportSubmit = async () => {
  if (!importFileContent.value) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }
  
  importing.value = true
  try {
    await importTemplate(importFileContent.value)
    ElMessage.success('模板导入成功')
    importDialogVisible.value = false
    importFileContent.value = ''
    await loadTemplates()
  } catch (error) {
    const message = error.response?.data?.error?.message || '导入失败'
    ElMessage.error(message)
  } finally {
    importing.value = false
  }
}

const handleValidateSchema = async () => {
  if (!templateForm.value.schemaDefinition || !templateForm.value.schemaOrgType) {
    ElMessage.warning('请先填写Schema定义和Schema.org类型')
    return
  }
  
  validating.value = true
  try {
    const response = await validateTemplateSchema({
      schemaDefinition: templateForm.value.schemaDefinition,
      schemaOrgType: templateForm.value.schemaOrgType
    })
    
    // axios拦截器已经解包
    const result = response.data ? response.data : response
    if (result) {
      validationResult.value = {
        valid: result.valid,
        message: result.valid ? '验证通过' : (result.errors ? result.errors.join('; ') : '验证失败')
      }
      
      if (result.valid) {
        ElMessage.success('Schema验证通过')
      } else {
        ElMessage.error('Schema验证失败')
      }
    }
  } catch (error) {
    ElMessage.error('验证失败')
    console.error(error)
  } finally {
    validating.value = false
  }
}

const handleSubmit = async () => {
  if (!templateFormRef.value) return
  
  await templateFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        await updateTemplate(currentTemplate.value.id, templateForm.value)
        ElMessage.success('模板更新成功')
      } else {
        await createTemplate(templateForm.value)
        ElMessage.success('模板创建成功')
      }
      
      dialogVisible.value = false
      await loadTemplates()
    } catch (error) {
      const message = error.response?.data?.error?.message || (isEdit.value ? '更新失败' : '创建失败')
      ElMessage.error(message)
    } finally {
      submitting.value = false
    }
  })
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

const formatJson = (jsonStr) => {
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, 2)
  } catch {
    return jsonStr
  }
}

const getDefaultSchemaDefinition = () => {
  return JSON.stringify({
    "@context": "https://schema.org",
    "@type": "Article",
    "fields": [
      {
        "name": "headline",
        "type": "string",
        "required": true,
        "label": "标题"
      },
      {
        "name": "author",
        "type": "Person",
        "required": true,
        "label": "作者"
      },
      {
        "name": "datePublished",
        "type": "date",
        "required": true,
        "label": "发布日期"
      },
      {
        "name": "articleBody",
        "type": "text",
        "required": true,
        "label": "正文"
      }
    ]
  }, null, 2)
}

// 生命周期
onMounted(() => {
  loadTemplates()
})
</script>

<style scoped>
.template-management {
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
}

.table-card {
  margin-bottom: 20px;
}

.form-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.validation-result {
  margin-left: 12px;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.schema-preview {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.5;
  max-height: 400px;
  overflow-y: auto;
  margin: 0;
}

.import-preview {
  margin-top: 20px;
}

.upload-demo {
  width: 100%;
}

:deep(.el-upload-dragger) {
  width: 100%;
}
</style>
