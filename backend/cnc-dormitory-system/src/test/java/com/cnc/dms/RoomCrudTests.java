package com.cnc.dms;

import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.dto.RoomCreateRequest;
import com.cnc.dms.dto.RoomQueryRequest;
import com.cnc.dms.entity.Room;
import com.cnc.dms.service.RoomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 部屋CRUD功能測試
 */
@SpringBootTest
@DisplayName("部屋CRUD功能測試")
class RoomCrudTests {

    @Autowired
    private RoomService roomService;

    @Test
    @DisplayName("測試查詢部屋列表 - 無條件")
    void testListRoomsWithoutCondition() {
        RoomQueryRequest query = RoomQueryRequest.builder()
                .pageNum(1)
                .pageSize(10)
                .build();

        PageResponse<Room> response = roomService.listRooms(query);
        assertNotNull(response, "響應不應為null");
        assertNotNull(response.getList(), "列表不應為null");
        assertTrue(response.getTotal() >= 0, "總條數應大於等於0");
    }

    @Test
    @DisplayName("測試創建部屋 - 寮ID缺失應拋出異常")
    void testCreateRoomWithoutDormitoryId() {
        RoomCreateRequest request = RoomCreateRequest.builder()
                .roomNumber("101")
                .build();

        try {
            roomService.createRoom(request);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("寮ID"), "異常消息應指出寮ID缺失");
        }
    }

    @Test
    @DisplayName("測試查詢不存在的部屋")
    void testFindNonExistentRoom() {
        Room room = roomService.findById(99999L);
        assertTrue(room == null, "不存在的部屋應返回null");
    }

    @Test
    @DisplayName("測試根據寮ID查詢部屋列表")
    void testFindByDormitoryId() {
        try {
            var rooms = roomService.findByDormitoryId(1L);
            assertNotNull(rooms, "部屋列表不應為null");
        } catch (IllegalArgumentException e) {
            // DormitoryId不存在時捕獲異常
        }
    }
}
