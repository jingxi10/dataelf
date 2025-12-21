<template>
  <el-dialog
    v-model="visible"
    title="分享内容"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-loading="loading" class="share-dialog">
      <!-- 分享链接 -->
      <div class="share-link-section">
        <h4>分享链接</h4>
        <div class="link-input-group">
          <el-input
            v-model="shareData.contentUrl"
            readonly
            class="link-input"
          />
          <el-button
            type="primary"
            @click="copyLink"
            :icon="DocumentCopy"
          >
            复制链接
          </el-button>
        </div>
      </div>

      <!-- 社交媒体分享 -->
      <div class="social-share-section">
        <h4>分享到社交媒体</h4>
        <div class="social-buttons">
          <!-- 微信 -->
          <div class="social-item" @click="shareToWechat">
            <div class="social-icon wechat">
              <svg viewBox="0 0 1024 1024" width="24" height="24">
                <path d="M664.250054 368.541681c10.015098 0 19.892049 0.732687 29.67281 1.795902-26.647917-122.810047-159.358451-214.077703-310.826188-214.077703-169.353083 0-308.085774 114.232694-308.085774 259.274068 0 83.708494 46.165436 152.460344 123.281791 205.78483l-30.80868 91.730191 107.688651-53.455469c38.558178 7.53665 69.459978 15.308661 107.924012 15.308661 9.66308 0 19.230993-0.470721 28.752858-1.225921-6.025227-20.36584-9.521864-41.723264-9.521864-63.862493C402.328693 476.632491 517.908058 368.541681 664.250054 368.541681zM498.62897 285.87389c23.200398 0 38.557154 15.120372 38.557154 38.061874 0 22.846334-15.356756 38.156018-38.557154 38.156018-23.107277 0-46.260603-15.309684-46.260603-38.156018C452.368366 300.994262 475.522716 285.87389 498.62897 285.87389zM283.016307 362.090758c-23.107277 0-46.402843-15.309684-46.402843-38.156018 0-22.941502 23.295566-38.061874 46.402843-38.061874 23.081695 0 38.46301 15.120372 38.46301 38.061874C321.479317 346.782098 306.098002 362.090758 283.016307 362.090758zM945.448458 606.151333c0-121.888048-123.258255-221.236753-261.683954-221.236753-146.57838 0-262.015505 99.348706-262.015505 221.236753 0 122.06508 115.437126 221.200938 262.015505 221.200938 30.66644 0 61.617359-7.609305 92.423993-15.262612l84.513836 45.786813-23.178909-76.17082C899.379213 735.776599 945.448458 674.90216 945.448458 606.151333zM598.803483 567.994292c-15.332197 0-30.807656-15.096836-30.807656-30.501688 0-15.190981 15.47546-30.477129 30.807656-30.477129 23.295566 0 38.558178 15.286148 38.558178 30.477129C637.361661 552.897456 622.099049 567.994292 598.803483 567.994292zM768.25071 567.994292c-15.213493 0-30.594809-15.096836-30.594809-30.501688 0-15.190981 15.381315-30.477129 30.594809-30.477129 23.107277 0 38.558178 15.286148 38.558178 30.477129C806.808888 552.897456 791.357987 567.994292 768.25071 567.994292z" fill="#00C800"/>
              </svg>
            </div>
            <span>微信</span>
          </div>

          <!-- 微博 -->
          <a
            :href="shareData.shareLinks?.weibo"
            target="_blank"
            class="social-item"
            @click="recordShare('weibo')"
          >
            <div class="social-icon weibo">
              <svg viewBox="0 0 1024 1024" width="24" height="24">
                <path d="M851.4 590.193c-22.196-66.233-90.385-90.422-105.912-91.863-15.523-1.442-29.593-9.94-19.295-27.505 10.302-17.566 29.304-68.684-7.248-104.681-36.564-36.14-116.512-22.462-173.094 0.866-56.434 23.327-53.39 7.055-51.65-8.925 1.89-16.848 32.355-111.02-60.791-122.395-91.408-11.375-124.063 91.779-124.063 91.779-10.633 24.901-13.05 63.655-5.79 85.704 7.255 22.049-0.436 29.159-26.475 26.763-26.038-2.397-128.449-3.684-187.638 75.841-59.189 79.377-75.335 181.782-75.335 181.782-16.849 111.02 41.478 226.677 204.831 272.745 163.35 46.072 383.562-6.367 462.378-153.497 78.959-147.13 66.081-205.114 43.885-271.348zM464.866 799.85c-121.16 12.191-219.56-49.105-219.56-136.945 0-87.843 98.397-159.46 219.56-171.653 121.159-12.191 219.559 49.105 219.559 136.945 0 87.84-98.397 159.46-219.559 171.653z" fill="#E71F19"/>
                <path d="M477.463 708.165c-67.186 5.947-118.824-31.088-115.483-82.725 3.338-51.641 60.337-88.676 127.523-82.729 67.182 5.947 118.82 31.088 115.486 82.725-3.341 51.64-60.344 88.676-127.526 82.729z m43.992-106.284c-8.493-14.213-27.337-19.998-42.362-12.942-14.88 7.053-19.821 24.188-11.182 38.401 8.644 14.213 27.337 20.287 42.362 12.945 14.88-7.056 19.821-24.191 11.182-38.404z m-20.291 73.391c-3.337 5.947-10.888 8.346-16.849 5.515-5.961-2.978-7.703-9.938-4.365-15.885 3.341-5.95 10.888-8.35 16.849-5.519 5.961 2.978 7.703 9.938 4.365 15.889z" fill="#FF9933"/>
                <path d="M812.379 319.935c-33.849-90.135-116.512-133.927-184.699-97.933-15.527 7.779-22.344 26.763-15.092 42.356 7.252 15.596 25.832 22.462 41.067 15.307 31.528-16.992 70.146 3.396 86.292 45.466 16.146 42.07 2.022 86.268-29.506 103.549-14.379 7.779-20.577 26.475-13.613 41.782 6.961 15.307 25.541 21.884 40.504 14.106 67.899-37.424 92.022-133.638 58.028-223.773-0.145-0.434-0.578-0.868-0.868-1.302zM701.235 372.145c-8.929-24.034-30.577-35.697-48.582-26.038-17.855 9.659-24.329 33.403-15.4 57.437 8.929 24.034 30.721 35.697 48.582 26.038 17.855-9.803 24.328-33.403 15.4-57.437z" fill="#E71F19"/>
              </svg>
            </div>
            <span>微博</span>
          </a>

          <!-- QQ空间 -->
          <a
            :href="shareData.shareLinks?.qzone"
            target="_blank"
            class="social-item"
            @click="recordShare('qzone')"
          >
            <div class="social-icon qzone">
              <svg viewBox="0 0 1024 1024" width="24" height="24">
                <path d="M511.09 63.88c-247.83 0-448.72 200.88-448.72 448.72s200.88 448.72 448.72 448.72 448.72-200.88 448.72-448.72-200.89-448.72-448.72-448.72z m267.61 619.17c-6.77 10.37-17.14 15.55-27.51 15.55-6.77 0-13.54-1.8-19.71-6.17-15.55-10.37-19.71-31.68-9.34-47.23 29.31-44.27 44.86-95.69 44.86-148.91 0-150.71-122.57-273.28-273.28-273.28s-273.28 122.57-273.28 273.28c0 53.22 15.55 104.64 44.86 148.91 10.37 15.55 6.17 36.86-9.34 47.23s-36.86 6.17-47.23-9.34c-34.88-52.62-53.22-113.98-53.22-177.46 0-179.42 145.91-325.33 325.33-325.33s325.33 145.91 325.33 325.33c-0.6 63.48-18.94 124.84-53.82 177.46z" fill="#FDDA44"/>
                <path d="M778.26 760.32c-18.34 0-33.08-14.74-33.08-33.08 0-18.34 14.74-33.08 33.08-33.08 18.34 0 33.08 14.74 33.08 33.08 0 18.34-14.74 33.08-33.08 33.08z" fill="#FDDA44"/>
              </svg>
            </div>
            <span>QQ空间</span>
          </a>

          <!-- Twitter -->
          <a
            :href="shareData.shareLinks?.twitter"
            target="_blank"
            class="social-item"
            @click="recordShare('twitter')"
          >
            <div class="social-icon twitter">
              <svg viewBox="0 0 1024 1024" width="24" height="24">
                <path d="M928 254.3c-30.6 13.2-63.9 22.7-98.2 26.4 35.4-21.1 62.3-54.4 75-94-32.7 19.5-69.7 33.8-108.2 41.2C765.4 194.6 721.1 174 672 174c-94.5 0-170.5 76.6-170.5 170.6 0 13.2 1.6 26.4 4.2 39.1-141.5-7.4-267.7-75-351.6-178.5-14.8 25.4-23.2 54.4-23.2 86.1 0 59.2 30.1 111.4 76 142.1-28-1.1-54.4-9-77.1-21.7v2.1c0 82.9 58.6 151.6 136.7 167.4-14.3 3.7-29.6 5.8-44.9 5.8-11.1 0-21.6-1.1-32.2-2.6C211 652 273.9 701.1 348.8 702.7c-58.6 45.9-132 72.9-211.7 72.9-14.3 0-27.5-.5-41.2-2.1C171.5 822 261.2 850 357.8 850 671.4 850 843 590.2 843 364.7c0-7.4 0-14.8-.5-22.2 33.2-24.3 62.3-54.4 85.5-88.2z" fill="#1DA1F2"/>
              </svg>
            </div>
            <span>Twitter</span>
          </a>

          <!-- Facebook -->
          <a
            :href="shareData.shareLinks?.facebook"
            target="_blank"
            class="social-item"
            @click="recordShare('facebook')"
          >
            <div class="social-icon facebook">
              <svg viewBox="0 0 1024 1024" width="24" height="24">
                <path d="M512 0C229.2 0 0 229.2 0 512s229.2 512 512 512 512-229.2 512-512S794.8 0 512 0z m144.9 354.9h-74.2c-8.7 0-18.4 11.5-18.4 26.8v53.4h92.6l-13.9 76.6h-78.7V768H486V511.7h-79.4v-76.6H486v-44.5c0-64.4 44.7-116.7 105.8-116.7h65.1v81z" fill="#3B5998"/>
              </svg>
            </div>
            <span>Facebook</span>
          </a>

          <!-- LinkedIn -->
          <a
            :href="shareData.shareLinks?.linkedin"
            target="_blank"
            class="social-item"
            @click="recordShare('linkedin')"
          >
            <div class="social-icon linkedin">
              <svg viewBox="0 0 1024 1024" width="24" height="24">
                <path d="M512 0C229.2 0 0 229.2 0 512s229.2 512 512 512 512-229.2 512-512S794.8 0 512 0zM368.6 755.2H264.3V411.8h104.3v343.4zM316.5 373.1c-33.4 0-60.5-27.1-60.5-60.5s27.1-60.5 60.5-60.5 60.5 27.1 60.5 60.5-27.1 60.5-60.5 60.5z m443.1 382.1H655.3V586.7c0-38.9-0.7-88.9-54.2-88.9-54.2 0-62.5 42.3-62.5 86.1v171.3H434.3V411.8h100.2v46.9h1.4c13.9-26.4 47.9-54.2 98.6-54.2 105.5 0 125 69.4 125 159.6v191.1z" fill="#0077B5"/>
              </svg>
            </div>
            <span>LinkedIn</span>
          </a>
        </div>
      </div>

      <!-- 二维码（微信分享） -->
      <el-dialog
        v-model="qrcodeVisible"
        title="微信扫码分享"
        width="300px"
        append-to-body
      >
        <div class="qrcode-container">
          <div ref="qrcodeRef" class="qrcode"></div>
          <p class="qrcode-tip">使用微信扫描二维码分享</p>
        </div>
      </el-dialog>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentCopy } from '@element-plus/icons-vue'
import { getShareLinks, recordShareAction } from '@/api/share'
import QRCode from 'qrcode'

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

const emit = defineEmits(['update:modelValue', 'shared'])

const visible = ref(false)
const loading = ref(false)
const qrcodeVisible = ref(false)
const qrcodeRef = ref(null)
const shareData = ref({
  contentUrl: '',
  title: '',
  description: '',
  shareLinks: {}
})

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    loadShareLinks()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const loadShareLinks = async () => {
  loading.value = true
  try {
    const response = await getShareLinks(props.contentId)
    // axios拦截器已经解包
    shareData.value = response.data ? response.data : response
  } catch (error) {
    console.error('Failed to load share links:', error)
    ElMessage.error('加载分享链接失败')
  } finally {
    loading.value = false
  }
}

const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(shareData.value.contentUrl)
    ElMessage.success('链接已复制到剪贴板')
    recordShare('copy')
  } catch (error) {
    // 降级方案
    const input = document.createElement('input')
    input.value = shareData.value.contentUrl
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    ElMessage.success('链接已复制到剪贴板')
    recordShare('copy')
  }
}

const shareToWechat = () => {
  qrcodeVisible.value = true
  nextTick(async () => {
    if (qrcodeRef.value) {
      qrcodeRef.value.innerHTML = ''
      try {
        const canvas = document.createElement('canvas')
        await QRCode.toCanvas(canvas, shareData.value.contentUrl, {
          width: 200,
          margin: 2,
          color: {
            dark: '#000000',
            light: '#ffffff'
          }
        })
        qrcodeRef.value.appendChild(canvas)
      } catch (error) {
        console.error('Failed to generate QR code:', error)
      }
    }
  })
  recordShare('wechat')
}

const recordShare = async (platform) => {
  try {
    await recordShareAction(props.contentId, platform)
    emit('shared', platform)
  } catch (error) {
    console.error('Failed to record share:', error)
  }
}

const handleClose = () => {
  visible.value = false
}
</script>

<style scoped>
.share-dialog {
  padding: 10px 0;
}

.share-link-section {
  margin-bottom: 30px;
}

.share-link-section h4,
.social-share-section h4 {
  margin: 0 0 15px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.link-input-group {
  display: flex;
  gap: 10px;
}

.link-input {
  flex: 1;
}

.social-share-section {
  margin-top: 20px;
}

.social-buttons {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.social-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 15px 10px;
  border: 1px solid #E8EAED;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
  color: #606266;
}

.social-item:hover {
  border-color: #1A73E8;
  background: #F8F9FA;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.social-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #F8F9FA;
}

.social-item:hover .social-icon {
  transform: scale(1.1);
}

.social-item span {
  font-size: 12px;
  font-weight: 500;
}

.qrcode-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.qrcode {
  margin-bottom: 15px;
}

.qrcode-tip {
  margin: 0;
  font-size: 14px;
  color: #666;
  text-align: center;
}

@media (max-width: 768px) {
  .social-buttons {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
