import axios from './axios'

/**
 * 用户注册
 * @param {Object} data - 注册数据
 * @param {string} data.email - 邮箱
 * @param {string} data.phone - 手机号
 * @param {string} data.password - 密码
 * @returns {Promise}
 */
export function register(data) {
  return axios.post('/auth/register', data)
}

/**
 * 用户登录
 * @param {Object} data - 登录数据
 * @param {string} data.email - 邮箱
 * @param {string} data.password - 密码
 * @returns {Promise}
 */
export function login(data) {
  return axios.post('/auth/login', data)
}

/**
 * 获取当前用户信息
 * @returns {Promise}
 */
export function getCurrentUser() {
  return axios.get('/auth/me')
}

/**
 * 用户登出
 * @returns {Promise}
 */
export function logout() {
  return axios.post('/auth/logout')
}
