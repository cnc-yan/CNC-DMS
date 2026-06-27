import apiClient from '../client'

/**
 * 操作ログ一覧検索
 * @param {Object} params - { operatorId, operationType, targetType, resultStatus, pageNum, pageSize }
 */
export function getOperationLogList(params) {
  return apiClient.get('/operation-logs/list', { params }).then(res => res.data)
}

/**
 * 操作ログ検索（単一）
 * @param {number} id
 */
export function getOperationLog(id) {
  return apiClient.get(`/operation-logs/${id}`).then(res => res.data)
}
