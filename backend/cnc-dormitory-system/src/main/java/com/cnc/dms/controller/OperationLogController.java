package com.cnc.dms.controller;

import com.cnc.dms.dto.OperationLogQueryRequest;
import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.dto.Result;
import com.cnc.dms.entity.OperationLog;
import com.cnc.dms.service.OperationLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作ログ管理 Controller（参照専用）
 */
@RestController
@RequestMapping("/api/operation-logs")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    /**
     * IDで操作ログを検索
     */
    @GetMapping("/{id}")
    public Result<OperationLog> getById(@PathVariable Long id) {
        try {
            OperationLog log = operationLogService.findById(id);
            if (log == null) {
                return Result.fail("操作ログが存在しません");
            }
            return Result.success(log);
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
    public Result<PageResponse<OperationLog>> listLogs(OperationLogQueryRequest query) {
        try {
            PageResponse<OperationLog> page = operationLogService.listLogs(query);
            return Result.success(page);
        } catch (Exception e) {
            return Result.fail("検索失敗", e.getMessage());
        }
    }
}
