import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as contentApi from '@/api/content'

export const useContentStore = defineStore('content', () => {
  const contents = ref([])
  const currentContent = ref(null)
  const loading = ref(false)
  const pagination = ref({
    page: 0,
    size: 20,
    total: 0,
    totalPages: 0
  })

  /**
   * 加载内容列表
   */
  async function loadContents(page = 0, size = 20) {
    try {
      loading.value = true
      const response = await contentApi.getPublicContents(page, size)
      
      // 处理分页响应
      if (response.content) {
        contents.value = response.content
        pagination.value = {
          page: response.number || page,
          size: response.size || size,
          total: response.totalElements || 0,
          totalPages: response.totalPages || 0
        }
      } else {
        contents.value = response.data || response || []
      }
    } catch (error) {
      console.error('加载内容列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载单个内容详情
   */
  async function loadContent(contentId) {
    try {
      loading.value = true
      const response = await contentApi.getContent(contentId)
      currentContent.value = response.data || response
      return currentContent.value
    } catch (error) {
      console.error('加载内容详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建内容
   */
  async function createContent(contentData) {
    try {
      loading.value = true
      const response = await contentApi.createContent(contentData)
      return response.data || response
    } catch (error) {
      console.error('创建内容失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新内容
   */
  async function updateContent(contentId, contentData) {
    try {
      loading.value = true
      const response = await contentApi.updateContent(contentId, contentData)
      
      // 更新本地缓存
      if (currentContent.value?.id === contentId) {
        currentContent.value = response.data || response
      }
      
      return response.data || response
    } catch (error) {
      console.error('更新内容失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 提交审核
   */
  async function submitForReview(contentId) {
    try {
      loading.value = true
      await contentApi.submitForReview(contentId)
      
      // 更新本地状态
      if (currentContent.value?.id === contentId) {
        currentContent.value.status = 'PENDING_REVIEW'
      }
    } catch (error) {
      console.error('提交审核失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 发布内容
   */
  async function publishContent(contentId) {
    try {
      loading.value = true
      await contentApi.publishContent(contentId)
      
      // 更新本地状态
      if (currentContent.value?.id === contentId) {
        currentContent.value.status = 'PUBLISHED'
      }
    } catch (error) {
      console.error('发布内容失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 清空当前内容
   */
  function clearCurrentContent() {
    currentContent.value = null
  }

  /**
   * 清空所有内容
   */
  function clearContents() {
    contents.value = []
    currentContent.value = null
    pagination.value = {
      page: 0,
      size: 20,
      total: 0,
      totalPages: 0
    }
  }

  return {
    contents,
    currentContent,
    loading,
    pagination,
    loadContents,
    loadContent,
    createContent,
    updateContent,
    submitForReview,
    publishContent,
    clearCurrentContent,
    clearContents
  }
})
