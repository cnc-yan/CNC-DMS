package com.cnc.dms.controller;

import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.dto.Result;
import com.cnc.dms.entity.ResidentRecord;
import com.cnc.dms.service.ResidentRecordService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 入居履歴管理 Controller
 */
@RestController
@RequestMapping("/api/resident-records")
public class ResidentRecordController {

    private final ResidentRecordService residentRecordService;

    public ResidentRecordController(ResidentRecordService residentRecordService) {
        this.residentRecordService = residentRecordService;
    }

    /**
     * IDで入居履歴を検索
     */
    @GetMapping("/{id}")
    public Result<ResidentRecord> getById(@PathVariable Long id) {
        try {
            ResidentRecord record = residentRecordService.findById(id);
            if (record == null) {
                return Result.fail("入居履歴が存在しません");
            }
            return Result.success(record);
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
    public Result<PageResponse<ResidentRecord>> listRecords(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Integer isActive,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            PageResponse<ResidentRecord> page = residentRecordService.listRecords(
                    employeeId, roomId, isActive, pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            return Result.fail("検索失敗", e.getMessage());
        }
    }

    /**
     * 入居登録
     */
    @PostMapping("/checkin")
    public Result<ResidentRecord> checkin(
            @RequestParam Long employeeId,
            @RequestParam Long roomId,
            @RequestParam String checkinDate,
            @RequestParam(required = false) String plannedCheckoutDate,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String createBy) {
        try {
            LocalDate date = LocalDate.parse(checkinDate);
            LocalDate plannedDate = plannedCheckoutDate != null ? LocalDate.parse(plannedCheckoutDate) : null;
            ResidentRecord record = residentRecordService.checkin(
                    employeeId, roomId, date, plannedDate, notes, createBy);
            return Result.success(record, "入居登録成功");
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (RuntimeException e) {
            return Result.fail("入居登録失敗", e.getMessage());
        } catch (Exception e) {
            return Result.fail("入居登録失敗", e.getMessage());
        }
    }

    /**
     * 退寮処理
     */
    @PutMapping("/checkout/{id}")
    public Result<ResidentRecord> checkout(
            @PathVariable Long id,
            @RequestParam(required = false) String checkoutDate,
            @RequestParam(required = false) String updateBy) {
        try {
            LocalDate date = checkoutDate != null ? LocalDate.parse(checkoutDate) : null;
            ResidentRecord record = residentRecordService.checkout(id, date, updateBy);
            return Result.success(record, "退寮処理成功");
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (RuntimeException e) {
            return Result.fail("退寮処理失敗", e.getMessage());
        } catch (Exception e) {
            return Result.fail("退寮処理失敗", e.getMessage());
        }
    }

    /**
     * 削除
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            int rows = residentRecordService.deleteRecord(id);
            if (rows > 0) {
                return Result.success("成功", "入居履歴削除成功");
            }
            return Result.fail("入居履歴削除失敗");
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (RuntimeException e) {
            return Result.fail("削除失敗", e.getMessage());
        } catch (Exception e) {
            return Result.fail("削除失敗", e.getMessage());
        }
    }

    /**
     * 部屋移動
     *
     * POST /api/resident-records/transfer?currentRecordId=1&newRoomId=2&transferDate=2026-06-15&updateBy=admin
     */
    @PostMapping("/transfer")
    public Result<ResidentRecord> transferRoom(
            @RequestParam Long currentRecordId,
            @RequestParam Long newRoomId,
            @RequestParam(required = false) String transferDate,
            @RequestParam(required = false) String updateBy) {
        try {
            LocalDate date = transferDate != null ? LocalDate.parse(transferDate) : null;
            ResidentRecord record = residentRecordService.transferRoom(
                    currentRecordId, newRoomId, date, updateBy);
            return Result.success(record, "部屋移動成功");
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (RuntimeException e) {
            return Result.fail("部屋移動失敗", e.getMessage());
        } catch (Exception e) {
            return Result.fail("部屋移動失敗", e.getMessage());
        }
    }

    /**
     * 利用料金確認一覧を取得
     *
     * GET /api/resident-records/usage-fees
     */
    @GetMapping("/usage-fees")
    public Result<List<ResidentRecord>> getUsageFeeList() {
        try {
            List<ResidentRecord> list = residentRecordService.getUsageFeeList();
            return Result.success(list);
        } catch (Exception e) {
            return Result.fail("取得失敗", e.getMessage());
        }
    }

    /**
     * 社員の長期利用情報を取得
     *
     * GET /api/resident-records/long-term-usage?employeeId=1
     */
    @GetMapping("/long-term-usage")
    public Result<Map<String, Object>> getLongTermUsageInfo(@RequestParam Long employeeId) {
        try {
            Map<String, Object> info = residentRecordService.getLongTermUsageInfo(employeeId);
            return Result.success(info);
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (RuntimeException e) {
            return Result.fail("取得失敗", e.getMessage());
        } catch (Exception e) {
            return Result.fail("取得失敗", e.getMessage());
        }
    }
}
