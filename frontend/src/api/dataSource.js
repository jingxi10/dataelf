import axios from './axios'

/**
 * 获取所有数据源
 * @returns {Promise}
 */
export function getAllDataSources() {
  return axios.get('/admin/data-sources')
}

/**
 * 获取启用的数据源
 * @returns {Promise}
 */
export function getEnabledDataSources() {
  return axios.get('/admin/data-sources/enabled')
}

/**
 * 获取指定数据源
 * @param {number} id - 数据源ID
 * @returns {Promise}
 */
export function getDataSource(id) {
  return axios.get(`/admin/data-sources/${id}`)
}

/**
 * 创建数据源
 * @param {Object} data - 数据源数据
 * @returns {Promise}
 */
export function createDataSource(data) {
  return axios.post('/admin/data-sources', data)
}

/**
 * 更新数据源
 * @param {number} id - 数据源ID
 * @param {Object} data - 数据源数据
 * @returns {Promise}
 */
export function updateDataSource(id, data) {
  return axios.put(`/admin/data-sources/${id}`, data)
}

/**
 * 删除数据源
 * @param {number} id - 数据源ID
 * @returns {Promise}
 */
export function deleteDataSource(id) {
  return axios.delete(`/admin/data-sources/${id}`)
}

/**
 * 启用/禁用数据源
 * @param {number} id - 数据源ID
 * @param {boolean} enabled - 是否启用
 * @returns {Promise}
 */
export function toggleDataSource(id, enabled) {
  return axios.patch(`/admin/data-sources/${id}/toggle`, null, {
    params: { enabled }
  })
}

/**
 * 手动触发抓取
 * @param {number} id - 数据源ID
 * @returns {Promise}
 */
export function triggerFetch(id) {
  return axios.post(`/admin/data-sources/${id}/fetch`)
}
