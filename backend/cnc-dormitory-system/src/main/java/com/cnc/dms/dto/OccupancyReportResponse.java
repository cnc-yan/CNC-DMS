package com.cnc.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 稼働率レポート レスポンス DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupancyReportResponse {

    /** 寮ID */
    private Long dormitoryId;

    /** 寮名 */
    private String dormName;

    /** 地域 */
    private String region;

    /** 総部屋数 */
    private Integer totalRooms;

    /** 入居中部屋数 */
    private Integer occupiedRooms;

    /** 空室数 */
    private Integer vacantRooms;

    /** 総定員数（全室の定員合計） */
    private Integer totalCapacity;

    /** 現在入居者数（全室の入居数合計） */
    private Integer totalOccupancy;

    /** 定員ベース稼働率 (%) = totalOccupancy / totalCapacity * 100 */
    private Double occupancyRate;
}
