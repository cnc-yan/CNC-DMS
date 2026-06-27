import apiClient from '../client'

/**
 * Excelファイルをアップロードしてインポート
 * @param {File} file アップロードするExcelファイル
 * @param {string} importType インポート種別 (DORMITORY/ROOM/EMPLOYEE/RESIDENT/BILLING)
 * @param {string} createBy 操作者ID
 * @returns {Promise<Object>} ImportResultResponse
 */
export function uploadExcel(file, importType, createBy) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('importType', importType)
  formData.append('createBy', createBy || 'system')
  return apiClient.post('/import/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }).then(res => res.data)
}

/**
 * インポートログ検索（単一）
 * @param {number} id
 */
export function getImportLog(id) {
  return apiClient.get(`/import/${id}`).then(res => res.data)
}

/**
 * インポートログ一覧検索
 * @param {Object} params - { importType, importStatus, pageNum, pageSize }
 */
export function getImportLogList(params) {
  return apiClient.get('/import/list', { params }).then(res => res.data)
}
