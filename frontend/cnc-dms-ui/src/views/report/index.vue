<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSummaryReport, getOccupancyReport } from '@/api/report'

const activeTab = ref('summary')
const summaryLoading = ref(false)
const occupancyLoading = ref(false)
// ========== サマリー ==========
const summaryData = ref(null)
const summaryCards = ref([])

async function fetchSummary() {
  summaryLoading.value = true
  try {
    const res = await getSummaryReport()
    if (res && res.success) {
      summaryData.value = res.body
      const d = res.body || {}
      summaryCards.value = [
        { title: '総寮数', value: d.totalDormitories ?? '-', icon: 'HomeFilled', color: '#409eff' },
        { title: '総部屋数', value: d.totalRooms ?? '-', icon: 'ScaleToOriginal', color: '#67c23a' },
        { title: '空室数', value: d.vacantRooms ?? '-', icon: 'CircleCheck', color: '#e6a23c' },
        { title: '現在入居者', value: d.currentResidents ?? '-', icon: 'UserFilled', color: '#f56c6c' },
        { title: '未入居社員', value: d.employeesWithoutResident ?? '-', icon: 'Warning', color: '#909399' },

      ]
    } else ElMessage.error(res?.message || 'サマリー取得に失敗しました')
  } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗') }
  finally { summaryLoading.value = false }
}

// ========== 稼働率レポート ==========
const occupancySearch = reactive({ region: '' })
const occupancyTable = ref([])

async function fetchOccupancy() {
  occupancyLoading.value = true
  try {
    const params = {}
    if (occupancySearch.region) params.region = occupancySearch.region
    const res = await getOccupancyReport(params)
    if (res && res.success) {
      occupancyTable.value = res.body || []
    } else ElMessage.error(res?.message || '稼働率レポート取得に失敗しました')
  } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗') }
  finally { occupancyLoading.value = false }
}

onMounted(() => {
  fetchSummary()
  fetchOccupancy()
})
</script>

<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header-bar">
          <span class="header-title">帳票・統計</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- サマリー -->
        <el-tab-pane label="ダッシュボードサマリー" name="summary">
          <div v-loading="summaryLoading">
            <div class="summary-grid" v-if="summaryCards.length">
              <div v-for="card in summaryCards" :key="card.title" class="summary-card" :style="{ borderTopColor: card.color }">
                <div class="summary-card-icon" :style="{ color: card.color }">
                  <el-icon :size="32"><component :is="card.icon" /></el-icon>
                </div>
                <div class="summary-card-body">
                  <div class="summary-card-value">{{ card.value }}</div>
                  <div class="summary-card-title">{{ card.title }}</div>
                </div>
              </div>
            </div>
            <el-empty v-else description="データがありません" />
          </div>
        </el-tab-pane>

        <!-- 稼働率レポート -->
        <el-tab-pane label="寮別稼働率" name="occupancy">
          <div class="search-area">
            <el-form :model="occupancySearch" inline>
              <el-form-item label="地域">
                <el-input v-model="occupancySearch.region" placeholder="絞り込み" clearable @keyup.enter="fetchOccupancy" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="fetchOccupancy">検索</el-button>
                <el-button @click="occupancySearch.region = ''; fetchOccupancy()">リセット</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table :data="occupancyTable" border v-loading="occupancyLoading">
            <el-table-column label="寮名" min-width="160" prop="dormName" />
            <el-table-column label="地域" width="80" prop="region" />
            <el-table-column label="総部屋数" width="80" prop="totalRooms" align="center" />
            <el-table-column label="入居中" width="80" prop="occupiedRooms" align="center" />
            <el-table-column label="空室" width="80" prop="vacantRooms" align="center" />
            <el-table-column label="稼働率" width="100" align="center">
              <template #default="{ row }">
                <el-progress :percentage="Math.round((row.occupancyRate || 0) * 100)" :status="row.occupancyRate >= 0.8 ? 'success' : row.occupancyRate >= 0.5 ? 'warning' : 'exception'" />
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>


      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.page-container { max-width: 1200px; }
.header-bar { display: flex; align-items: center; justify-content: space-between; }
.header-title { font-size: 16px; font-weight: 600; }
.search-area { margin-bottom: 16px; }

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.summary-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  border-top: 3px solid #409eff;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.summary-card-icon {
  flex-shrink: 0;
}

.summary-card-body {
  flex: 1;
  min-width: 0;
}

.summary-card-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.summary-card-title {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
</style>
