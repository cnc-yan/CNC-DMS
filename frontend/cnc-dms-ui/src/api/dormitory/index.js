import apiClient from '../client'

/**
 * 寮一覧検索
 * @param {Object} params - { dormName, region, status, pageNum, pageSize }
 */
export function getDormitoryList(params) {
  return apiClient.get('/dormitories/list', { params }).then(res => res.data)
}

/**
 * 寮検索（単一）
 * @param {number} id
 */
export function getDormitory(id) {
  return apiClient.get(`/dormitories/${id}`).then(res => res.data)
}

/**
 * 寮作成
 * @param {Object} data - { dormName, region, address, dormCondition, totalRooms, createBy }
 */
export function createDormitory(data) {
  return apiClient.post('/dormitories', data).then(res => res.data)
}

/**
 * 寮更新
 * @param {Object} data - { id, dormName, region, address, dormCondition, totalRooms, status, version, updateBy }
 */
export function updateDormitory(data) {
  return apiClient.put('/dormitories/update', data).then(res => res.data)
}

/**
 * 寮削除
 * @param {number} id
 */
export function deleteDormitory(id) {
  return apiClient.delete(`/dormitories/${id}`).then(res => res.data)
}
