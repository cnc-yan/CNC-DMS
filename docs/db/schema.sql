-- ============================================================
-- CNC-DMS 寮管理システム DDL（MySQL 8）
-- 設計書: docs/architecture/寮管理システム基本設計仕様書.md
--         docs/architecture/dorm_management_system_tree_structure.md
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;
-- ============================================================
-- 1. ユーザー・認証
-- ============================================================

-- ユーザー表 (tbl_user)
DROP TABLE IF EXISTS tbl_user;
CREATE TABLE tbl_user (
    userid VARCHAR(50) PRIMARY KEY COMMENT 'ユーザーID',
    password VARCHAR(100) NOT NULL COMMENT 'パスワード(BCryptハッシュ)',
    userName VARCHAR(100) NOT NULL COMMENT 'ユーザー名',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '有効フラグ',
    create_by VARCHAR(50) DEFAULT NULL COMMENT '作成者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    update_by VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ユーザー表';


-- ============================================================
-- 2. マスタ系テーブル
-- ============================================================

-- 部門表 (tbl_department)
DROP TABLE IF EXISTS tbl_department;
CREATE TABLE tbl_department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '部門ID',
    dept_code VARCHAR(50) NOT NULL COMMENT '部門コード',
    dept_name VARCHAR(100) NOT NULL COMMENT '部門名',
    region VARCHAR(50) DEFAULT NULL COMMENT '所在地',
    parent_id BIGINT DEFAULT NULL COMMENT '親部門ID',
    sort_order INT DEFAULT 0 COMMENT '並び順',
    status TINYINT DEFAULT 1 COMMENT '状態(0=無効,1=有効)',
    version BIGINT DEFAULT 0 COMMENT '楽観的ロック用バージョン',
    create_by VARCHAR(50) DEFAULT NULL COMMENT '作成者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    update_by VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    UNIQUE KEY uk_dept_code (dept_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部門表';

-- 社員表 (tbl_employee)
DROP TABLE IF EXISTS tbl_employee;
CREATE TABLE tbl_employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '社員ID',
    emp_no VARCHAR(50) NOT NULL COMMENT '社員番号',
    emp_name VARCHAR(100) NOT NULL COMMENT '氏名',
    gender VARCHAR(10) NOT NULL COMMENT '性別(MALE/FEMALE)',
    nationality VARCHAR(30) NOT NULL COMMENT '国籍',
    dept_id BIGINT DEFAULT NULL COMMENT '所属部門ID',
    first_use_date DATE DEFAULT NULL COMMENT '初回利用日(日本社員のみ)',
    phone VARCHAR(20) DEFAULT NULL COMMENT '電話番号',
    email VARCHAR(100) DEFAULT NULL COMMENT 'メールアドレス',
    hire_date DATE NOT NULL COMMENT '入社日',
    resign_date DATE DEFAULT NULL COMMENT '退職日',
    emp_status TINYINT DEFAULT 1 COMMENT '状態(0=退職,1=在職)',
    version BIGINT DEFAULT 0 COMMENT '楽観的ロック用バージョン',
    create_by VARCHAR(50) DEFAULT NULL COMMENT '作成者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    update_by VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    UNIQUE KEY uk_emp_no (emp_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社員表';

-- 寮マスタ (tbl_dormitory)
DROP TABLE IF EXISTS tbl_dormitory;
CREATE TABLE tbl_dormitory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '寮ID',
    dorm_name VARCHAR(100) NOT NULL COMMENT '寮名称',
    region VARCHAR(50) NOT NULL COMMENT '所在地域',
    address VARCHAR(255) DEFAULT NULL COMMENT '住所',
    dorm_condition VARCHAR(10) DEFAULT 'ANY' COMMENT '入居条件(MALE/FEMALE/ANY)',
    total_rooms INT DEFAULT 0 COMMENT '総部屋数',
    memo1 VARCHAR(255) DEFAULT NULL COMMENT 'メモ1',
    memo2 VARCHAR(255) DEFAULT NULL COMMENT 'メモ2',
    memo3 VARCHAR(255) DEFAULT NULL COMMENT 'メモ3',
    status TINYINT DEFAULT 1 COMMENT '状態(0=閉鎖,1=運用中)',
    version BIGINT DEFAULT 0 COMMENT '楽観的ロック用バージョン',
    create_by VARCHAR(50) DEFAULT NULL COMMENT '作成者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    update_by VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='寮マスタ';

-- 部屋マスタ (tbl_room)
DROP TABLE IF EXISTS tbl_room;
CREATE TABLE tbl_room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '部屋ID',
    dormitory_id BIGINT NOT NULL COMMENT '所属寮ID',
    room_number VARCHAR(20) NOT NULL COMMENT '部屋番号',
    capacity INT DEFAULT 1 COMMENT '定員',
    current_occupancy INT DEFAULT 0 COMMENT '現在入居数',
    daily_rate DECIMAL(10,2) DEFAULT 0.00 COMMENT '日額寮費',
    room_type TINYINT DEFAULT 0 COMMENT '室区分(0=不明,1=和室,2=洋室)',
    ac_type TINYINT DEFAULT 0 COMMENT 'エアコン区分(0=なし,1=あり)',
    memo1 VARCHAR(255) DEFAULT NULL COMMENT 'メモ1',
    memo2 VARCHAR(255) DEFAULT NULL COMMENT 'メモ2',
    memo3 VARCHAR(255) DEFAULT NULL COMMENT 'メモ3',
    status TINYINT DEFAULT 1 COMMENT '状態(0=利用不可,1=空室,2=入居中,3=予約済,4=清掃中)',
    version BIGINT DEFAULT 0 COMMENT '楽観的ロック用バージョン',
    create_by VARCHAR(50) DEFAULT NULL COMMENT '作成者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    update_by VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    UNIQUE KEY uk_dorm_room (dormitory_id, room_number),
    CONSTRAINT fk_room_dormitory FOREIGN KEY (dormitory_id) REFERENCES tbl_dormitory(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部屋マスタ';


-- ============================================================
-- 3. トランザクション系テーブル
-- ============================================================

-- 入居履歴 (tbl_resident_record)
DROP TABLE IF EXISTS tbl_resident_record;
CREATE TABLE tbl_resident_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '入居履歴ID',
    employee_id BIGINT NOT NULL COMMENT '社員ID',
    room_id BIGINT NOT NULL COMMENT '部屋ID',
    checkin_date DATE NOT NULL COMMENT '入居日',
    planned_checkout_date DATE DEFAULT NULL COMMENT '退寮予定日',
    checkout_date DATE DEFAULT NULL COMMENT '退去日',
    checkout_reason VARCHAR(200) DEFAULT NULL COMMENT '退去理由',
    is_active TINYINT DEFAULT 1 COMMENT '有効フラグ(0=退去済,1=入居中)',
    notes VARCHAR(500) DEFAULT NULL COMMENT '備考',
    total_fee DECIMAL(12,2) DEFAULT NULL COMMENT '利用料金合計(退寮時に計算・設定)',
    status VARCHAR(20) DEFAULT 'CHECKED_IN' COMMENT 'ステータス(DRAFT/SUBMITTED/APPROVED/CHECKED_IN/CHECKED_OUT)',
    version BIGINT DEFAULT 0 COMMENT '楽観的ロック用バージョン',
    create_by VARCHAR(50) DEFAULT NULL COMMENT '作成者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    update_by VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    CONSTRAINT fk_resident_employee FOREIGN KEY (employee_id) REFERENCES tbl_employee(id),
    CONSTRAINT fk_resident_room FOREIGN KEY (room_id) REFERENCES tbl_room(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入居履歴';

-- 寮費設定テーブル (tbl_billing)
DROP TABLE IF EXISTS tbl_billing;
CREATE TABLE tbl_billing (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '料金設定ID',
    room_id BIGINT NOT NULL COMMENT '部屋ID',
    daily_rate DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '一日料金',
    memo1 VARCHAR(255) DEFAULT NULL COMMENT 'メモ1',
    memo2 VARCHAR(255) DEFAULT NULL COMMENT 'メモ2',
    memo3 VARCHAR(255) DEFAULT NULL COMMENT 'メモ3',
    version BIGINT DEFAULT 0 COMMENT '楽観的ロック用バージョン',
    create_by VARCHAR(50) DEFAULT NULL COMMENT '作成者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    update_by VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    CONSTRAINT fk_billing_room FOREIGN KEY (room_id) REFERENCES tbl_room(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='寮費設定テーブル';


-- ============================================================
-- 4. 監査・ログ系テーブル
-- ============================================================

-- 操作ログ (tbl_operation_log)

-- 操作ログ (tbl_operation_log)
DROP TABLE IF EXISTS tbl_operation_log;
CREATE TABLE tbl_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ログID',
    operator_id VARCHAR(50) DEFAULT NULL COMMENT '操作者ID',
    operation_type VARCHAR(30) NOT NULL COMMENT '操作タイプ(LOGIN/CHECKIN/CHECKOUT/BILLING_CONFIRM/EXPORT等)',
    target_type VARCHAR(50) DEFAULT NULL COMMENT '対象種別',
    target_id VARCHAR(50) DEFAULT NULL COMMENT '対象ID',
    description VARCHAR(500) DEFAULT NULL COMMENT '操作説明',
    client_ip VARCHAR(45) DEFAULT NULL COMMENT 'クライアントIP',
    trace_id VARCHAR(64) DEFAULT NULL COMMENT 'トレースID',
    request_url VARCHAR(255) DEFAULT NULL COMMENT 'リクエストURL',
    http_method VARCHAR(10) DEFAULT NULL COMMENT 'HTTPメソッド',
    request_params TEXT DEFAULT NULL COMMENT 'リクエストパラメータ(JSON)',
    before_json TEXT DEFAULT NULL COMMENT '変更前データ(JSON)',
    after_json TEXT DEFAULT NULL COMMENT '変更後データ(JSON)',
    result_status TINYINT DEFAULT 1 COMMENT '結果(0=失敗,1=成功)',
    version BIGINT DEFAULT 0 COMMENT '楽観的ロック用バージョン',
    create_by VARCHAR(50) DEFAULT NULL COMMENT '作成者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    update_by VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作ログ';

-- インポートログ (tbl_import_log)
DROP TABLE IF EXISTS tbl_import_log;
CREATE TABLE tbl_import_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'インポートログID',
    file_name VARCHAR(255) NOT NULL COMMENT 'ファイル名',
    import_type VARCHAR(50) NOT NULL COMMENT 'インポート種別(DORMITORY/ROOM/EMPLOYEE/RESIDENT/BILLING)',
    total_count INT DEFAULT 0 COMMENT '総件数',
    success_count INT DEFAULT 0 COMMENT '成功件数',
    error_count INT DEFAULT 0 COMMENT 'エラー件数',
    error_details TEXT DEFAULT NULL COMMENT 'エラー詳細(JSON)',
    import_status TINYINT DEFAULT 0 COMMENT '状態(0=処理中,1=成功,2=一部失敗,3=失敗)',
    operator_id VARCHAR(50) DEFAULT NULL COMMENT '操作者ID',
    version BIGINT DEFAULT 0 COMMENT '楽観的ロック用バージョン',
    create_by VARCHAR(50) DEFAULT NULL COMMENT '作成者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    update_by VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='インポートログ';


-- ============================================================
-- 5. サンプルデータ
-- ============================================================

-- 5-1. ユーザー
INSERT INTO tbl_user (userid, password, userName, enabled, create_by, create_time) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'システム管理者', TRUE, 'system', NOW()),
('yamada', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '山田太郎', TRUE, 'system', NOW()),
('tanaka', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '田中花子', TRUE, 'system', NOW()),
('sato', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '佐藤次郎', TRUE, 'system', NOW()),
('zhang', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '張偉', TRUE, 'system', NOW()),
('li', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李娜', TRUE, 'system', NOW());

-- 5-2. 部門
INSERT INTO tbl_department (dept_code, dept_name, region, parent_id, sort_order, status, create_by, create_time) VALUES
('DEPT-001', '東京本社', '東京', NULL, 1, 1, 'system', NOW()),
('DEPT-002', '大阪支社', '大阪', NULL, 2, 1, 'system', NOW()),
('DEPT-003', '名古屋支社', '名古屋', NULL, 3, 1, 'system', NOW()),
('DEPT-004', '福岡支社', '福岡', NULL, 4, 1, 'system', NOW()),
('DEPT-005', '札幌営業所', '北海道', NULL, 5, 1, 'system', NOW()),
('DEPT-101', '東京本社 営業部', '東京', 1, 1, 1, 'system', NOW()),
('DEPT-102', '東京本社 開発部', '東京', 1, 2, 1, 'system', NOW()),
('DEPT-201', '大阪支社 営業部', '大阪', 2, 1, 1, 'system', NOW());

-- 5-3. 社員
INSERT INTO tbl_employee (emp_no, emp_name, gender, nationality, dept_id, first_use_date, phone, email, hire_date, emp_status, create_by, create_time) VALUES
-- 日本社員（正社員）
('EMP-00001', '山田太郎', 'MALE', '日本', 101, '2020-06-01', '090-1111-1111', 'yamada.taro@example.com', '2020-04-01', 1, 'system', NOW()),
('EMP-00002', '田中花子', 'FEMALE', '日本', 101, '2021-07-15', '090-2222-2222', 'tanaka.hanako@example.com', '2021-04-01', 1, 'system', NOW()),
('EMP-00003', '佐藤次郎', 'MALE', '日本', 102, NULL, '090-3333-3333', 'sato.jiro@example.com', '2019-04-01', 1, 'system', NOW()),
('EMP-00004', '鈴木一郎', 'MALE', '日本', 102, '2022-04-01', '090-4444-4444', 'suzuki.ichiro@example.com', '2022-04-01', 1, 'system', NOW()),
('EMP-00005', '高橋美咲', 'FEMALE', '日本', 101, NULL, '090-5555-5555', 'takahashi.misaki@example.com', '2023-04-01', 1, 'system', NOW()),
-- 日本社員（契約社員）
('EMP-00006', '渡辺健一', 'MALE', '日本', 201, '2023-10-01', '090-6666-6666', 'watanabe.kenichi@example.com', '2023-10-01', 1, 'system', NOW()),
('EMP-00007', '伊藤さくら', 'FEMALE', '日本', 201, NULL, '090-7777-7777', 'ito.sakura@example.com', '2024-01-15', 1, 'system', NOW()),
-- 中国出張社員（短期出張）
('EMP-00008', '張偉', 'MALE', '中国', 102, NULL, '080-1111-0001', 'zhangwei@example.com', '2025-01-01', 1, 'system', NOW()),
('EMP-00009', '王芳', 'FEMALE', '中国', 101, NULL, '080-1111-0002', 'wangfang@example.com', '2025-03-01', 1, 'system', NOW()),
-- 中国出張社員（中長期出張）
('EMP-00010', '李娜', 'FEMALE', '中国', 102, NULL, '080-1111-0003', 'lina@example.com', '2024-06-01', 1, 'system', NOW()),
('EMP-00011', '劉強', 'MALE', '中国', 201, NULL, '080-1111-0004', 'liuqiang@example.com', '2024-09-01', 1, 'system', NOW()),
-- 退職者
('EMP-00012', '小林大輔', 'MALE', '日本', 102, '2019-06-01', '090-8888-8888', 'kobayashi.daisuke@example.com', '2018-04-01', 0, 'system', NOW());

-- 5-4. 寮
INSERT INTO tbl_dormitory (dorm_name, region, address, dorm_condition, total_rooms, status, create_by, create_time) VALUES
('東京第一寮', '東京', '東京都港区芝浦3-1-1', 'MALE', 6, 1, 'system', NOW()),
('東京第二寮', '東京', '東京都新宿区西新宿2-2-2', 'FEMALE', 4, 1, 'system', NOW()),
('大阪第一寮', '大阪', '大阪市北区梅田1-3-3', 'MALE', 4, 1, 'system', NOW()),
('大阪第二寮', '大阪', '大阪市中央区難波4-4-4', 'FEMALE', 3, 1, 'system', NOW()),
('名古屋寮', '名古屋', '名古屋市中村区名駅5-5-5', 'ANY', 3, 1, 'system', NOW());

-- 5-5. 部屋
INSERT INTO tbl_room (dormitory_id, room_number, capacity, current_occupancy, daily_rate, room_type, ac_type, memo1, memo2, memo3, status, create_by, create_time) VALUES
-- 東京第一寮（男性寮）
(1, '101', 2, 1, 1500.00, 0, 1, '南向き', NULL, NULL, 2, 'system', NOW()),
(1, '102', 2, 1, 1500.00, 0, 1, NULL, NULL, NULL, 2, 'system', NOW()),
(1, '103', 1, 0, 1800.00, 1, 0, '和室', NULL, NULL, 1, 'system', NOW()),
(1, '201', 2, 0, 1500.00, 0, 1, NULL, NULL, NULL, 1, 'system', NOW()),
(1, '202', 2, 1, 1500.00, 0, 1, NULL, NULL, NULL, 2, 'system', NOW()),
(1, '203', 1, 0, 1800.00, 2, 1, '洋室', '角部屋', NULL, 1, 'system', NOW()),
-- 東京第二寮（女性寮）
(2, '101', 1, 0, 1200.00, 0, 1, NULL, NULL, NULL, 1, 'system', NOW()),
(2, '102', 1, 1, 1200.00, 0, 0, NULL, NULL, NULL, 2, 'system', NOW()),
(2, '103', 2, 0, 1300.00, 1, 1, '和室', '広め', NULL, 1, 'system', NOW()),
(2, '201', 1, 1, 1200.00, 2, 1, '洋室', NULL, NULL, 2, 'system', NOW()),
-- 大阪第一寮（男性寮）
(3, '101', 2, 1, 1300.00, 0, 1, NULL, NULL, NULL, 2, 'system', NOW()),
(3, '102', 1, 1, 1500.00, 0, 1, NULL, NULL, NULL, 2, 'system', NOW()),
(3, '103', 2, 0, 1300.00, 0, 0, NULL, NULL, NULL, 1, 'system', NOW()),
(3, '201', 1, 1, 1500.00, 0, 1, NULL, NULL, NULL, 2, 'system', NOW()),
-- 大阪第二寮（女性寮）
(4, '101', 1, 1, 1100.00, 0, 1, NULL, NULL, NULL, 2, 'system', NOW()),
(4, '102', 1, 0, 1100.00, 0, 0, NULL, NULL, NULL, 1, 'system', NOW()),
(4, '103', 2, 0, 1200.00, 0, 1, NULL, NULL, NULL, 1, 'system', NOW()),
-- 名古屋寮（ANY）
(5, '301', 1, 0, 1000.00, 0, 1, NULL, NULL, NULL, 1, 'system', NOW()),
(5, '302', 2, 0, 1100.00, 1, 0, '和室', NULL, NULL, 1, 'system', NOW()),
(5, '303', 1, 0, 1000.00, 2, 1, '洋室', NULL, NULL, 1, 'system', NOW());

-- 5-6. 入居履歴
INSERT INTO tbl_resident_record (employee_id, room_id, checkin_date, checkout_date, checkout_reason, is_active, notes, total_fee, status, create_by, create_time) VALUES
-- 現在入居中
(1, 1, '2020-06-01', NULL, NULL, 1, '東京本社 営業部', NULL, 'CHECKED_IN', 'system', NOW()),
(2, 9, '2021-07-15', NULL, NULL, 1, '東京本社 営業部', NULL, 'CHECKED_IN', 'system', NOW()),
(3, 7, '2019-06-01', NULL, NULL, 1, '東京本社 開発部', NULL, 'CHECKED_IN', 'system', NOW()),
(4, 2, '2022-04-01', NULL, NULL, 1, '東京本社 開発部', NULL, 'CHECKED_IN', 'system', NOW()),
(6, 11, '2023-10-01', NULL, NULL, 1, '大阪支社 営業部', NULL, 'CHECKED_IN', 'system', NOW()),
(7, 14, '2024-01-15', NULL, NULL, 1, '大阪支社 営業部', NULL, 'CHECKED_IN', 'system', NOW()),
(10, 15, '2024-06-01', NULL, NULL, 1, '中国出張(中長期)', NULL, 'CHECKED_IN', 'system', NOW()),
(11, 12, '2024-09-01', NULL, NULL, 1, '中国出張(中長期)', NULL, 'CHECKED_IN', 'system', NOW()),
(8, 5, '2025-01-01', NULL, NULL, 1, '中国出張(短期)', NULL, 'CHECKED_IN', 'system', NOW()),
(9, 8, '2025-03-01', NULL, NULL, 1, '中国出張(短期)', NULL, 'CHECKED_IN', 'system', NOW()),
-- 過去の入居履歴（退去済）※ total_fee = 日額 × 入居日数
(1, 3, '2018-06-01', '2020-05-31', '部屋移動のため', 0, '201号室へ移動', 1314000.00, 'CHECKED_OUT', 'system', NOW()),
(12, 7, '2019-06-01', '2023-03-31', '退職のため', 0, '退職に伴う退去', 1678800.00, 'CHECKED_OUT', 'system', NOW()),
(5, 4, '2023-04-01', '2024-03-31', '転勤のため', 0, '大阪転勤のため退去', 547500.00, 'CHECKED_OUT', 'system', NOW());

-- 5-7. 寮費設定データ
INSERT INTO tbl_billing (room_id, daily_rate, memo1, create_by, create_time) VALUES
(1, 1500.00, '東京第一寮 101号室', 'system', NOW()),
(2, 1500.00, '東京第一寮 102号室', 'system', NOW()),
(3, 1800.00, '東京第一寮 103号室', 'system', NOW()),
(4, 1500.00, '東京第一寮 201号室', 'system', NOW()),
(5, 1500.00, '東京第一寮 202号室', 'system', NOW()),
(6, 1800.00, '東京第一寮 203号室', 'system', NOW()),
(7, 1200.00, '東京第二寮 101号室', 'system', NOW()),
(8, 1200.00, '東京第二寮 102号室', 'system', NOW()),
(9, 1300.00, '東京第二寮 103号室', 'system', NOW()),
(10, 1200.00, '東京第二寮 201号室', 'system', NOW()),
(11, 1300.00, '大阪第一寮 101号室', 'system', NOW()),
(12, 1500.00, '大阪第一寮 102号室', 'system', NOW()),
(13, 1300.00, '大阪第一寮 103号室', 'system', NOW()),
(14, 1500.00, '大阪第一寮 201号室', 'system', NOW()),
(15, 1100.00, '大阪第二寮 101号室', 'system', NOW()),
(16, 1100.00, '大阪第二寮 102号室', 'system', NOW()),
(17, 1200.00, '大阪第二寮 103号室', 'system', NOW()),
(18, 1000.00, '名古屋寮 301号室', 'system', NOW()),
(19, 1100.00, '名古屋寮 302号室', 'system', NOW()),
(20, 1000.00, '名古屋寮 303号室', 'system', NOW());

-- 5-8. 操作ログ
INSERT INTO tbl_operation_log (operator_id, operation_type, target_type, target_id, description, client_ip, trace_id, result_status, create_by, create_time) VALUES
('admin', 'LOGIN', NULL, NULL, '管理者ログイン', '192.168.1.100', 'TRACE-001', 1, 'system', NOW()),
('yamada', 'LOGIN', NULL, NULL, '山田太郎ログイン', '192.168.1.101', 'TRACE-002', 1, 'system', NOW()),
('admin', 'CHECKIN', 'RESIDENT_RECORD', '1', '山田太郎 入居処理', '192.168.1.100', 'TRACE-003', 1, 'system', NOW()),
('admin', 'CHECKOUT', 'RESIDENT_RECORD', '12', '小林大輔 退去処理', '192.168.1.100', 'TRACE-004', 1, 'system', NOW()),
('admin', 'BILLING_CONFIRM', 'BILLING', '1', '山田太郎 5月分寮費確定', '192.168.1.100', 'TRACE-005', 1, 'system', NOW()),
('admin', 'EXPORT', 'BILLING', NULL, '寮費データCSV出力', '192.168.1.100', 'TRACE-006', 1, 'system', NOW()),
('admin', 'LOGIN', NULL, NULL, '管理者ログイン(失敗)', '10.0.0.1', 'TRACE-007', 0, 'system', '2026-06-13 08:00:00'),
('sato', 'LOGIN', NULL, NULL, '佐藤次郎ログイン', '192.168.1.102', 'TRACE-008', 1, 'system', '2026-06-13 09:00:00');


