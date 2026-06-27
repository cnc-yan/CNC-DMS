package com.cnc.dms.controller;

import com.cnc.dms.dto.OccupancyReportResponse;
import com.cnc.dms.dto.ReportQueryRequest;
import com.cnc.dms.dto.Result;
import com.cnc.dms.dto.SummaryReportResponse;
import com.cnc.dms.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 帳票・統計 Controller
 * 設計書「report」モジュール対応
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * ダッシュボード用サマリー情報を取得
     *
     * GET /api/reports/summary
     */
    @GetMapping("/summary")
    public Result<SummaryReportResponse> getSummary() {
        try {
            SummaryReportResponse summary = reportService.getSummary();
            return Result.success(summary);
        } catch (Exception e) {
            return Result.fail("サマリー取得失敗", e.getMessage());
        }
    }

    /**
     * 寮別稼働率レポートを取得
     *
     * GET /api/reports/occupancy?region=
     */
    @GetMapping("/occupancy")
    public Result<List<OccupancyReportResponse>> getOccupancyReport(ReportQueryRequest query) {
        try {
            List<OccupancyReportResponse> report = reportService.getOccupancyReport(query);
            return Result.success(report);
        } catch (Exception e) {
            return Result.fail("稼働率レポート取得失敗", e.getMessage());
        }
    }

}
