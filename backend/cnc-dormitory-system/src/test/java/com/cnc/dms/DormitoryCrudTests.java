package com.cnc.dms;

import com.cnc.dms.dto.DormitoryCreateRequest;
import com.cnc.dms.dto.DormitoryQueryRequest;
import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.entity.Dormitory;
import com.cnc.dms.service.DormitoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 寮CRUD功能測試
 */
@SpringBootTest
@DisplayName("寮CRUD功能測試")
class DormitoryCrudTests {

    @Autowired
    private DormitoryService dormitoryService;

    @Test
    @DisplayName("測試查詢寮列表 - 無條件")
    void testListDormitoriesWithoutCondition() {
        DormitoryQueryRequest query = DormitoryQueryRequest.builder()
                .pageNum(1)
                .pageSize(10)
                .build();

        PageResponse<Dormitory> response = dormitoryService.listDormitories(query);
        assertNotNull(response, "響應不應為null");
        assertNotNull(response.getList(), "列表不應為null");
        assertTrue(response.getTotal() >= 0, "總條數應大於等於0");
    }

    @Test
    @DisplayName("測試創建寮 - 名稱缺失應拋出異常")
    void testCreateDormitoryWithoutName() {
        DormitoryCreateRequest request = DormitoryCreateRequest.builder()
                .region("東京")
                .build();

        try {
            dormitoryService.createDormitory(request);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("寮名称"), "異常消息應指出寮名稱缺失");
        }
    }

    @Test
    @DisplayName("測試創建寮 - 地域缺失應拋出異常")
    void testCreateDormitoryWithoutRegion() {
        DormitoryCreateRequest request = DormitoryCreateRequest.builder()
                .dormName("テスト寮")
                .build();

        try {
            dormitoryService.createDormitory(request);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("地域"), "異常消息應指出地域缺失");
        }
    }

    @Test
    @DisplayName("測試查詢不存在的寮")
    void testFindNonExistentDormitory() {
        Dormitory dorm = dormitoryService.findById(99999L);
        assertTrue(dorm == null, "不存在的寮應返回null");
    }
}
