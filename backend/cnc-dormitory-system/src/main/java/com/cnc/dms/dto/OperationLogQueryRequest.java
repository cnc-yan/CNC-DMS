package com.cnc.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 操作ログ 検索リクエスト DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogQueryRequest {

    /** 操作者ID */
    private String operatorId;

    /** 操作タイプ */
    private String operationType;

    /** 対象種別 */
    private String targetType;

    /** 結果(0=失敗,1=成功) */
    private Integer resultStatus;

    /** ページ番号（1から） */
    @Builder.Default
    private Integer pageNum = 1;

    /** 1ページあたりの件数 */
    @Builder.Default
    private Integer pageSize = 10;
}
