import apiClient from './client'

/**
 * 登录
 * @param {string} userid
 * @param {string} password
 * @returns {Promise<{ success: boolean, message: string, token: string }>}
 */
export function login(userid, password) {
  return apiClient.post('/auth/login', { userid, password }).then(res => res.data)
}

/**
 * 获取当前登录用户信息
 * @returns {Promise<{ userid: string, userName: string }>}
 */
export function getCurrentUser() {
  return apiClient.get('/auth/me').then(res => res.data)
}
