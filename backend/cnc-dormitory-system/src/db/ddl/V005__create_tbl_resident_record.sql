-- ============================================================
-- DDL: 入居履歴 (tbl_resident_record)
-- バージョン: V005
-- 説明: 社員の入寮・退寮履歴を管理するテーブル
--        checkout_date が NULL の場合は 9999-12-31 として扱う
--        重複入居禁止の制約はアプリケーション層でチェック
-- 文字セット: utf8mb4  エンジン: InnoDB
-- 設計根拠: CNC-DMS_DD.md §2.1, §5
-- ============================================================
DROP TABLE IF EXISTS tbl_resident_record;

CREATE TABLE tbl_resident_record (
    id              BIGINT          NOT NULL AUTO_INCREMENT          COMMENT '入居履歴ID(主キー)',
    employee_id     BIGINT          NOT NULL                         COMMENT '社員ID(tbl_employee.id)',
    room_id         BIGINT          NOT NULL                         COMMENT '部屋ID(tbl_room.id)',
    checkin_date    DATE            NOT NULL                         COMMENT '入寮日',
    planned_checkout_date DATE      DEFAULT NULL                     COMMENT '退寮予定日',
    checkout_date   DATE            DEFAULT NULL                     COMMENT '退寮日(NULL=入居中, 9999-12-31として扱う)',
    is_active       TINYINT         DEFAULT 1                        COMMENT '現在入居中か: 1-入居中, 0-退寮済',
    notes           VARCHAR(500)    DEFAULT NULL                     COMMENT '備考',
    total_fee       DECIMAL(12,2)   DEFAULT NULL                     COMMENT '利用料金合計(退寮時に計算・設定)',
    version         BIGINT          DEFAULT 0                        COMMENT '楽観ロックバージョン(§6.1)',
    create_by       VARCHAR(50)     DEFAULT NULL                     COMMENT '作成者',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP        COMMENT '作成日時',
    update_by       VARCHAR(50)     DEFAULT NULL                     COMMENT '更新者',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP      COMMENT '更新日時(自動更新)',
    PRIMARY KEY (id),
    KEY idx_employee_id (employee_id),
    KEY idx_room_id (room_id),
    KEY idx_record_date (checkin_date, checkout_date),
    KEY idx_room_overlap (room_id, checkin_date, checkout_date),
    KEY idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入居履歴(重複入居禁止チェック用の複合索引あり)';
