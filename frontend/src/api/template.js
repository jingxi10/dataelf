import axios from './axios'

/**
 * 获取模板列表
 * @returns {Promise}
 */
export function getTemplateList() {
  return axios.get('/templates')
}

/**
 * 获取模板详情
 * @param {number} templateId - 模板ID
 * @returns {Promise}
 */
export function getTemplateDetail(templateId) {
  return axios.get(`/templates/${templateId}`)
}

/**
 * 创建模板
 * @param {Object} data - 模板数据
 * @param {string} data.name - 模板名称
 * @param {string} data.type - 模板类型
 * @param {string} data.schemaOrgType - Schema.org类型
 * @param {string} data.description - 模板描述
 * @param {string} data.schemaDefinition - Schema定义
 * @returns {Promise}
 */
export function createTemplate(data) {
  return axios.post('/templates', data)
}

/**
 * 更新模板
 * @param {number} templateId - 模板ID
 * @param {Object} data - 模板数据
 * @returns {Promise}
 */
export function updateTemplate(templateId, data) {
  return axios.put(`/templates/${templateId}`, data)
}

/**
 * 删除模板
 * @param {number} templateId - 模板ID
 * @returns {Promise}
 */
export function deleteTemplate(templateId) {
  return axios.delete(`/templates/${templateId}`)
}

/**
 * 导出模板
 * @param {number} templateId - 模板ID
 * @returns {Promise}
 */
export function exportTemplate(templateId) {
  return axios.get(`/templates/${templateId}/export`)
}

/**
 * 导入模板
 * @param {string} jsonContent - JSON格式的模板内容
 * @returns {Promise}
 */
export function importTemplate(jsonContent) {
  return axios.post('/templates/import', { jsonContent })
}

/**
 * 验证模板Schema
 * @param {Object} data - 验证数据
 * @param {string} data.schemaDefinition - Schema定义
 * @param {string} data.schemaOrgType - Schema.org类型
 * @returns {Promise}
 */
export function validateTemplateSchema(data) {
  return axios.post('/templates/validate', data)
}
