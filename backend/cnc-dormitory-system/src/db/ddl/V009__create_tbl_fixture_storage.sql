-- ============================================================
-- DDL: 備品保管管理 (tbl_fixture_storage)
-- 版本: V009
-- 说明: 備品の保管場所・在庫数を管理
-- ============================================================
DROP TABLE IF EXISTS tbl_fixture_storage;

CREATE TABLE tbl_fixture_storage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '保管ID',
    fixture_id BIGINT NOT NULL COMMENT '備品ID',
    storage_location VARCHAR(100) NOT NULL COMMENT '保管場所',
    quantity INT NOT NULL DEFAULT 0 COMMENT '保管数',
    status TINYINT DEFAULT 1 COMMENT '状態(1=保管中,2=使用中,3=廃棄済)',
    notes VARCHAR(500) DEFAULT NULL COMMENT '備考',
    version BIGINT DEFAULT 0 COMMENT '楽観的ロック用バージョン',
    create_by VARCHAR(50) DEFAULT NULL COMMENT '作成者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    update_by VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    CONSTRAINT fk_storage_fixture FOREIGN KEY (fixture_id) REFERENCES tbl_fixture(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='備品保管管理';
