package com.cnc.dms.controller;

import com.cnc.dms.dto.CalendarOccupancyResponse;
import com.cnc.dms.dto.Result;
import com.cnc.dms.service.CalendarService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * カレンダー表示用 Controller
 * 入居管理-寮割カレンダー 対応
 */
@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    /**
     * 指定された寮・年月の occupancy カレンダーデータを取得
     *
     * GET /api/calendar/occupancy?dormitoryId=1&year=2026&month=6
     */
    @GetMapping("/occupancy")
    public Result<CalendarOccupancyResponse> getOccupancyCalendar(
            @RequestParam Long dormitoryId,
            @RequestParam int year,
            @RequestParam int month) {
        try {
            CalendarOccupancyResponse response = calendarService.getOccupancyCalendar(dormitoryId, year, month);
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (Exception e) {
            return Result.fail("カレンダーデータ取得失敗", e.getMessage());
        }
    }
}
