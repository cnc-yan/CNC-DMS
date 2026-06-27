package com.cnc.dms.controller;

import com.cnc.dms.dto.DepartmentCreateRequest;
import com.cnc.dms.dto.DepartmentQueryRequest;
import com.cnc.dms.dto.DepartmentUpdateRequest;
import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.dto.Result;
import com.cnc.dms.entity.Department;
import com.cnc.dms.service.DepartmentService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 所属管理 Controller
 */
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * IDで所属を検索
     */
    @GetMapping("/{id}")
    public Result<Department> getById(@PathVariable Long id) {
        try {
            Department dept = departmentService.findById(id);
            if (dept == null) {
                return Result.fail("部門が存在しません");
            }
            return Result.success(dept);
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (Exception e) {
            return Result.fail("検索失敗", e.getMessage());
        }
    }

    /**
     * 条件付きページング検索
     */
    @GetMapping("/list")
    public Result<PageResponse<Department>> listDepartments(DepartmentQueryRequest query) {
        try {
            PageResponse<Department> page = departmentService.listDepartments(query);
            return Result.success(page);
        } catch (Exception e) {
            return Result.fail("検索失敗", e.getMessage());
        }
    }

    /**
     * 新規作成
     */
    @PostMapping("")
    public Result<String> create(@RequestBody DepartmentCreateRequest request) {
        try {
            int rows = departmentService.createDepartment(request);
            if (rows > 0) {
                return Result.success("成功", "部門作成成功");
            }
            return Result.fail("部門作成失敗");
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (RuntimeException e) {
            return Result.fail("作成失敗", e.getMessage());
        } catch (Exception e) {
            return Result.fail("作成失敗", e.getMessage());
        }
    }

    /**
     * 更新
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody DepartmentUpdateRequest request) {
        try {
            int rows = departmentService.updateDepartment(request);
            if (rows > 0) {
                return Result.success("成功", "部門更新成功");
            }
            return Result.fail("部門更新失敗");
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (RuntimeException e) {
            return Result.fail("更新失敗", e.getMessage());
        } catch (Exception e) {
            return Result.fail("更新失敗", e.getMessage());
        }
    }

    /**
     * 削除
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            int rows = departmentService.deleteDepartment(id);
            if (rows > 0) {
                return Result.success("成功", "部門削除成功");
            }
            return Result.fail("部門削除失敗");
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (RuntimeException e) {
            return Result.fail("削除失敗", e.getMessage());
        } catch (Exception e) {
            return Result.fail("削除失敗", e.getMessage());
        }
    }
}
