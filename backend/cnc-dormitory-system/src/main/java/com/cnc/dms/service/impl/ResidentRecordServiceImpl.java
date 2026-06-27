package com.cnc.dms.service.impl;

import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.entity.Employee;
import com.cnc.dms.entity.ResidentRecord;
import com.cnc.dms.entity.Room;
import com.cnc.dms.mapper.EmployeeMapper;
import com.cnc.dms.mapper.ResidentRecordMapper;
import com.cnc.dms.mapper.RoomMapper;
import com.cnc.dms.service.ResidentRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 入居履歴 Service 実装
 *
 * 業務ロジック（重複入居禁止チェック、退寮処理、部屋移動など）をService層に集約
 */
@Service
public class ResidentRecordServiceImpl implements ResidentRecordService {

    private final ResidentRecordMapper residentRecordMapper;
    private final EmployeeMapper employeeMapper;
    private final RoomMapper roomMapper;

    public ResidentRecordServiceImpl(ResidentRecordMapper residentRecordMapper,
                                     EmployeeMapper employeeMapper,
                                     RoomMapper roomMapper) {
        this.residentRecordMapper = residentRecordMapper;
        this.employeeMapper = employeeMapper;
        this.roomMapper = roomMapper;
    }

    /**
     * 部屋の現在人数を更新し、定員に応じて状態を自動変更する
     *
     * @param roomId 部屋ID
     * @param delta  増減値（入居:+1, 退去:-1）
     */
    private void updateRoomOccupancy(Long roomId, int delta) {
        Room room = roomMapper.findById(roomId);
        if (room == null) {
            throw new RuntimeException("部屋が存在しません");
        }

        int current = room.getCurrentOccupancy() != null ? room.getCurrentOccupancy() : 0;
        int newOccupancy = current + delta;
        if (newOccupancy < 0) {
            newOccupancy = 0;
        }
        room.setCurrentOccupancy(newOccupancy);

        // 利用不可（status=0）の場合は自動更新しない
        if (room.getStatus() != null && room.getStatus() != 0) {
            if (newOccupancy <= 0) {
                room.setStatus(1); // 空室
            } else {
                room.setStatus(2); // 入居中（定員に達しているかどうかに関わらず）
            }
        }

        int rows = roomMapper.update(room);
        if (rows == 0) {
            throw new RuntimeException("部屋情報の更新に失敗しました（楽観ロック競合）");
        }
    }

    @Override
    public ResidentRecord findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("入居履歴IDは必須です");
        }
        return residentRecordMapper.findById(id);
    }

    @Override
    public PageResponse<ResidentRecord> listRecords(Long employeeId, Long roomId,
                                                     Integer isActive,
                                                     Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        int offset = (pageNum - 1) * pageSize;

        List<ResidentRecord> list = residentRecordMapper.findAll(
                employeeId, roomId, isActive, offset, pageSize
        );

        Long total = residentRecordMapper.countByCondition(
                employeeId, roomId, isActive
        );

        int totalPage = (int) ((total + pageSize - 1) / pageSize);

        return PageResponse.<ResidentRecord>builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .total(total)
                .totalPage(totalPage)
                .list(list)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResidentRecord checkin(Long employeeId, Long roomId,
                                  LocalDate checkinDate, LocalDate plannedCheckoutDate,
                                  String notes, String createBy) {
        if (employeeId == null) {
            throw new IllegalArgumentException("社員IDは必須です");
        }
        if (roomId == null) {
            throw new IllegalArgumentException("部屋IDは必須です");
        }
        if (checkinDate == null) {
            throw new IllegalArgumentException("入寮日は必須です");
        }

        // 重複入居禁止チェック（同一部屋で期間が重なる入居がないか）
        // 退寮予定日がある場合はそちらを期間終了として扱う
        List<ResidentRecord> overlaps = residentRecordMapper.findOverlapRecords(
                roomId, checkinDate, plannedCheckoutDate
        );
        if (!overlaps.isEmpty()) {
            throw new RuntimeException("指定された期間に既に入居履歴が存在します（重複入居不可）");
        }

        // 社員の有効な入居履歴チェック（同一社員が既に入居中でないか）
        List<ResidentRecord> activeRecords = residentRecordMapper.findActiveByEmployeeId(employeeId);
        if (!activeRecords.isEmpty()) {
            throw new RuntimeException("この社員は既に入居中です");
        }

        ResidentRecord record = ResidentRecord.builder()
                .employeeId(employeeId)
                .roomId(roomId)
                .checkinDate(checkinDate)
                .plannedCheckoutDate(plannedCheckoutDate)
                .checkoutDate(null)
                .isActive(1)
                .notes(notes)
                .createBy(createBy)
                .build();

        residentRecordMapper.insert(record);

        // 部屋の現在人数+1、定員に応じて状態を更新
        updateRoomOccupancy(roomId, 1);

        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResidentRecord checkout(Long id, LocalDate checkoutDate, String updateBy) {
        if (id == null) {
            throw new IllegalArgumentException("入居履歴IDは必須です");
        }

        ResidentRecord record = residentRecordMapper.findById(id);
        if (record == null) {
            throw new RuntimeException("入居履歴が存在しません");
        }
        if (record.getIsActive() == 0) {
            throw new RuntimeException("既に退寮済みです");
        }

        LocalDate actualCheckout = checkoutDate != null ? checkoutDate : LocalDate.now();

        if (actualCheckout.isBefore(record.getCheckinDate())) {
            throw new IllegalArgumentException("退寮日は入寮日より後である必要があります");
        }

        // 利用料金を計算: 日額 × 入居日数（入居日〜退寮日）
        Room room = roomMapper.findById(record.getRoomId());
        if (room != null && room.getDailyRate() != null) {
            long days = ChronoUnit.DAYS.between(record.getCheckinDate(), actualCheckout);
            if (days < 0) days = 0;
            BigDecimal totalFee = room.getDailyRate().multiply(BigDecimal.valueOf(days));
            record.setTotalFee(totalFee);
        } else {
            // 万が一 dailyRate が取得できない場合でも、DBの日額が入居時のスナップショットとして使える
            record.setTotalFee(BigDecimal.ZERO);
        }

        record.setCheckoutDate(actualCheckout);
        record.setIsActive(0);
        record.setStatus("CHECKED_OUT");
        record.setUpdateBy(updateBy);

        int rows = residentRecordMapper.update(record);
        if (rows == 0) {
            throw new RuntimeException("退寮処理に失敗しました（楽観ロック競合）");
        }

        // 部屋の現在人数-1、定員に応じて状態を更新
        updateRoomOccupancy(record.getRoomId(), -1);

        return residentRecordMapper.findById(id);
    }

    @Override
    public List<ResidentRecord> getUsageFeeList() {
        List<ResidentRecord> records = residentRecordMapper.findUsageFees();
        LocalDate today = LocalDate.now();
        for (ResidentRecord record : records) {
            // 退寮済み: DBに保存済みのtotalFeeを使用
            // 入居中: 日額×日数(入居日〜本日)をその場で計算
            if (record.getIsActive() == 1 && record.getDailyRate() != null) {
                long days = ChronoUnit.DAYS.between(record.getCheckinDate(), today);
                if (days < 0) days = 0;
                record.setTotalFee(record.getDailyRate().multiply(BigDecimal.valueOf(days)));
            }
        }
        return records;
    }

    @Override
    public int deleteRecord(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("入居履歴IDは必須です");
        }

        ResidentRecord existing = residentRecordMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("入居履歴が存在しません");
        }

        return residentRecordMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResidentRecord transferRoom(Long currentRecordId, Long newRoomId,
                                       LocalDate transferDate, String updateBy) {
        if (currentRecordId == null) {
            throw new IllegalArgumentException("現在の入居履歴IDは必須です");
        }
        if (newRoomId == null) {
            throw new IllegalArgumentException("新しい部屋IDは必須です");
        }
        if (transferDate == null) {
            transferDate = LocalDate.now();
        }

        // 現在の入居履歴を取得
        ResidentRecord currentRecord = residentRecordMapper.findById(currentRecordId);
        if (currentRecord == null) {
            throw new RuntimeException("入居履歴が存在しません");
        }
        if (currentRecord.getIsActive() == 0) {
            throw new RuntimeException("既に退寮済みのため部屋移動できません");
        }

        // 新しい部屋の存在確認
        Room newRoom = roomMapper.findById(newRoomId);
        if (newRoom == null) {
            throw new RuntimeException("新しい部屋が存在しません");
        }

        // 移動日が入居日より後であることを確認
        if (transferDate.isBefore(currentRecord.getCheckinDate())) {
            throw new IllegalArgumentException("移動日は入居日より後である必要があります");
        }

        // 新しい部屋の重複チェック
        List<ResidentRecord> overlaps = residentRecordMapper.findOverlapRecords(
                newRoomId, transferDate, null
        );
        if (!overlaps.isEmpty()) {
            throw new RuntimeException("新しい部屋の指定期間に既に入居履歴が存在します");
        }

        // 現在の入居をチェックアウト（利用料金も計算）
        Room oldRoom = roomMapper.findById(currentRecord.getRoomId());
        if (oldRoom != null && oldRoom.getDailyRate() != null) {
            long days = ChronoUnit.DAYS.between(currentRecord.getCheckinDate(), transferDate);
            if (days < 0) days = 0;
            BigDecimal totalFee = oldRoom.getDailyRate().multiply(BigDecimal.valueOf(days));
            currentRecord.setTotalFee(totalFee);
        }
        currentRecord.setCheckoutDate(transferDate);
        currentRecord.setIsActive(0);
        currentRecord.setStatus("CHECKED_OUT");
        currentRecord.setUpdateBy(updateBy);
        residentRecordMapper.update(currentRecord);

        // 旧部屋の現在人数-1、状態更新
        updateRoomOccupancy(currentRecord.getRoomId(), -1);

        // 新しい部屋にチェックイン
        ResidentRecord newRecord = ResidentRecord.builder()
                .employeeId(currentRecord.getEmployeeId())
                .roomId(newRoomId)
                .checkinDate(transferDate)
                .checkoutDate(null)
                .isActive(1)
                .status("CHECKED_IN")
                .notes("部屋移動: " + currentRecord.getRoomId() + " → " + newRoomId)
                .createBy(updateBy)
                .build();
        residentRecordMapper.insert(newRecord);

        // 新部屋の現在人数+1、状態更新
        updateRoomOccupancy(newRoomId, 1);

        return newRecord;
    }

    @Override
    public Map<String, Object> getLongTermUsageInfo(Long employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("社員IDは必須です");
        }

        Map<String, Object> result = new HashMap<>();

        // 社員情報を取得
        Employee employee = employeeMapper.findById(employeeId);
        if (employee == null) {
            throw new RuntimeException("社員が存在しません");
        }

        // 初回利用日（社員マスタから）
        LocalDate firstUseDate = employee.getFirstUseDate();
        result.put("employeeId", employeeId);
        result.put("empName", employee.getEmpName());
        result.put("firstUseDate", firstUseDate);

        if (firstUseDate != null) {
            // 通算利用期間（初回利用日から現在までの日数）
            long totalDays = ChronoUnit.DAYS.between(firstUseDate, LocalDate.now());
            long totalMonths = ChronoUnit.MONTHS.between(firstUseDate, LocalDate.now());
            result.put("totalUsageDays", totalDays);
            result.put("totalUsageMonths", totalMonths);

            // 長期利用警告（3年以上 = 36ヶ月）
            boolean longTermWarning = totalMonths >= 36;
            result.put("longTermWarning", longTermWarning);

            // 寮費値上げ判断（5年以上 = 60ヶ月）
            boolean rateIncreaseJudgment = totalMonths >= 60;
            result.put("rateIncreaseJudgment", rateIncreaseJudgment);

            // 警告メッセージ
            if (longTermWarning) {
                String message = "長期利用警告: " + totalMonths + "ヶ月間継続して入居しています。";
                if (rateIncreaseJudgment) {
                    message += "寮費値上げの検討が必要です。";
                }
                result.put("warningMessage", message);
            }
        } else {
            // 初回利用日未設定の場合
            result.put("totalUsageDays", 0);
            result.put("totalUsageMonths", 0);
            result.put("longTermWarning", false);
            result.put("rateIncreaseJudgment", false);
            result.put("warningMessage", null);
            result.put("firstUseDateUnset", true);
        }

        return result;
    }
}
