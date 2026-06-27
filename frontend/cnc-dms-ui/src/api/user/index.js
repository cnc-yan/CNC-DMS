import apiClient from '../client'

/**
 * 获取分页用户列表
 * @param {Object} params - { pageNum, pageSize, userid }
 */
export function getUserList(params) {
  // 前端使用 username 变量，后端使用 userid 参数
  const backendParams = { ...params }
  if (backendParams.username) {
    backendParams.userid = backendParams.username
    delete backendParams.username
  }
  return apiClient.get('/user/list', { params: backendParams }).then(res => res.data)
}

/**
 * 获取单个用户
 * @param {string} userid
 */
export function getUser(userid) {
  return apiClient.get('/user', { params: { user_id: userid } }).then(res => res.data)
}

/**
 * 更新用户
 * @param {Object} data - { userid, userName, enabled, ... }
 */
export function updateUser(data) {
  return apiClient.put('/user/update', data).then(res => res.data)
}

/**
 * 删除单个用户
 */
export function deleteUser(userid) {
  return apiClient.delete('/user', { params: { user_id: userid } }).then(res => res.data)
}

/**
 * 批量删除
 */
export function deleteUsersBulk(userids) {
  return apiClient.delete('/user', { params: { user_id: userids } }).then(res => res.data)
}

/**
 * 创建用户
 * @param {Object} data - { userid, userName, password, enabled, ... }
 */
export function createUser(data) {
  return apiClient.post('/user', data).then(res => res.data)
}
