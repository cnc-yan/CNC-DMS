package com.cnc.dms.service;

import com.cnc.dms.dto.ImportLogQueryRequest;
import com.cnc.dms.dto.ImportResultResponse;
import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.entity.ImportLog;
import org.springframework.web.multipart.MultipartFile;

/**
 * Excelインポート Service インターフェース
 */
public interface ImportService {

    /**
     * Excelファイルをインポートする
     *
     * @param file       アップロードされたExcelファイル
     * @param importType インポート種別 (DORMITORY/ROOM/EMPLOYEE/RESIDENT/BILLING)
     * @param operatorId 操作者ID
     * @return インポート結果
     */
    ImportResultResponse importExcel(MultipartFile file, String importType, String operatorId);

    /**
     * インポート履歴をIDで検索
     */
    ImportLog findById(Long id);

    /**
     * インポート履歴一覧を取得
     */
    PageResponse<ImportLog> listImportLogs(ImportLogQueryRequest query);
}
