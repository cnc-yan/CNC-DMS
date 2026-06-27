# 前端适配新表结构 - 完成总结

## 修改概述

已根据新的后端表结构（`userid`, `user_name` 等）对前端代码进行了全面适配。关键改动如下：

### 1. 认证模块（登录）

**文件：** `src/api/auth.js`, `src/store/auth.js`

**变更：**
- 登录 API 改为发送 `userid` 字段而非 `username`
- 前端登录请求现在是：
  ```json
  POST /api/auth/login
  {
    "userid": "admin",
    "password": "admin123"
  }
  ```
- 获取当前用户信息时，优先使用后端返回的 `user_name`（若无则用 `userid` 或 `username`）

### 2. 用户管理 API 层

**文件：** `src/api/user/index.js`

**变更：**
- `getUserList(params)` - 前端发送 `username` 参数时自动转换为后端的 `userid` 参数
- `getUser(userid)` - 参数改为 `userid`（后端主键）
- `updateUser(data)` - 请求体使用新字段名 `userid`, `user_name`
- `deleteUser(userid)` - 参数改为 `userid`
- `createUser(data)` - 新增用户，使用新字段名

### 3. 用户列表页（查看/搜索/删除）

**文件：** `src/views/system/user.vue`

**变更：**
- 表格列调整：显示来自后端的 `userid`（显示标签为"用户ID"）和 `user_name`（显示标签为"用户名"）
- 搜索框：仍显示"用户ID"，发送 `username` 参数（API 层自动转换）
- 删除逻辑：改为使用 `userid` 或 `id` 字段（兼容两种可能）

### 4. 用户详情/编辑/新增页

**文件：** `src/views/system/userDetail.vue`

**变更：**
- 表单字段调整：
  - "用户ID"（对应 form.username，最终发送为 `userid`）
  - "用户名"（对应 form.user_name，最终发送为 `user_name`）
- **新增模式**（`/system/user/new`）：可输入用户ID和用户名后新增
- **编辑模式**（`/system/user/:id`）：从后端获取用户信息并允许修改
- 删除按钮：仅在编辑模式显示

### 5. 文档更新

- `func_front.md` - 记录了新表结构的适配说明和字段映射表
- `DEBUGGING_GUIDE.md` - 提供登录问题排查指南和测试方法

---

## 前端假设的后端 API 返回格式

前端适配代码基于以下后端 API 返回格式。**若后端实际返回格式不同，需调整前端代码。**

### 登录响应

```json
{
  "success": true,
  "token": "JWT_TOKEN_STRING",
  "message": "登录成功"
}
```

### 获取当前用户（`GET /api/auth/me`）

```json
{
  "userid": "admin",
  "user_name": "管理员",
  "enabled": true,
  ...
}
```

### 获取用户列表（`GET /api/user/list?pageNum=1&pageSize=10&userid=admin`）

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
        "user_name": "管理员",
        "enabled": true,
        "create_time": "2026-06-05T10:00:00",
        "create_by": "system"
      },
      {
        "userid": "user1",
        "user_name": "用户1",
        "enabled": true,
        "create_time": "2026-06-05T11:00:00"
      }
    ]
  }
}
```

### 获取单个用户（`GET /api/user/admin`）

```json
{
  "success": true,
  "message": "查询成功",
  "body": {
    "userid": "admin",
    "user_name": "管理员",
    "enabled": true,
    "create_by": "system",
    "create_time": "2026-06-05T10:00:00",
    "update_by": null,
    "update_time": null
  }
}
```

### 更新用户（`PUT /api/user/update`）

请求体：
```json
{
  "userid": "admin",
  "user_name": "管理员（已更新）",
  "enabled": true,
  "update_by": "admin"
}
```

响应（示例）：
```json
{
  "success": true,
  "message": "用户信息更新成功",
  "body": "成功"
}
```

### 新增用户（`POST /api/user`）

请求体：
```json
{
  "userid": "newuser",
  "user_name": "新用户",
  "password": "password123",
  "enabled": true,
  "update_by": "admin"
}
```

响应（示例）：
```json
{
  "success": true,
  "message": "用户创建成功",
  "body": { "userid": "newuser", ... }
}
```

### 删除用户（`DELETE /api/user/admin`）

响应（示例）：
```json
{
  "success": true,
  "message": "用户删除成功",
  "body": "成功"
}
```

---

## 测试步骤

### 第一步：启动开发服务器

```powershell
cd 'C:\Users\Administrator\IdeaProjects\CNC-DMS\frontend\cnc-dms-ui'
npm install
npm run dev
```

### 第二步：测试登录

1. 打开浏览器到 `http://localhost:5173` (或开发服务器显示的地址)
2. 输入用户ID（如 `admin`）和密码（如 `admin123`）
3. 观察浏览器 Network 标签，检查发送到后端的请求是否为：
   ```json
   {
     "userid": "admin",
     "password": "admin123"
   }
   ```
4. 若登录失败，查看后端返回的错误信息（参考 `DEBUGGING_GUIDE.md`）

### 第三步：测试用户管理

1. 登录成功后，点 Home 页的「用户信息一览」按钮
2. 验证列表页：
   - 表格应显示用户列表（用户ID、用户名、启用、创建时间）
   - 搜索框能按用户ID搜索
   - 分页正常工作
3. 验证删除：
   - 勾选列表项的 checkbox
   - 点头部"删除"按钮
   - 确认删除（观察 Network 的 DELETE 请求）
4. 验证新增/编辑：
   - 点"新增用户"，应跳转到编辑页面
   - 输入用户ID和用户名后保存
   - 点列表中的用户ID，应进入编辑页面
   - 修改信息后保存

---

## 常见问题

### Q: 登录时提示"用户名或密码错误"

**A:** 可能原因：
1. 后端数据库中没有该用户，或密码错误
2. 前端发送的字段名不对（已改为 `userid`，但后端仍期望 `username`）
3. 后端登录 API 返回的格式不符合预期

**排查：**
- 查看浏览器 Network 标签，确认发送的是 `{"userid":"...", "password":"..."}`
- 查看后端日志，了解后端收到的参数
- 参考 `DEBUGGING_GUIDE.md` 中的排查步骤

### Q: 登录成功，但用户管理页面无法加载用户列表

**A:** 可能原因：
1. 后端用户列表 API 返回的字段名与前端期望不符
2. 后端 API 路径或查询参数格式不对

**排查：**
- 查看 Network 显示的请求和响应
- 查看浏览器 Console 的错误信息
- 确认后端返回的字段名（应是 `userid`, `user_name` 等）

### Q: 前端字段名与后端不匹配，需要调整怎么办？

**A:** 如果后端返回的字段名与上述假设不同，需要修改以下文件：
- `src/api/user/index.js` - API 参数和转换逻辑
- `src/views/system/user.vue` - 表格列绑定和搜索逻辑
- `src/views/system/userDetail.vue` - 表单字段绑定
- `src/store/auth.js` - 用户信息处理

---

## 文件清单

修改或创建的文件：

- ✓ `src/api/auth.js` - 改为发送 userid
- ✓ `src/api/user/index.js` - 添加字段转换逻辑
- ✓ `src/store/auth.js` - 适配新字段名
- ✓ `src/views/system/user.vue` - 调整表格和搜索
- ✓ `src/views/system/userDetail.vue` - 调整表单字段
- ✓ `src/router/index.js` - 路由配置（无改变）
- ✓ `func_front.md` - 更新文档
- ✓ `DEBUGGING_GUIDE.md` - 新增调试指南

---

## 下一步

1. **立即行动：**
   - 启动前端开发服务器
   - 尝试登录，检查是否成功
   - 若失败，参考 `DEBUGGING_GUIDE.md` 排查

2. **若登录仍失败：**
   - 收集后端返回的实际错误信息
   - 告知后端 API 的实际返回格式
   - 我将调整前端代码以适配

3. **若登录成功：**
   - 测试用户管理功能（列表、搜索、新增、删除、编辑）
   - 若有问题，参考 "常见问题" 部分

---

**版本：** 2026-06-05  
**状态：** 前端已适配新表结构，待后端验证


