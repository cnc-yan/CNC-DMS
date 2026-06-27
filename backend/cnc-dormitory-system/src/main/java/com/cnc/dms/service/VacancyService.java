package com.cnc.dms.service;

import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.dto.VacancyQueryRequest;
import com.cnc.dms.dto.VacancyResponse;

import java.util.List;

/**
 * 空き室管理 Service インターフェース
 * 設計書「8. 空き室管理」対応
 */
public interface VacancyService {

    /**
     * 空き室一覧を検索（ページング対応）
     */
    PageResponse<VacancyResponse> listVacantRooms(VacancyQueryRequest query);

    /**
     * 全ての空き室を取得（ページングなし）
     */
    List<VacancyResponse> getAllVacantRooms(VacancyQueryRequest query);

    /**
     * 特定の部屋が現在入居可能か判定
     */
    boolean isRoomAvailable(Long roomId);
}
