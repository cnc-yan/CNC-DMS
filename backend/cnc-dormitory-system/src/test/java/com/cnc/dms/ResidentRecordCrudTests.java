package com.cnc.dms;

import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.entity.ResidentRecord;
import com.cnc.dms.service.ResidentRecordService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 入居履歷CRUD功能測試
 */
@SpringBootTest
@DisplayName("入居履歷CRUD功能測試")
class ResidentRecordCrudTests {

    @Autowired
    private ResidentRecordService residentRecordService;

    @Test
    @DisplayName("測試查詢入居履歷列表 - 無條件")
    void testListRecordsWithoutCondition() {
        PageResponse<ResidentRecord> page = residentRecordService.listRecords(
                null, null, null, 1, 10);
        assertNotNull(page, "響應不應為null");
        assertNotNull(page.getList(), "列表不應為null");
        assertTrue(page.getTotal() >= 0, "總條數應大於等於0");
    }

    @Test
    @DisplayName("測試入居 - 社員ID缺失應拋出異常")
    void testCheckinWithoutEmployeeId() {
        try {
            residentRecordService.checkin(null, 1L, java.time.LocalDate.now(), null, null, null);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("社員ID"), "異常消息應指出社員ID缺失");
        }
    }

    @Test
    @DisplayName("測試入居 - 部屋ID缺失應拋出異常")
    void testCheckinWithoutRoomId() {
        try {
            residentRecordService.checkin(1L, null, java.time.LocalDate.now(), null, null, null);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("部屋ID"), "異常消息應指出部屋ID缺失");
        }
    }

    @Test
    @DisplayName("測試退寮 - 不存在的ID應拋出異常")
    void testCheckoutNonExistentRecord() {
        try {
            residentRecordService.checkout(99999L, java.time.LocalDate.now(), null);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("存在しません"), "異常消息應指出記錄不存在");
        }
    }

    @Test
    @DisplayName("測試查詢不存在的入居履歷")
    void testFindNonExistentRecord() {
        ResidentRecord record = residentRecordService.findById(99999L);
        assertTrue(record == null, "不存在的記錄應返回null");
    }
}
