package com.cnc.dms.mapper;

import com.cnc.dms.entity.Dormitory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 寮 Mapper 接口
 */
@Mapper
public interface DormitoryMapper {

    /**
     * IDで寮を検索
     */
    Dormitory findById(@Param("id") Long id);

    /**
     * 条件付き一覧検索（ページング対応）
     */
    List<Dormitory> findAll(@Param("dormName") String dormName,
                            @Param("region") String region,
                            @Param("status") Integer status,
                            @Param("offset") Integer offset,
                            @Param("limit") Integer limit);

    /**
     * 条件付き総件数取得
     */
    Long countByCondition(@Param("dormName") String dormName,
                          @Param("region") String region,
                          @Param("status") Integer status);

    /**
     * 新規作成
     */
    int insert(Dormitory dormitory);

    /**
     * 更新
     */
    int update(Dormitory dormitory);

    /**
     * IDで削除
     */
    int deleteById(@Param("id") Long id);

    /**
     * 全寮数を取得
     */
    Long countTotal();

    /**
     * 地域別の寮数を取得
     */
    List<Dormitory> countByRegion();
}
