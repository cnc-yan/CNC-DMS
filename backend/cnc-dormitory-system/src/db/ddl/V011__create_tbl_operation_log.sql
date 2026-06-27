-- ============================================================
-- DDL: 操作ログ (tbl_operation_log)
-- 版本: V011
-- 说明: ユーザー操作の監査ログ
-- ============================================================
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
