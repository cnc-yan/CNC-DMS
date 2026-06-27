# CNC-DMS 寮管理システム API 仕様書

## 共通仕様

### Base URL
```
http://localhost:8080
```

### 認証方式
- **JWT Bearer Token**
- ログイン API で取得したトークンを `Authorization: Bearer <token>` ヘッダーに設定
- 認証不要パス: `/api/auth/**`, `/test/**`

### 共通レスポンス形式

#### 成功レスポンス
```json
{
  "success": true,
  "message": "操作成功",
  "error": null,
  "body": { ... }
}
```

#### 失敗レスポンス
```json
{
  "success": false,
  "message": "エラーメッセージ",
  "error": "詳細エラー情報（省略可）",
  "body": null
}
```

#### ページングレスポンス（body内）
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "total": 100,
  "totalPage": 10,
  "list": [ ... ]
}
```

### HTTP ステータスコード
| コード | 説明 |
|--------|------|
| 200 | 成功 |
| 400 | バリデーションエラー / 業務例外 |
| 401 | 未認証（トークンなし/期限切れ） |
| 403 | 認可エラー（権限不足） |
| 404 | リソース未検出 |
| 409 | 楽観ロック競合 |
| 500 | サーバー内部エラー |

### 日付形式
- **日付**: `yyyy-MM-dd`（例: `2026-06-13`）
- **日時**: `yyyy-MM-dd'T'HH:mm:ss`（例: `2026-06-13T10:30:00`）

---

## 1. 認証 API

### 1.1 ログイン

ユーザー認証を行い、JWT トークンを取得する。

```
POST /api/auth/login
```

**認証**: 不要

**Request Body**:
```json
{
  "userid": "admin",
  "password": "admin123"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| userid | String | ✅ | ログインID |
| password | String | ✅ | パスワード |

**Response (成功)**:
```json
{
  "success": true,
  "message": "操作成功",
  "body": {
    "success": true,
    "message": "登录成功",
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

**Response (失敗)**:
```json
{
  "success": false,
  "message": "用户不存在",
  "error": null,
  "body": null
}
```

---

### 1.2 現在のユーザー情報取得

ログイン中のユーザー情報を取得する。

```
GET /api/auth/me
```

**認証**: 不要（トークンなしの場合は未認応としてレスポンス）

**Response (認証済み)**:
```json
{
  "success": true,
  "message": "操作成功",
  "body": {
    "username": "admin"
  }
}
```

**Response (未認証)**:
```json
{
  "success": false,
  "message": "未登录或Token已过期",
  "body": {
    "username": null
  }
}
```

---

## 2. ユーザー管理 API

### 2.1 ユーザー検索（単一）

ユーザーIDを指定してユーザー情報を取得する。

```
GET /api/user?user_id={user_id}
```

**認証**: 要 JWT


| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| user_id | String | ✅ | ユーザーID |

**Response**:
```json
{
  "success": true,
  "message": "查询成功",
  "body": {
    "userid": "admin",
    "password": "admin123",
    "userName": "管理者",
    "enabled": true,
    "createBy": "system",
    "createTime": "2026-01-01T00:00:00",
    "updateBy": "system",
    "updateTime": "2026-06-13T10:00:00"
  }
}
```

---

### 2.2 ユーザー一覧検索

条件を指定してユーザーをページング検索する。

```
GET /api/user/list?userid=&userName=&enabled=&pageNum=1&pageSize=10
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| userid | String | - | ユーザーID（部分一致） |
| userName | String | - | ユーザー名（部分一致） |
| enabled | Boolean | - | 有効/無効 |
| pageNum | Integer | - | ページ番号（デフォルト: 1） |
| pageSize | Integer | - | 1ページの件数（デフォルト: 10） |

**Response**:
```json
{
  "success": true,
  "message": "查询成功",
  "body": {
    "pageNum": 1,
    "pageSize": 10,
    "total": 2,
    "totalPage": 1,
    "list": [
      {
        "userid": "admin",
        "userName": "管理者",
        "enabled": true,
        "createTime": "2026-01-01T00:00:00"
      }
    ]
  }
}
```

---

### 2.3 ユーザー作成

新規ユーザーを作成する。

```
POST /api/user
```

**認証**: 要 JWT

**Request Body**:
```json
{
  "userid": "newuser",
  "password": "password123",
  "userName": "新規ユーザー",
  "enabled": true,
  "createBy": "admin"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| userid | String | ✅ | ユーザーID（一意） |
| password | String | ✅ | パスワード |
| userName | String | ✅ | ユーザー名 |
| enabled | Boolean | - | 有効状態（デフォルト: true） |
| createBy | String | - | 作成者 |

**Response**: `Result<String>`
```json
{
  "success": true,
  "message": "用户创建成功",
  "body": "成功"
}
```

---

### 2.4 ユーザー更新

ユーザー情報を更新する。

```
PUT /api/user/update
```

**認証**: 要 JWT

**Request Body**:
```json
{
  "userid": "newuser",
  "userName": "更新ユーザー",
  "enabled": true,
  "updateBy": "admin"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| userid | String | ✅ | ユーザーID |
| userName | String | - | ユーザー名 |
| enabled | Boolean | - | 有効状態 |
| updateBy | String | - | 更新者 |

**Response**: `Result<String>`

---

### 2.5 ユーザー削除（単一/一括）

ユーザーを削除する。複数指定で一括削除。

```
DELETE /api/user?user_id=admin&user_id=test
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| user_id | String[] | ✅ | 削除するユーザーID（1個以上） |

**Response**: `Result<String>`
```json
{
  "success": true,
  "message": "已删除 2 个用户",
  "body": "成功"
}
```

---

## 3. 部門管理 API

### 3.1 部門検索（単一）

```
GET /api/departments/{id}
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id | Long | ✅ | 部門ID |

**Response**: `Result<Department>`
```json
{
  "success": true,
  "body": {
    "id": 1,
    "deptCode": "DEPT-001",
    "deptName": "東京本社",
    "region": "東京",
    "parentId": null,
    "sortOrder": 1,
    "status": 1,
    "version": 0,
    "createBy": "system",
    "createTime": "2026-01-01T00:00:00",
    "updateBy": null,
    "updateTime": "2026-01-01T00:00:00"
  }
}
```

---

### 3.2 部門一覧検索

```
GET /api/departments/list?deptCode=&deptName=&region=&status=&pageNum=1&pageSize=10
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| deptCode | String | - | 部門コード（部分一致） |
| deptName | String | - | 部門名（部分一致） |
| region | String | - | 地域（完全一致） |
| status | Integer | - | 状態: 1=有効, 0=無効 |
| pageNum | Integer | - | ページ番号（デフォルト: 1） |
| pageSize | Integer | - | 1ページの件数（デフォルト: 10） |

**Response**: `Result<PageResponse<Department>>`

---

### 3.3 部門作成

```
POST /api/departments
```

**認証**: 要 JWT

**Request Body**:
```json
{
  "deptCode": "DEPT-005",
  "deptName": "札幌支社",
  "region": "北海道",
  "parentId": null,
  "sortOrder": 5,
  "createBy": "admin"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| deptCode | String | ✅ | 部門コード（一意） |
| deptName | String | ✅ | 部門名 |
| region | String | - | 地域 |
| parentId | Long | - | 親部門ID |
| sortOrder | Integer | - | 表示順 |
| createBy | String | - | 作成者 |

**Response**: `Result<String>`

---

### 3.4 部門更新

```
PUT /api/departments/update
```

**認証**: 要 JWT

**Request Body**:
```json
{
  "id": 1,
  "deptName": "東京本社（更新）",
  "region": "東京",
  "parentId": null,
  "sortOrder": 1,
  "status": 1,
  "version": 0,
  "updateBy": "admin"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id | Long | ✅ | 部門ID |
| deptName | String | - | 部門名 |
| region | String | - | 地域 |
| parentId | Long | - | 親部門ID |
| sortOrder | Integer | - | 表示順 |
| status | Integer | - | 状態 |
| version | Long | ✅ | 楽観ロック用バージョン |
| updateBy | String | - | 更新者 |

**Response**: `Result<String>`

---

### 3.5 部門削除

```
DELETE /api/departments/{id}
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id | Long | ✅ | 部門ID |

**Response**: `Result<String>`

---

## 4. 社員管理 API

### 4.1 社員検索（単一）

```
GET /api/employees/{id}
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id | Long | ✅ | 社員ID |

**Response**: `Result<Employee>`
```json
{
  "success": true,
  "body": {
    "id": 1,
    "empNo": "EMP-00001",
    "empName": "山田太郎",
    "gender": "MALE",
    "nationality": "日本",
    "deptId": 1,
    "firstUseDate": null,
    "phone": "090-1234-5678",
    "email": "taro.yamada@example.com",
    "hireDate": "2020-04-01",
    "resignDate": null,
    "empStatus": 1,
    "version": 0,
    "createBy": "system",
    "createTime": "2026-01-01T00:00:00"
  }
}
```

---

### 4.2 社員一覧検索

```
GET /api/employees/list?empNo=&empName=&deptId=&empStatus=&pageNum=1&pageSize=10
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| empNo | String | - | 工番（部分一致） |
| empName | String | - | 社員名（部分一致） |
| deptId | Long | - | 部門ID |
| empStatus | Integer | - | 状態: 1=在職, 2=離職, 3=休職中 |
| pageNum | Integer | - | ページ番号（デフォルト: 1） |
| pageSize | Integer | - | 1ページの件数（デフォルト: 10） |

**Response**: `Result<PageResponse<Employee>>`

---

### 4.3 社員作成

```
POST /api/employees
```

**認証**: 要 JWT

**Request Body**:
```json
{
  "empNo": "EMP-00003",
  "empName": "鈴木一郎",
  "gender": "MALE",
  "nationality": "日本",
  "deptId": 1,
  "hireDate": "2022-04-01",
  "phone": "090-9876-5432",
  "email": "ichiro.suzuki@example.com",
  "createBy": "admin"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| empNo | String | ✅ | 工番（一意） |
| empName | String | ✅ | 社員名 |
| gender | String | - | 性別: MALE / FEMALE |
| nationality | String | - | 国籍 |
| deptId | Long | - | 所属部門ID |
| hireDate | LocalDate | - | 入社日 |
| phone | String | - | 電話番号 |
| email | String | - | メール |
| createBy | String | - | 作成者 |

**Response**: `Result<String>`

---

### 4.4 社員更新

```
PUT /api/employees/update
```

**認証**: 要 JWT

**Request Body**:
```json
{
  "id": 1,
  "empName": "山田太郎（更新）",
  "gender": "MALE",
  "deptId": 2,
  "phone": "090-1111-2222",
  "email": "taro.yamada@example.co.jp",
  "empStatus": 1,
  "version": 0,
  "updateBy": "admin"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id | Long | ✅ | 社員ID |
| empName | String | - | 社員名 |
| gender | String | - | 性別 |
| nationality | String | - | 国籍 |
| deptId | Long | - | 所属部門ID |
| phone | String | - | 電話番号 |
| email | String | - | メール |
| empStatus | Integer | - | 状態 |
| version | Long | ✅ | 楽観ロック用バージョン |
| updateBy | String | - | 更新者 |

**Response**: `Result<String>`

---

### 4.5 社員削除

```
DELETE /api/employees/{id}
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id | Long | ✅ | 社員ID |

**Response**: `Result<String>`

---

## 5. 寮管理 API

### 5.1 寮検索（単一）

```
GET /api/dormitories/{id}
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id | Long | ✅ | 寮ID |

**Response**: `Result<Dormitory>`
```json
{
  "success": true,
  "body": {
    "id": 1,
    "dormName": "東京第一寮",
    "region": "東京",
    "address": "東京都港区...",
    "dormCondition": "MALE",
    "totalRooms": 50,
    "status": 1,
    "version": 0,
    "createBy": "system",
    "createTime": "2026-01-01T00:00:00"
  }
}
```

---

### 5.2 寮一覧検索

```
GET /api/dormitories/list?dormName=&region=&status=&pageNum=1&pageSize=10
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| dormName | String | - | 寮名（部分一致） |
| region | String | - | 地域（完全一致） |
| status | Integer | - | 状態: 1=運用中, 0=停止中 |
| pageNum | Integer | - | ページ番号（デフォルト: 1） |
| pageSize | Integer | - | 1ページの件数（デフォルト: 10） |

**Response**: `Result<PageResponse<Dormitory>>`

---

### 5.3 寮作成

```
POST /api/dormitories
```

**認証**: 要 JWT

**Request Body**:
```json
{
  "dormName": "名古屋第一寮",
  "region": "名古屋",
  "address": "名古屋市中区...",
  "dormCondition": "ANY",
  "totalRooms": 30,
  "createBy": "admin"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| dormName | String | ✅ | 寮名 |
| region | String | ✅ | 地域 |
| address | String | - | 住所 |
| dormCondition | String | - | 入居条件: MALE / FEMALE / ANY |
| totalRooms | Integer | - | 総部屋数 |
| createBy | String | - | 作成者 |

**Response**: `Result<String>`

---

### 5.4 寮更新

```
PUT /api/dormitories/update
```

**認証**: 要 JWT

**Request Body**:
```json
{
  "id": 1,
  "dormName": "東京第一寮（更新）",
  "region": "東京",
  "address": "東京都港区...",
  "dormCondition": "MALE",
  "totalRooms": 55,
  "status": 1,
  "version": 0,
  "updateBy": "admin"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id | Long | ✅ | 寮ID |
| dormName | String | - | 寮名 |
| region | String | - | 地域 |
| address | String | - | 住所 |
| dormCondition | String | - | 入居条件 |
| totalRooms | Integer | - | 総部屋数 |
| status | Integer | - | 状態 |
| version | Long | ✅ | 楽観ロック用バージョン |
| updateBy | String | - | 更新者 |

**Response**: `Result<String>`

---

### 5.5 寮削除

```
DELETE /api/dormitories/{id}
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id | Long | ✅ | 寮ID |

**Response**: `Result<String>`

---

## 6. 部屋管理 API

### 6.1 部屋検索（単一）

```
GET /api/rooms/{id}
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id | Long | ✅ | 部屋ID |

**Response**: `Result<Room>`
```json
{
  "success": true,
  "body": {
    "id": 1,
    "dormitoryId": 1,
    "roomNumber": "101",
    "floor": 1,
    "capacity": 2,
    "currentOccupancy": 0,
    "dailyRate": 1500.00,
    "roomType": 0,
    "acType": 1,
    "memo1": "南向き",
    "memo2": null,
    "memo3": null,
    "status": 1,
    "version": 0,
    "createBy": "system",
    "createTime": "2026-01-01T00:00:00"
  }
}
```

---

### 6.2 部屋一覧検索

```
GET /api/rooms/list?dormitoryId=&roomNumber=&status=&pageNum=1&pageSize=10
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| dormitoryId | Long | - | 寮ID |
| roomNumber | String | - | 部屋番号（部分一致） |
| status | Integer | - | 状態: 1=空室, 2=満室, 0=使用不可 |
| pageNum | Integer | - | ページ番号（デフォルト: 1） |
| pageSize | Integer | - | 1ページの件数（デフォルト: 10） |

**Response**: `Result<PageResponse<Room>>`

---

### 6.3 寮に属する部屋一覧

```
GET /api/rooms/by-dormitory/{dormitoryId}
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| dormitoryId | Long | ✅ | 寮ID |

**Response**: `Result<List<Room>>`

---

### 6.4 部屋作成

```
POST /api/rooms
```

**認証**: 要 JWT

**Request Body**:
```json
{
  "dormitoryId": 1,
  "roomNumber": "103",
  "floor": 1,
  "capacity": 2,
  "dailyRate": 1500.00,
  "roomType": 0,
  "acType": 1,
  "memo1": "窓あり",
  "memo2": null,
  "memo3": null,
  "createBy": "admin"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| dormitoryId | Long | ✅ | 所属寮ID |
| roomNumber | String | ✅ | 部屋番号 |
| floor | Integer | - | 階数 |
| capacity | Integer | - | 定員（デフォルト: 1） |
| dailyRate | BigDecimal | - | 一日単価 |
| roomType | Integer | - | 室区分: 0=不明, 1=和室, 2=洋室 |
| acType | Integer | - | エアコン: 0=なし, 1=あり |
| memo1 | String | - | メモ1 |
| memo2 | String | - | メモ2 |
| memo3 | String | - | メモ3 |
| createBy | String | - | 作成者 |

**Response**: `Result<String>`

---

### 6.5 部屋更新

```
PUT /api/rooms/update
```

**認証**: 要 JWT

**Request Body**:
```json
{
  "id": 1,
  "roomNumber": "101",
  "floor": 1,
  "capacity": 2,
  "dailyRate": 1600.00,
  "roomType": 0,
  "acType": 1,
  "memo1": "南向き",
  "memo2": null,
  "memo3": null,
  "status": 1,
  "version": 0,
  "updateBy": "admin"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id | Long | ✅ | 部屋ID |
| roomNumber | String | - | 部屋番号 |
| floor | Integer | - | 階数 |
| capacity | Integer | - | 定員 |
| dailyRate | BigDecimal | - | 一日単価 |
| roomType | Integer | - | 室区分: 0=不明, 1=和室, 2=洋室 |
| acType | Integer | - | エアコン: 0=なし, 1=あり |
| memo1 | String | - | メモ1 |
| memo2 | String | - | メモ2 |
| memo3 | String | - | メモ3 |
| status | Integer | - | 状態 |
| version | Long | ✅ | 楽観ロック用バージョン |
| updateBy | String | - | 更新者 |

**Response**: `Result<String>`

---

### 6.6 部屋削除

```
DELETE /api/rooms/{id}
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id | Long | ✅ | 部屋ID |

**Response**: `Result<String>`

---

## 7. 入居履歴 API

### 7.1 入居履歴検索（単一）

```
GET /api/resident-records/{id}
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id | Long | ✅ | 入居履歴ID |

**Response**: `Result<ResidentRecord>`
```json
{
  "success": true,
  "body": {
    "id": 1,
    "employeeId": 1,
    "roomId": 1,
    "checkinDate": "2026-01-15",
    "plannedCheckoutDate": null,
    "checkoutDate": null,
    "isActive": 1,
    "notes": null,
    "totalFee": null,
    "dailyRate": 1500.00,
    "dormName": "東京第一寮",
    "roomNumber": "101",
    "version": 0,
    "createBy": "system",
    "createTime": "2026-01-15T00:00:00"
  }
}
```

---

### 7.2 入居履歴一覧検索

```
GET /api/resident-records/list?employeeId=&roomId=&isActive=&pageNum=1&pageSize=10
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| employeeId | Long | - | 社員ID |
| roomId | Long | - | 部屋ID |
| isActive | Integer | - | 入居状態: 1=入居中, 0=退寮済 |
| pageNum | Integer | - | ページ番号（デフォルト: 1） |
| pageSize | Integer | - | 1ページの件数（デフォルト: 10） |

**Response**: `Result<PageResponse<ResidentRecord>>`

---

### 7.3 入居登録（チェックイン）

```
POST /api/resident-records/checkin?employeeId=1&roomId=1&checkinDate=2026-06-01&plannedCheckoutDate=2026-06-30&notes=&createBy=admin
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| employeeId | Long | ✅ | 社員ID |
| roomId | Long | ✅ | 部屋ID |
| checkinDate | String | ✅ | 入寮日（yyyy-MM-dd） |
| plannedCheckoutDate | String | - | 退寮予定日（yyyy-MM-dd） |
| notes | String | - | 備考 |
| createBy | String | - | 作成者 |

**Response**: `Result<ResidentRecord>`

**業務ルール**:
- 同一社員の重複入居不可（既に入居中の場合エラー）
- 同一部屋の期間重複不可（`plannedCheckoutDate` を考慮してチェック）

---

### 7.4 退寮処理（チェックアウト）

```
PUT /api/resident-records/checkout/{id}?checkoutDate=2026-06-30&updateBy=admin
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id (Path) | Long | ✅ | 入居履歴ID |
| checkoutDate | String | - | 退寮日（省略時は当日、yyyy-MM-dd） |
| updateBy | String | - | 更新者 |

**Response**: `Result<ResidentRecord>`

**業務ルール**:
- 退寮日は入寮日より後である必要がある
- 既に退寮済みの場合はエラー
- 退寮時に **利用料金 = 日額 × 入居日数（入居日〜退寮日）** を自動計算し `totalFee` に設定

---

### 7.5 入居履歴削除

```
DELETE /api/resident-records/{id}
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id | Long | ✅ | 入居履歴ID |

**Response**: `Result<String>`

---

## 8. 変更履歴 API（参照専用）

### 8.1 変更履歴検索（単一）

```
GET /api/change-history/{id}
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id | Long | ✅ | 履歴ID |

**Response**: `Result<ChangeHistory>`
```json
{
  "success": true,
  "body": {
    "id": 1,
    "tableName": "tbl_employee",
    "recordId": "1",
    "operationType": "UPDATE",
    "oldData": "{\"empName\":\"旧名前\"}",
    "newData": "{\"empName\":\"新名前\"}",
    "changedBy": "admin",
    "changedTime": "2026-06-13T10:00:00",
    "remarks": "社員名変更",
    "createTime": "2026-06-13T10:00:00"
  }
}
```

---

### 8.2 変更履歴一覧検索

```
GET /api/change-history/list?tableName=tbl_employee&recordId=1
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| tableName | String | ✅ | テーブル名 |
| recordId | String | ✅ | レコードID |

**Response**: `Result<List<ChangeHistory>>`

---

## 9. 利用料金確認 API

### 9.1 利用料金一覧取得

入居履歴に基づき、社員ごとの利用料金を一覧取得する。
退寮済みの場合は退寮日までの確定料金、入居中は本日までの計算料金を表示する。

```
GET /api/resident-records/usage-fees
```

**認証**: 要 JWT

**Response**: `Result<List<ResidentRecord>>`
```json
{
  "success": true,
  "body": [
    {
      "id": 1,
      "employeeId": 1,
      "empName": "山田太郎",
      "empNo": "EMP-00001",
      "roomId": 1,
      "roomNumber": "101",
      "dormName": "東京第一寮",
      "dailyRate": 1500.00,
      "checkinDate": "2020-06-01",
      "checkoutDate": null,
      "isActive": 1,
      "totalFee": 1095000.00
    }
  ]
}
```

**算出ロジック**:
- **退寮済み**: DB保存済みの `totalFee`（退寮時に自動計算: 日額 × 入居日数）
- **入居中**: 日額 × 入居日数（入居日〜本日）を動的計算（DB未保存）
    "version": 0,
    "createBy": "system",
    "createTime": "2026-06-01T00:00:00"
  }
}
```

---

### 9.2 部屋別日額設定（旧・寮費設定）

部屋ごとの日額料金設定を管理する。

```
GET /api/billings/list?roomId=&pageNum=1&pageSize=10
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| roomId | Long | - | 部屋ID |
| pageNum | Integer | - | ページ番号（デフォルト: 1） |
| pageSize | Integer | - | 1ページの件数（デフォルト: 10） |

**Response**: `Result<PageResponse<Billing>>`

---

## 10. 操作ログ API（参照専用）

### 13.1 操作ログ検索（単一）

```
GET /api/operation-logs/{id}
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id | Long | ✅ | ログID |

**Response**: `Result<OperationLog>`
```json
{
  "success": true,
  "body": {
    "id": 1,
    "operatorId": "admin",
    "operationType": "LOGIN",
    "targetType": null,
    "targetId": null,
    "description": "管理者ログイン",
    "clientIp": "192.168.1.100",
    "traceId": "TRACE-001",
    "resultStatus": 1,
    "createTime": "2026-06-13T10:00:00"
  }
}
```

---

### 13.2 操作ログ一覧検索

```
GET /api/operation-logs/list?operatorId=&operationType=&targetType=&resultStatus=&pageNum=1&pageSize=10
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| operatorId | String | - | 操作者ID（部分一致） |
| operationType | String | - | 操作タイプ |
| targetType | String | - | 対象種別 |
| resultStatus | Integer | - | 結果: 0=失敗, 1=成功 |
| pageNum | Integer | - | ページ番号（デフォルト: 1） |
| pageSize | Integer | - | 1ページの件数（デフォルト: 10） |

**Response**: `Result<PageResponse<OperationLog>>`

---

## 11. 開発用 API（dev/qa 環境のみ）

> **注意**: これらのAPIは `@Profile({"dev", "qa"})` により本番環境では無効化されています。

### 14.1 認証テスト

```
GET /api/test/auth
```

**認証**: 不要

現在の認証状態を確認する。フロントエンド開発時のトークン検証に使用。

**Response (認証済み)**:
```
Login User : admin
```

**Response (未認証)**:
```
User is not authenticated
```

---

### 14.2 JWT トークン発行

```
GET /test/token?userId=admin
```

**認証**: 不要

任意のユーザーIDのJWTトークンを生成する。フロントエンド開発時に使用。

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| userId | String | - | ユーザーID（デフォルト: admin） |

**Response**:
```json
{
  "success": true,
  "message": "JWT token generated for userId: admin",
  "body": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

## エンティティフィールド一覧

### User
| フィールド | 型 | 説明 |
|-----------|-----|------|
| userid | String | ユーザーID（PK） |
| password | String | パスワード |
| userName | String | ユーザー名 |
| enabled | Boolean | 有効フラグ |
| createBy | String | 作成者 |
| createTime | LocalDateTime | 作成日時 |
| updateBy | String | 更新者 |
| updateTime | LocalDateTime | 更新日時 |

### Department
| フィールド | 型 | 説明 |
|-----------|-----|------|
| id | Long | 部門ID（PK） |
| deptCode | String | 部門コード（UK） |
| deptName | String | 部門名 |
| region | String | 地域 |
| parentId | Long | 親部門ID |
| sortOrder | Integer | 表示順 |
| status | Integer | 状態: 1=有効, 0=無効 |
| version | Long | 楽観ロックバージョン |
| createBy / createTime / updateBy / updateTime | - | 監査フィールド |

### Employee
| フィールド | 型 | 説明 |
|-----------|-----|------|
| id | Long | 社員ID（PK） |
| empNo | String | 工番（UK） |
| empName | String | 社員名 |
| gender | String | 性別: MALE / FEMALE |
| nationality | String | 国籍 |
| deptId | Long | 所属部門ID |
| firstUseDate | LocalDate | 初回入寮日 |
| phone | String | 電話番号 |
| email | String | メール |
| hireDate | LocalDate | 入社日 |
| resignDate | LocalDate | 退職日 |
| empStatus | Integer | 状態: 1=在職, 2=離職, 3=休職中 |
| version | Long | 楽観ロックバージョン |

### Dormitory
| フィールド | 型 | 説明 |
|-----------|-----|------|
| id | Long | 寮ID（PK） |
| dormName | String | 寮名 |
| region | String | 地域 |
| address | String | 住所 |
| dormCondition | String | 入居条件: MALE / FEMALE / ANY |
| totalRooms | Integer | 総部屋数 |
| status | Integer | 状態: 1=運用中, 0=停止中 |
| version | Long | 楽観ロックバージョン |

### Room
| フィールド | 型 | 説明 |
|-----------|-----|------|
| id | Long | 部屋ID（PK） |
| dormitoryId | Long | 所属寮ID |
| roomNumber | String | 部屋番号 |
| floor | Integer | 階数 |
| capacity | Integer | 定員 |
| currentOccupancy | Integer | 現在入居者数 |
| dailyRate | BigDecimal | 一日単価 |
| roomType | Integer | 室区分: 0=不明, 1=和室, 2=洋室 |
| acType | Integer | エアコン: 0=なし, 1=あり |
| memo1 | String | メモ1 |
| memo2 | String | メモ2 |
| memo3 | String | メモ3 |
| status | Integer | 状態: 1=空室, 2=満室, 0=使用不可 |
| version | Long | 楽観ロックバージョン |

### ResidentRecord
| フィールド | 型 | 説明 |
|-----------|-----|------|
| id | Long | 入居履歴ID（PK） |
| employeeId | Long | 社員ID |
| roomId | Long | 部屋ID |
| checkinDate | LocalDate | 入寮日 |
| plannedCheckoutDate | LocalDate | 退寮予定日 |
| checkoutDate | LocalDate | 退寮日（NULL=入居中） |
| isActive | Integer | 入居状態: 1=入居中, 0=退寮済 |
| notes | String | 備考 |
| totalFee | BigDecimal | 利用料金合計（退寮時に計算・設定） |
| dailyRate | BigDecimal | 日額（部屋テーブルからJOIN） |
| dormName | String | 寮名（JOIN） |
| roomNumber | String | 部屋番号（JOIN） |
| empName | String | 社員名（JOIN） |
| empNo | String | 工番（JOIN） |
| version | Long | 楽観ロックバージョン |

### ChangeHistory
| フィールド | 型 | 説明 |
|-----------|-----|------|
| id | Long | 履歴ID（PK） |
| tableName | String | 変更対象テーブル |
| recordId | String | 変更対象レコードID |
| operationType | String | 操作種別: INSERT / UPDATE / DELETE |
| oldData | String | 変更前データ（JSON） |
| newData | String | 変更後データ（JSON） |
| changedBy | String | 変更者 |
| changedTime | LocalDateTime | 変更日時 |
| remarks | String | 備考 |

### Billing
| フィールド | 型 | 説明 |
|-----------|-----|------|
| id | Long | 料金設定ID（PK） |
| roomId | Long | 部屋ID |
| dailyRate | BigDecimal | 日額 |
| memo1 | String | メモ1 |
| memo2 | String | メモ2 |
| memo3 | String | メモ3 |
| version | Long | 楽観ロックバージョン |

### OperationLog
| フィールド | 型 | 説明 |
|-----------|-----|------|
| id | Long | ログID（PK） |
| operatorId | String | 操作者ID |
| operationType | String | 操作タイプ |
| targetType | String | 対象種別 |
| targetId | String | 対象ID |
| description | String | 操作説明 |
| clientIp | String | クライアントIP |
| traceId | String | トレースID |
| resultStatus | Integer | 結果: 0=失敗, 1=成功 |
| version | Long | 楽観ロックバージョン |

---

## 12. Excelインポート API

### 14.1 Excelファイルアップロード・インポート

Excelファイルをアップロードし、指定した種別のデータをインポートする。

```
POST /api/import/upload?importType=DORMITORY&createBy=admin
```

**認証**: 要 JWT

**Content-Type**: `multipart/form-data`

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| file | File | ✅ | アップロードするExcelファイル（.xlsx/.xls） |
| importType | String | ✅ | インポート種別: DORMITORY / ROOM / EMPLOYEE / RESIDENT / BILLING |
| createBy | String | - | 操作者ID（デフォルト: system） |

**Response (成功)**:
```json
{
  "success": true,
  "message": "インポート処理完了",
  "body": {
    "importLogId": 1,
    "fileName": "寮マスタ.xlsx",
    "importType": "DORMITORY",
    "totalCount": 10,
    "successCount": 10,
    "errorCount": 0,
    "errorMessages": [],
    "importStatus": 1
  }
}
```

**Response (一部失敗)**:
```json
{
  "success": true,
  "message": "インポート処理完了",
  "body": {
    "importLogId": 2,
    "fileName": "部屋情報.xlsx",
    "importType": "ROOM",
    "totalCount": 20,
    "successCount": 18,
    "errorCount": 2,
    "errorMessages": ["行3: 寮IDが空です", "行15: 部屋番号が空です"],
    "importStatus": 2
  }
}
```

---

### 14.2 インポート履歴検索（単一）

```
GET /api/import/{id}
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id (Path) | Long | ✅ | インポートログID |

**Response**: `Result<ImportLog>`

---

### 14.3 インポート履歴一覧検索

```
GET /api/import/list?importType=&importStatus=&pageNum=1&pageSize=10
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| importType | String | - | インポート種別で絞り込み |
| importStatus | Integer | - | 状態: 0=処理中, 1=成功, 2=一部失敗, 3=失敗 |
| pageNum | Integer | - | ページ番号（デフォルト: 1） |
| pageSize | Integer | - | 1ページの件数（デフォルト: 10） |

**Response**: `Result<PageResponse<ImportLog>>`



## 14. 帳票・統計 API

### 16.1 ダッシュボードサマリー

システム全体のサマリー情報を取得する。

```
GET /api/reports/summary
```

**認証**: 要 JWT

**Response**:
```json
{
  "success": true,
  "body": {
    "totalDormitories": 5,
    "totalRooms": 20,
    "vacantRooms": 8,
    "currentResidents": 10,
    "totalResidentRecords": 13,
    "employeesWithoutResident": 2,
    "totalBillingAmount": "465000.00",
    "totalBillingCount": 15
  }
}
```

---

### 16.2 寮別稼働率レポート

寮ごとの部屋稼働率を取得する。

```
GET /api/reports/occupancy?region=
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| region | String | - | 地域で絞り込み（省略時は全地域） |

**Response**:
```json
{
  "success": true,
  "body": [
    {
      "dormitoryId": 1,
      "dormName": "東京第一寮",
      "region": "東京",
      "totalRooms": 6,
      "occupiedRooms": 3,
      "vacantRooms": 3,
      "occupancyRate": 50.0
    }
  ]
}
```

---

### 16.3 請求サマリー

指定した月または全期間の請求サマリーを取得する。

```
GET /api/reports/billing?month=2026-06
```

**認証**: 要 JWT

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| month | String | - | 対象年月（yyyy-MM）。省略時は全期間の集計 |

**Response**:
```json
{
  "success": true,
  "body": {
    "billingMonth": "2026-06",
    "billingCount": 10,
    "totalDays": 310,
    "totalAmount": 465000.00
  }
}
```

---

## エラーレスポンス例

### 401 未認証
```json
{
  "success": false,
  "message": "未登录或Token已过期，请重新登录"
}
```

### 403 権限なし
```json
{
  "success": false,
  "message": "没有权限访问该资源"
}
```

### 409 楽観ロック競合
```json
{
  "success": false,
  "message": "同时更新による竞合が発生しました。再試行してください。"
}
```

### 500 サーバーエラー
```json
{
  "success": false,
  "message": "サーバー内部エラーが発生しました"
}
```
