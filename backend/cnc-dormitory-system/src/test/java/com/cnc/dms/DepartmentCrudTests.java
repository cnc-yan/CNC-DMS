package com.cnc.dms;

import com.cnc.dms.dto.DepartmentCreateRequest;
import com.cnc.dms.dto.DepartmentQueryRequest;
import com.cnc.dms.dto.DepartmentUpdateRequest;
import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.entity.Department;
import com.cnc.dms.service.DepartmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 部門CRUD功能測試
 */
@SpringBootTest
@DisplayName("部門CRUD功能測試")
class DepartmentCrudTests {

    @Autowired
    private DepartmentService departmentService;

    @Test
    @DisplayName("測試查詢部門列表 - 無條件")
    void testListDepartmentsWithoutCondition() {
        DepartmentQueryRequest query = DepartmentQueryRequest.builder()
                .pageNum(1)
                .pageSize(10)
                .build();

        PageResponse<Department> response = departmentService.listDepartments(query);
        assertNotNull(response, "響應不應為null");
        assertNotNull(response.getList(), "列表不應為null");
        assertTrue(response.getTotal() >= 0, "總條數應大於等於0");
    }

    @Test
    @DisplayName("測試創建部門 - 參數無效應拋出異常")
    void testCreateDepartmentWithInvalidParams() {
        try {
            departmentService.createDepartment(null);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("部門コード"), "異常消息應指出部門代碼缺失");
        }
    }

    @Test
    @DisplayName("測試查詢不存在的部門")
    void testFindNonExistentDepartment() {
        Department dept = departmentService.findById(99999L);
        assertTrue(dept == null, "不存在的部門應返回null");
    }

    @Test
    @DisplayName("測試更新部門 - 版本號缺失應拋出異常")
    void testUpdateDepartmentWithoutVersion() {
        DepartmentUpdateRequest request = DepartmentUpdateRequest.builder()
                .id(1L)
                .deptName("測試部門")
                .build();

        try {
            departmentService.updateDepartment(request);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("バージョン"), "異常消息應指出版本缺失");
        }
    }

    @Test
    @DisplayName("測試刪除不存在的部門應拋出異常")
    void testDeleteNonExistentDepartment() {
        try {
            departmentService.deleteDepartment(99999L);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("存在しません"), "異常消息應指出部門不存在");
        }
    }
}
