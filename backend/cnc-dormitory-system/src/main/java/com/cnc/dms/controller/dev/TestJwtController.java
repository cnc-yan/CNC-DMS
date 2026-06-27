package com.cnc.dms.controller.dev;

import com.cnc.dms.dto.Result;
import com.cnc.dms.util.JwtUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * JWTトークン発行用 Controller（dev/qa 環境のみ有効）
 * フロントエンド開発時に任意のユーザーのJWTトークンを取得するために使用
 */
@RestController
@RequestMapping("/test")
@Profile({"dev", "qa"})
public class TestJwtController {

    private final JwtUtil jwtUtil;
    private final JdbcTemplate jdbcTemplate;

    public TestJwtController(JwtUtil jwtUtil, JdbcTemplate jdbcTemplate) {
        this.jwtUtil = jwtUtil;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/token")
    public Result<String> token(@RequestParam(defaultValue = "admin") String userId) {
        String token = jwtUtil.generateToken(userId);
        return Result.success(token, "JWT token generated for userId: " + userId);
    }

    /**
     * 指定したテーブルのカラム一覧を取得（デバッグ用）
     */
    @GetMapping("/columns")
    public Result<List<Map<String, Object>>> columns(@RequestParam(required = false) String tableName) {
        try {
            if (tableName == null || tableName.isBlank()) {
                return Result.fail("テーブル名 (tableName) は必須です。例: /test/columns?tableName=tbl_user");
            }
            String sql = "SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT " +
                         "FROM information_schema.COLUMNS " +
                         "WHERE TABLE_SCHEMA = (SELECT DATABASE()) AND TABLE_NAME = ? " +
                         "ORDER BY ORDINAL_POSITION";
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql, tableName);
            return Result.success(columns);
        } catch (Exception e) {
            return Result.fail("エラー: " + e.getMessage());
        }
    }

    /**
     * データベース内の全テーブル一覧を取得（デバッグ用）
     */
    @GetMapping("/tables")
    public Result<List<Map<String, Object>>> tables() {
        try {
            String sql = "SELECT TABLE_NAME, TABLE_COMMENT " +
                         "FROM information_schema.TABLES " +
                         "WHERE TABLE_SCHEMA = (SELECT DATABASE()) " +
                         "ORDER BY TABLE_NAME";
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(sql);
            return Result.success(tables);
        } catch (Exception e) {
            return Result.fail("エラー: " + e.getMessage());
        }
    }

    /**
     * スキーマをDDL定義に合わせて修正する（開発用）
     * 各ALTERを個別に実行し、エラーを個別に報告
     */
    @GetMapping("/fix-schema")
    public Result<String> fixSchema() {
        StringBuilder result = new StringBuilder();

        // Helper: execute single SQL and catch error
        java.util.function.Consumer<String> exec = sql -> {
            try { jdbcTemplate.execute(sql); result.append("  OK\n"); }
            catch (Exception e) { result.append("  SKIP: ").append(e.getMessage()).append("\n"); }
        };

        // ===== tbl_department =====
        result.append("=== tbl_department ===\n");
        exec.accept("ALTER TABLE tbl_department ADD COLUMN dept_code VARCHAR(50) NOT NULL DEFAULT 'TEMP' AFTER id");
        exec.accept("ALTER TABLE tbl_department ADD COLUMN region VARCHAR(50) DEFAULT NULL AFTER dept_name");
        exec.accept("ALTER TABLE tbl_department ADD COLUMN parent_id BIGINT DEFAULT NULL AFTER region");
        exec.accept("ALTER TABLE tbl_department ADD COLUMN sort_order INT DEFAULT 0 AFTER parent_id");
        exec.accept("ALTER TABLE tbl_department ADD COLUMN status TINYINT DEFAULT 1 AFTER sort_order");
        exec.accept("ALTER TABLE tbl_department ADD COLUMN version BIGINT DEFAULT 0 AFTER status");
        exec.accept("ALTER TABLE tbl_department ADD COLUMN create_by VARCHAR(50) DEFAULT NULL AFTER version");
        exec.accept("ALTER TABLE tbl_department ADD COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP AFTER create_by");
        exec.accept("ALTER TABLE tbl_department ADD COLUMN update_by VARCHAR(50) DEFAULT NULL AFTER create_time");
        exec.accept("ALTER TABLE tbl_department ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP AFTER update_by");
        exec.accept("UPDATE tbl_department SET dept_code = CONCAT('DEPT-', id) WHERE dept_code = 'TEMP'");

        // ===== tbl_employee =====
        result.append("=== tbl_employee ===\n");
        exec.accept("ALTER TABLE tbl_employee CHANGE COLUMN full_name emp_name VARCHAR(100) NOT NULL");
        exec.accept("ALTER TABLE tbl_employee ADD COLUMN first_use_date DATE DEFAULT NULL AFTER dept_id");
        exec.accept("ALTER TABLE tbl_employee ADD COLUMN phone VARCHAR(20) DEFAULT NULL AFTER first_use_date");
        exec.accept("ALTER TABLE tbl_employee ADD COLUMN email VARCHAR(100) DEFAULT NULL AFTER phone");
        exec.accept("ALTER TABLE tbl_employee ADD COLUMN hire_date DATE DEFAULT NULL AFTER email");
        exec.accept("ALTER TABLE tbl_employee ADD COLUMN resign_date DATE DEFAULT NULL AFTER hire_date");
        exec.accept("ALTER TABLE tbl_employee ADD COLUMN emp_status TINYINT DEFAULT 1 AFTER resign_date");
        exec.accept("ALTER TABLE tbl_employee ADD COLUMN version BIGINT DEFAULT 0 AFTER emp_status");
        exec.accept("ALTER TABLE tbl_employee ADD COLUMN update_by VARCHAR(50) DEFAULT NULL AFTER create_time");
        exec.accept("ALTER TABLE tbl_employee ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER update_by");
        exec.accept("UPDATE tbl_employee SET emp_status = 1 WHERE emp_status IS NULL");

        // ===== tbl_dormitory =====
        result.append("=== tbl_dormitory ===\n");
        exec.accept("ALTER TABLE tbl_dormitory ADD COLUMN region VARCHAR(50) DEFAULT NULL AFTER dorm_name");
        exec.accept("ALTER TABLE tbl_dormitory CHANGE COLUMN `condition` dorm_condition VARCHAR(10) DEFAULT 'ANY'");
        exec.accept("ALTER TABLE tbl_dormitory ADD COLUMN total_rooms INT DEFAULT 0 AFTER dorm_condition");
        exec.accept("ALTER TABLE tbl_dormitory ADD COLUMN status TINYINT DEFAULT 1 AFTER total_rooms");
        exec.accept("ALTER TABLE tbl_dormitory ADD COLUMN version BIGINT DEFAULT 0 AFTER status");
        exec.accept("ALTER TABLE tbl_dormitory ADD COLUMN create_by VARCHAR(50) DEFAULT NULL AFTER version");
        exec.accept("ALTER TABLE tbl_dormitory ADD COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP AFTER create_by");
        exec.accept("ALTER TABLE tbl_dormitory ADD COLUMN update_by VARCHAR(50) DEFAULT NULL AFTER create_time");
        exec.accept("ALTER TABLE tbl_dormitory ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP AFTER update_by");

        // ===== tbl_room =====
        result.append("=== tbl_room ===\n");
        exec.accept("ALTER TABLE tbl_room CHANGE COLUMN room_no room_number VARCHAR(20) NOT NULL");
        exec.accept("ALTER TABLE tbl_room CHANGE COLUMN daily_fee daily_rate DECIMAL(10,2) DEFAULT 0.00");
        exec.accept("ALTER TABLE tbl_room ADD COLUMN floor INT DEFAULT NULL AFTER room_number");
        exec.accept("ALTER TABLE tbl_room ADD COLUMN current_occupancy INT DEFAULT 0 AFTER capacity");
        exec.accept("ALTER TABLE tbl_room ADD COLUMN status TINYINT DEFAULT 1 AFTER daily_rate");
        exec.accept("ALTER TABLE tbl_room ADD COLUMN version BIGINT DEFAULT 0 AFTER status");
        exec.accept("ALTER TABLE tbl_room ADD COLUMN create_by VARCHAR(50) DEFAULT NULL AFTER version");
        exec.accept("ALTER TABLE tbl_room ADD COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP AFTER create_by");
        exec.accept("ALTER TABLE tbl_room ADD COLUMN update_by VARCHAR(50) DEFAULT NULL AFTER create_time");
        exec.accept("ALTER TABLE tbl_room ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP AFTER update_by");

        // ===== tbl_resident_record =====
        result.append("=== tbl_resident_record ===\n");
        exec.accept("ALTER TABLE tbl_resident_record CHANGE COLUMN entry_date checkin_date DATE NOT NULL");
        exec.accept("ALTER TABLE tbl_resident_record CHANGE COLUMN leave_date checkout_date DATE DEFAULT NULL");
        exec.accept("ALTER TABLE tbl_resident_record ADD COLUMN is_active TINYINT DEFAULT 1 AFTER checkout_date");
        exec.accept("ALTER TABLE tbl_resident_record ADD COLUMN notes VARCHAR(500) DEFAULT NULL AFTER is_active");
        exec.accept("ALTER TABLE tbl_resident_record ADD COLUMN create_by VARCHAR(50) DEFAULT NULL AFTER version");
        exec.accept("ALTER TABLE tbl_resident_record ADD COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP AFTER create_by");
        exec.accept("ALTER TABLE tbl_resident_record ADD COLUMN update_by VARCHAR(50) DEFAULT NULL AFTER create_time");
        exec.accept("ALTER TABLE tbl_resident_record ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP AFTER update_by");
        exec.accept("UPDATE tbl_resident_record SET is_active = 1 WHERE checkout_date IS NULL");

        return Result.success(result.toString(), "スキーマ修正完了");
    }
}
