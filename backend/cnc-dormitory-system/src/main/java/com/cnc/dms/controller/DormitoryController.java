package com.cnc.dms.controller;

import com.cnc.dms.dto.DormitoryCreateRequest;
import com.cnc.dms.dto.DormitoryQueryRequest;
import com.cnc.dms.dto.DormitoryUpdateRequest;
import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.dto.Result;
import com.cnc.dms.entity.Dormitory;
import com.cnc.dms.service.DormitoryService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 寮管理 Controller
 */
@RestController
@RequestMapping("/api/dormitories")
public class DormitoryController {

    private final DormitoryService dormitoryService;

    public DormitoryController(DormitoryService dormitoryService) {
        this.dormitoryService = dormitoryService;
    }

    /**
     * IDで寮を検索
     */
    @GetMapping("/{id}")
    public Result<Dormitory> getById(@PathVariable Long id) {
        try {
            Dormitory dorm = dormitoryService.findById(id);
            if (dorm == null) {
                return Result.fail("寮が存在しません");
            }
            return Result.success(dorm);
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
    public Result<PageResponse<Dormitory>> listDormitories(DormitoryQueryRequest query) {
        try {
            PageResponse<Dormitory> page = dormitoryService.listDormitories(query);
            return Result.success(page);
        } catch (Exception e) {
            return Result.fail("検索失敗", e.getMessage());
        }
    }

    /**
     * 新規作成
     */
    @PostMapping("")
    public Result<String> create(@RequestBody DormitoryCreateRequest request) {
        try {
            int rows = dormitoryService.createDormitory(request);
            if (rows > 0) {
                return Result.success("成功", "寮作成成功");
            }
            return Result.fail("寮作成失敗");
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
    public Result<String> update(@RequestBody DormitoryUpdateRequest request) {
        try {
            int rows = dormitoryService.updateDormitory(request);
            if (rows > 0) {
                return Result.success("成功", "寮更新成功");
            }
            return Result.fail("寮更新失敗");
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
            int rows = dormitoryService.deleteDormitory(id);
            if (rows > 0) {
                return Result.success("成功", "寮削除成功");
            }
            return Result.fail("寮削除失敗");
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (RuntimeException e) {
            return Result.fail("削除失敗", e.getMessage());
        } catch (Exception e) {
            return Result.fail("削除失敗", e.getMessage());
        }
    }
}
