package com.cnc.dms.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作ログ Entity
 * 対応テーブル: tbl_operation_log
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog {

    private Long id;
    private String operatorId;
    private String operationType;
    private String targetType;
    private String targetId;
    private String description;
    private String clientIp;
    private String traceId;
    private String requestUrl;
    private String httpMethod;
    private String requestParams;
    private String beforeJson;
    private String afterJson;
    private Integer resultStatus;
    private Long version;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
