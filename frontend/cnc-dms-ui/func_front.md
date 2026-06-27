# Frontend Function Index

## レイアウト
- `src/layout/MainLayout.vue` - メインレイアウト（サイドバーナビゲーション + ヘッダー + コンテンツエリア）
  - サイドバー：システム管理/マスタ管理/入居管理の階層メニュー
  - ヘッダー：パンくずリスト、ユーザー情報、ログアウト

## Views
| 画面 | ファイル | 説明 |
|------|----------|------|
| ログイン | `src/views/LoginView.vue` | ログインページ（レイアウトなし） |
| ホーム | `src/views/HomeView.vue` | ダッシュボード（全機能へのクイックリンク） |
| ユーザー管理一覧 | `src/views/system/user.vue` | ユーザー一覧（検索/一括削除/Dialog新規編集） |
| ユーザー詳細 | `src/views/system/userDetail.vue` | ユーザー詳細表示＋Dialog編集 |
| 部門管理一覧 | `src/views/department/index.vue` | 部門一覧（検索/一括削除/Dialog新規編集） |
| 部門詳細 | `src/views/department/detail.vue` | 部門詳細表示＋Dialog編集 |
| 社員管理一覧 | `src/views/employee/index.vue` | 社員一覧（検索/一括削除/Dialog新規編集） |
| 社員詳細 | `src/views/employee/detail.vue` | 社員詳細表示＋Dialog編集 |
| 寮管理一覧 | `src/views/dormitory/index.vue` | 寮一覧（検索/一括削除/Dialog新規編集） |
| 寮詳細 | `src/views/dormitory/detail.vue` | 寮詳細表示＋Dialog編集＋部屋一覧へのリンク |
| 部屋管理一覧 | `src/views/room/index.vue` | 部屋一覧（寮ID指定フィルタ対応） |
| 部屋詳細 | `src/views/room/detail.vue` | 部屋詳細表示＋Dialog編集 |
| 入居履歴一覧 | `src/views/residentRecord/index.vue` | 入居履歴一覧＋入居登録/退寮処理Dialog |
| 入居履歴詳細 | `src/views/residentRecord/detail.vue` | 入居履歴詳細＋退寮処理/削除 |
| 寮割カレンダー | `src/views/calendar/index.vue` | カレンダー表示（月切替/地域フィルタ/黄色セル/赤背景/ツールチップ） |
| 寮費管理一覧 | `src/views/billing/index.vue` | 寮費一覧（検索/一括削除/Dialog新規編集/確定） |
| 寮費詳細 | `src/views/billing/detail.vue` | 寮費詳細表示＋Dialog編集/確定/削除 |
| 備品マスタ一覧 | `src/views/fixture/index.vue` | 備品一覧（検索/一括削除/Dialog新規編集） |
| 備品詳細 | `src/views/fixture/detail.vue` | 備品詳細表示＋Dialog編集/削除＋保管一覧へのリンク |
| 入居時備品準備 | `src/views/fixtureInventory/index.vue` | 備品準備一覧（検索/一括削除/Dialog新規編集） |
| 備品保管管理 | `src/views/fixtureStorage/index.vue` | 保管一覧（検索/一括削除/Dialog新規編集/備品ID指定フィルタ対応） |
| 操作ログ一覧 | `src/views/operationLog/index.vue` | 操作ログ一覧＋詳細表示Dialog（参照専用） |
| 変更履歴一覧 | `src/views/changeHistory/index.vue` | 変更履歴一覧＋詳細表示Dialog（参照専用） |
| 空き室管理 | `src/views/vacancy/index.vue` | 空き室一覧（寮/性別フィルタ、空き容量表示） |
| Excelインポート | `src/views/importLog/index.vue` | Excelアップロード・インポート履歴一覧＋詳細Dialog |
| 帳票・統計 | `src/views/report/index.vue` | サマリーカード/寮別稼働率/請求サマリーのタブ切替 |

## API
| モジュール | ファイル | 主な関数 |
|-----------|----------|----------|
| 認証 | `src/api/auth.js` | login, getCurrentUser |
| ユーザー | `src/api/user/index.js` | getUserList, getUser, createUser, updateUser, deleteUser, deleteUsersBulk |
| 部門 | `src/api/department/index.js` | getDepartmentList, getDepartment, createDepartment, updateDepartment, deleteDepartment |
| 社員 | `src/api/employee/index.js` | getEmployeeList, getEmployee, createEmployee, updateEmployee, deleteEmployee |
| 寮 | `src/api/dormitory/index.js` | getDormitoryList, getDormitory, createDormitory, updateDormitory, deleteDormitory |
| 部屋 | `src/api/room/index.js` | getRoomList, getRoom, getRoomsByDormitory, createRoom, updateRoom, deleteRoom |
| 入居履歴 | `src/api/residentRecord/index.js` | getResidentRecordList, getResidentRecord, checkin, checkout, deleteResidentRecord |
| 変更履歴 | `src/api/changeHistory/index.js` | getChangeHistory, getChangeHistoryList |
| 寮費 | `src/api/billing/index.js` | getBillingList, getBilling, getBillingsByResidentRecord, createBilling, updateBilling, confirmBilling, deleteBilling |
| 備品マスタ | `src/api/fixture/index.js` | getFixtureList, getFixture, createFixture, updateFixture, deleteFixture |
| 入居時備品準備 | `src/api/fixtureInventory/index.js` | getFixtureInventoryList, getFixtureInventory, getFixtureInventoriesByResidentRecord, createFixtureInventory, updateFixtureInventory, deleteFixtureInventory |
| 備品保管 | `src/api/fixtureStorage/index.js` | getFixtureStorageList, getFixtureStorage, getFixtureStoragesByFixture, createFixtureStorage, updateFixtureStorage, deleteFixtureStorage |
| 操作ログ | `src/api/operationLog/index.js` | getOperationLogList, getOperationLog |
| 空き室 | `src/api/vacancy/index.js` | getVacancyList, getAllVacantRooms, checkRoomAvailability |
| Excelインポート | `src/api/importLog/index.js` | uploadExcel, getImportLogList, getImportLog |
| レポート | `src/api/report/index.js` | getSummaryReport, getOccupancyReport, getBillingReport |

## Store
| ファイル | 説明 |
|----------|------|
| `src/store/auth.js` | 認証状態管理（token, userid, username, login/logout/fetchCurrentUser） |

## ルーティング

| カテゴリ | パス | 名称 | コンポーネント | 説明 |
|---------|------|------|---------------|------|
| 認証 | `/login` | Login | LoginView | ログインページ（layout: false） |
| - | `/` | - | redirect→/home | ルートリダイレクト |
| ホーム | `/home` | Home | HomeView | ダッシュボード |
| システム | `/system/user` | UserList | user.vue | ユーザー一覧 |
| システム | `/system/user/:id` | UserDetail | userDetail.vue | ユーザー詳細 |
| マスタ | `/master/department` | DepartmentList | department/index.vue | 部門一覧 |
| マスタ | `/master/department/:id` | DepartmentDetail | department/detail.vue | 部門詳細 |
| マスタ | `/master/employee` | EmployeeList | employee/index.vue | 社員一覧 |
| マスタ | `/master/employee/:id` | EmployeeDetail | employee/detail.vue | 社員詳細 |
| マスタ | `/master/dormitory` | DormitoryList | dormitory/index.vue | 寮一覧 |
| マスタ | `/master/dormitory/:id` | DormitoryDetail | dormitory/detail.vue | 寮詳細 |
| マスタ | `/master/room` | RoomList | room/index.vue | 部屋一覧 |
| マスタ | `/master/room/:id` | RoomDetail | room/detail.vue | 部屋詳細 |
| 入居 | `/resident/records` | ResidentRecordList | residentRecord/index.vue | 入居履歴一覧 |
| 入居 | `/resident/records/:id` | ResidentRecordDetail | residentRecord/detail.vue | 入居履歴詳細 |
| 入居 | `/resident/calendar` | CalendarView | calendar/index.vue | 寮割カレンダー |
| 寮費 | `/billing` | BillingList | billing/index.vue | 寮費一覧 |
| 寮費 | `/billing/:id` | BillingDetail | billing/detail.vue | 寮費詳細 |
| マスタ | `/master/fixture` | FixtureList | fixture/index.vue | 備品マスタ一覧 |
| マスタ | `/master/fixture/:id` | FixtureDetail | fixture/detail.vue | 備品詳細 |
| マスタ | `/master/fixture-inventory` | FixtureInventoryList | fixtureInventory/index.vue | 入居時備品準備 |
| マスタ | `/master/fixture-storage` | FixtureStorageList | fixtureStorage/index.vue | 備品保管管理 |
| システム | `/system/operation-log` | OperationLogList | operationLog/index.vue | 操作ログ一覧 |
| システム | `/system/change-history` | ChangeHistoryList | changeHistory/index.vue | 変更履歴一覧 |
| 空き室 | `/vacancy` | VacancyList | vacancy/index.vue | 空き室一覧 |
| インポート | `/import` | ImportLogList | importLog/index.vue | Excelインポート |
| レポート | `/report` | ReportView | report/index.vue | 帳票・統計 |

## 画面機能一覧

### レイアウト（MainLayout）
- アコーディオン式サイドバーメニュー
- 折りたたみ可能なサイドバー
- パンくずリスト
- ユーザーアバター/ログアウトドロップダウン
- 全認証必須ルートはこのレイアウト内に表示

### ホーム画面（HomeView）
- ウェルカムバナー
- 機能別クイックリンクカード（16機能）

### ユーザー管理（user.vue / userDetail.vue）
- ユーザーID/名での部分一致検索
- 一括削除
- 新規作成/編集は Dialog 弾窓
- テーブル行ダブルクリックで詳細へ遷移

### 部門管理（department/index.vue）
- 部門コード/名/地域/状態での検索
- 一括削除
- Dialog 新規作成/編集（楽観ロック対応）

### 社員管理（employee/index.vue）
- 工番/名/状態での検索
- 性別（MALE/FEMALE）表示
- 入社日選択対応

### 寮管理（dormitory/index.vue）
- 寮名/地域/状態での検索
- 入居条件表示（MALE/FEMALE/ANY）
- 詳細画面から部屋一覧へリンク

### 部屋管理（room/index.vue）
- 寮ID指定で by-dormitory API を利用可能
- 1日単価表示（￥フォーマット）
- 部屋状態（空室/満室/使用不可）

### 入居履歴（residentRecord/index.vue）
- 入居登録（チェックイン）Dialog
- 退寮処理（チェックアウト）Dialog（デフォルト当日）
- 入居中/退寮済の状態表示

### 寮割カレンダー（calendar/index.vue）
- 月切替（前月/翌月/今日）
- 地域フィルタ
- 黄色セル（入居開始日）
- 赤背景セル（退寮警告：14日以内）
- セルホバーでツールチップ表示（入居者詳細）
- 凡例表示

### 寮費管理（billing/index.vue / detail.vue）
- 入居履歴ID/対象年月/状態での検索
- 一括削除
- Dialog 新規作成/編集（楽観ロック対応）
- 寮費確定ボタン（state=0→1→2→3 のワークフロー）
- 金額・日額表示（￥フォーマット）
- 詳細画面から確定/編集/削除

### 備品マスタ管理（fixture/index.vue / detail.vue）
- 備品名称/種別/状態での検索
- 一括削除
- Dialog 新規作成/編集
- 詳細画面から保管一覧へリンク

### 入居時備品準備（fixtureInventory/index.vue）
- 入居履歴ID/備品ID/準備状況での検索
- 一括削除
- Dialog 新規作成/編集
- 準備状況（未準備/準備済/設置済）表示

### 備品保管管理（fixtureStorage/index.vue）
- 備品ID指定フィルタ対応
- 保管場所/状態での検索
- 一括削除
- Dialog 新規作成/編集
- 状態（保管中/使用中/廃棄済）表示

### 操作ログ（operationLog/index.vue）
- 操作者/操作タイプ/対象種別/結果での検索
- 参照専用（読取りのみ）
- 詳細Dialogで変更前/変更後JSON表示
- HTTPメソッド/URL/IPアドレス表示
- ページネーション

### 変更履歴（changeHistory/index.vue）
- テーブル名/レコードIDでの検索
- 参照専用（読取りのみ）
- 詳細Dialogで変更前/変更後データ表示
- ページネーション

### 空き室管理（vacancy/index.vue）
- 寮指定/性別条件での絞り込み検索
- 空き容量（定員 - 現在人数）のカラー表示
- 寮名＋入居条件＋部屋番号＋日額の一覧
- ページネーション

### Excelインポート（importLog/index.vue）
- インポート種別（寮/部屋/社員/入居履歴/寮費）選択
- .xlsx/.xls ファイルアップロード
- アップロード後即時インポート実行
- インポート履歴一覧（種別/状態フィルタ）
- 詳細Dialogでエラーメッセージ表示
- ページネーション

### 帳票・統計（report/index.vue）
- 3タブ構成：ダッシュボードサマリー / 寮別稼働率 / 請求サマリー
- サマリーカード（総寮数/総部屋数/空室数/現在入居者数/未入居社員数/総請求金額）
- 寮別稼働率テーブル（進捗バー表示）＋地域フィルタ
- 請求サマリー（対象月指定/全期間）
