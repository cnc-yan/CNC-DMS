import apiClient from '../client'

/**
 * 入居履歴一覧検索
 * @param {Object} params - { employeeId, roomId, isActive, pageNum, pageSize }
 */
export function getResidentRecordList(params) {
  return apiClient.get('/resident-records/list', { params }).then(res => res.data)
}

/**
 * 入居履歴検索（単一）
 * @param {number} id
 */
export function getResidentRecord(id) {
  return apiClient.get(`/resident-records/${id}`).then(res => res.data)
}

/**
 * 入居登録（チェックイン）
 * @param {Object} params - { employeeId, roomId, checkinDate, plannedCheckoutDate, notes, createBy }
 */
export function checkin(params) {
  return apiClient.post('/resident-records/checkin', null, { params }).then(res => res.data)
}

/**
 * 退寮処理（チェックアウト）
 * @param {number} id
 * @param {Object} params - { checkoutDate, updateBy }
 */
export function checkout(id, params) {
  return apiClient.put(`/resident-records/checkout/${id}`, null, { params }).then(res => res.data)
}

/**
 * 入居履歴削除
 * @param {number} id
 */
export function deleteResidentRecord(id) {
  return apiClient.delete(`/resident-records/${id}`).then(res => res.data)
}

/**
 * 部屋移動
 * @param {Object} params - { currentRecordId, newRoomId, transferDate, updateBy }
 */
export function transferRoom(params) {
  return apiClient.post('/resident-records/transfer', null, { params }).then(res => res.data)
}

/**
 * 利用料金確認一覧取得
 */
export function getUsageFeeList() {
  return apiClient.get('/resident-records/usage-fees').then(res => res.data)
}

/**
 * 長期利用情報取得
 * @param {number} employeeId
 */
export function getLongTermUsageInfo(employeeId) {
  return apiClient.get('/resident-records/long-term-usage', { params: { employeeId } }).then(res => res.data)
}
