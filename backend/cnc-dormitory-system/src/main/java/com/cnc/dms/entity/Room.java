package com.cnc.dms.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 部屋 Entity
 * 対応テーブル: tbl_room
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    private Long id;
    private Long dormitoryId;
    private String roomNumber;
    private Integer capacity;
    private Integer currentOccupancy;
    private BigDecimal dailyRate;
    private Integer roomType;
    private Integer acType;
    private String memo1;
    private String memo2;
    private String memo3;
    private Integer status;
    private Long version;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    // 寮テーブルからの結合フィールド（空き室検索等で使用）
    private String dormName;
    private String dormCondition;
    private String region;
}
