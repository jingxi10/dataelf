import axios from './axios'

/**
 * 创建内容
 * @param {Object} data - 内容数据
 * @returns {Promise}
 */
export function createContent(data) {
  return axios.post('/content', data)
}

/**
 * 更新内容
 * @param {number} contentId - 内容ID
 * @param {Object} data - 内容数据
 * @returns {Promise}
 */
export function updateContent(contentId, data) {
  return axios.put(`/content/${contentId}`, data)
}

/**
 * 获取内容详情
 * @param {number} contentId - 内容ID
 * @returns {Promise}
 */
export function getContent(contentId) {
  return axios.get(`/content/${contentId}`)
}

/**
 * 提交审核
 * @param {number} contentId - 内容ID
 * @returns {Promise}
 */
export function submitForReview(contentId) {
  return axios.post(`/content/${contentId}/submit`)
}

/**
 * 发布内容
 * @param {number} contentId - 内容ID
 * @returns {Promise}
 */
export function publishContent(contentId) {
  return axios.post(`/content/${contentId}/publish`)
}

/**
 * 获取我的内容列表
 * @param {Object} params - 分页参数
 * @returns {Promise}
 */
export function getMyContents(params) {
  return axios.get('/content/my', { params })
}

/**
 * 获取我的草稿列表
 * @param {Object} params - 分页参数
 * @returns {Promise}
 */
export function getMyDrafts(params) {
  return axios.get('/content/my/drafts', { params })
}

/**
 * 获取我的已发布内容列表
 * @param {Object} params - 分页参数
 * @returns {Promise}
 */
export function getMyPublishedContents(params) {
  return axios.get('/content/my/published', { params })
}

/**
 * 获取我的待审核内容列表
 * @param {Object} params - 分页参数
 * @returns {Promise}
 */
export function getMyPendingContents(params) {
  return axios.get('/content/my/pending', { params })
}

/**
 * 删除内容
 * @param {number} contentId - 内容ID
 * @returns {Promise}
 */
export function deleteContent(contentId) {
  return axios.delete(`/content/${contentId}`)
}

/**
 * 获取多格式输出
 * @param {number} contentId - 内容ID
 * @returns {Promise}
 */
export function getContentOutput(contentId) {
  return axios.get(`/content/${contentId}/output`)
}

/**
 * 更新版权信息
 * @param {number} contentId - 内容ID
 * @param {Object} data - 版权信息
 * @returns {Promise}
 */
export function updateCopyrightInfo(contentId, data) {
  return axios.put(`/content/${contentId}/copyright`, data)
}

/**
 * 重新排序字段
 * @param {number} contentId - 内容ID
 * @param {Array} fieldOrder - 字段顺序
 * @returns {Promise}
 */
export function reorderFields(contentId, fieldOrder) {
  return axios.put(`/content/${contentId}/reorder`, { fieldOrder })
}

/**
 * 获取已发布的内容列表（公开接口）
 * @param {number} page - 页码
 * @param {number} size - 每页数量
 * @returns {Promise}
 */
export function getPublicContents(page = 0, size = 20) {
  return axios.get('/public/contents', { 
    params: { page, size } 
  })
}

/**
 * 获取已发布的内容列表（公开接口）- 带参数版本
 * @param {Object} params - 分页参数
 * @returns {Promise}
 */
export function getPublishedContents(params) {
  return axios.get('/public/contents', { params })
}

/**
 * 根据分类获取内容
 * @param {number} categoryId - 分类ID
 * @param {Object} params - 分页参数
 * @returns {Promise}
 */
export function getContentsByCategory(categoryId, params) {
  return axios.get(`/public/contents/category/${categoryId}`, { params })
}

/**
 * 搜索内容
 * @param {string} keyword - 搜索关键词
 * @param {Object} params - 分页参数
 * @returns {Promise}
 */
export function searchContents(keyword, params = {}) {
  return axios.get('/public/contents/search', { 
    params: { keyword, ...params } 
  })
}

/**
 * 导出内容为JSON-LD格式
 * @param {number} contentId - 内容ID
 * @returns {Promise}
 */
export function exportAsJsonLd(contentId) {
  return axios.get(`/export/${contentId}/jsonld`, {
    responseType: 'blob'
  })
}

/**
 * 导出内容为HTML格式
 * @param {number} contentId - 内容ID
 * @returns {Promise}
 */
export function exportAsHtml(contentId) {
  return axios.get(`/export/${contentId}/html`, {
    responseType: 'blob'
  })
}

/**
 * 导出内容为Markdown格式
 * @param {number} contentId - 内容ID
 * @returns {Promise}
 */
export function exportAsMarkdown(contentId) {
  return axios.get(`/export/${contentId}/markdown`, {
    responseType: 'blob'
  })
}

/**
 * 导出内容为CSV格式
 * @param {number} contentId - 内容ID
 * @returns {Promise}
 */
export function exportAsCsv(contentId) {
  return axios.get(`/export/${contentId}/csv`, {
    responseType: 'blob'
  })
}
