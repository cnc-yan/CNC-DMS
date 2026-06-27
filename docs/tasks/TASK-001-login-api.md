# TASK-001 登录API

# 目标

实现基础登录功能。

---

# API

POST /api/auth/login

---

# Request

```json id="lg001"
{
  "username": "admin",
  "password": "123456"
}
```

---

# Response

```json id="lg002"
{
  "success": true,
  "data": {
    "token": "jwt-token"
  }
}
```

---

# 技术要求

* SpringBoot 3
* Java 21
* MyBatis
* MySQL 8

---

# 架构要求

必须包含：

* Controller
* Service
* Mapper
* DTO
* Entity

---

# JWT要求

* 登录成功后返回JWT
* 暂不实现RefreshToken

---

# 密码规则

第一版：

* 明文比较
* 后续再升级BCrypt

---

# MVP阶段不实现

* OAuth
* RBAC
* 多角色
* 登录失败锁定
* 二次认证

---

# AI生成要求

请生成：

* package结构
* import完整
* Controller完整
* Service完整
* DTO完整
* MyBatis Mapper完整
* SQL完整
* JWT工具类
* JUnit5测试
