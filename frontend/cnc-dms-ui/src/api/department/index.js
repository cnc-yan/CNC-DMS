import apiClient from '../client'

/**
 * 部門一覧検索
 * @param {Object} params - { deptCode, deptName, region, status, pageNum, pageSize }
 */
export function getDepartmentList(params) {
  return apiClient.get('/departments/list', { params }).then(res => res.data)
}

/**
 * 部門検索（単一）
 * @param {number} id
 */
export function getDepartment(id) {
  return apiClient.get(`/departments/${id}`).then(res => res.data)
}

/**
 * 部門作成
 * @param {Object} data - { deptCode, deptName, region, parentId, sortOrder, createBy }
 */
export function createDepartment(data) {
  return apiClient.post('/departments', data).then(res => res.data)
}

/**
 * 部門更新
 * @param {Object} data - { id, deptName, region, parentId, sortOrder, status, version, updateBy }
 */
export function updateDepartment(data) {
  return apiClient.put('/departments/update', data).then(res => res.data)
}

/**
 * 部門削除
 * @param {number} id
 */
export function deleteDepartment(id) {
  return apiClient.delete(`/departments/${id}`).then(res => res.data)
}
