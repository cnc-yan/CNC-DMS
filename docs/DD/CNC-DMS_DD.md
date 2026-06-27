# CNC-DMS 寮管理システム 詳細設計仕様書

## 1. システム構成設計

本システムは、保守性・拡張性・運用性を考慮し、前後端分離アーキテクチャを採用する。

### 1.1 技術スタック

#### バックエンド
- Java 21
- Spring Boot 3.5
- MyBatis
- MySQL 8
- Redis
- Spring Security

#### フロントエンド
- Vue 3 (Composition API)
- Vite
- Axios
- Element Plus
- ECharts

#### インフラ
- AWS Lightsail
- Nginx
- Spring Boot Jar
- MySQL 8

### 1.2 ディレクトリ構成

#### バックエンド
```text
com.cnc.dms
├── controller
├── service
├── mapper
├── entity
├── dto
├── vo
├── config
├── exception
└── util
```

#### フロントエンド
```text
src
├── api
├── assets
├── components
├── layout
├── router
├── store
└── views
```

## 2. データベース詳細設計

全テーブルに監査フィールド(create_by, create_time, update_by, update_time)を保持する。

### 2.1 主要テーブル定義
- tbl_user（ユーザーマスタ）
- tbl_dormitory（寮マスタ）
- tbl_room（部屋マスタ）
- tbl_employee（社員マスタ）
- tbl_resident_record（入居履歴）
- tbl_department（所属マスタ）
- tbl_change_history（変更履歴）

### 2.2 索引設計
- idx_emp_no
- idx_record_date
- idx_room_overlap

## 3. APIインターフェース設計

レスポンス形式:

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

### 認証系
- POST /api/auth/login
- GET /api/auth/me

### 業務系
- GET /api/dormitories
- POST /api/resident/record
- PUT /api/resident/record/{id}
- POST /api/csv/import

## 4. 画面詳細設計

### 寮割カレンダー画面
- 地域フィルタ
- 月切替
- 黄色セル表示
- 責任者★表示
- 14日以内退寮警告

## 5. 業務ロジック・制約設計

### 重複入居禁止

```sql
new.checkin_date <= old.checkout_date
AND new.checkout_date >= old.checkin_date
```

### 日付管理
- DATE型使用
- NULL → 9999-12-31

### 寮費計算

```text
(退寮日 - 入寮日 + 1) × 部屋単価
```

## 6. 非機能要件

### 楽観ロック
tbl_resident_record.version を利用

### セキュリティ
JWT認証

### ログ管理
tbl_change_history にJSON形式で保存
