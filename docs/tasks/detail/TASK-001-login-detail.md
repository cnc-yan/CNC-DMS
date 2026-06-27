# TASK-001 登录API 详细设计

## 业务流程

用户输入：

- username
- password

↓

系统验证用户是否存在

↓

验证密码是否正确

↓

生成JWT

↓

返回Token

---

## 数据流程

tbl_user

↓

UserMapper

↓

AuthService

↓

JwtUtil

↓

AuthController

---

## Controller设计

AuthController

POST /api/auth/login

---

## Request DTO

LoginRequest

字段：

username
password

---

## Response DTO

LoginResponse

字段：

token

---

## Entity

User

对应：

tbl_user

---

## Mapper

UserMapper

方法：

findByUsername()

---

## Service

AuthService

方法：

login()

---

## JWT

JwtUtil

方法：

generateToken()

validateToken()

---

## 异常处理

用户不存在

返回：

401

---

密码错误

返回：

401

---

## MVP规则

密码明文比较

暂不加密

---

## 后续版本

BCrypt

RBAC

RefreshToken

OAuth2