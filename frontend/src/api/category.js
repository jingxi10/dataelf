import axios from './axios'

/**
 * 获取所有分类（树形结构）
 * @returns {Promise}
 */
export function getAllCategories() {
  return axios.get('/public/categories')
}

/**
 * 获取分类树
 * @returns {Promise}
 */
export function getCategoryTree() {
  return axios.get('/public/categories/tree')
}

/**
 * 获取指定分类
 * @param {number} categoryId - 分类ID
 * @returns {Promise}
 */
export function getCategory(categoryId) {
  return axios.get(`/public/categories/${categoryId}`)
}

/**
 * 获取子分类
 * @param {number} categoryId - 分类ID
 * @returns {Promise}
 */
export function getChildCategories(categoryId) {
  return axios.get(`/public/categories/${categoryId}/children`)
}

/**
 * 创建分类（管理员）
 * @param {Object} data - 分类数据
 * @returns {Promise}
 */
export function createCategory(data) {
  return axios.post('/admin/categories', data)
}

/**
 * 更新分类（管理员）
 * @param {number} categoryId - 分类ID
 * @param {Object} data - 分类数据
 * @returns {Promise}
 */
export function updateCategory(categoryId, data) {
  return axios.put(`/admin/categories/${categoryId}`, data)
}

/**
 * 删除分类（管理员）
 * @param {number} categoryId - 分类ID
 * @returns {Promise}
 */
export function deleteCategory(categoryId) {
  return axios.delete(`/admin/categories/${categoryId}`)
}
