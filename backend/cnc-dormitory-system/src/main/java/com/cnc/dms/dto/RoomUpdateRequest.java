package com.cnc.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 部屋更新リクエスト DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomUpdateRequest {

    /** 部屋ID（必須） */
    private Long id;

    /** 部屋番号 */
    private String roomNumber;

    /** 定員 */
    private Integer capacity;

    /** 一日単価 */
    private BigDecimal dailyRate;

    /** 室区分 (0=不明, 1=和室, 2=洋室) */
    private Integer roomType;

    /** エアコン区分 (0=なし, 1=あり) */
    private Integer acType;

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
