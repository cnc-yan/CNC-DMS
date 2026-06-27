package com.cnc.dms.controller;

import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.dto.Result;
import com.cnc.dms.dto.VacancyQueryRequest;
import com.cnc.dms.dto.VacancyResponse;
import com.cnc.dms.service.VacancyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 空き室管理 Controller
 * 設計書「8. 空き室管理」対応
 */
@RestController
@RequestMapping("/api/vacancy")
public class VacancyController {

    private final VacancyService vacancyService;

    public VacancyController(VacancyService vacancyService) {
        this.vacancyService = vacancyService;
    }

    /**
     * 空き室一覧をページング検索
     *
     * GET /api/vacancy/list?dormitoryId=&gender=&pageNum=1&pageSize=10
     */
    @GetMapping("/list")
    public Result<PageResponse<VacancyResponse>> listVacantRooms(VacancyQueryRequest query) {
        try {
            PageResponse<VacancyResponse> page = vacancyService.listVacantRooms(query);
            return Result.success(page);
        } catch (Exception e) {
            return Result.fail("検索失敗", e.getMessage());
        }
    }

    /**
     * 全ての空き室一覧を取得（ページングなし）
     *
     * GET /api/vacancy/all?dormitoryId=&gender=
     */
    @GetMapping("/all")
    public Result<List<VacancyResponse>> getAllVacantRooms(VacancyQueryRequest query) {
        try {
            List<VacancyResponse> list = vacancyService.getAllVacantRooms(query);
            return Result.success(list);
        } catch (Exception e) {
            return Result.fail("検索失敗", e.getMessage());
        }
    }

    /**
     * 部屋が入居可能か判定
     *
     * GET /api/vacancy/check?roomId=1
     */
    @GetMapping("/check")
    public Result<Boolean> checkRoomAvailability(@RequestParam Long roomId) {
        try {
            boolean available = vacancyService.isRoomAvailable(roomId);
            return Result.success(available);
        } catch (Exception e) {
            return Result.fail("判定失敗", e.getMessage());
        }
    }
}
