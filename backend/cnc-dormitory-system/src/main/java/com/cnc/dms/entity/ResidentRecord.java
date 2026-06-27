package com.cnc.dms.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 入居履歴 Entity
 * 対応テーブル: tbl_resident_record
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResidentRecord {

    private Long id;
    private Long employeeId;
    private Long roomId;
    private LocalDate checkinDate;
    private LocalDate plannedCheckoutDate;
    private LocalDate checkoutDate;
    private Integer isActive;
    private String notes;
    private BigDecimal totalFee;
    private String status;
    private Long version;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    // 部屋・寮テーブルからの結合フィールド
    private String roomNumber;
    private Long dormitoryId;
    private String dormName;
    private java.math.BigDecimal dailyRate;

    // 社員テーブルからの結合フィールド
    private String empName;
    private String empNo;
}
