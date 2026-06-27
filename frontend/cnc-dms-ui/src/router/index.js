import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import HomeView from '@/views/HomeView.vue'
import UserList from '@/views/system/user.vue'
import UserDetail from '@/views/system/userDetail.vue'
import DepartmentList from '@/views/department/index.vue'
import DepartmentDetail from '@/views/department/detail.vue'
import EmployeeList from '@/views/employee/index.vue'
import EmployeeDetail from '@/views/employee/detail.vue'
import DormitoryList from '@/views/dormitory/index.vue'
import DormitoryDetail from '@/views/dormitory/detail.vue'
import RoomList from '@/views/room/index.vue'
import RoomDetail from '@/views/room/detail.vue'
import ResidentRecordList from '@/views/residentRecord/index.vue'
import ResidentRecordDetail from '@/views/residentRecord/detail.vue'
import CalendarView from '@/views/calendar/index.vue'
import OperationLogList from '@/views/operationLog/index.vue'
import ImportLogList from '@/views/importLog/index.vue'
import ReportView from '@/views/report/index.vue'

const routes = [
  {
    path: '/',
    redirect: '/home',
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { guest: true, layout: false, title: 'ログイン' },
  },
  {
    path: '/home',
    name: 'Home',
    component: HomeView,
    meta: { requiresAuth: true, title: 'ホーム' },
  },
  // ========== システム管理 ==========
  {
    path: '/system/user',
    name: 'UserList',
    component: UserList,
    meta: { requiresAuth: true, title: 'ユーザー管理' },
  },
  {
    path: '/system/user/new',
    redirect: '/system/user',
    meta: { requiresAuth: true },
  },
  {
    path: '/system/user/:id',
    name: 'UserDetail',
    component: UserDetail,
    meta: { requiresAuth: true, title: 'ユーザー詳細' },
  },
  // ========== マスタ管理：部門 ==========
  {
    path: '/master/department',
    name: 'DepartmentList',
    component: DepartmentList,
    meta: { requiresAuth: true, title: '部門管理' },
  },
  {
    path: '/master/department/:id',
    name: 'DepartmentDetail',
    component: DepartmentDetail,
    meta: { requiresAuth: true, title: '部門詳細' },
  },
  // ========== マスタ管理：社員 ==========
  {
    path: '/master/employee',
    name: 'EmployeeList',
    component: EmployeeList,
    meta: { requiresAuth: true, title: '社員管理' },
  },
  {
    path: '/master/employee/:id',
    name: 'EmployeeDetail',
    component: EmployeeDetail,
    meta: { requiresAuth: true, title: '社員詳細' },
  },
  // ========== マスタ管理：寮 ==========
  {
    path: '/master/dormitory',
    name: 'DormitoryList',
    component: DormitoryList,
    meta: { requiresAuth: true, title: '寮管理' },
  },
  {
    path: '/master/dormitory/:id',
    name: 'DormitoryDetail',
    component: DormitoryDetail,
    meta: { requiresAuth: true, title: '寮詳細' },
  },
  // ========== マスタ管理：部屋 ==========
  {
    path: '/master/room',
    name: 'RoomList',
    component: RoomList,
    meta: { requiresAuth: true, title: '部屋管理' },
  },
  {
    path: '/master/room/:id',
    name: 'RoomDetail',
    component: RoomDetail,
    meta: { requiresAuth: true, title: '部屋詳細' },
  },
  // ========== 入居管理 ==========
  {
    path: '/resident/records',
    name: 'ResidentRecordList',
    component: ResidentRecordList,
    meta: { requiresAuth: true, title: '入居履歴' },
  },
  {
    path: '/resident/records/:id',
    name: 'ResidentRecordDetail',
    component: ResidentRecordDetail,
    meta: { requiresAuth: true, title: '入居履歴詳細' },
  },
  {
    path: '/resident/calendar',
    name: 'CalendarView',
    component: CalendarView,
    meta: { requiresAuth: true, title: '寮利用状況確認' },
  },
  // ========== 監査ログ ==========
  {
    path: '/system/operation-log',
    name: 'OperationLogList',
    component: OperationLogList,
    meta: { requiresAuth: true, title: '操作ログ' },
  },
  // ========== Excelインポート ==========
  {
    path: '/import',
    name: 'ImportLogList',
    component: ImportLogList,
    meta: { requiresAuth: true, title: 'Excelインポート' },
  },
  // ========== 帳票・統計 ==========
  {
    path: '/report',
    name: 'ReportView',
    component: ReportView,
    meta: { requiresAuth: true, title: '帳票・統計' },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL || '/'),
  routes,
})

// --- Navigation Guard ---
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  const requiresAuth = to.meta.requiresAuth;
  const isGuest = to.meta.guest;

  if (requiresAuth && !token) {
    // If the route requires authentication and no token is found, redirect to login
    next('/login');
  } else if (isGuest && token) {
    // If the route is for guests (like login) and a token exists, redirect to home
    next('/home');
  } else {
    // Otherwise, proceed to the next route
    next();
  }
});

export default router
