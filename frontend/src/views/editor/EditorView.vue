<template>
  <div class="editor-view">
    <el-card class="editor-container">
      <template #header>
        <div class="editor-header">
          <h2>内容编辑器</h2>
          <div class="header-actions">
            <el-button @click="handleSaveDraft" :loading="saving">保存草稿</el-button>
            <el-button
              v-if="contentId"
              :icon="Download"
              @click="showExportDialog"
            >
              导出
            </el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">提交审核</el-button>
          </div>
        </div>
      </template>

      <el-row :gutter="20">
        <!-- 左侧：编辑区域 -->
        <el-col :span="14">
          <!-- 模板选择器 -->
          <div class="section template-selector">
            <h3>选择模板</h3>
            <el-select
              v-model="selectedTemplateId"
              placeholder="请选择内容模板"
              @change="handleTemplateChange"
              :disabled="!!contentId"
              style="width: 100%"
            >
              <el-option
                v-for="template in templates"
                :key="template.id"
                :label="template.name"
                :value="template.id"
              >
                <span>{{ template.name }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">
                  {{ template.type }}
                </span>
              </el-option>
            </el-select>
          </div>

          <!-- 基本信息 -->
          <div class="section" v-if="selectedTemplateId">
            <h3>基本信息</h3>
            <el-form :model="formData" label-width="100px">
              <el-form-item label="标题" required>
                <el-input
                  v-model="formData.title"
                  placeholder="请输入内容标题"
                  @input="handleFieldChange"
                />
              </el-form-item>
              <el-form-item label="分类" required>
                <el-cascader
                  v-model="formData.categoryIds"
                  :options="categoryOptions"
                  :props="{ multiple: true, checkStrictly: true, value: 'id', label: 'name', children: 'children' }"
                  placeholder="请选择分类（可多选）"
                  clearable
                  filterable
                  style="width: 100%"
                  @change="handleFieldChange"
                />
              </el-form-item>
              <el-form-item label="标签">
                <el-select
                  v-model="formData.tagIds"
                  multiple
                  filterable
                  allow-create
                  placeholder="请选择或输入标签"
                  style="width: 100%"
                  @change="handleFieldChange"
                >
                  <el-option
                    v-for="tag in tagOptions"
                    :key="tag.id"
                    :label="tag.name"
                    :value="tag.id"
                  />
                </el-select>
              </el-form-item>
            </el-form>
          </div>

          <!-- 内容正文编辑区 -->
          <div class="section content-body-section" v-if="selectedTemplateId">
            <h3>
              内容正文
              <el-tooltip content="支持插入图片、视频等多媒体内容" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </h3>
            <RichTextEditor
              v-model="formData.structuredData.articleBody"
              placeholder="请输入内容正文，支持插入图片、视频..."
              @update:modelValue="handleFieldChange"
            />
          </div>

          <!-- 字段配置面板（可拖拽排序，排除articleBody因为已单独编辑） -->
          <div class="section fields-panel" v-if="selectedTemplateId && filteredFields.length">
            <h3>
              内容字段
              <el-tooltip content="拖拽字段可调整顺序" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </h3>
            
            <draggable
              v-model="orderedFields"
              item-key="name"
              handle=".drag-handle"
              @end="handleFieldReorder"
            >
              <template #item="{ element }">
                <div class="field-item" v-if="element.name !== 'articleBody'">
                  <div class="field-header">
                    <el-icon class="drag-handle"><Rank /></el-icon>
                    <span class="field-label">
                      {{ element.label }}
                      <span v-if="element.required" class="required-mark">*</span>
                    </span>
                  </div>
                  <div class="field-content">
                    <!-- 文本输入 -->
                    <el-input
                      v-if="element.type === 'text'"
                      v-model="formData.structuredData[element.name]"
                      :placeholder="`请输入${element.label}`"
                      @input="handleFieldChange"
                    />
                    <!-- 多行文本/富文本 -->
                    <RichTextEditor
                      v-else-if="element.type === 'textarea' || element.type === 'richtext'"
                      v-model="formData.structuredData[element.name]"
                      :placeholder="`请输入${element.label}`"
                      @update:modelValue="handleFieldChange"
                    />
                    <!-- 日期 -->
                    <el-date-picker
                      v-else-if="element.type === 'date'"
                      v-model="formData.structuredData[element.name]"
                      type="date"
                      :placeholder="`请选择${element.label}`"
                      style="width: 100%"
                      @change="handleFieldChange"
                    />
                    <!-- URL -->
                    <el-input
                      v-else-if="element.type === 'url'"
                      v-model="formData.structuredData[element.name]"
                      :placeholder="`请输入${element.label}`"
                      @input="handleFieldChange"
                    >
                      <template #prepend>https://</template>
                    </el-input>
                    <!-- 数字 -->
                    <el-input-number
                      v-else-if="element.type === 'number'"
                      v-model="formData.structuredData[element.name]"
                      :placeholder="`请输入${element.label}`"
                      style="width: 100%"
                      @change="handleFieldChange"
                    />
                    <!-- 默认文本 -->
                    <el-input
                      v-else
                      v-model="formData.structuredData[element.name]"
                      :placeholder="`请输入${element.label}`"
                      @input="handleFieldChange"
                    />
                  </div>
                </div>
              </template>
            </draggable>

            <!-- 结构化验证提示 -->
            <el-alert
              v-if="validationErrors.length > 0"
              title="结构化数据验证失败"
              type="error"
              :closable="false"
              class="validation-alert"
            >
              <ul>
                <li v-for="(error, index) in validationErrors" :key="index">
                  {{ error }}
                </li>
              </ul>
            </el-alert>

            <!-- 完整性评分 -->
            <div v-if="integrityScore !== null" class="integrity-score">
              <span>完整性评分：</span>
              <el-progress
                :percentage="integrityScore"
                :color="getScoreColor(integrityScore)"
                :stroke-width="20"
              />
            </div>
          </div>

          <!-- 版权说明编辑区 -->
          <div class="section copyright-section" v-if="selectedTemplateId">
            <h3>版权说明</h3>
            <el-form :model="formData" label-width="100px">
              <el-form-item label="内容出处">
                <el-input
                  v-model="formData.contentSource"
                  placeholder="请输入内容来源（如：原创、转载自XXX）"
                  @input="handleFieldChange"
                />
              </el-form-item>
              <el-form-item label="作者姓名">
                <el-input
                  v-model="formData.authorName"
                  placeholder="请输入作者姓名"
                  @input="handleFieldChange"
                />
              </el-form-item>
              <el-form-item label="原创标识">
                <el-radio-group v-model="formData.isOriginal" @change="handleFieldChange">
                  <el-radio :label="true">原创</el-radio>
                  <el-radio :label="false">转载</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="版权声明">
                <el-input
                  v-model="formData.copyrightNotice"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入版权声明文本"
                  @input="handleFieldChange"
                />
              </el-form-item>
            </el-form>
          </div>
        </el-col>

        <!-- 右侧：实时预览 -->
        <el-col :span="10">
          <div class="preview-panel" v-if="selectedTemplateId">
            <el-tabs v-model="activePreviewTab" type="card">
              <!-- JSON-LD预览 -->
              <el-tab-pane label="JSON-LD" name="jsonld">
                <div class="preview-content">
                  <pre><code>{{ formattedJsonLd }}</code></pre>
                </div>
              </el-tab-pane>

              <!-- HTML预览 -->
              <el-tab-pane label="HTML" name="html">
                <div class="preview-content">
                  <pre><code>{{ formattedHtml }}</code></pre>
                </div>
              </el-tab-pane>

              <!-- Markdown预览 -->
              <el-tab-pane label="Markdown" name="markdown">
                <div class="preview-content">
                  <pre><code>{{ formattedMarkdown }}</code></pre>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 导出对话框 -->
    <ExportDialog
      v-if="contentId"
      v-model="exportDialogVisible"
      :content-id="contentId"
      @exported="handleExported"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled, Rank, Download } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'
import { getTemplateList, getTemplateDetail } from '@/api/template'
import {
  createContent,
  updateContent,
  getContent,
  submitForReview,
  getContentOutput
} from '@/api/content'
import { getCategoryTree } from '@/api/category'
import { getTagList } from '@/api/tag'
import ExportDialog from '@/components/ExportDialog.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'

const route = useRoute()
const router = useRouter()

// 状态管理
const templates = ref([])
const selectedTemplateId = ref(null)
const selectedTemplate = ref(null)
const schemaFields = ref([])
const orderedFields = ref([])
const activePreviewTab = ref('jsonld')
const saving = ref(false)
const submitting = ref(false)
const contentId = ref(null)
const validationErrors = ref([])
const integrityScore = ref(null)
const exportDialogVisible = ref(false)
const categoryOptions = ref([])
const tagOptions = ref([])

// 表单数据
const formData = reactive({
  title: '',
  structuredData: {},
  contentSource: '',
  authorName: '',
  isOriginal: true,
  copyrightNotice: '',
  fieldOrder: [],
  categoryIds: [],
  tagIds: []
})

// 过滤掉articleBody的字段（因为单独编辑）
const filteredFields = computed(() => {
  return orderedFields.value.filter(field => field.name !== 'articleBody')
})

// 预览数据
const previewData = reactive({
  jsonLd: '',
  html: '',
  markdown: ''
})

// 格式化的预览内容
const formattedJsonLd = computed(() => {
  return generateJsonLd()
})

const formattedHtml = computed(() => {
  return generateHtml()
})

const formattedMarkdown = computed(() => {
  return generateMarkdown()
})

// 初始化
onMounted(async () => {
  await loadTemplates()
  await loadCategories()
  await loadTags()
  
  // 如果URL中有contentId，加载现有内容
  if (route.query.id) {
    contentId.value = parseInt(route.query.id)
    await loadContent(contentId.value)
  }
})

// 加载分类树
async function loadCategories() {
  try {
    const response = await getCategoryTree()
    categoryOptions.value = response.data || response || []
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 加载标签列表
async function loadTags() {
  try {
    const response = await getTagList()
    tagOptions.value = response.data || response || []
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

// 加载模板列表
async function loadTemplates() {
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
    console.error('加载模板列表失败:', error)
    ElMessage.error('加载模板列表失败')
  }
}

// 加载内容
async function loadContent(id) {
  try {
    const response = await getContent(id)
    // axios拦截器已经解包，response直接就是内容对象
    const content = response.data ? response.data : response
    if (content) {
      selectedTemplateId.value = content.templateId
      formData.title = content.title
      formData.structuredData = content.structuredData || {}
      formData.contentSource = content.contentSource || ''
      formData.authorName = content.authorName || ''
      formData.isOriginal = content.isOriginal !== false
      formData.copyrightNotice = content.copyrightNotice || ''
      formData.categoryIds = content.categoryIds || []
      formData.tagIds = content.tagIds || []
      
      await handleTemplateChange(content.templateId)
    }
  } catch (error) {
    console.error('加载内容失败:', error)
    ElMessage.error('加载内容失败')
  }
}

// 处理模板选择变化
async function handleTemplateChange(templateId) {
  if (!templateId) return
  
  try {
    const response = await getTemplateDetail(templateId)
    // axios拦截器已经解包，response直接就是模板对象
    const template = response.data ? response.data : response
    if (template) {
      selectedTemplate.value = template
      parseSchemaDefinition(template.schemaDefinition)
    }
  } catch (error) {
    console.error('加载模板详情失败:', error)
    ElMessage.error('加载模板详情失败')
  }
}

// 解析Schema定义
function parseSchemaDefinition(schemaDefinition) {
  try {
    const schema = typeof schemaDefinition === 'string' 
      ? JSON.parse(schemaDefinition) 
      : schemaDefinition
    
    schemaFields.value = schema.fields || []
    orderedFields.value = [...schemaFields.value]
    
    // 初始化structuredData
    schemaFields.value.forEach(field => {
      if (!(field.name in formData.structuredData)) {
        formData.structuredData[field.name] = ''
      }
    })
  } catch (error) {
    console.error('解析Schema定义失败:', error)
    ElMessage.error('模板格式错误')
  }
}

// 处理字段变化
function handleFieldChange() {
  validateStructuredData()
  calculateIntegrityScore()
}

// 处理字段重排序
function handleFieldReorder() {
  formData.fieldOrder = orderedFields.value.map(field => field.name)
}

// 验证结构化数据
function validateStructuredData() {
  validationErrors.value = []
  
  schemaFields.value.forEach(field => {
    if (field.required) {
      const value = formData.structuredData[field.name]
      if (!value || (typeof value === 'string' && value.trim() === '')) {
        validationErrors.value.push(`${field.label} 为必填字段`)
      }
    }
  })
}

// 计算完整性评分
function calculateIntegrityScore() {
  const totalFields = schemaFields.value.length
  if (totalFields === 0) {
    integrityScore.value = 0
    return
  }
  
  let filledFields = 0
  schemaFields.value.forEach(field => {
    const value = formData.structuredData[field.name]
    if (value && (typeof value !== 'string' || value.trim() !== '')) {
      filledFields++
    }
  })
  
  integrityScore.value = Math.round((filledFields / totalFields) * 100)
}

// 获取评分颜色
function getScoreColor(score) {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

// 生成JSON-LD
function generateJsonLd() {
  if (!selectedTemplate.value) return ''
  
  const jsonLd = {
    '@context': 'https://schema.org',
    '@type': selectedTemplate.value.schemaOrgType,
    ...formData.structuredData
  }
  
  // 添加版权信息
  if (formData.authorName) {
    jsonLd.author = {
      '@type': 'Person',
      name: formData.authorName
    }
  }
  
  if (formData.copyrightNotice) {
    jsonLd.copyrightNotice = formData.copyrightNotice
  }
  
  if (formData.contentSource) {
    jsonLd.sourceOrganization = formData.contentSource
  }
  
  return JSON.stringify(jsonLd, null, 2)
}

// 生成HTML
function generateHtml() {
  if (!selectedTemplate.value) return ''
  
  let html = `<article itemscope itemtype="https://schema.org/${selectedTemplate.value.schemaOrgType}">\n`
  
  orderedFields.value.forEach(field => {
    const value = formData.structuredData[field.name]
    if (value) {
      html += `  <div itemprop="${field.name}">${value}</div>\n`
    }
  })
  
  // 添加版权信息
  if (formData.authorName || formData.copyrightNotice || formData.contentSource) {
    html += `  <footer class="copyright-section">\n`
    if (formData.authorName) {
      html += `    <div itemprop="author" itemscope itemtype="https://schema.org/Person">\n`
      html += `      <span>作者：</span><span itemprop="name">${formData.authorName}</span>\n`
      html += `    </div>\n`
    }
    if (formData.copyrightNotice) {
      html += `    <div itemprop="copyrightNotice">${formData.copyrightNotice}</div>\n`
    }
    if (formData.contentSource) {
      html += `    <div>来源：<span itemprop="sourceOrganization">${formData.contentSource}</span></div>\n`
    }
    html += `  </footer>\n`
  }
  
  html += `</article>`
  
  return html
}

// 生成Markdown
function generateMarkdown() {
  if (!selectedTemplate.value) return ''
  
  let markdown = `# ${formData.title}\n\n`
  
  orderedFields.value.forEach(field => {
    const value = formData.structuredData[field.name]
    if (value) {
      markdown += `## ${field.label}\n\n${value}\n\n`
    }
  })
  
  // 添加版权信息
  if (formData.authorName || formData.copyrightNotice || formData.contentSource) {
    markdown += `---\n\n`
    if (formData.authorName) {
      markdown += `**作者：** ${formData.authorName}\n\n`
    }
    if (formData.contentSource) {
      markdown += `**来源：** ${formData.contentSource}\n\n`
    }
    if (formData.copyrightNotice) {
      markdown += `**版权声明：** ${formData.copyrightNotice}\n\n`
    }
  }
  
  return markdown
}

// 保存草稿
async function handleSaveDraft() {
  if (!selectedTemplateId.value) {
    ElMessage.warning('请先选择模板')
    return
  }
  
  if (!formData.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  
  saving.value = true
  
  try {
    const data = {
      templateId: selectedTemplateId.value,
      title: formData.title,
      structuredData: formData.structuredData,
      contentSource: formData.contentSource,
      authorName: formData.authorName,
      isOriginal: formData.isOriginal,
      copyrightNotice: formData.copyrightNotice,
      fieldOrder: formData.fieldOrder,
      categoryIds: formData.categoryIds.flat(),
      tagIds: formData.tagIds
    }
    
    let response
    if (contentId.value) {
      response = await updateContent(contentId.value, data)
    } else {
      response = await createContent(data)
    }
    
    // axios拦截器已经解包
    const result = response.data ? response.data : response
    if (result) {
      if (!contentId.value && result.id) {
        contentId.value = result.id
        router.replace({ query: { id: contentId.value } })
      }
      ElMessage.success('保存成功')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.response?.data?.error?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 提交审核
async function handleSubmit() {
  // 验证
  validateStructuredData()
  
  if (validationErrors.value.length > 0) {
    ElMessage.error('请填写所有必填字段')
    return
  }
  
  if (!formData.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      '提交后内容将进入审核队列，确认提交吗？',
      '确认提交',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    submitting.value = true
    
    // 先保存
    await handleSaveDraft()
    
    // 再提交审核
    if (contentId.value) {
      await submitForReview(contentId.value)
      ElMessage.success('提交成功，请等待审核')
      router.push('/my-content')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
      ElMessage.error(error.response?.data?.error?.message || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}

// 显示导出对话框
function showExportDialog() {
  if (!contentId.value) {
    ElMessage.warning('请先保存内容')
    return
  }
  exportDialogVisible.value = true
}

// 导出完成后的处理
function handleExported(format) {
  console.log('导出完成，格式:', format)
}
</script>

<style scoped>
.editor-view {
  padding: 20px;
  min-height: calc(100vh - 60px);
  background-color: #f5f7fa;
}

.editor-container {
  max-width: 1400px;
  margin: 0 auto;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.editor-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.section {
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.section h3 {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.template-selector {
  margin-bottom: 20px;
}

.fields-panel {
  max-height: 600px;
  overflow-y: auto;
}

.field-item {
  margin-bottom: 20px;
  padding: 15px;
  background: #f9fafb;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.field-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.field-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.drag-handle {
  cursor: move;
  color: #909399;
  font-size: 18px;
}

.drag-handle:hover {
  color: #409eff;
}

.field-label {
  font-weight: 500;
  color: #606266;
}

.required-mark {
  color: #f56c6c;
  margin-left: 4px;
}

.field-content {
  width: 100%;
}

.validation-alert {
  margin-top: 20px;
}

.validation-alert ul {
  margin: 10px 0 0 0;
  padding-left: 20px;
}

.validation-alert li {
  margin: 5px 0;
}

.integrity-score {
  margin-top: 20px;
  padding: 15px;
  background: #f0f9ff;
  border-radius: 6px;
}

.integrity-score span {
  display: block;
  margin-bottom: 10px;
  font-weight: 500;
  color: #606266;
}

.copyright-section {
  background: #fffbf0;
  border: 1px solid #ffd666;
}

.content-body-section {
  background: #f0f9ff;
  border: 1px solid #91d5ff;
}

.content-body-section h3 {
  color: #1890ff;
}

.preview-panel {
  position: sticky;
  top: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.preview-content {
  max-height: 600px;
  overflow-y: auto;
  padding: 15px;
  background: #f5f7fa;
}

.preview-content pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #2c3e50;
}

.preview-content code {
  display: block;
}

/* 拖拽时的样式 */
.sortable-ghost {
  opacity: 0.5;
  background: #e1f3ff;
}

.sortable-drag {
  opacity: 0.8;
}
</style>
