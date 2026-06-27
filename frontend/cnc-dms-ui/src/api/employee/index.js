import apiClient from '../client'

/**
 * 社員一覧検索
 * @param {Object} params - { empNo, empName, deptId, empStatus, pageNum, pageSize }
 */
export function getEmployeeList(params) {
  return apiClient.get('/employees/list', { params }).then(res => res.data)
}

/**
 * 社員検索（単一）
 * @param {number} id
 */
export function getEmployee(id) {
  return apiClient.get(`/employees/${id}`).then(res => res.data)
}

/**
 * 社員作成
 * @param {Object} data - { empNo, empName, gender, nationality, deptId, hireDate, phone, email, createBy }
 */
export function createEmployee(data) {
  return apiClient.post('/employees', data).then(res => res.data)
}

/**
 * 社員更新
 * @param {Object} data - { id, empName, gender, nationality, deptId, phone, email, empStatus, version, updateBy }
 */
export function updateEmployee(data) {
  return apiClient.put('/employees/update', data).then(res => res.data)
}

/**
 * 社員削除
 * @param {number} id
 */
export function deleteEmployee(id) {
  return apiClient.delete(`/employees/${id}`).then(res => res.data)
}

/**
 * 初回利用日取得
 * @param {number} id
 */
export function getFirstUseDate(id) {
  return apiClient.get(`/employees/${id}/first-use-date`).then(res => res.data)
}

/**
 * 初回利用日更新
 * @param {number} id
 * @param {string} firstUseDate
 * @param {string} updateBy
 */
export function updateFirstUseDate(id, firstUseDate, updateBy) {
  return apiClient.put(`/employees/${id}/first-use-date`, null, { params: { firstUseDate, updateBy } }).then(res => res.data)
}
