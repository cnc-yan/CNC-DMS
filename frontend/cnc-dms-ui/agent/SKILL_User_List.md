1. 页面跳转
#1-1. 用户登录认证功能模块基础上，显示主菜单页面。登录认证成功后的跳转页面上添加显示主菜单
      显示主菜单显示 以按钮模式显示（用户信息一览），其他功能的菜单显示预留位置，方便其他功能模块开发后添加

2. 页面显示
#2-1. 点击按钮（用户信息一览）后，跳转到用户信息一览，并调用API /api/user/list显示用户一览
#2-2. 显示用户一览的用户ID以链接的形式显示，一览包含 用户ID，用户名，创建日期
##2-2-1. 用户一览的用户ID前面显示CHECK BOX，选中时，按下删除按钮，可以删除该条数据，可以多条删除
#2-3. 用户ID链接点击后，跳转到用户信息画面，调用API /api/user显示用户详细信息
##2-3-1. 用户详细信息画面，显示删除按钮，更新按钮，可以删除，更新该条用户数据。
#2-4. 点击返回按钮，回到前一画面


3. 用户信息一览页面显示数据调用如下API
#获取 JWT Token（登录）
API 信息:
方法: POST
URL: http://localhost:8080/api/auth/login
Content-Type: application/json


#执行用户 CRUD API 调用
API 1⃣: 查询单个用户
项目
值
方法
GET
URL http://localhost:8080/api/user/1
响应RESPONSE BODY
{
  "success": true,
  "message": "查询成功",
  "error": null,
  "body": {
    "userid": "admin",
    "userName": "管理员",
    "enabled": true,
    "createBy": "system",
    "createTime": "2026-06-05T10:00:00",
    "password": "admin123",
    "updateBy": null,
    "updateTime": "2026-06-05T10:00:00"
  }
}


API 2:分页查询用户列表
项目
值
方法
GET
URL http://localhost:8080/api/user/list
Query Params
pageNum=1&pageSize=10

响应RESPONSE BODY
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
        "userName": "管理员",
        "enabled": true,
        "createTime": "2026-06-05T10:00:00"
      },
      {
        "userid": "user1",
        "userName": "用户1",
        "enabled": true,
        "createTime": "2026-06-05T11:00:00"
      }
    ]
  }
}

API 3⃣:按用户名查询
项目
值
方法
GET
URL http://localhost:8080/api/user/list
Query Params
userid=admin&pageNum=1&pageSize=10
响应RESPONSE BODY
{
  "success": true,
  "message": "查询成功",
  "body": {
    "pageNum": 1,
    "pageSize": 10,
    "total": 1,
    "totalPage": 1,
    "list": [
      {
        "userid": "admin",
        "userName": "管理员",
        "enabled": true
      }
    ]
  }
}


API 4  更新用户信息
项目
值
方法
PUT
URL http://localhost:8080/api/user/update
Content-Type
application/json

RESPONSE BODY   
{
  "success": true,
  "message": "查询成功",
  "body": {
    "total": 1,
    "list": [
      {
        "userid": "admin",
        "userName": "管理员",
        "enabled": true
      }
    ]
  }
}


API 5  删除用户
项目
值
方法
DELETE
URL http://localhost:8080/api/user/2

Request BODY
   {
     "userid": admin,
     "userName": "系统管理员（已更新）",
     "enabled": true,
     "updateBy": "admin"
   }

RESPONSE BODY   
   {
  "success": true,
  "message": "用户信息更新成功",
  "error": null,
  "body": "成功"
   }
