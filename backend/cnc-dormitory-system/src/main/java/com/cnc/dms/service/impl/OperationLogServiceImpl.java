package com.cnc.dms.service.impl;

import com.cnc.dms.dto.OperationLogQueryRequest;
import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.entity.OperationLog;
import com.cnc.dms.mapper.OperationLogMapper;
import com.cnc.dms.service.OperationLogService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作ログ Service 実装
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    public OperationLog findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ログIDは必須です");
        }
        return operationLogMapper.findById(id);
    }

    @Override
    public PageResponse<OperationLog> listLogs(OperationLogQueryRequest query) {
        if (query == null) {
            query = OperationLogQueryRequest.builder().build();
        }
        if (query.getPageNum() == null || query.getPageNum() < 1) {
            query.setPageNum(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(10);
        }

        int offset = (query.getPageNum() - 1) * query.getPageSize();

        List<OperationLog> list = operationLogMapper.findAll(
                query.getOperatorId(),
                query.getOperationType(),
                query.getTargetType(),
                query.getResultStatus(),
                offset,
                query.getPageSize()
        );

        Long total = operationLogMapper.countByCondition(
                query.getOperatorId(),
                query.getOperationType(),
                query.getTargetType(),
                query.getResultStatus()
        );

        int totalPage = (int) ((total + query.getPageSize() - 1) / query.getPageSize());

        return PageResponse.<OperationLog>builder()
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .total(total)
                .totalPage(totalPage)
                .list(list)
                .build();
    }

    @Override
    public int createLog(OperationLog operationLog) {
        if (operationLog == null || operationLog.getOperationType() == null) {
            throw new IllegalArgumentException("操作タイプは必須です");
        }
        return operationLogMapper.insert(operationLog);
    }
}
