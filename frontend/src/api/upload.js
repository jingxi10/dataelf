import axios from './axios'

/**
 * 上传图片
 * @param {File} file - 图片文件
 * @returns {Promise}
 */
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return axios.post('/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传视频
 * @param {File} file - 视频文件
 * @returns {Promise}
 */
export function uploadVideo(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return axios.post('/upload/video', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    // 增加超时时间，大文件上传需要更长时间
    timeout: 300000 // 5分钟
  })
}

/**
 * 上传文件（自动识别类型）
 * @param {File} file - 文件
 * @returns {Promise}
 */
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return axios.post('/upload/file', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除文件
 * @param {string} url - 文件URL
 * @returns {Promise}
 */
export function deleteFile(url) {
  return axios.delete('/upload', {
    params: { url }
  })
}
