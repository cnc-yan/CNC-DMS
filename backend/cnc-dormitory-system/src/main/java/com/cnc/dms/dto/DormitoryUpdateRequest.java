package com.cnc.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 寮更新リクエスト DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DormitoryUpdateRequest {

    /** 寮ID（必須） */
    private Long id;

    /** 寮名称 */
    private String dormName;

    /** 地域 */
    private String region;

    /** 住所 */
    private String address;

    /** 入居条件 */
    private String dormCondition;

    /** 総部屋数 */
    private Integer totalRooms;

    /** メモ1 */
    private String memo1;

    /** メモ2 */
    private String memo2;

    /** メモ3 */
    private String memo3;

    /** 状態 */
    private Integer status;

    /** 楽観ロックバージョン（必須） */
    private Long version;

    /** 更新者 */
    private String updateBy;
}
