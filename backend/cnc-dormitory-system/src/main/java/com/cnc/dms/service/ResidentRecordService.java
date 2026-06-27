package com.cnc.dms.service;

import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.entity.ResidentRecord;
import com.cnc.dms.entity.Room;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 入居履歴 Service 接口
 */
public interface ResidentRecordService {

    /**
     * IDで入居履歴を検索
     */
    ResidentRecord findById(Long id);

    /**
     * 条件付きページング検索
     */
    PageResponse<ResidentRecord> listRecords(Long employeeId, Long roomId,
                                              Integer isActive,
                                              Integer pageNum, Integer pageSize);

    /**
     * 入居登録（重複チェック含む）
     */
    ResidentRecord checkin(Long employeeId, Long roomId,
                           LocalDate checkinDate, LocalDate plannedCheckoutDate,
                           String notes, String createBy);

    /**
     * 退寮処理
     */
    ResidentRecord checkout(Long id, LocalDate checkoutDate, String updateBy);

    /**
     * 部屋移動
     * 現在入居中の社員を別の部屋に移動する（チェックアウト＋チェックインを一括処理）
     */
    ResidentRecord transferRoom(Long currentRecordId, Long newRoomId,
                                LocalDate transferDate, String updateBy);

    /**
     * 入居履歴削除
     */
    int deleteRecord(Long id);

    /**
     * 社員の長期利用情報を取得
     * @param employeeId 社員ID
     * @return 初回利用日、通算利用日数、警告フラグ等を含むMap
     */
    Map<String, Object> getLongTermUsageInfo(Long employeeId);

    /**
     * 利用料金確認一覧を取得（入居履歴＋社員名＋日額＋計算済み料金）
     */
    List<ResidentRecord> getUsageFeeList();
}
