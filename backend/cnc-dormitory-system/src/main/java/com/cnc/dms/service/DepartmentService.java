package com.cnc.dms.service;

import com.cnc.dms.dto.DepartmentCreateRequest;
import com.cnc.dms.dto.DepartmentQueryRequest;
import com.cnc.dms.dto.DepartmentUpdateRequest;
import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.entity.Department;

/**
 * 所属 Service 接口
 */
public interface DepartmentService {

    /**
     * IDで所属を検索
     */
    Department findById(Long id);

    /**
     * 所属コードで検索
     */
    Department findByDeptCode(String deptCode);

    /**
     * 条件付きページング検索
     */
    PageResponse<Department> listDepartments(DepartmentQueryRequest query);

    /**
     * 新規作成
     */
    int createDepartment(DepartmentCreateRequest request);

    /**
     * 更新
     */
    int updateDepartment(DepartmentUpdateRequest request);

    /**
     * IDで削除
     */
    int deleteDepartment(Long id);
}
