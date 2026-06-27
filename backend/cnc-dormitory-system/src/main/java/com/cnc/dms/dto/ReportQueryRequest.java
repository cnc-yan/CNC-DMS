package com.cnc.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * レポート検索リクエスト DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportQueryRequest {

    /** 対象年（yyyy） */
    private String year;

    /** 対象月（yyyy-MM） */
    private String month;

    /** 地域 */
    private String region;
}
