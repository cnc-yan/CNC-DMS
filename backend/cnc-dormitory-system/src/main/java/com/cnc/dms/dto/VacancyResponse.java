package com.cnc.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 空き室情報レスポンス DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacancyResponse {

    private Long id;
    private Long dormitoryId;
    private String dormName;
    private String dormCondition;
    private String roomNumber;
    private Integer capacity;
    private Integer currentOccupancy;
    private Integer availableCapacity;
    private BigDecimal dailyRate;
    private String region;
}
