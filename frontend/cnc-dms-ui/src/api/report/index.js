import apiClient from '../client'

/**
 * ダッシュボード用サマリー情報を取得
 */
export function getSummaryReport() {
  return apiClient.get('/reports/summary').then(res => res.data)
}

/**
 * 寮別稼働率レポートを取得
 * @param {Object} params - { year, month, region }
 */
export function getOccupancyReport(params) {
  return apiClient.get('/reports/occupancy', { params }).then(res => res.data)
}


