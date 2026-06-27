package com.cnc.dms.mapper;

import com.cnc.dms.entity.ResidentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 入居履歴 Mapper 接口
 */
@Mapper
public interface ResidentRecordMapper {

    /**
     * IDで入居履歴を検索
     */
    ResidentRecord findById(@Param("id") Long id);

    /**
     * 条件付き一覧検索（ページング対応）
     */
    List<ResidentRecord> findAll(@Param("employeeId") Long employeeId,
                                 @Param("roomId") Long roomId,
                                 @Param("isActive") Integer isActive,
                                 @Param("offset") Integer offset,
                                 @Param("limit") Integer limit);

    /**
     * 条件付き総件数取得
     */
    Long countByCondition(@Param("employeeId") Long employeeId,
                          @Param("roomId") Long roomId,
                          @Param("isActive") Integer isActive);

    /**
     * 社員の有効な入居履歴を取得
     */
    List<ResidentRecord> findActiveByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 指定された寮・日付範囲のアクティブな入居履歴を取得（カレンダー用）
     */
    List<ResidentRecord> findByDormitoryAndDateRange(@Param("dormitoryId") Long dormitoryId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * 部屋の重複入居チェック
     * 指定期間と重複するレコードを検索
     */
    List<ResidentRecord> findOverlapRecords(@Param("roomId") Long roomId,
                                             @Param("checkinDate") LocalDate checkinDate,
                                             @Param("checkoutDate") LocalDate checkoutDate);

    /**
     * 新規作成
     */
    int insert(ResidentRecord record);

    /**
     * 更新
     */
    int update(ResidentRecord record);

    /**
     * IDで削除
     */
    int deleteById(@Param("id") Long id);

    /**
     * 利用料金確認一覧を取得
     */
    List<ResidentRecord> findUsageFees();

    /**
     * 入居中（アクティブ）の件数を取得
     */
    Long countActive();

    /**
     * 全入居履歴の総件数を取得
     */
    Long countTotalRecords();

    /**
     * 入居履歴のない社員数を取得
     */
    Long countEmployeesWithoutResident();
}
