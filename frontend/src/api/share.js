import axios from './axios'

/**
 * 获取分享链接
 * @param {number} contentId - 内容ID
 * @returns {Promise}
 */
export function getShareLinks(contentId) {
  return axios.get(`/public/share/${contentId}`)
}

/**
 * 记录分享行为
 * @param {number} contentId - 内容ID
 * @param {string} platform - 分享平台
 * @returns {Promise}
 */
export function recordShareAction(contentId, platform) {
  return axios.post(`/public/share/${contentId}/record`, null, {
    params: { platform }
  })
}
