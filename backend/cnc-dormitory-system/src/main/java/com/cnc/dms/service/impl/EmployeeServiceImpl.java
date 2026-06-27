package com.cnc.dms.service.impl;

import com.cnc.dms.dto.EmployeeCreateRequest;
import com.cnc.dms.dto.EmployeeQueryRequest;
import com.cnc.dms.dto.EmployeeUpdateRequest;
import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.entity.Employee;
import com.cnc.dms.mapper.EmployeeMapper;
import com.cnc.dms.mapper.ResidentRecordMapper;
import com.cnc.dms.service.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 社員 Service 実装
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final ResidentRecordMapper residentRecordMapper;

    public EmployeeServiceImpl(EmployeeMapper employeeMapper,
                               ResidentRecordMapper residentRecordMapper) {
        this.employeeMapper = employeeMapper;
        this.residentRecordMapper = residentRecordMapper;
    }

    @Override
    public Employee findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("社員IDは必須です");
        }
        return employeeMapper.findById(id);
    }

    @Override
    public Employee findByEmpNo(String empNo) {
        if (empNo == null || empNo.isBlank()) {
            throw new IllegalArgumentException("工番は必須です");
        }
        return employeeMapper.findByEmpNo(empNo);
    }

    @Override
    public PageResponse<Employee> listEmployees(EmployeeQueryRequest query) {
        if (query == null) {
            query = EmployeeQueryRequest.builder().build();
        }
        if (query.getPageNum() == null || query.getPageNum() < 1) {
            query.setPageNum(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(10);
        }

        int offset = (query.getPageNum() - 1) * query.getPageSize();

        List<Employee> list = employeeMapper.findAll(
                query.getEmpNo(),
                query.getEmpName(),
                query.getDeptId(),
                query.getEmpStatus(),
                offset,
                query.getPageSize()
        );

        Long total = employeeMapper.countByCondition(
                query.getEmpNo(),
                query.getEmpName(),
                query.getDeptId(),
                query.getEmpStatus()
        );

        int totalPage = (int) ((total + query.getPageSize() - 1) / query.getPageSize());

        return PageResponse.<Employee>builder()
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .total(total)
                .totalPage(totalPage)
                .list(list)
                .build();
    }

    @Override
    public int createEmployee(EmployeeCreateRequest request) {
        if (request == null || request.getEmpNo() == null || request.getEmpNo().isBlank()) {
            throw new IllegalArgumentException("工番は必須です");
        }
        if (request.getEmpName() == null || request.getEmpName().isBlank()) {
            throw new IllegalArgumentException("社員名は必須です");
        }
        if (request.getGender() == null || request.getGender().isBlank()) {
            throw new IllegalArgumentException("性別は必須です");
        }
        if (request.getNationality() == null || request.getNationality().isBlank()) {
            throw new IllegalArgumentException("国籍は必須です");
        }

        // 工番の重複チェック
        Employee existing = employeeMapper.findByEmpNo(request.getEmpNo());
        if (existing != null) {
            throw new RuntimeException("工番が既に存在します: " + request.getEmpNo());
        }

        Employee employee = Employee.builder()
                .empNo(request.getEmpNo())
                .empName(request.getEmpName())
                .gender(request.getGender())
                .nationality(request.getNationality())
                .deptId(request.getDeptId())
                .hireDate(request.getHireDate())
                .phone(request.getPhone())
                .email(request.getEmail())
                .empStatus(1)
                .createBy(request.getCreateBy())
                .build();

        return employeeMapper.insert(employee);
    }

    @Override
    public int updateEmployee(EmployeeUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("社員IDは必須です");
        }
        if (request.getVersion() == null) {
            throw new IllegalArgumentException("バージョン情報は必須です");
        }

        Employee existing = employeeMapper.findById(request.getId());
        if (existing == null) {
            throw new RuntimeException("社員が存在しません");
        }

        Employee employee = Employee.builder()
                .id(request.getId())
                .empName(request.getEmpName())
                .gender(request.getGender())
                .nationality(request.getNationality())
                .deptId(request.getDeptId())
                .phone(request.getPhone())
                .email(request.getEmail())
                .hireDate(request.getHireDate())
                .empStatus(request.getEmpStatus())
                .version(request.getVersion())
                .updateBy(request.getUpdateBy())
                .build();

        int rows = employeeMapper.update(employee);
        if (rows == 0) {
            throw new RuntimeException("更新に失敗しました（楽観ロック競合）");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteEmployee(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("社員IDは必須です");
        }

        Employee existing = employeeMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("社員が存在しません");
        }

        // 社員の入居履歴の存在チェック
        Long recordCount = residentRecordMapper.countByCondition(existing.getId(), null, null);
        if (recordCount != null && recordCount > 0) {
            throw new RuntimeException(
                    "社員「" + existing.getEmpName() + "」には " + recordCount
                    + " 件の入居履歴が存在するため削除できません。先に入居履歴を削除してください。"
            );
        }

        return employeeMapper.deleteById(id);
    }

    @Override
    public LocalDate getFirstUseDate(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("社員IDは必須です");
        }
        Employee employee = employeeMapper.findById(id);
        if (employee == null) {
            throw new RuntimeException("社員が存在しません");
        }
        return employee.getFirstUseDate();
    }

    @Override
    public int updateFirstUseDate(Long id, LocalDate firstUseDate, String updateBy) {
        if (id == null) {
            throw new IllegalArgumentException("社員IDは必須です");
        }

        Employee employee = employeeMapper.findById(id);
        if (employee == null) {
            throw new RuntimeException("社員が存在しません");
        }

        employee.setFirstUseDate(firstUseDate);
        employee.setUpdateBy(updateBy);
        return employeeMapper.update(employee);
    }
}
