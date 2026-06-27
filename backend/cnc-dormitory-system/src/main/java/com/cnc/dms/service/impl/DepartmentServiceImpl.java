package com.cnc.dms.service.impl;

import com.cnc.dms.dto.DepartmentCreateRequest;
import com.cnc.dms.dto.DepartmentQueryRequest;
import com.cnc.dms.dto.DepartmentUpdateRequest;
import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.entity.Department;
import com.cnc.dms.mapper.DepartmentMapper;
import com.cnc.dms.mapper.EmployeeMapper;
import com.cnc.dms.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 所属 Service 実装
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final EmployeeMapper employeeMapper;

    public DepartmentServiceImpl(DepartmentMapper departmentMapper,
                                 EmployeeMapper employeeMapper) {
        this.departmentMapper = departmentMapper;
        this.employeeMapper = employeeMapper;
    }

    @Override
    public Department findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("部門IDは必須です");
        }
        return departmentMapper.findById(id);
    }

    @Override
    public Department findByDeptCode(String deptCode) {
        if (deptCode == null || deptCode.isBlank()) {
            throw new IllegalArgumentException("部門コードは必須です");
        }
        return departmentMapper.findByDeptCode(deptCode);
    }

    @Override
    public PageResponse<Department> listDepartments(DepartmentQueryRequest query) {
        if (query == null) {
            query = DepartmentQueryRequest.builder().build();
        }
        if (query.getPageNum() == null || query.getPageNum() < 1) {
            query.setPageNum(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(10);
        }

        int offset = (query.getPageNum() - 1) * query.getPageSize();

        List<Department> list = departmentMapper.findAll(
                query.getDeptCode(),
                query.getDeptName(),
                query.getRegion(),
                query.getStatus(),
                offset,
                query.getPageSize()
        );

        Long total = departmentMapper.countByCondition(
                query.getDeptCode(),
                query.getDeptName(),
                query.getRegion(),
                query.getStatus()
        );

        int totalPage = (int) ((total + query.getPageSize() - 1) / query.getPageSize());

        return PageResponse.<Department>builder()
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .total(total)
                .totalPage(totalPage)
                .list(list)
                .build();
    }

    @Override
    public int createDepartment(DepartmentCreateRequest request) {
        if (request == null || request.getDeptCode() == null || request.getDeptCode().isBlank()) {
            throw new IllegalArgumentException("部門コードは必須です");
        }
        if (request.getDeptName() == null || request.getDeptName().isBlank()) {
            throw new IllegalArgumentException("部門名称は必須です");
        }

        // 部門コードの重複チェック
        Department existing = departmentMapper.findByDeptCode(request.getDeptCode());
        if (existing != null) {
            throw new RuntimeException("部門コードが既に存在します: " + request.getDeptCode());
        }

        Department department = Department.builder()
                .deptCode(request.getDeptCode())
                .deptName(request.getDeptName())
                .region(request.getRegion())
                .parentId(request.getParentId())
                .sortOrder(request.getSortOrder())
                .status(1)
                .createBy(request.getCreateBy())
                .build();

        return departmentMapper.insert(department);
    }

    @Override
    public int updateDepartment(DepartmentUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("部門IDは必須です");
        }
        if (request.getVersion() == null) {
            throw new IllegalArgumentException("バージョン情報は必須です");
        }

        Department existing = departmentMapper.findById(request.getId());
        if (existing == null) {
            throw new RuntimeException("部門が存在しません");
        }

        Department department = Department.builder()
                .id(request.getId())
                .deptName(request.getDeptName())
                .region(request.getRegion())
                .parentId(request.getParentId())
                .sortOrder(request.getSortOrder())
                .status(request.getStatus())
                .version(request.getVersion())
                .updateBy(request.getUpdateBy())
                .build();

        int rows = departmentMapper.update(department);
        if (rows == 0) {
            throw new RuntimeException("更新に失敗しました（楽観ロック競合）");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDepartment(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("部門IDは必須です");
        }

        Department existing = departmentMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("部門が存在しません");
        }

        // 部門に所属する社員の存在チェック
        Long empCount = employeeMapper.countByCondition(null, null, existing.getId(), null);
        if (empCount != null && empCount > 0) {
            throw new RuntimeException(
                    "部門「" + existing.getDeptName() + "」には " + empCount
                    + " 件の社員が所属しているため削除できません。先に社員を削除または異動してください。"
            );
        }

        return departmentMapper.deleteById(id);
    }
}
