import axios from './axios'

/**
 * 获取用户列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.status - 用户状态筛选
 * @param {string} params.search - 搜索关键词
 * @returns {Promise}
 */
export function getUserList(params) {
  return axios.get('/admin/users', { params })
}

/**
 * 获取待审核用户列表
 * @returns {Promise}
 */
export function getPendingUsers() {
  return axios.get('/admin/users/pending')
}

/**
 * 获取即将到期的用户列表
 * @returns {Promise}
 */
export function getExpiringUsers() {
  return axios.get('/admin/users/expiring')
}

/**
 * 批准用户账号
 * @param {number} userId - 用户ID
 * @param {number} validDays - 有效天数
 * @returns {Promise}
 */
export function approveUser(userId, validDays) {
  return axios.post('/admin/users/approve', { 
    userId, 
    validDays 
  })
}

/**
 * 延长账号时长
 * @param {number} userId - 用户ID
 * @param {number} days - 延长天数
 * @returns {Promise}
 */
export function extendAccount(userId, days) {
  return axios.post('/admin/users/extend', { 
    userId, 
    days 
  })
}

/**
 * 获取用户详情
 * @param {number} userId - 用户ID
 * @returns {Promise}
 */
export function getUserDetail(userId) {
  return axios.get(`/admin/users/${userId}`)
}

/**
 * 获取审核队列
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @returns {Promise}
 */
export function getReviewQueue(params) {
  return axios.get('/admin/content/review-queue', { params })
}

/**
 * 批准内容
 * @param {number} contentId - 内容ID
 * @returns {Promise}
 */
export function approveContent(contentId) {
  return axios.post(`/admin/content/${contentId}/approve`)
}

/**
 * 拒绝内容
 * @param {number} contentId - 内容ID
 * @param {string} reason - 拒绝原因
 * @returns {Promise}
 */
export function rejectContent(contentId, reason) {
  return axios.post(`/admin/content/${contentId}/reject`, { reason })
}

/**
 * 直接发布内容
 * @param {number} contentId - 内容ID
 * @returns {Promise}
 */
export function directPublish(contentId) {
  return axios.post(`/admin/content/${contentId}/publish`)
}

/**
 * 检查内容完整性
 * @param {number} contentId - 内容ID
 * @returns {Promise}
 */
export function checkIntegrity(contentId) {
  return axios.get(`/admin/content/${contentId}/integrity`)
}
