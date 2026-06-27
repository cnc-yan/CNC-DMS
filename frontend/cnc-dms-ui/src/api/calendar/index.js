import apiClient from '../client'

/**
 * カレンダー occupancy データを取得
 * @param {Object} params - { dormitoryId, year, month }
 */
export function getOccupancyCalendar(params) {
  return apiClient.get('/calendar/occupancy', { params }).then(res => res.data)
}
