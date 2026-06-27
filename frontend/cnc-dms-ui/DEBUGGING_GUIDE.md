# 前端调试与诊断指南

## 背景

后端表结构已更新为使用 `userid` 作为主键和登录账号字段，以及 `user_name` 作为用户姓名字段。前端已适配这些变更。

## 登录问题排查

如果仍无法登录，请按以下步骤排查：

### 步骤 1：检查后端服务是否运行

在浏览器地址栏直接访问后端：
```
http://localhost:8080/api/auth/login
```

应该返回 405 Method Not Allowed（因为是 GET）或其他后端特定的错误，说明后端基础连接正常。

### 步骤 2：查看浏览器控制台和网络日志

1. 打开浏览器开发者工具（F12）。
2. 进入 **Console** 标签，查看是否有 JavaScript 错误。
3. 进入 **Network** 标签，勾选"Preserve log"。
4. 在登录页输入用户名（e.g., `admin`）和密码（e.g., `admin123`），点击登录。
5. 观察 Network 标签中的请求：
   - 应该看到一条 POST 请求到 `http://localhost:8080/api/auth/login`
   - 请求体应该包含：`{"userid":"admin","password":"admin123"}` **(注意：现在是 userid，不是 username)**
   - 查看响应体和状态码

### 步骤 3：确认后端 API 响应格式

后端登录 API 应返回以下格式（示例）：

**成功响应（200 OK）：**
```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "登录成功"
}
```

**失败响应（401 或 400）：**
```json
{
  "success": false,
  "message": "用户名或密码错误"
}
```

### 步骤 4：常见问题排查

**问题：Network 显示 404 或 502**
- ✓ 解决：确保后端服务在 `http://localhost:8080` 运行，且已部署 `/api/auth/login` 端点。
- ✓ 或配置 Vite 开发代理（见下文）。

**问题：Network 显示 200 但仍无法登录**
- ✓ 检查响应体是否包含 `"success": true` 和 `"token"` 字段。
- ✓ 若响应格式不同，前端代码（`src/store/auth.js` 中的 `login` 函数）可能需要调整。

**问题：CORS 错误**
- ✓ 解决：后端需要允许来自前端的跨域请求（在 Spring Boot 添加 CORS 配置）。
- ✓ 或使用 Vite 开发代理（见下文）。

### 步骤 5：验证发送的字段名

在浏览器 Console 执行以下代码，查看前端实际发送的内容：

```javascript
// 进入 Console，执行：
const loginData = { userid: "admin", password: "admin123" };
console.log("前端发送内容:", JSON.stringify(loginData));

// 应输出：前端发送内容: {"userid":"admin","password":"admin123"}
```

---

## 配置 Vite 开发代理（可选，用于处理 CORS）

如果后端没有配置 CORS 或你想避免跨域问题，可在 `vite.config.js` 中添加代理：

编辑 `vite.config.js`（项目根目录），添加以下内容：

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

配置后，前端请求 `/api/auth/login` 会自动转发到 `http://localhost:8080/api/auth/login`，避免 CORS 问题。

---

## 字段名对应表

前端 UI 显示 vs 后端表字段名 vs API 参数：

| UI 显示 | 前端变量 | 后端表字段 | API 发送字段名 |
|--------|--------|---------|-------------|
| 用户名（登录页） | form.username | userid | userid |
| 用户名（列表页） | searchForm.username | userid | userid* |
| 用户ID（列表/详情页） | row.userid / form.username | userid | userid |
| 用户名（列表/详情页） | row.user_name / form.user_name | user_name | user_name |

*注：`src/api/user/index.js` 中 `getUserList` 函数的参数转换逻辑会自动将前端的 `username` 参数改为后端期望的 `userid` 参数。

---

## 验证用户管理功能

登录成功后，点击 Home 页的「用户信息一览」按钮测试用户管理功能：

1. **列表页**：应显示用户列表，列包括"用户ID"（对应后端的 `userid` 字段）、"用户名"（对应 `user_name`）、"启用"、"创建时间"。
2. **搜索**：在搜索框输入用户ID（如 `admin`），点搜索，应过滤列表。
3. **新增**：点"新增用户"，应跳转到编辑页面，输入"用户ID"和"用户名"后保存。
4. **删除**：勾选列表中的用户，点头部"删除"按钮，确认后删除。
5. **详情/编辑**：点列表中的"用户ID"链接，应显示详情页面，可修改用户信息并保存。

---

## 手动测试登录（curl 命令）

如果需要直接测试后端 API，可在 PowerShell 中使用 `curl` 命令：

```powershell
# 测试登录
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"userid":"admin","password":"admin123"}'

# 预期输出应包含 success: true 和 token
```

如果返回"用户名或密码错误"，检查：
- 数据库 `tbl_user` 中是否存在该用户。
- 密码是否存储正确（若有加密，后端登录逻辑是否正确处理）。
- 用户的 `enabled` 字段是否为 1（启用）。

---

## 更新前端代码（如后端返回格式不同）

如果排查后确认后端返回的字段名或结构与前端期望不同，需要修改：

1. **后端返回字段名不同**：
   - 修改 `src/store/auth.js` 中的 `fetchCurrentUser` 函数。
   - 修改 `src/api/user/index.js` 中字段转换逻辑。
   - 修改 `src/views/system/user.vue` 和 `src/views/system/userDetail.vue` 中的字段绑定。

2. **后端 API 路径或参数不同**：
   - 修改 `src/api/auth.js` 和 `src/api/user/index.js` 中的 URL 和参数格式。

---

## 日志输出

为便于调试，前端在以下位置已添加错误/成功提示：

- **登录失败**：ElMessage 显示"用户名或密码错误"（来自后端 API 响应）。
- **列表加载失败**：ElMessage 显示"查询失败"或具体的后端错误信息。
- **保存/删除失败**：ElMessage 显示具体错误。

观察这些提示信息可快速定位问题。

---

## 联系与反馈

若按上述步骤排查后仍无法解决，请收集以下信息：

1. 浏览器 Console 中的完整错误栈。
2. Network 标签中登录请求的完整请求体和响应体。
3. 后端日志（若有可用）。
4. 使用 `curl` 手动测试后端 API 的输出（如上命令）。


