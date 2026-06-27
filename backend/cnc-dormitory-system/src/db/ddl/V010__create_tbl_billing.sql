-- ============================================================
-- DDL: 寮費請求テーブル (tbl_billing)
-- 版本: V010
-- 说明: 入居者ごとの月次寮費請求を管理
-- ============================================================
DROP TABLE IF EXISTS tbl_billing;

CREATE TABLE tbl_billing (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '寮費ID',
    resident_record_id BIGINT NOT NULL COMMENT '入居履歴ID',
    billing_month VARCHAR(7) NOT NULL COMMENT '請求対象年月(yyyy-MM)',
    days_count INT NOT NULL DEFAULT 0 COMMENT '対象日数',
    daily_rate DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '該当期間の日額',
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '金額',
    status TINYINT DEFAULT 0 COMMENT '状態(0=未計算,1=計算済,2=確認済,3=確定)',
    notes VARCHAR(500) DEFAULT NULL COMMENT '備考',
    version BIGINT DEFAULT 0 COMMENT '楽観的ロック用バージョン',
    create_by VARCHAR(50) DEFAULT NULL COMMENT '作成者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    update_by VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    CONSTRAINT fk_billing_resident FOREIGN KEY (resident_record_id) REFERENCES tbl_resident_record(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='寮費請求テーブル';
