Create Vue3 authentication module for CNC Dormitory Management System.

Tech Stack:

* Vue3
* Vite
* Pinia
* Vue Router
* Axios
* Element Plus

Backend API:

POST http://localhost:8080/api/auth/login

Request:
{
"username":"admin",
"password":"admin123"
}

Response:
{
"success":true,
"message":"登录成功",
"token":"xxxxx"
}

GET http://localhost:8080/api/auth/me

Response:
{
"username":"admin"
}

Generate:

src/api/auth.js

src/store/auth.js

src/router/index.js

src/views/LoginView.vue

src/views/HomeView.vue

Update:

src/App.vue

src/main.js

Requirements:

1. Login page using Element Plus
2. Username and password form
3. Call login API
4. Save token into localStorage
5. Configure axios interceptor
6. Automatically send Authorization header
7. Redirect to HomeView after login success
8. HomeView calls /api/auth/me
9. Show current username
10. Use Composition API
11. Use clean enterprise code style
12. Add logout button
13. Logout removes token and returns LoginView
