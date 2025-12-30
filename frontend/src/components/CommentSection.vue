<template>
  <div class="comment-section">
    <div class="comment-header">
      <h3>评论 ({{ totalComments }})</h3>
    </div>

    <!-- 评论输入框 -->
    <div v-if="isAuthenticated" class="comment-input">
      <el-input
        v-model="commentText"
        type="textarea"
        :rows="3"
        placeholder="写下你的评论..."
        maxlength="500"
        show-word-limit
      />
      <div class="input-actions">
        <el-button
          type="primary"
          @click="submitComment"
          :disabled="!commentText.trim()"
        >
          发表评论
        </el-button>
      </div>
    </div>

    <div v-else class="login-prompt">
      <p>
        <router-link to="/login">登录</router-link>
        后发表评论
      </p>
    </div>

    <!-- 评论列表 -->
    <div v-if="loading" class="loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <div v-else-if="comments.length === 0" class="empty-comments">
      <p>暂无评论，快来发表第一条评论吧！</p>
    </div>

    <div v-else class="comment-list">
      <div
        v-for="comment in sortedComments"
        :key="comment.id"
        class="comment-item"
        :class="{ 'pinned': comment.isPinned }"
      >
        <div class="comment-avatar">
          <el-icon><User /></el-icon>
        </div>
        <div class="comment-content">
          <div class="comment-meta">
            <span class="comment-author">{{ comment.userEmail || '匿名用户' }}</span>
            <el-tag v-if="comment.isPinned" type="danger" size="small" class="pinned-tag">
              <el-icon><Top /></el-icon>
              置顶
            </el-tag>
            <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
          </div>
          <div class="comment-text">{{ comment.commentText }}</div>
          
          <!-- 操作按钮 -->
          <div v-if="canOperate(comment)" class="comment-actions">
            <!-- 管理员置顶按钮 -->
            <el-button
              v-if="isAdmin"
              :type="comment.isPinned ? 'warning' : 'default'"
              size="small"
              :icon="Top"
              @click="handleTogglePin(comment)"
            >
              {{ comment.isPinned ? '取消置顶' : '置顶' }}
            </el-button>
            <!-- 删除按钮（管理员或作者） -->
            <el-button
              v-if="canDelete(comment)"
              type="danger"
              size="small"
              :icon="Delete"
              @click="handleDelete(comment)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="comment-pagination">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="totalComments"
        layout="prev, pager, next"
        @current-change="loadComments"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, User, Delete, Top } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { createComment, getCommentsByContent, deleteComment, togglePinComment } from '@/api/comment'

const props = defineProps({
  contentId: {
    type: Number,
    required: true
  }
})

const authStore = useAuthStore()
const isAuthenticated = computed(() => authStore.isAuthenticated)
const isAdmin = computed(() => authStore.isAdmin)
const currentUserId = computed(() => authStore.user?.id)

const commentText = ref('')
const comments = ref([])
const loading = ref(false)
const submitting = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalComments = ref(0)
const totalPages = ref(0)

const loadComments = async (page = 1) => {
  loading.value = true
  try {
    const params = {
      page: page - 1,
      size: pageSize.value,
      sort: 'createdAt,desc'
    }

    const response = await getCommentsByContent(props.contentId, params)
    // axios拦截器已经解包
    const data = response.content ? response : (response.data || response)
    comments.value = data.content || []
    totalComments.value = data.totalElements || 0
    totalPages.value = data.totalPages || 0
  } catch (error) {
    console.error('Failed to load comments:', error)
  } finally {
    loading.value = false
  }
}

const submitComment = async () => {
  if (!commentText.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    await createComment({
      contentId: props.contentId,
      commentText: commentText.value.trim()
    })

    ElMessage.success('评论发表成功')
    commentText.value = ''
    currentPage.value = 1
    await loadComments(1)
  } catch (error) {
    console.error('Failed to submit comment:', error)
    ElMessage.error(error.response?.data?.error?.message || '评论发表失败')
  } finally {
    submitting.value = false
  }
}

// 按置顶状态排序评论
const sortedComments = computed(() => {
  return [...comments.value].sort((a, b) => {
    // 置顶的评论排在前面
    if (a.isPinned && !b.isPinned) return -1
    if (!a.isPinned && b.isPinned) return 1
    // 其他按创建时间倒序
    return new Date(b.createdAt) - new Date(a.createdAt)
  })
})

// 检查是否可以操作评论
const canOperate = (comment) => {
  return isAdmin.value || comment.userId === currentUserId.value
}

// 检查是否可以删除评论
const canDelete = (comment) => {
  return isAdmin.value || comment.userId === currentUserId.value
}

// 删除评论
const handleDelete = async (comment) => {
  try {
    await ElMessageBox.confirm(
      '确认删除此评论？删除后无法恢复。',
      '删除评论',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteComment(comment.id)
    ElMessage.success('评论已删除')
    await loadComments(currentPage.value)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error(error.response?.data?.message || '删除评论失败')
    }
  }
}

// 置顶/取消置顶评论
const handleTogglePin = async (comment) => {
  try {
    const newPinStatus = !comment.isPinned
    await togglePinComment(comment.id, newPinStatus)
    ElMessage.success(newPinStatus ? '评论已置顶' : '已取消置顶')
    await loadComments(currentPage.value)
  } catch (error) {
    console.error('置顶操作失败:', error)
    ElMessage.error(error.response?.data?.message || '置顶操作失败')
  }
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

onMounted(() => {
  loadComments(1)
})
</script>

<style scoped>
.comment-section {
  margin-top: 3rem;
  padding-top: 2rem;
  border-top: 1px solid #E8EAED;
}

.comment-header {
  margin-bottom: 1.5rem;
}

.comment-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #333;
}

.comment-input {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: #F8F9FA;
  border-radius: 8px;
}

.input-actions {
  margin-top: 1rem;
  display: flex;
  justify-content: flex-end;
}

.login-prompt {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: #F8F9FA;
  border-radius: 8px;
  text-align: center;
}

.login-prompt p {
  margin: 0;
  color: #666;
}

.login-prompt a {
  color: #1A73E8;
  text-decoration: none;
  font-weight: 500;
}

.login-prompt a:hover {
  text-decoration: underline;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 2rem;
  color: #666;
}

.empty-comments {
  padding: 3rem 2rem;
  text-align: center;
  color: #999;
}

.empty-comments p {
  margin: 0;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.comment-item {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  background: #FFFFFF;
  border: 1px solid #E8EAED;
  border-radius: 8px;
  transition: all 0.3s;
}

.comment-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.comment-item.pinned {
  background: #FFF7E6;
  border-color: #FFD700;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.1);
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #E8F0FE;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.comment-avatar .el-icon {
  font-size: 20px;
  color: #1A73E8;
}

.comment-content {
  flex: 1;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  flex-wrap: wrap;
}

.comment-author {
  font-weight: 500;
  color: #333;
}

.pinned-tag {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.comment-time {
  font-size: 0.875rem;
  color: #999;
}

.comment-text {
  color: #666;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
  margin-bottom: 0.75rem;
}

.comment-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.comment-pagination {
  margin-top: 2rem;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .comment-item {
    flex-direction: column;
  }
  
  .comment-avatar {
    width: 32px;
    height: 32px;
  }
}
</style>
