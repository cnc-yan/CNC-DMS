package com.cnc.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 寮作成リクエスト DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DormitoryCreateRequest {

    /** 寮名称（必須） */
    private String dormName;

    /** 地域（必須） */
    private String region;

    /** 住所 */
    private String address;

    /** 入居条件: MALE / FEMALE / ANY */
    private String dormCondition;

    /** 総部屋数 */
    private Integer totalRooms;

    /** メモ1 */
    private String memo1;

    /** メモ2 */
    private String memo2;

    /** メモ3 */
    private String memo3;

    /** 作成者 */
    private String createBy;
}
