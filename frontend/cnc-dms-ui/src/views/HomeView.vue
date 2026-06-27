<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

onMounted(async () => {
  await authStore.fetchCurrentUser()
})

const quickLinks = [
  { title: 'ユーザー管理', desc: 'システムユーザーの管理', path: '/system/user', color: '#409eff', icon: 'User' },
  { title: '部門管理', desc: '所属部門のマスタ管理', path: '/master/department', color: '#67c23a', icon: 'OfficeBuilding' },
  { title: '社員管理', desc: '社員情報のマスタ管理', path: '/master/employee', color: '#e6a23c', icon: 'Avatar' },
  { title: '寮管理', desc: '寮施設のマスタ管理', path: '/master/dormitory', color: '#f56c6c', icon: 'HomeFilled' },
  { title: '部屋管理', desc: '各部屋情報の管理', path: '/master/room', color: '#909399', icon: 'ScaleToOriginal' },
  { title: '入居履歴', desc: '入退寮の履歴管理', path: '/resident/records', color: '#b37feb', icon: 'Document' },
  { title: '寮利用状況確認', desc: 'カレンダーで入居状況を確認', path: '/resident/calendar', color: '#36cfc9', icon: 'Calendar' },
  { title: '操作ログ', desc: 'システム操作の監査ログ', path: '/system/operation-log', color: '#2f54eb', icon: 'View' },
  { title: '変更履歴', desc: 'データ変更の履歴追跡', path: '/system/change-history', color: '#722ed1', icon: 'Timer' },
  { title: 'Excelインポート', desc: 'Excelデータ取込・履歴管理', path: '/import', color: '#1890ff', icon: 'Upload' },
  { title: '帳票・統計', desc: 'サマリー・稼働率・請求レポート', path: '/report', color: '#fa541c', icon: 'DataAnalysis' },
]
</script>

<template>
  <div class="home-container">
    <!-- ウェルカムバナー -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h1 class="welcome-title">
          ようこそ、{{ authStore.username || 'ユーザー' }} さん
        </h1>
        <p class="welcome-desc">CNC-DMS 寮管理システムへようこそ。以下のメニューから操作を選択してください。</p>
      </div>
    </div>

    <!-- クイックリンク -->
    <div class="quick-links">
      <div
        v-for="link in quickLinks"
        :key="link.path"
        class="quick-link-card"
        :style="{ borderTopColor: link.color }"
        @click="router.push(link.path)"
      >
        <div class="card-icon" :style="{ backgroundColor: link.color + '15', color: link.color }">
          <el-icon :size="28">
            <component :is="link.icon" />
          </el-icon>
        </div>
        <div class="card-body">
          <h3 class="card-title">{{ link.title }}</h3>
          <p class="card-desc">{{ link.desc }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  max-width: 1000px;
  margin: 0 auto;
}

.welcome-banner {
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  border-radius: 8px;
  padding: 32px;
  margin-bottom: 24px;
  color: #fff;
}

.welcome-title {
  margin: 0 0 8px 0;
  font-size: 22px;
  font-weight: 600;
}

.welcome-desc {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
}

.quick-links {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.quick-link-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  border-top: 3px solid #409eff;
  cursor: pointer;
  transition: all 0.25s ease;
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.quick-link-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.card-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-body {
  flex: 1;
  min-width: 0;
}

.card-title {
  margin: 0 0 4px 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.card-desc {
  margin: 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.4;
}
</style>
