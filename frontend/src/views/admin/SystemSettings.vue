<template>
  <div class="system-settings">
    <el-card class="settings-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h3>系统设置</h3>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="border-card">
        <!-- 基本设置 -->
        <el-tab-pane label="基本设置" name="basic">
          <el-form
            ref="basicFormRef"
            :model="basicForm"
            label-width="120px"
            class="settings-form"
          >
            <el-form-item label="网站名称">
              <el-input
                v-model="basicForm.siteName"
                placeholder="请输入网站名称"
              />
            </el-form-item>

            <el-form-item label="网站描述">
              <el-input
                v-model="basicForm.siteDescription"
                type="textarea"
                :rows="3"
                placeholder="请输入网站描述"
              />
            </el-form-item>

            <el-form-item label="首页标题">
              <el-input
                v-model="basicForm.heroTitle"
                placeholder="请输入首页标题"
              />
            </el-form-item>

            <el-form-item label="首页副标题">
              <el-input
                v-model="basicForm.heroSubtitle"
                placeholder="请输入首页副标题"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveBasicSettings" :loading="saving">
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- Logo设置 -->
        <el-tab-pane label="Logo设置" name="logo">
          <div class="logo-settings">
            <div class="current-logo" v-if="currentLogoUrl">
              <h4>当前Logo</h4>
              <img :src="currentLogoUrl" alt="当前Logo" class="logo-preview" />
            </div>

            <el-divider />

            <h4>上传新Logo</h4>
            <el-upload
              class="logo-uploader"
              :action="uploadAction"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleLogoSuccess"
              :on-error="handleLogoError"
              :before-upload="beforeLogoUpload"
              accept="image/jpeg,image/png,image/gif,image/webp"
              drag
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                将Logo文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持JPG、PNG、GIF、WebP格式，文件大小不超过5MB
                </div>
              </template>
            </el-upload>

            <div v-if="uploadedLogoUrl" class="uploaded-logo">
              <h4>新上传的Logo</h4>
              <img :src="uploadedLogoUrl" alt="新Logo" class="logo-preview" />
              <p class="success-text">✓ Logo上传成功！刷新页面即可看到新Logo</p>
            </div>
          </div>
        </el-tab-pane>

        <!-- SEO设置 -->
        <el-tab-pane label="SEO设置" name="seo">
          <el-alert
            title="robots.txt已配置"
            type="success"
            :closable="false"
            show-icon
            class="seo-alert"
          >
            <p>系统已自动配置robots.txt文件，优化AI爬虫访问。</p>
            <p>访问地址：<a href="/robots.txt" target="_blank">/robots.txt</a></p>
          </el-alert>

          <el-divider />

          <div class="seo-info">
            <h4>SEO优化功能</h4>
            <ul>
              <li>✓ 结构化数据（JSON-LD）自动生成</li>
              <li>✓ Schema.org标准标记</li>
              <li>✓ 语义化HTML输出</li>
              <li>✓ AI专用API接口</li>
              <li>✓ Sitemap自动生成</li>
              <li>✓ robots.txt访问控制</li>
            </ul>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getPublicConfig, getAllConfig, updateConfig } from '@/api/system'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const activeTab = ref('basic')
const saving = ref(false)
const currentLogoUrl = ref('')
const uploadedLogoUrl = ref('')

const basicFormRef = ref(null)
const basicForm = ref({
  siteName: '',
  siteDescription: '',
  heroTitle: '',
  heroSubtitle: ''
})

const uploadAction = `${import.meta.env.VITE_API_BASE_URL}/api/admin/config/logo`
const uploadHeaders = {
  Authorization: `Bearer ${authStore.token}`
}

const loadSettings = async () => {
  try {
    const response = await getPublicConfig()
    // axios拦截器已经解包
    const config = response.data ? response.data : response
    basicForm.value = {
      siteName: config.siteName || '',
      siteDescription: config.siteDescription || '',
      heroTitle: config.heroTitle || '',
      heroSubtitle: config.heroSubtitle || ''
    }
    currentLogoUrl.value = config.logoUrl || ''
  } catch (error) {
    console.error('Failed to load settings:', error)
    ElMessage.error('加载设置失败')
  }
}

const saveBasicSettings = async () => {
  saving.value = true
  try {
    // 保存每个配置项
    const configs = [
      { configKey: 'site.name', configValue: basicForm.value.siteName, description: '网站名称' },
      { configKey: 'site.description', configValue: basicForm.value.siteDescription, description: '网站描述' },
      { configKey: 'site.hero.title', configValue: basicForm.value.heroTitle, description: '首页标题' },
      { configKey: 'site.hero.subtitle', configValue: basicForm.value.heroSubtitle, description: '首页副标题' }
    ]

    for (const config of configs) {
      await updateConfig(config)
    }

    ElMessage.success('设置保存成功')
  } catch (error) {
    console.error('Failed to save settings:', error)
    ElMessage.error('保存设置失败')
  } finally {
    saving.value = false
  }
}

const beforeLogoUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传JPG、PNG、GIF、WebP格式的图片！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB！')
    return false
  }
  return true
}

const handleLogoSuccess = (response) => {
  if (response.success) {
    uploadedLogoUrl.value = response.data.logoUrl
    currentLogoUrl.value = response.data.logoUrl
    ElMessage.success('Logo上传成功')
  } else {
    ElMessage.error(response.error?.message || 'Logo上传失败')
  }
}

const handleLogoError = (error) => {
  console.error('Logo upload error:', error)
  ElMessage.error('Logo上传失败')
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.system-settings {
  padding: 0;
}

.settings-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.settings-form {
  max-width: 600px;
  padding: 20px 0;
}

.logo-settings {
  padding: 20px 0;
}

.current-logo h4,
.uploaded-logo h4,
.seo-info h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.logo-preview {
  max-width: 300px;
  max-height: 150px;
  object-fit: contain;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px;
  background: #f5f7fa;
}

.logo-uploader {
  margin: 20px 0;
}

.uploaded-logo {
  margin-top: 30px;
  padding: 20px;
  background: #f0f9ff;
  border-radius: 8px;
  border: 1px solid #b3d8ff;
}

.success-text {
  margin: 16px 0 0 0;
  color: #67c23a;
  font-weight: 500;
}

.seo-alert {
  margin-bottom: 20px;
}

.seo-alert p {
  margin: 8px 0;
}

.seo-alert a {
  color: #409eff;
  text-decoration: none;
}

.seo-alert a:hover {
  text-decoration: underline;
}

.seo-info {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.seo-info ul {
  margin: 16px 0 0 0;
  padding-left: 20px;
}

.seo-info li {
  margin: 8px 0;
  color: #606266;
  line-height: 1.6;
}

:deep(.el-tabs--border-card) {
  border: 1px solid #e4e7ed;
  box-shadow: none;
}

:deep(.el-upload-dragger) {
  width: 100%;
  max-width: 500px;
}
</style>
