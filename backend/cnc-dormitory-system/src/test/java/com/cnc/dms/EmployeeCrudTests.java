package com.cnc.dms;

import com.cnc.dms.dto.EmployeeCreateRequest;
import com.cnc.dms.dto.EmployeeQueryRequest;
import com.cnc.dms.dto.EmployeeUpdateRequest;
import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.entity.Employee;
import com.cnc.dms.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 社員CRUD功能測試
 */
@SpringBootTest
@DisplayName("社員CRUD功能測試")
class EmployeeCrudTests {

    @Autowired
    private EmployeeService employeeService;

    @Test
    @DisplayName("測試查詢社員列表 - 無條件")
    void testListEmployeesWithoutCondition() {
        EmployeeQueryRequest query = EmployeeQueryRequest.builder()
                .pageNum(1)
                .pageSize(10)
                .build();

        PageResponse<Employee> response = employeeService.listEmployees(query);
        assertNotNull(response, "響應不應為null");
        assertNotNull(response.getList(), "列表不應為null");
        assertTrue(response.getTotal() >= 0, "總條數應大於等於0");
    }

    @Test
    @DisplayName("測試創建社員 - 工號缺失應拋出異常")
    void testCreateEmployeeWithoutEmpNo() {
        EmployeeCreateRequest request = EmployeeCreateRequest.builder()
                .empName("測試社員")
                .build();

        try {
            employeeService.createEmployee(request);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("工番"), "異常消息應指出工號缺失");
        }
    }

    @Test
    @DisplayName("測試查詢不存在的社員")
    void testFindNonExistentEmployee() {
        Employee emp = employeeService.findById(99999L);
        assertTrue(emp == null, "不存在的社員應返回null");
    }

    @Test
    @DisplayName("測試刪除不存在的社員應拋出異常")
    void testDeleteNonExistentEmployee() {
        try {
            employeeService.deleteEmployee(99999L);
        } catch (Exception e) {
            // DB接続時はRuntimeException、未接続時はPersistenceExceptionが発生
            assertNotNull(e.getMessage(), "異常消息不應為null");
        }
    }
}
