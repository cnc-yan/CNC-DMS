import apiClient from '../client'

/**
 * 部屋一覧検索
 * @param {Object} params - { dormitoryId, roomNumber, status, pageNum, pageSize }
 */
export function getRoomList(params) {
  return apiClient.get('/rooms/list', { params }).then(res => res.data)
}

/**
 * 部屋検索（単一）
 * @param {number} id
 */
export function getRoom(id) {
  return apiClient.get(`/rooms/${id}`).then(res => res.data)
}

/**
 * 寮に属する部屋一覧
 * @param {number} dormitoryId
 */
export function getRoomsByDormitory(dormitoryId) {
  return apiClient.get(`/rooms/by-dormitory/${dormitoryId}`).then(res => res.data)
}

/**
 * 部屋作成
 * @param {Object} data - { dormitoryId, roomNumber, capacity, dailyRate, createBy }
 */
export function createRoom(data) {
  return apiClient.post('/rooms', data).then(res => res.data)
}

/**
 * 部屋更新
 * @param {Object} data - { id, roomNumber, capacity, dailyRate, status, version, updateBy }
 */
export function updateRoom(data) {
  return apiClient.put('/rooms/update', data).then(res => res.data)
}

/**
 * 部屋削除
 * @param {number} id
 */
export function deleteRoom(id) {
  return apiClient.delete(`/rooms/${id}`).then(res => res.data)
}
