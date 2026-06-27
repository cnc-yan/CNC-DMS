package com.cnc.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * カレンダー occupancy レスポンス DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarOccupancyResponse {

    private Integer year;
    private Integer month;
    private Long dormitoryId;
    private String dormitoryName;
    private List<RoomOccupancy> rooms;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomOccupancy {
        private Long roomId;
        private String roomNumber;
        private Integer capacity;
        private BigDecimal dailyRate;
        /**
         * Key: 日付 (1-31)
         * Value: VACANT / OCCUPIED / CHECKIN / CHECKOUT
         */
        private Map<Integer, String> days;
    }
}
