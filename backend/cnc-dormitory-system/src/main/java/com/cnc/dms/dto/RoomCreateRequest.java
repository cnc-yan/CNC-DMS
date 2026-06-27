package com.cnc.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 部屋作成リクエスト DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomCreateRequest {

    /** 所属寮ID（必須） */
    private Long dormitoryId;

    /** 部屋番号（必須） */
    private String roomNumber;

    /** 定員（デフォルト1） */
    @Builder.Default
    private Integer capacity = 1;

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

    /** 作成者 */
    private String createBy;
}
