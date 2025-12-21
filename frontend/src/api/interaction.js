import axios from './axios'

/**
 * 用户交互API
 */

// 执行交互操作（点赞、收藏、分享）
export const interact = (action, contentId) => {
  return axios.post(`/user/interact/${action}`, null, {
    params: { contentId }
  })
}

// 取消交互操作
export const removeInteraction = (action, contentId) => {
  return axios.delete(`/user/interact/${action}`, {
    params: { contentId }
  })
}

// 获取交互状态
export const getInteractionStatus = (contentId) => {
  return axios.get('/user/interact/status', {
    params: { contentId }
  })
}

// 获取收藏列表
export const getFavorites = (page = 0, size = 20) => {
  return axios.get('/user/favorites', {
    params: { page, size }
  })
}

// 生成分享链接
export const generateShareLink = (contentId) => {
  return axios.post('/user/share', null, {
    params: { contentId }
  })
}

// 发表评论
export const createComment = (contentId, commentText) => {
  return axios.post('/user/comments', {
    contentId,
    commentText
  })
}

// 获取评论列表
export const getComments = (contentId, page = 0, size = 20) => {
  return axios.get('/user/comments', {
    params: { contentId, page, size }
  })
}
