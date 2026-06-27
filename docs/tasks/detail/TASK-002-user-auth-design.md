# 用户认证模块详细设计

## 功能说明

登录成功后生成JWT Token

## 流程

用户输入用户名密码

↓

LoginController

↓

UserService

↓

UserMapper

↓

数据库校验

↓

生成JWT

↓

返回Token

## Token有效期

8小时

## Header

Authorization

Bearer Token

## 返回格式

{
  "success": true,
  "token": "xxxxx"
}