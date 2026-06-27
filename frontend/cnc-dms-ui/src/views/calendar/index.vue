<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getDormitoryList } from '@/api/dormitory'
import { getOccupancyCalendar } from '@/api/calendar'
import { getEmployeeList } from '@/api/employee'
import { getRoomList } from '@/api/room'
import { checkin } from '@/api/residentRecord'
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()

// ==================== 日付管理 ====================
const currentMonth = ref(dayjs())
const monthTitle = computed(() => currentMonth.value.format('YYYY年 M月'))

function prevMonth() { currentMonth.value = dayjs(currentMonth.value).subtract(1, 'month') }
function nextMonth() { currentMonth.value = dayjs(currentMonth.value).add(1, 'month') }
function goToday() { currentMonth.value = dayjs() }

const weekDays = ['日', '月', '火', '水', '木', '金', '土']

// ==================== 寮選択 ====================
const dormitoryOptions = ref([])
const selectedDormitoryId = ref(null)

async function fetchDormitoryOptions() {
  try {
    const res = await getDormitoryList({ pageNum: 1, pageSize: 100 })
    if (res && res.success) {
      dormitoryOptions.value = (res.body?.list || []).map(d => ({
        value: d.id,
        label: `${d.dormName}${d.region ? ' (' + d.region + ')' : ''}`,
      }))
    }
  } catch { /* ignore */ }
}

// ==================== カレンダーデータ ====================
const calendarData = ref(null)
const loading = ref(false)
const rooms = ref([])

async function fetchCalendarData() {
  if (!selectedDormitoryId.value) return
  loading.value = true
  try {
    const params = {
      dormitoryId: selectedDormitoryId.value,
      year: currentMonth.value.year(),
      month: currentMonth.value.month() + 1,
    }
    const res = await getOccupancyCalendar(params)
    if (res && res.success) {
      calendarData.value = res.body
      rooms.value = res.body?.rooms || []
    } else {
      ElMessage.error(res?.message || 'カレンダーデータの取得に失敗しました')
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗')
  } finally {
    loading.value = false
  }
}

// ==================== 曜日表示 ====================
function getDayOfWeek(day) {
  if (!calendarData.value) return ''
  const date = dayjs(`${calendarData.value.year}-${String(calendarData.value.month).padStart(2, '0')}-${String(day).padStart(2, '0')}`)
  return weekDays[date.day()]
}

function isToday(day) {
  if (!calendarData.value) return false
  const today = dayjs()
  return today.year() === calendarData.value.year &&
         today.month() + 1 === calendarData.value.month &&
         today.date() === day
}

function isWeekend(day) {
  if (!calendarData.value) return false
  const date = dayjs(`${calendarData.value.year}-${String(calendarData.value.month).padStart(2, '0')}-${String(day).padStart(2, '0')}`)
  const d = date.day()
  return d === 0 || d === 6
}

// ==================== セルスタイル ====================
function getCellClass(status, day) {
  const classes = []
  if (status === 'VACANT') classes.push('cell-vacant')
  else if (status === 'OCCUPIED') classes.push('cell-occupied')
  else if (status === 'CHECKIN') classes.push('cell-checkin')
  else if (status === 'CHECKOUT') classes.push('cell-checkout')
  if (isWeekend(day)) classes.push('cell-weekend')
  if (isToday(day)) classes.push('cell-today')
  return classes
}

// ==================== 日数 ====================
const daysInMonth = computed(() => {
  if (!calendarData.value) return 0
  return dayjs(`${calendarData.value.year}-${String(calendarData.value.month).padStart(2, '0')}-01`).daysInMonth()
})

// ==================== 状態ラベル ====================
function getStatusLabel(status) {
  const map = {
    VACANT: '空室',
    OCCUPIED: '入居中',
    CHECKIN: '入居日',
    CHECKOUT: '退去日',
  }
  return map[status] || status
}

// ==================== ツールチップ ====================
const tooltipVisible = ref(false)
const tooltipData = reactive({ roomNumber: '', day: 0, status: '' })
const tooltipPos = reactive({ x: 0, y: 0 })

function showTooltip(event, room, day, status) {
  tooltipData.roomNumber = room.roomNumber
  tooltipData.day = day
  tooltipData.status = status
  tooltipPos.x = event.clientX
  tooltipPos.y = event.clientY
  tooltipVisible.value = true
}

function hideTooltip() {
  tooltipVisible.value = false
}

// ==================== 社員選択肢 ====================
const employeeOptions = ref([])
async function fetchEmployeeOptions() {
  try {
    const res = await getEmployeeList({ pageNum: 1, pageSize: 999 })
    if (res && res.success) {
      employeeOptions.value = (res.body?.list || []).map(e => ({
        value: e.id,
        label: `${e.empName}（${e.empNo}）`,
      }))
    }
  } catch { /* ignore */ }
}

// ==================== 入居登録ダイアログ ====================
const checkinDialogVisible = ref(false)
const checkinFormRef = ref(null)
const checkinLoading = ref(false)
const checkinForm = reactive({
  employeeId: null,
  roomId: null,
  roomNumber: '',
  checkinDate: '',
  plannedCheckoutDate: '',
  notes: '',
})

const checkinRules = {
  employeeId: [{ required: true, message: '社員IDを入力してください', trigger: 'blur' }],
}

function openCheckinDialog(room, day) {
  if (!calendarData.value) return
  const dateStr = `${calendarData.value.year}-${String(calendarData.value.month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  // デフォルトの退寮予定日：入寮日から30日後
  const defaultPlanned = dayjs(dateStr).add(30, 'day').format('YYYY-MM-DD')
  checkinForm.employeeId = null
  checkinForm.roomId = room.roomId
  checkinForm.roomNumber = room.roomNumber
  checkinForm.checkinDate = dateStr
  checkinForm.plannedCheckoutDate = defaultPlanned
  checkinForm.notes = ''
  if (checkinFormRef.value) checkinFormRef.value.resetFields()
  checkinDialogVisible.value = true
}

async function handleCheckin() {
  if (!checkinFormRef.value) return
  await checkinFormRef.value.validate(async (valid) => {
    if (!valid) return
    checkinLoading.value = true
    try {
      const params = {
        employeeId: checkinForm.employeeId,
        roomId: checkinForm.roomId,
        checkinDate: checkinForm.checkinDate,
        plannedCheckoutDate: checkinForm.plannedCheckoutDate || null,
        notes: checkinForm.notes || null,
        createBy: authStore.userid || authStore.username || 'system',
      }
      const res = await checkin(params)
      if (res && res.success) {
        ElMessage.success('入居登録しました')
        checkinDialogVisible.value = false
        fetchCalendarData()
      } else {
        ElMessage.error(res?.message || '入居登録に失敗しました')
      }
    } catch (err) {
      ElMessage.error(err?.response?.data?.message || err?.message || '入居登録失敗')
    } finally {
      checkinLoading.value = false
    }
  })
}

// ==================== 初期化 ====================
onMounted(() => {
  fetchDormitoryOptions()
  fetchEmployeeOptions()
})

// 寮選択変更時 / 月変更時にデータ再取得
watch(selectedDormitoryId, () => { fetchCalendarData() })
watch(currentMonth, () => { if (selectedDormitoryId.value) fetchCalendarData() }, { deep: true })
</script>

<template>
  <div class="calendar-container">
    <el-card>
      <template #header>
        <div class="header-bar">
          <span class="header-title">寮利用状況確認</span>
          <div class="header-actions">
            <el-select
              v-model="selectedDormitoryId"
              placeholder="寮を選択してください"
              style="width: 240px"
              filterable
            >
              <el-option
                v-for="d in dormitoryOptions"
                :key="d.value"
                :value="d.value"
                :label="d.label"
              />
            </el-select>
            <el-divider direction="vertical" />
            <!-- 凡例 -->
            <div class="legend">
              <span class="legend-item"><span class="legend-dot dot-vacant"></span> 空室</span>
              <span class="legend-item"><span class="legend-dot dot-occupied"></span> 入居中</span>
              <span class="legend-item"><span class="legend-dot dot-checkin"></span> 入居日</span>
              <span class="legend-item"><span class="legend-dot dot-checkout"></span> 退去日</span>
            </div>
          </div>
        </div>
      </template>

      <!-- 月切替 -->
      <div v-if="selectedDormitoryId" class="month-nav">
        <el-button @click="prevMonth" text>
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <span class="month-title">{{ monthTitle }}</span>
        <el-button @click="nextMonth" text>
          <el-icon><ArrowRight /></el-icon>
        </el-button>
        <el-button size="small" style="margin-left: 12px" @click="goToday">今日</el-button>
      </div>

      <!-- 選択なしメッセージ -->
      <el-empty v-if="!selectedDormitoryId" description="表示する寮を選択してください" />

      <!-- カレンダーグリッド -->
      <div v-else v-loading="loading" class="calendar-wrapper">
        <!-- 横スクロール -->
        <div class="calendar-scroll">
          <div class="calendar-grid" :style="{ gridTemplateColumns: '80px repeat(' + daysInMonth + ', minmax(52px, 1fr))' }">
            <!-- ヘッダー行 -->
            <div class="grid-header grid-row-header">部屋</div>
            <div
              v-for="day in daysInMonth"
              :key="'h-' + day"
              class="grid-header grid-day-header"
              :class="{ 'day-weekend': isWeekend(day), 'day-today': isToday(day) }"
            >
              <div class="day-num">{{ day }}</div>
              <div class="day-week">{{ getDayOfWeek(day) }}</div>
            </div>

            <!-- データ行 -->
            <template v-for="room in rooms" :key="room.roomId">
              <div class="grid-row-label" :title="room.roomNumber + ' (定員:' + room.capacity + ')'">
                <span class="room-number">{{ room.roomNumber }}</span>
                <span class="room-capacity">定員{{ room.capacity }}</span>
              </div>
              <div
                v-for="day in daysInMonth"
                :key="'d-' + room.roomId + '-' + day"
                class="grid-cell"
                :class="getCellClass(room.days[day], day)"
                @mouseenter="(e) => showTooltip(e, room, day, room.days[day])"
                @mouseleave="hideTooltip"
                @click="room.days[day] === 'VACANT' ? openCheckinDialog(room, day) : null"
              >
                <span class="cell-status-label">{{ getStatusLabel(room.days[day]) }}</span>
              </div>
            </template>
          </div>
        </div>

        <!-- グリッドフッター -->
        <div class="grid-footer-info">
          <el-icon style="margin-right: 4px;"><InfoFilled /></el-icon>
          <span>空室（緑色）のセルをクリックすると入居登録ダイアログが開きます</span>
        </div>
      </div>
    </el-card>

    <!-- ツールチップ -->
    <el-card
      v-show="tooltipVisible"
      class="tooltip-card"
      :style="{ left: tooltipPos.x + 12 + 'px', top: tooltipPos.y - 10 + 'px' }"
      shadow="always"
    >
      <div class="tooltip-item">
        <div class="tooltip-header">
          <strong>{{ tooltipData.roomNumber }}号室</strong>
          <el-tag
            :type="tooltipData.status === 'VACANT' ? 'success' : tooltipData.status === 'OCCUPIED' ? 'danger' : tooltipData.status === 'CHECKIN' ? 'warning' : 'info'"
            size="small"
            effect="dark"
          >
            {{ getStatusLabel(tooltipData.status) }}
          </el-tag>
        </div>
        <div class="tooltip-body">
          {{ tooltipData.day }}日 - {{ getStatusLabel(tooltipData.status) }}
        </div>
      </div>
    </el-card>

    <!-- 入居登録ダイアログ -->
    <el-dialog v-model="checkinDialogVisible" title="入居登録（チェックイン）" width="500px" :close-on-click-modal="false">
      <el-form ref="checkinFormRef" :model="checkinForm" :rules="checkinRules" label-width="100px" status-icon>
        <el-form-item label="部屋番号">
          <el-tag type="success" size="large">{{ checkinForm.roomNumber }}号室</el-tag>
        </el-form-item>
        <el-form-item label="入寮日">
          <el-tag type="primary">{{ checkinForm.checkinDate }}</el-tag>
        </el-form-item>
        <el-form-item label="退寮予定日">
          <el-date-picker
            v-model="checkinForm.plannedCheckoutDate"
            type="date"
            placeholder="退寮予定日を選択"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :min-date="dayjs(checkinForm.checkinDate).add(1, 'day').toDate()"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="社員" prop="employeeId">
          <el-select v-model="checkinForm.employeeId" placeholder="社員を選択" filterable clearable style="width: 100%">
            <el-option v-for="emp in employeeOptions" :key="emp.value" :value="emp.value" :label="emp.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="備考">
          <el-input v-model="checkinForm.notes" type="textarea" :rows="2" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkinDialogVisible = false">キャンセル</el-button>
        <el-button type="success" :loading="checkinLoading" @click="handleCheckin">入居登録</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.calendar-container {
  position: relative;
}

.header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.legend {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #606266;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 3px;
  border: 1px solid rgba(0,0,0,0.08);
}

.dot-vacant { background: #67c23a; }
.dot-occupied { background: #f56c6c; }
.dot-checkin { background: #e6a23c; }
.dot-checkout { background: #909399; }

.month-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  padding: 8px 0;
}

.month-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 16px;
  min-width: 140px;
  text-align: center;
}

/* ===== カレンダーラッパー ===== */
.calendar-wrapper {
  position: relative;
}

.calendar-scroll {
  overflow-x: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

/* ===== カレンダーグリッド ===== */
.calendar-grid {
  display: grid;
  min-width: 100%;
}

/* ヘッダー */
.grid-header {
  padding: 8px 4px;
  text-align: center;
  font-weight: 600;
  font-size: 12px;
  background: #f5f7fa;
  border-bottom: 2px solid #e4e7ed;
  border-right: 1px solid #ebeef5;
  position: sticky;
  top: 0;
  z-index: 2;
}

.grid-row-header {
  text-align: center;
  font-size: 13px;
  color: #303133;
  position: sticky;
  left: 0;
  z-index: 3;
  border-right: 2px solid #e4e7ed;
}

.grid-day-header {
  padding: 6px 2px;
}

.day-num {
  font-size: 14px;
  color: #303133;
  line-height: 1.3;
}

.day-week {
  font-size: 10px;
  color: #909399;
  line-height: 1.2;
}

.day-weekend .day-num {
  color: #f56c6c;
}

.day-today .day-num {
  background: #409eff;
  color: #fff;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* 行ラベル */
.grid-row-label {
  padding: 6px 8px;
  background: #fafafa;
  border-bottom: 1px solid #ebeef5;
  border-right: 2px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  position: sticky;
  left: 0;
  z-index: 1;
  min-height: 44px;
}

.room-number {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.room-capacity {
  font-size: 10px;
  color: #909399;
}

/* セル */
.grid-cell {
  min-height: 44px;
  padding: 2px;
  border-right: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s, box-shadow 0.15s;
  position: relative;
}

.grid-cell:last-child {
  border-right: none;
}

.cell-status-label {
  font-size: 10px;
  font-weight: 500;
  white-space: nowrap;
}

/* 状態別カラー */
.cell-vacant {
  background: #e8f5e9;
  color: #2e7d32;
  cursor: pointer;
}

.cell-vacant:hover {
  background: #c8e6c9;
  box-shadow: inset 0 0 0 2px #67c23a;
}

.cell-occupied {
  background: #ffebee;
  color: #c62828;
  cursor: default;
}

.cell-checkin {
  background: #fff8e1;
  color: #f57f17;
  cursor: default;
}

.cell-checkout {
  background: #f3e5f5;
  color: #7b1fa2;
  cursor: default;
}

.cell-weekend {
  background: rgba(0,0,0,0.02);
}

.cell-today {
  box-shadow: inset 0 0 0 2px #409eff;
}

/* ===== ツールチップ ===== */
.tooltip-card {
  position: fixed;
  z-index: 9999;
  width: 200px;
  padding: 4px;
}

.tooltip-item {
  padding: 4px;
}

.tooltip-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.tooltip-body {
  font-size: 12px;
  color: #909399;
  padding-left: 2px;
}

/* ===== フッター情報 ===== */
.grid-footer-info {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
  gap: 4px;
}
</style>
