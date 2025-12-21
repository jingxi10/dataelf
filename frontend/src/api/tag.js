import axios from './axios'

/**
 * 获取所有标签
 * @returns {Promise}
 */
export function getAllTags() {
  return axios.get('/tags')
}

/**
 * 获取标签列表（别名）
 * @returns {Promise}
 */
export function getTagList() {
  return axios.get('/tags')
}

/**
 * 获取热门标签
 * @returns {Promise}
 */
export function getPopularTags() {
  return axios.get('/tags/popular')
}

/**
 * 获取指定标签
 * @param {number} tagId - 标签ID
 * @returns {Promise}
 */
export function getTag(tagId) {
  return axios.get(`/tags/${tagId}`)
}

/**
 * 搜索标签
 * @param {string} keyword - 搜索关键词
 * @returns {Promise}
 */
export function searchTags(keyword) {
  return axios.get('/tags/search', { params: { keyword } })
}

/**
 * 根据标签搜索内容
 * @param {string} tagName - 标签名称
 * @param {number} page - 页码
 * @param {number} size - 每页数量
 * @returns {Promise}
 */
export function searchContentsByTag(tagName, page = 0, size = 20) {
  return axios.get(`/tags/${tagName}/contents`, { params: { page, size } })
}

/**
 * 创建标签（管理员）
 * @param {Object} data - 标签数据
 * @returns {Promise}
 */
export function createTag(data) {
  return axios.post('/tags', data)
}

/**
 * 删除标签（管理员）
 * @param {number} tagId - 标签ID
 * @returns {Promise}
 */
export function deleteTag(tagId) {
  return axios.delete(`/tags/${tagId}`)
}
