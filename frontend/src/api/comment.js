import axios from './axios'

/**
 * 创建评论
 * @param {Object} data - 评论数据
 * @returns {Promise}
 */
export function createComment(data) {
  return axios.post('/user/comments', data)
}

/**
 * 获取内容的评论列表
 * @param {number} contentId - 内容ID
 * @param {Object} params - 分页参数
 * @returns {Promise}
 */
export function getCommentsByContent(contentId, params) {
  return axios.get(`/public/comments/content/${contentId}`, { params })
}

/**
 * 获取评论数量
 * @param {number} contentId - 内容ID
 * @returns {Promise}
 */
export function getCommentCount(contentId) {
  return axios.get(`/public/comments/content/${contentId}/count`)
}

/**
 * 删除评论
 * @param {number} commentId - 评论ID
 * @returns {Promise}
 */
export function deleteComment(commentId) {
  return axios.delete(`/user/comments/${commentId}`)
}

/**
 * 置顶/取消置顶评论（管理员）
 * @param {number} commentId - 评论ID
 * @param {boolean} pin - 是否置顶
 * @returns {Promise}
 */
export function togglePinComment(commentId, pin) {
  return axios.post(`/admin/comments/${commentId}/pin`, { pin })
}
