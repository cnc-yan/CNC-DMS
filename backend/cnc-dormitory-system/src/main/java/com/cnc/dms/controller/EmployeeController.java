package com.cnc.dms.controller;

import com.cnc.dms.dto.EmployeeCreateRequest;
import com.cnc.dms.dto.EmployeeQueryRequest;
import com.cnc.dms.dto.EmployeeUpdateRequest;
import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.dto.Result;
import com.cnc.dms.entity.Employee;
import com.cnc.dms.service.EmployeeService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 社員管理 Controller
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * IDで社員を検索
     */
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        try {
            Employee emp = employeeService.findById(id);
            if (emp == null) {
                return Result.fail("社員が存在しません");
            }
            return Result.success(emp);
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
    public Result<PageResponse<Employee>> listEmployees(EmployeeQueryRequest query) {
        try {
            PageResponse<Employee> page = employeeService.listEmployees(query);
            return Result.success(page);
        } catch (Exception e) {
            return Result.fail("検索失敗", e.getMessage());
        }
    }

    /**
     * 新規作成
     */
    @PostMapping("")
    public Result<String> create(@RequestBody EmployeeCreateRequest request) {
        try {
            int rows = employeeService.createEmployee(request);
            if (rows > 0) {
                return Result.success("成功", "社員作成成功");
            }
            return Result.fail("社員作成失敗");
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
    public Result<String> update(@RequestBody EmployeeUpdateRequest request) {
        try {
            int rows = employeeService.updateEmployee(request);
            if (rows > 0) {
                return Result.success("成功", "社員更新成功");
            }
            return Result.fail("社員更新失敗");
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
            int rows = employeeService.deleteEmployee(id);
            if (rows > 0) {
                return Result.success("成功", "社員削除成功");
            }
            return Result.fail("社員削除失敗");
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (RuntimeException e) {
            return Result.fail("削除失敗", e.getMessage());
        } catch (Exception e) {
            return Result.fail("削除失敗", e.getMessage());
        }
    }

    /**
     * 初回利用日を取得
     *
     * GET /api/employees/{id}/first-use-date
     */
    @GetMapping("/{id}/first-use-date")
    public Result<LocalDate> getFirstUseDate(@PathVariable Long id) {
        try {
            LocalDate date = employeeService.getFirstUseDate(id);
            return Result.success(date);
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (RuntimeException e) {
            return Result.fail("取得失敗", e.getMessage());
        } catch (Exception e) {
            return Result.fail("取得失敗", e.getMessage());
        }
    }

    /**
     * 初回利用日を更新
     *
     * PUT /api/employees/{id}/first-use-date?firstUseDate=2020-06-01&updateBy=admin
     */
    @PutMapping("/{id}/first-use-date")
    public Result<String> updateFirstUseDate(
            @PathVariable Long id,
            @RequestParam String firstUseDate,
            @RequestParam(required = false) String updateBy) {
        try {
            LocalDate date = LocalDate.parse(firstUseDate);
            int rows = employeeService.updateFirstUseDate(id, date, updateBy);
            if (rows > 0) {
                return Result.success("成功", "初回利用日更新成功");
            }
            return Result.fail("初回利用日更新失敗");
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (RuntimeException e) {
            return Result.fail("更新失敗", e.getMessage());
        } catch (Exception e) {
            return Result.fail("更新失敗", e.getMessage());
        }
    }
}
