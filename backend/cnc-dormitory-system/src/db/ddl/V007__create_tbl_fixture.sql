-- ============================================================
-- DDL: 備品マスタ (tbl_fixture)
-- 版本: V007
-- 说明: 管理寮内備品のマスタ情報
-- ============================================================
DROP TABLE IF EXISTS tbl_fixture;

CREATE TABLE tbl_fixture (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '備品ID',
    fixture_code VARCHAR(50) NOT NULL COMMENT '備品コード',
    fixture_name VARCHAR(100) NOT NULL COMMENT '備品名称',
    fixture_type VARCHAR(50) NOT NULL COMMENT '備品種別(FURNITURE/APPLIANCE/BEDDING/KITCHEN/OTHER)',
    unit VARCHAR(20) DEFAULT '個' COMMENT '単位',
    unit_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '単価',
    description VARCHAR(500) DEFAULT NULL COMMENT '備考',
    status TINYINT DEFAULT 1 COMMENT '状態(0=廃止,1=有効)',
    version BIGINT DEFAULT 0 COMMENT '楽観的ロック用バージョン',
    create_by VARCHAR(50) DEFAULT NULL COMMENT '作成者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    update_by VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    UNIQUE KEY uk_fixture_code (fixture_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='備品マスタ';
