package com.cnc.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 空き室検索リクエスト DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacancyQueryRequest {

    /** 寮ID（絞り込み） */
    private Long dormitoryId;

    /** 性別条件 (MALE/FEMALE/ANY) */
    private String gender;

    @Builder.Default
    private Integer pageNum = 1;

    @Builder.Default
    private Integer pageSize = 10;
}
