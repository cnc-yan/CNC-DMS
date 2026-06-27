package com.cnc.dms.service;

import com.cnc.dms.dto.CalendarOccupancyResponse;

/**
 * カレンダー表示用 Service 接口
 */
public interface CalendarService {

    /**
     * 指定された寮・年月の occupancy カレンダーデータを取得
     *
     * @param dormitoryId 寮ID
     * @param year        年
     * @param month       月 (1-12)
     * @return カレンダー occupancy データ
     */
    CalendarOccupancyResponse getOccupancyCalendar(Long dormitoryId, int year, int month);
}
