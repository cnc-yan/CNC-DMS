-- ============================================================
-- DDL: 入居時備品準備 (tbl_fixture_inventory)
-- 版本: V008
-- 说明: 入居者ごとの備品準備状況を管理
-- ============================================================
DROP TABLE IF EXISTS tbl_fixture_inventory;

CREATE TABLE tbl_fixture_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '備品在庫ID',
    resident_record_id BIGINT NOT NULL COMMENT '入居履歴ID',
    fixture_id BIGINT NOT NULL COMMENT '備品ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    preparation_status TINYINT DEFAULT 0 COMMENT '準備状況(0=未準備,1=準備済,2=設置済)',
    notes VARCHAR(500) DEFAULT NULL COMMENT '備考',
    version BIGINT DEFAULT 0 COMMENT '楽観的ロック用バージョン',
    create_by VARCHAR(50) DEFAULT NULL COMMENT '作成者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    update_by VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    CONSTRAINT fk_inventory_resident FOREIGN KEY (resident_record_id) REFERENCES tbl_resident_record(id),
    CONSTRAINT fk_inventory_fixture FOREIGN KEY (fixture_id) REFERENCES tbl_fixture(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入居時備品準備';
