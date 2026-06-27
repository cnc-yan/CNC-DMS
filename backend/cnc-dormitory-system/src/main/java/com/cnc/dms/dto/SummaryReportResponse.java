package com.cnc.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ダッシュボードサマリー レスポンス DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryReportResponse {

    /** 総寮数 */
    private Long totalDormitories;

    /** 総部屋数 */
    private Long totalRooms;

    /** 空室数 */
    private Long vacantRooms;

    /** 入居者数（現在入居中） */
    private Long currentResidents;

    /** 全入居履歴件数 */
    private Long totalResidentRecords;

    /** 未入居社員数 */
    private Long employeesWithoutResident;
}
