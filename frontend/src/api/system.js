import axios from './axios'

/**
 * 获取公开配置
 * @returns {Promise}
 */
export function getPublicConfig() {
  return axios.get('/public/config')
}

/**
 * 获取所有配置（管理员）
 * @returns {Promise}
 */
export function getAllConfig() {
  return axios.get('/admin/config')
}

/**
 * 更新配置（管理员）
 * @param {Object} data - 配置数据
 * @returns {Promise}
 */
export function updateConfig(data) {
  return axios.put('/admin/config', data)
}

/**
 * 上传Logo（管理员）
 * @param {File} file - Logo文件
 * @returns {Promise}
 */
export function uploadLogo(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return axios.post('/admin/config/logo', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
