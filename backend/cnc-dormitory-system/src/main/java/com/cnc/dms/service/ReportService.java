package com.cnc.dms.service;

import com.cnc.dms.dto.OccupancyReportResponse;
import com.cnc.dms.dto.ReportQueryRequest;
import com.cnc.dms.dto.SummaryReportResponse;

import java.util.List;

/**
 * 帳票・統計 Service インターフェース
 * 設計書「report」モジュール対応
 */
public interface ReportService {

    /**
     * ダッシュボード用サマリー情報を取得
     */
    SummaryReportResponse getSummary();

    /**
     * 寮別稼働率レポートを取得
     */
    List<OccupancyReportResponse> getOccupancyReport(ReportQueryRequest query);
}
