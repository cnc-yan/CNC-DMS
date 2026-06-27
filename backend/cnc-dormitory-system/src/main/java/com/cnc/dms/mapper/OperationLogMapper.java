package com.cnc.dms.mapper;

import com.cnc.dms.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作ログ Mapper 接口
 */
@Mapper
public interface OperationLogMapper {

    /**
     * IDで操作ログを検索
     */
    OperationLog findById(@Param("id") Long id);

    /**
     * 条件付き一覧検索（ページング対応）
     */
    List<OperationLog> findAll(@Param("operatorId") String operatorId,
                               @Param("operationType") String operationType,
                               @Param("targetType") String targetType,
                               @Param("resultStatus") Integer resultStatus,
                               @Param("offset") Integer offset,
                               @Param("limit") Integer limit);

    /**
     * 条件付き総件数取得
     */
    Long countByCondition(@Param("operatorId") String operatorId,
                          @Param("operationType") String operationType,
                          @Param("targetType") String targetType,
                          @Param("resultStatus") Integer resultStatus);

    /**
     * 新規作成
     */
    int insert(OperationLog operationLog);

    /**
     * IDで削除
     */
    int deleteById(@Param("id") Long id);
}
