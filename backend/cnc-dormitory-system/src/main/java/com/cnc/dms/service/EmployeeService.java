package com.cnc.dms.service;

import com.cnc.dms.dto.EmployeeCreateRequest;
import com.cnc.dms.dto.EmployeeQueryRequest;
import com.cnc.dms.dto.EmployeeUpdateRequest;
import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.entity.Employee;

import java.time.LocalDate;

/**
 * 社員 Service 接口
 */
public interface EmployeeService {

    /**
     * IDで社員を検索
     */
    Employee findById(Long id);

    /**
     * 工番で社員を検索
     */
    Employee findByEmpNo(String empNo);

    /**
     * 条件付きページング検索
     */
    PageResponse<Employee> listEmployees(EmployeeQueryRequest query);

    /**
     * 新規作成
     */
    int createEmployee(EmployeeCreateRequest request);

    /**
     * 更新
     */
    int updateEmployee(EmployeeUpdateRequest request);

    /**
     * IDで削除
     */
    int deleteEmployee(Long id);

    /**
     * 社員の初回利用日を取得
     */
    LocalDate getFirstUseDate(Long id);

    /**
     * 社員の初回利用日を更新
     */
    int updateFirstUseDate(Long id, LocalDate firstUseDate, String updateBy);
}
