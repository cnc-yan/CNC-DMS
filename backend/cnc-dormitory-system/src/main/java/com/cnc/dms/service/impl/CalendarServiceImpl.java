package com.cnc.dms.service.impl;

import com.cnc.dms.dto.CalendarOccupancyResponse;
import com.cnc.dms.dto.CalendarOccupancyResponse.RoomOccupancy;
import com.cnc.dms.entity.Dormitory;
import com.cnc.dms.entity.ResidentRecord;
import com.cnc.dms.entity.Room;
import com.cnc.dms.mapper.DormitoryMapper;
import com.cnc.dms.mapper.ResidentRecordMapper;
import com.cnc.dms.mapper.RoomMapper;
import com.cnc.dms.service.CalendarService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * カレンダー Service 実装
 */
@Service
public class CalendarServiceImpl implements CalendarService {

    private final RoomMapper roomMapper;
    private final ResidentRecordMapper residentRecordMapper;
    private final DormitoryMapper dormitoryMapper;

    public CalendarServiceImpl(RoomMapper roomMapper,
                               ResidentRecordMapper residentRecordMapper,
                               DormitoryMapper dormitoryMapper) {
        this.roomMapper = roomMapper;
        this.residentRecordMapper = residentRecordMapper;
        this.dormitoryMapper = dormitoryMapper;
    }

    @Override
    public CalendarOccupancyResponse getOccupancyCalendar(Long dormitoryId, int year, int month) {
        // 1. Validate
        if (dormitoryId == null) {
            throw new IllegalArgumentException("寮IDは必須です");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("月は1〜12の範囲で指定してください");
        }

        // 2. Get dormitory info
        Dormitory dormitory = dormitoryMapper.findById(dormitoryId);
        if (dormitory == null) {
            throw new IllegalArgumentException("寮が存在しません: " + dormitoryId);
        }

        // 3. Get all rooms for the dormitory
        List<Room> rooms = roomMapper.findByDormitoryId(dormitoryId);

        // 4. Get active resident records overlapping with the month
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        List<ResidentRecord> records = residentRecordMapper.findByDormitoryAndDateRange(
                dormitoryId, monthStart, monthEnd
        );

        // Build a map: roomId -> list of records for that room
        Map<Long, List<ResidentRecord>> recordsByRoom = records.stream()
                .collect(Collectors.groupingBy(ResidentRecord::getRoomId));

        int daysInMonth = yearMonth.lengthOfMonth();

        // 5. For each room, compute daily status
        List<RoomOccupancy> roomOccupancies = new ArrayList<>();
        for (Room room : rooms) {
            Map<Integer, String> dayStatus = new HashMap<>(daysInMonth);
            List<ResidentRecord> roomRecords = recordsByRoom.getOrDefault(room.getId(), Collections.emptyList());

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate currentDate = yearMonth.atDay(day);
                String status = computeDayStatus(roomRecords, currentDate);
                dayStatus.put(day, status);
            }

            roomOccupancies.add(RoomOccupancy.builder()
                    .roomId(room.getId())
                    .roomNumber(room.getRoomNumber())
                    .capacity(room.getCapacity())
                    .dailyRate(room.getDailyRate())
                    .days(dayStatus)
                    .build());
        }

        return CalendarOccupancyResponse.builder()
                .year(year)
                .month(month)
                .dormitoryId(dormitoryId)
                .dormitoryName(dormitory.getDormName())
                .rooms(roomOccupancies)
                .build();
    }

    /**
     * 該当日の部屋状態を判定
     * 退寮予定日(plannedCheckoutDate)を考慮し、予定日を過ぎた日は空室扱いとする
     */
    private String computeDayStatus(List<ResidentRecord> records, LocalDate date) {
        for (ResidentRecord record : records) {
            LocalDate checkin = record.getCheckinDate();
            LocalDate plannedCheckout = record.getPlannedCheckoutDate();
            LocalDate checkout = record.getCheckoutDate();

            // 入居日と同日 → CHECKIN
            if (checkin != null && checkin.equals(date)) {
                return "CHECKIN";
            }

            // 実際の退去日と同日 → CHECKOUT
            if (checkout != null && checkout.equals(date)) {
                return "CHECKOUT";
            }

            // 退寮予定日と同日 → CHECKOUT（予定）
            if (checkout == null && plannedCheckout != null && plannedCheckout.equals(date)) {
                return "CHECKOUT";
            }

            // 入居日以降の判定
            if (checkin != null && !checkin.isAfter(date)) {
                // 実際に退去済み：退去日より後なら空室
                if (checkout != null) {
                    if (checkout.isAfter(date)) {
                        return "OCCUPIED";
                    }
                } else if (plannedCheckout != null) {
                    // 退寮予定日が設定されている：予定日より前は入居、以降は空室
                    if (!plannedCheckout.isAfter(date)) {
                        // 予定日を含めて空室（予定日はCHECKOUTとして上で処理済みなので、ここでは予定日より後のみ）
                        continue;
                    }
                    return "OCCUPIED";
                } else {
                    // 退寮予定なし、退去もなし → 入居中
                    return "OCCUPIED";
                }
            }
        }
        return "VACANT";
    }
}
