-- ============================================================
-- H2 テスト用スキーマ（MySQL互換モード）
-- ============================================================

-- ユーザー表 (tbl_user)
DROP TABLE IF EXISTS tbl_user;
CREATE TABLE tbl_user (
    userid VARCHAR(50) PRIMARY KEY,
    password VARCHAR(100) NOT NULL,
    userName VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    create_by VARCHAR(50),
    create_time DATETIME,
    update_by VARCHAR(50),
    update_time DATETIME
);

-- 部門表 (tbl_department)
DROP TABLE IF EXISTS tbl_department;
CREATE TABLE tbl_department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dept_code VARCHAR(50) NOT NULL,
    dept_name VARCHAR(100) NOT NULL,
    region VARCHAR(50) DEFAULT NULL,
    parent_id BIGINT DEFAULT NULL,
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    version BIGINT DEFAULT 0,
    create_by VARCHAR(50) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(50) DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dept_code (dept_code)
);

-- 社員表 (tbl_employee)
DROP TABLE IF EXISTS tbl_employee;
CREATE TABLE tbl_employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    emp_no VARCHAR(50) NOT NULL,
    emp_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    nationality VARCHAR(30) NOT NULL,
    dept_id BIGINT DEFAULT NULL,
    first_use_date DATE DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    email VARCHAR(100) DEFAULT NULL,
    hire_date DATE NOT NULL,
    resign_date DATE DEFAULT NULL,
    emp_status TINYINT DEFAULT 1,
    version BIGINT DEFAULT 0,
    create_by VARCHAR(50) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(50) DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_emp_no (emp_no)
);

-- 寮マスタ (tbl_dormitory)
DROP TABLE IF EXISTS tbl_dormitory;
CREATE TABLE tbl_dormitory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dorm_name VARCHAR(100) NOT NULL,
    region VARCHAR(50) NOT NULL,
    address VARCHAR(255) DEFAULT NULL,
    dorm_condition VARCHAR(10) DEFAULT 'ANY',
    total_rooms INT DEFAULT 0,
    memo1 VARCHAR(255) DEFAULT NULL,
    memo2 VARCHAR(255) DEFAULT NULL,
    memo3 VARCHAR(255) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    version BIGINT DEFAULT 0,
    create_by VARCHAR(50) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(50) DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 部屋マスタ (tbl_room)
DROP TABLE IF EXISTS tbl_room;
CREATE TABLE tbl_room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dormitory_id BIGINT NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    capacity INT DEFAULT 1,
    current_occupancy INT DEFAULT 0,
    daily_rate DECIMAL(10,2) DEFAULT 0.00,
    room_type TINYINT DEFAULT 0,
    ac_type TINYINT DEFAULT 0,
    memo1 VARCHAR(255) DEFAULT NULL,
    memo2 VARCHAR(255) DEFAULT NULL,
    memo3 VARCHAR(255) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    version BIGINT DEFAULT 0,
    create_by VARCHAR(50) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(50) DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dorm_room (dormitory_id, room_number)
);

-- 入居履歴 (tbl_resident_record)
DROP TABLE IF EXISTS tbl_resident_record;
CREATE TABLE tbl_resident_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    checkin_date DATE NOT NULL,
    planned_checkout_date DATE DEFAULT NULL,
    checkout_date DATE DEFAULT NULL,
    is_active TINYINT DEFAULT 1,
    notes VARCHAR(500) DEFAULT NULL,
    total_fee DECIMAL(12,2) DEFAULT NULL,
    status VARCHAR(20) DEFAULT 'CHECKED_IN',
    version BIGINT DEFAULT 0,
    create_by VARCHAR(50) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(50) DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 変更履歴 (tbl_change_history)
DROP TABLE IF EXISTS tbl_change_history;
CREATE TABLE tbl_change_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(50) NOT NULL,
    record_id VARCHAR(50) NOT NULL,
    operation_type VARCHAR(10) NOT NULL,
    old_data TEXT DEFAULT NULL,
    new_data TEXT DEFAULT NULL,
    changed_by VARCHAR(50) DEFAULT NULL,
    changed_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remarks VARCHAR(500) DEFAULT NULL,
    create_by VARCHAR(50) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(50) DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- テストデータ（ユーザー）
INSERT INTO tbl_user (userid, password, userName, enabled, create_by, create_time) VALUES
('admin', 'admin123', '管理者', TRUE, 'system', NOW()),
('test', 'test123', 'テストユーザー', TRUE, 'system', NOW());

-- テストデータ（部門）
INSERT INTO tbl_department (dept_code, dept_name, region, parent_id, sort_order, status, create_by, create_time) VALUES
('DEPT-001', '東京本社', '東京', NULL, 1, 1, 'system', NOW()),
('DEPT-002', '大阪支社', '大阪', NULL, 2, 1, 'system', NOW()),
('DEPT-003', '名古屋支社', '名古屋', NULL, 3, 1, 'system', NOW()),
('DEPT-004', '福岡支社', '福岡', NULL, 4, 1, 'system', NOW()),
('DEPT-005', '中国事業部', '東京', NULL, 5, 1, 'system', NOW()),
('DEPT-101', '東京本社 営業部', '東京', 1, 1, 1, 'system', NOW());

-- テストデータ（社員）
INSERT INTO tbl_employee (emp_no, emp_name, gender, nationality, dept_id, first_use_date, phone, email, hire_date, emp_status, create_by, create_time) VALUES
('EMP-00001', '山田太郎', 'MALE', '日本', 1, '2020-06-01', '090-1111-1111', 'yamada.taro@example.com', '2020-04-01', 1, 'system', NOW()),
('EMP-00002', '田中花子', 'FEMALE', '日本', 2, '2021-07-15', '090-2222-2222', 'tanaka.hanako@example.com', '2021-04-01', 1, 'system', NOW()),
('EMP-00003', '佐藤次郎', 'MALE', '日本', 101, NULL, '090-3333-3333', 'sato.jiro@example.com', '2019-04-01', 1, 'system', NOW()),
('EMP-00004', '李偉', 'MALE', '中国', 5, '2025-11-30', '080-1111-0001', 'liwei@example.com', '2025-06-01', 1, 'system', NOW()),
('EMP-00005', '王娜', 'FEMALE', '中国', 5, '2024-08-30', '080-1111-0002', 'wangna@example.com', '2024-03-01', 1, 'system', NOW()),
('EMP-00006', '張強', 'MALE', '中国', 5, '2026-03-01', '080-1111-0003', 'zhangqiang@example.com', '2025-09-01', 1, 'system', NOW()),
('EMP-00007', '劉美', 'FEMALE', '中国', 5, '2024-07-04', '080-1111-0004', 'limei@example.com', '2024-01-15', 1, 'system', NOW());

-- テストデータ（寮）
INSERT INTO tbl_dormitory (dorm_name, region, address, dorm_condition, total_rooms, status, create_by, create_time) VALUES
('豊洲C寮', '東京', '東京都江東区豊洲4-10-4号棟-802', 'MALE', 4, 1, 'system', NOW()),
('豊洲D寮', '東京', '東京都江東区豊洲4-10-4号棟-910', 'ANY', 4, 1, 'system', NOW()),
('大島C寮', '東京', '東京都江東区大島6-1-5号棟519号室', 'ANY', 3, 1, 'system', NOW()),
('大島H寮', '東京', '東京都江東区大島6-1-5号棟416号室', 'FEMALE', 5, 1, 'system', NOW()),
('青砥A寮', '東京', '東京都葛飾区青戸3-11-2-615', 'ANY', 3, 1, 'system', NOW());

-- テストデータ（部屋）
INSERT INTO tbl_room (dormitory_id, room_number, capacity, daily_rate, room_type, ac_type, memo1, status, create_by, create_time) VALUES
-- 豊洲C寮
(1, '801', 2, 1500.00, 2, 0, '洋室', 2, 'system', NOW()),
(1, '802', 2, 1700.00, 2, 1, '洋室', 2, 'system', NOW()),
(1, '803', 1, 1200.00, 2, 0, '手前洋室', 1, 'system', NOW()),
(1, '804', 1, 1200.00, 2, 0, '手前洋室', 2, 'system', NOW()),
-- 豊洲D寮
(2, '901', 2, 1700.00, 2, 1, '洋室', 2, 'system', NOW()),
(2, '902', 2, 1500.00, 2, 0, '洋室', 2, 'system', NOW()),
(2, '903', 2, 1500.00, 2, 0, '洋室', 1, 'system', NOW()),
(2, '904', 1, 1200.00, 2, 0, '手前洋室', 2, 'system', NOW()),
-- 大島C寮
(3, '501', 1, 1300.00, 1, 0, '中和室', 2, 'system', NOW()),
(3, '502', 2, 1700.00, 2, 1, '洋室', 2, 'system', NOW()),
(3, '503', 1, 1200.00, 1, 0, '小和室', 2, 'system', NOW()),
-- 大島H寮
(4, '401', 1, 1300.00, 1, 0, '中和室', 1, 'system', NOW()),
(4, '402', 1, 1300.00, 1, 0, '中和室', 2, 'system', NOW()),
(4, '403', 2, 1700.00, 2, 1, '洋室', 1, 'system', NOW()),
(4, '404', 2, 1700.00, 2, 1, '洋室', 2, 'system', NOW()),
(4, '405', 1, 1200.00, 0, 0, '小部屋', 2, 'system', NOW()),
-- 青砥A寮
(5, '601', 1, 1300.00, 1, 0, '中和室', 1, 'system', NOW()),
(5, '602', 2, 1700.00, 2, 1, '洋室', 2, 'system', NOW()),
(5, '603', 1, 1200.00, 0, 0, '小部屋', 1, 'system', NOW());

-- テストデータ（入居履歴）
INSERT INTO tbl_resident_record (employee_id, room_id, checkin_date, planned_checkout_date, checkout_date, is_active, notes, total_fee, status, create_by, create_time) VALUES
-- 豊洲C寮 入居中
(4, 1, '2025-11-30', NULL, NULL, 1, '中国事業部 洋室', NULL, 'CHECKED_IN', 'system', NOW()),
(5, 2, '2024-08-30', NULL, NULL, 1, '中国事業部 洋室AC有', NULL, 'CHECKED_IN', 'system', NOW()),
(7, 4, '2024-07-04', NULL, NULL, 1, '中国事業部 手前洋室', NULL, 'CHECKED_IN', 'system', NOW()),
-- 大島C寮 入居中
(6, 9, '2026-03-01', '2026-08-31', NULL, 1, '中国事業部 中和室', NULL, 'CHECKED_IN', 'system', NOW()),
-- 大島H寮 入居中
(3, 12, '2019-06-01', NULL, NULL, 1, '開発部', NULL, 'CHECKED_IN', 'system', NOW()),
-- 青砥A寮 入居中
(1, 17, '2020-06-01', NULL, NULL, 1, '営業部', NULL, 'CHECKED_IN', 'system', NOW()),
-- 過去の入居履歴（退去済）
(2, 8, '2026-02-01', '2026-05-30', '2026-05-30', 0, '退去済', 210000.00, 'CHECKED_OUT', 'system', NOW()),
(6, 10, '2026-03-01', '2026-04-01', '2026-04-01', 0, '短期入居 退去', 51000.00, 'CHECKED_OUT', 'system', NOW());
