package com.cnc.dms.service;

import com.cnc.dms.dto.OperationLogQueryRequest;
import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.entity.OperationLog;

/**
 * 操作ログ Service 接口
 */
public interface OperationLogService {

    /**
     * IDで操作ログを検索
     */
    OperationLog findById(Long id);

    /**
     * 条件付きページング検索
     */
    PageResponse<OperationLog> listLogs(OperationLogQueryRequest query);

    /**
     * 操作ログを作成
     */
    int createLog(OperationLog operationLog);
}
