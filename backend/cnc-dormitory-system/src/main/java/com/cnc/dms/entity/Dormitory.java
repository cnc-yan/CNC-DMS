package com.cnc.dms.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 寮 Entity
 * 対応テーブル: tbl_dormitory
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dormitory {

    private Long id;
    private String dormName;
    private String region;
    private String address;
    private String dormCondition;
    private Integer totalRooms;
    private String memo1;
    private String memo2;
    private String memo3;
    private Integer status;
    private Long version;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
