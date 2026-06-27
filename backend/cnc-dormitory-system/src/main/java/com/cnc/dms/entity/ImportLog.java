package com.cnc.dms.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * インポートログ Entity
 * 対応テーブル: tbl_import_log
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportLog {

    private Long id;
    private String fileName;
    private String importType;
    private Integer totalCount;
    private Integer successCount;
    private Integer errorCount;
    private String errorDetails;
    private Integer importStatus;
    private String operatorId;
    private Long version;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
