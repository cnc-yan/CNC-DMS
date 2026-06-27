package com.cnc.dms.controller;

import com.cnc.dms.dto.ImportLogQueryRequest;
import com.cnc.dms.dto.ImportResultResponse;
import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.dto.Result;
import com.cnc.dms.entity.ImportLog;
import com.cnc.dms.service.ImportService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Excelインポート Controller
 * 設計書「10. Excelインポート」対応
 */
@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    /**
     * Excelファイルをアップロードしてインポートする
     *
     * POST /api/import/upload?importType=DORMITORY&createBy=admin
     *
     * @param file       アップロードするExcelファイル
     * @param importType インポート種別 (DORMITORY/ROOM/EMPLOYEE/RESIDENT/BILLING)
     * @param createBy   操作者ID
     * @return インポート結果
     */
    @PostMapping("/upload")
    public Result<ImportResultResponse> uploadExcel(@RequestParam("file") MultipartFile file,
                                                     @RequestParam("importType") String importType,
                                                     @RequestParam(value = "createBy", defaultValue = "system") String createBy) {
        try {
            // ファイル検証
            if (file.isEmpty()) {
                return Result.fail("ファイルが空です");
            }
            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
                return Result.fail("Excelファイル（.xlsx/.xls）のみアップロード可能です");
            }

            ImportResultResponse result = importService.importExcel(file, importType, createBy);
            return Result.success(result, "インポート処理完了");
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (Exception e) {
            return Result.fail("インポート失敗", e.getMessage());
        }
    }

    /**
     * インポート履歴をIDで検索
     */
    @GetMapping("/{id}")
    public Result<ImportLog> getById(@PathVariable Long id) {
        try {
            ImportLog log = importService.findById(id);
            if (log == null) {
                return Result.fail("インポートログが存在しません");
            }
            return Result.success(log);
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (Exception e) {
            return Result.fail("検索失敗", e.getMessage());
        }
    }

    /**
     * インポート履歴一覧を取得
     */
    @GetMapping("/list")
    public Result<PageResponse<ImportLog>> listImportLogs(ImportLogQueryRequest query) {
        try {
            PageResponse<ImportLog> page = importService.listImportLogs(query);
            return Result.success(page);
        } catch (Exception e) {
            return Result.fail("検索失敗", e.getMessage());
        }
    }
}
