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

            <el-divider content-position="left">Hero区域样式</el-divider>

            <el-form-item label="背景颜色">
              <div class="color-picker-row">
                <el-color-picker
                  v-model="basicForm.heroBgColor"
                  show-alpha
                />
                <el-input
                  v-model="basicForm.heroBgColor"
                  placeholder="如: #f0f7ff 或 linear-gradient(135deg, #f0f7ff 0%, #e8f4fd 100%)"
                  class="color-input"
                />
                <el-button size="small" @click="basicForm.heroBgColor = ''">重置</el-button>
              </div>
              <div class="form-tip">支持颜色值或渐变，留空使用默认渐变背景</div>
            </el-form-item>

            <el-form-item label="文字颜色">
              <div class="color-picker-row">
                <el-color-picker
                  v-model="basicForm.heroTextColor"
                />
                <el-input
                  v-model="basicForm.heroTextColor"
                  placeholder="如: #1a73e8"
                  class="color-input"
                />
                <el-button size="small" @click="basicForm.heroTextColor = ''">重置</el-button>
              </div>
              <div class="form-tip">设置Hero区域标题和副标题的文字颜色</div>
            </el-form-item>

            <!-- 预览 -->
            <el-form-item label="效果预览">
              <div class="hero-preview" :style="heroPreviewStyle">
                <div class="preview-title">{{ basicForm.heroTitle || '首页标题' }}</div>
                <div class="preview-subtitle">{{ basicForm.heroSubtitle || '首页副标题' }}</div>
              </div>
            </el-form-item>

            <el-divider content-position="left">邮件设置</el-divider>

            <el-form-item label="发件人名称">
              <el-input
                v-model="basicForm.mailFromName"
                placeholder="如：数流精灵"
              />
              <div class="form-tip">邮件发送时显示的发件人名称，支持中文</div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveBasicSettings">
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

        <!-- 页脚链接设置 -->
        <el-tab-pane label="页脚链接" name="footer">
          <div class="footer-settings">
            <div class="footer-tip">
              <el-alert type="info" :closable="false" show-icon>
                配置首页底部的链接分组，每个分组包含多个链接。
              </el-alert>
            </div>

            <h4 class="section-label">链接分组</h4>
            <div v-for="(group, groupIndex) in footerLinkGroups" :key="groupIndex" class="link-group">
              <div class="group-header">
                <el-input
                  v-model="group.group"
                  placeholder="分组名称"
                  style="width: 200px"
                />
                <el-button type="danger" size="small" @click="removeGroup(groupIndex)">
                  删除分组
                </el-button>
              </div>
              
              <div class="group-links">
                <div v-for="(link, linkIndex) in group.links" :key="linkIndex" class="link-item">
                  <el-input
                    v-model="link.name"
                    placeholder="链接名称"
                    style="width: 150px"
                  />
                  <el-input
                    v-model="link.url"
                    placeholder="链接地址 (如: /about 或 https://...)"
                    style="flex: 1"
                  />
                  <el-button type="danger" size="small" circle @click="removeLink(groupIndex, linkIndex)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
                <el-button type="primary" size="small" plain @click="addLink(groupIndex)">
                  + 添加链接
                </el-button>
              </div>
            </div>

            <div class="footer-actions">
              <el-button type="primary" plain @click="addGroup">+ 添加分组</el-button>
              <el-button type="primary" @click="saveFooterLinks">保存链接分组</el-button>
            </div>

            <el-divider />

            <h4 class="section-label">底部链接（网站地图、协议等）</h4>
            <div class="bottom-links-section">
              <div v-for="(link, index) in footerBottomLinks" :key="index" class="link-item">
                <el-input
                  v-model="link.name"
                  placeholder="链接名称"
                  style="width: 150px"
                />
                <el-input
                  v-model="link.url"
                  placeholder="链接地址"
                  style="flex: 1"
                />
                <el-button type="danger" size="small" circle @click="removeBottomLink(index)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
              <div class="footer-actions">
                <el-button type="primary" size="small" plain @click="addBottomLink">+ 添加底部链接</el-button>
                <el-button type="primary" @click="saveFooterBottomLinks">保存底部链接</el-button>
              </div>
            </div>

            <el-divider />

            <h4 class="section-label">备案号与版权信息</h4>
            <div class="copyright-settings">
              <el-form label-width="100px">
                <el-form-item label="备案号">
                  <el-input
                    v-model="footerCopyright.icp"
                    placeholder="如：京ICP备12345678号"
                  />
                </el-form-item>
                <el-form-item label="版权信息">
                  <el-input
                    v-model="footerCopyright.copyright"
                    placeholder="如：© 2024 数流精灵 版权所有"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveFooterCopyright">保存备案与版权信息</el-button>
                </el-form-item>
              </el-form>
            </div>

            <el-divider />

            <h4 class="section-label">首页社交图标链接</h4>
            <div class="social-links-settings">
              <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
                配置首页底部社交图标的链接地址
              </el-alert>
              <el-form label-width="120px">
                <el-form-item label="分享链接">
                  <el-input
                    v-model="socialLinks.share"
                    placeholder="如: https://example.com/share 或 /share"
                  />
                  <div class="form-tip">分享图标（三个点V形）的链接地址</div>
                </el-form-item>
                <el-form-item label="评论链接">
                  <el-input
                    v-model="socialLinks.comment"
                    placeholder="如: https://example.com/comment 或 /comment"
                  />
                  <div class="form-tip">评论图标（气泡）的链接地址</div>
                </el-form-item>
                <el-form-item label="链接地址">
                  <el-input
                    v-model="socialLinks.link"
                    placeholder="如: https://example.com/link 或 /link"
                  />
                  <div class="form-tip">链接图标（回形针）的链接地址</div>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveSocialLinks">保存社交链接</el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Delete } from '@element-plus/icons-vue'
import { getPublicConfig, getAllConfig, updateConfig, uploadLogo } from '@/api/system'
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
  heroSubtitle: '',
  heroBgColor: '',
  heroTextColor: '',
  mailFromName: '数流精灵'
})

// 页脚链接配置
const footerLinkGroups = ref([
  { group: '产品功能', links: [{ name: '结构化模板', url: '#' }, { name: '内容编辑器', url: '#' }] },
  { group: '资源中心', links: [{ name: '帮助文档', url: '#' }, { name: '教程指南', url: '#' }] },
  { group: '关于我们', links: [{ name: '公司介绍', url: '#' }, { name: '联系我们', url: '#' }] }
])

// 页脚底部链接配置
const footerBottomLinks = ref([
  { name: '网站地图', url: '#' },
  { name: 'AI数据接口', url: '#' },
  { name: '机器人协议', url: '#' }
])

// 备案号与版权信息
const footerCopyright = ref({
  icp: '',
  copyright: ''
})

// 社交链接配置
const socialLinks = ref({
  share: '#',
  comment: '#',
  link: '#'
})

// Hero 预览样式
const heroPreviewStyle = computed(() => {
  const style = {}
  if (basicForm.value.heroBgColor) {
    style.background = basicForm.value.heroBgColor
  } else {
    style.background = 'linear-gradient(135deg, #f0f7ff 0%, #e8f4fd 100%)'
  }
  if (basicForm.value.heroTextColor) {
    style.color = basicForm.value.heroTextColor
  }
  return style
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
      heroSubtitle: config.heroSubtitle || '',
      heroBgColor: config.heroBgColor || '',
      heroTextColor: config.heroTextColor || '',
      mailFromName: config.mailFromName || '数流精灵'
    }
    currentLogoUrl.value = config.logoUrl || ''
    // 加载页脚链接
    if (config.footerLinks) {
      try {
        const links = typeof config.footerLinks === 'string' ? JSON.parse(config.footerLinks) : config.footerLinks
        if (Array.isArray(links) && links.length > 0) {
          footerLinkGroups.value = links
        }
      } catch (e) {
        console.error('解析页脚链接失败:', e)
      }
    }
    // 加载页脚底部链接
    if (config.footerBottomLinks) {
      try {
        const links = typeof config.footerBottomLinks === 'string' ? JSON.parse(config.footerBottomLinks) : config.footerBottomLinks
        if (Array.isArray(links) && links.length > 0) {
          footerBottomLinks.value = links
        }
      } catch (e) {
        console.error('解析页脚底部链接失败:', e)
      }
    }
    // 加载备案号与版权信息
    if (config.footerIcp) {
      footerCopyright.value.icp = config.footerIcp
    }
    if (config.footerCopyright) {
      footerCopyright.value.copyright = config.footerCopyright
    }
    // 加载社交链接配置
    if (config.socialLinks) {
      try {
        const links = typeof config.socialLinks === 'string' ? JSON.parse(config.socialLinks) : config.socialLinks
        if (links && typeof links === 'object') {
          socialLinks.value = {
            share: links.share || '#',
            comment: links.comment || '#',
            link: links.link || '#'
          }
        }
      } catch (e) {
        console.error('解析社交链接配置失败:', e)
      }
    }
    // 加载邮件发件人名称配置（管理员专用）
    try {
      const mailConfigResponse = await getAllConfig()
      const allConfigs = mailConfigResponse.data || mailConfigResponse
      if (allConfigs && allConfigs['mail.from.name']) {
        basicForm.value.mailFromName = allConfigs['mail.from.name']
      }
    } catch (e) {
      console.error('加载邮件配置失败:', e)
      // 如果加载失败，使用默认值
      basicForm.value.mailFromName = '数流精灵'
    }
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
      { configKey: 'site.hero.subtitle', configValue: basicForm.value.heroSubtitle, description: '首页副标题' },
      { configKey: 'site.hero.bgColor', configValue: basicForm.value.heroBgColor, description: 'Hero背景颜色' },
      { configKey: 'site.hero.textColor', configValue: basicForm.value.heroTextColor, description: 'Hero文字颜色' },
      { configKey: 'mail.from.name', configValue: basicForm.value.mailFromName || '数流精灵', description: '邮件发件人名称' }
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

// 页脚链接管理
const addGroup = () => {
  footerLinkGroups.value.push({ group: '新分组', links: [{ name: '链接名称', url: '#' }] })
}

const removeGroup = (index) => {
  footerLinkGroups.value.splice(index, 1)
}

const addLink = (groupIndex) => {
  footerLinkGroups.value[groupIndex].links.push({ name: '', url: '#' })
}

const removeLink = (groupIndex, linkIndex) => {
  footerLinkGroups.value[groupIndex].links.splice(linkIndex, 1)
}

const saveFooterLinks = async () => {
  try {
    await updateConfig({
      configKey: 'site.footer.links',
      configValue: JSON.stringify(footerLinkGroups.value),
      description: '页脚链接配置'
    })
    ElMessage.success('页脚链接保存成功')
  } catch (error) {
    console.error('保存页脚链接失败:', error)
    ElMessage.error('保存失败')
  }
}

// 底部链接管理
const addBottomLink = () => {
  footerBottomLinks.value.push({ name: '', url: '#' })
}

const removeBottomLink = (index) => {
  footerBottomLinks.value.splice(index, 1)
}

const saveFooterBottomLinks = async () => {
  try {
    await updateConfig({
      configKey: 'site.footer.bottomLinks',
      configValue: JSON.stringify(footerBottomLinks.value),
      description: '页脚底部链接配置'
    })
    ElMessage.success('底部链接保存成功')
  } catch (error) {
    console.error('保存底部链接失败:', error)
    ElMessage.error('保存失败')
  }
}

// 保存备案号与版权信息
const saveFooterCopyright = async () => {
  try {
    await updateConfig({
      configKey: 'site.footer.icp',
      configValue: footerCopyright.value.icp,
      description: '备案号'
    })
    await updateConfig({
      configKey: 'site.footer.copyright',
      configValue: footerCopyright.value.copyright,
      description: '版权信息'
    })
    ElMessage.success('备案与版权信息保存成功')
  } catch (error) {
    console.error('保存备案与版权信息失败:', error)
    ElMessage.error('保存失败')
  }
}

// 保存社交链接
const saveSocialLinks = async () => {
  try {
    await updateConfig({
      configKey: 'site.social.links',
      configValue: JSON.stringify(socialLinks.value),
      description: '首页社交图标链接配置'
    })
    ElMessage.success('社交链接保存成功')
  } catch (error) {
    console.error('保存社交链接失败:', error)
    ElMessage.error('保存失败')
  }
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

/* 页脚链接设置 */
.footer-settings {
  padding: 20px 0;
}

.footer-tip {
  margin-bottom: 20px;
}

.link-group {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.group-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.group-links {
  padding-left: 16px;
}

.link-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.footer-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.section-label {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.bottom-links-section {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
}

.copyright-settings {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
}

.social-links-settings {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
}

/* 颜色选择器行 */
.color-picker-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.color-input {
  flex: 1;
  max-width: 400px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* Hero 预览 */
.hero-preview {
  width: 100%;
  max-width: 500px;
  padding: 32px 24px;
  border-radius: 8px;
  text-align: center;
  border: 1px solid #e4e7ed;
}

.preview-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 8px;
  color: inherit;
}

.preview-subtitle {
  font-size: 16px;
  opacity: 0.8;
  color: inherit;
}
</style>
