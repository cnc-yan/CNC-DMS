package com.cnc.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * インポート結果レスポンス DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultResponse {

    /** インポートログID */
    private Long importLogId;

    /** ファイル名 */
    private String fileName;

    /** インポート種別 */
    private String importType;

    /** 総件数 */
    private Integer totalCount;

    /** 成功件数 */
    private Integer successCount;

    /** エラー件数 */
    private Integer errorCount;

    /** エラー詳細リスト */
    private List<String> errorMessages;

    /** 状態: 1=成功, 2=一部失敗, 3=失敗 */
    private Integer importStatus;
}
