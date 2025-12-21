import axios from './axios'

/**
 * 获取用户所有通知
 */
export const getUserNotifications = () => {
  return axios.get('/notifications')
}

/**
 * 获取未读通知
 */
export const getUnreadNotifications = () => {
  return axios.get('/notifications/unread')
}

/**
 * 获取未读通知数量
 */
export const getUnreadCount = () => {
  return axios.get('/notifications/unread/count')
}

/**
 * 标记通知为已读
 */
export const markAsRead = (notificationId) => {
  return axios.put(`/notifications/${notificationId}/read`)
}

/**
 * 标记所有通知为已读
 */
export const markAllAsRead = () => {
  return axios.put('/notifications/read-all')
}
