<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const appTitle = import.meta.env.VITE_APP_TITLE

const isCollapse = ref(false)

const menuItems = [
  {
    title: 'ホーム',
    index: '/home',
    icon: 'HomeFilled',
  },
  {
    title: 'システム管理',
    index: 'system',
    icon: 'Setting',
    children: [
      { title: 'ユーザー管理', index: '/system/user' },
      { title: '操作ログ', index: '/system/operation-log' },
    ],
  },
  {
    title: 'マスタ管理',
    index: 'master',
    icon: 'Document',
    children: [
      { title: '部門管理', index: '/master/department' },
      { title: '社員管理', index: '/master/employee' },
      { title: '寮管理', index: '/master/dormitory' },
      { title: '部屋管理', index: '/master/room' },
    ],
  },
  {
    title: '入居管理',
    index: 'resident',
    icon: 'UserFilled',
    children: [
      { title: '入居履歴', index: '/resident/records' },
      { title: '寮利用状況確認', index: '/resident/calendar' },

    ],
  },
  {
    title: '帳票・統計',
    index: '/report',
    icon: 'DataAnalysis',
  },
  {
    title: 'Excelインポート',
    index: '/import',
    icon: 'Upload',
  },
]

const activeMenu = computed(() => {
  // Find the matching menu item for the current route
  const path = route.path
  // Check exact match first, then parent match
  for (const item of menuItems) {
    if (item.index === path) return item.index
    if (item.children) {
      for (const child of item.children) {
        if (path.startsWith(child.index)) return child.index
      }
    }
  }
  return path
})

function handleMenuSelect(index) {
  router.push(index)
}

function handleLogout() {
  authStore.logout()
  ElMessage.success('ログアウトしました')
  router.push('/login')
}
</script>

<template>
  <div class="layout-container">
    <!-- サイドバー -->
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      :router="false"
      class="layout-sidebar"
      background-color="#001529"
      text-color="#ffffffbf"
      active-text-color="#fff"
      @select="handleMenuSelect"
    >
      <div class="sidebar-logo" @click="router.push('/home')">
        <span v-if="!isCollapse" class="logo-text">{{ appTitle }}</span>
        <span v-else class="logo-text logo-small">DM</span>
      </div>

      <template v-for="item in menuItems" :key="item.index">
        <el-sub-menu v-if="item.children" :index="item.index">
          <template #title>
            <el-icon v-if="item.icon">
              <component :is="item.icon" />
            </el-icon>
            <span>{{ item.title }}</span>
          </template>
          <el-menu-item
            v-for="child in item.children"
            :key="child.index"
            :index="child.index"
          >
            {{ child.title }}
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item v-else :index="item.index">
          <el-icon v-if="item.icon">
            <component :is="item.icon" />
          </el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </template>
    </el-menu>

    <!-- メインコンテンツエリア -->
    <div class="layout-main">
      <!-- ヘッダー -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-button
            :icon="isCollapse ? 'Expand' : 'Fold'"
            text
            @click="isCollapse = !isCollapse"
          />
          <el-breadcrumb separator="/" class="header-breadcrumb">
            <el-breadcrumb-item :to="{ path: '/home' }">ホーム</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.path !== '/home'">
              {{ route.meta?.title || route.name }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="welcome-text" v-if="authStore.username">
            {{ authStore.username }}
          </span>
          <el-dropdown trigger="click">
            <el-avatar :size="32" icon="UserFilled" class="header-avatar" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  ログアウト
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- ページコンテンツ -->
      <el-main class="layout-content">
        <router-view />
      </el-main>
    </div>
  </div>
</template>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.layout-sidebar {
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  border-right: none;
  flex-shrink: 0;
  transition: width 0.3s;
}

.sidebar-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-text {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
}

.logo-small {
  font-size: 18px;
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 0 16px;
  height: 60px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-breadcrumb {
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.welcome-text {
  color: #606266;
  font-size: 14px;
}

.header-avatar {
  cursor: pointer;
  background: #409eff;
}

.layout-content {
  padding: 20px;
  background: #f0f2f5;
  overflow-y: auto;
  flex: 1;
}
</style>
