<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getResidentRecordList, getResidentRecord, checkin, checkout, deleteResidentRecord, transferRoom, getLongTermUsageInfo } from '@/api/residentRecord'
import { getEmployeeList } from '@/api/employee'
import { getRoomList } from '@/api/room'
import { getDormitoryList } from '@/api/dormitory'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

const searchForm = reactive({
  employeeId: null,
  roomId: null,
  isActive: null,
})

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const selectedRows = ref([])

// ==================== 選択肢データ ====================
const employeeOptions = ref([])
const roomOptions = ref([])
const dormitoryOptions = ref([])

async function fetchEmployeeOptions() {
  try {
    const res = await getEmployeeList({ pageNum: 1, pageSize: 999 })
    if (res && res.success) {
      employeeOptions.value = (res.body?.list || []).map(e => ({
        value: e.id, label: `${e.empName}（${e.empNo}）`,
      }))
    }
  } catch { /* ignore */ }
}
async function fetchRoomOptions() {
  try {
    const res = await getRoomList({ pageNum: 1, pageSize: 999 })
    if (res && res.success) {
      roomOptions.value = (res.body?.list || []).map(r => ({
        value: r.id, label: `${r.dormName || ''} ${r.roomNumber}号室`.trim(),
      }))
    }
  } catch { /* ignore */ }
}
async function fetchDormitoryOptions() {
  try {
    const res = await getDormitoryList({ pageNum: 1, pageSize: 999 })
    if (res && res.success) {
      dormitoryOptions.value = (res.body?.list || []).map(d => ({
        value: d.id, label: d.dormName,
      }))
    }
  } catch { /* ignore */ }
}

// ==================== チェックインダイアログ ====================
const checkinDialogVisible = ref(false)
const checkinFormRef = ref(null)
const checkinLoading = ref(false)
const checkinForm = reactive({
  employeeId: null,
  roomId: null,
  checkinDate: '',
  plannedCheckoutDate: '',
  notes: '',
})

const checkinRules = {
  employeeId: [{ required: true, message: '社員を選択してください', trigger: 'change' }],
  roomId: [{ required: true, message: '部屋を選択してください', trigger: 'change' }],
  checkinDate: [{ required: true, message: '入寮日を選択してください', trigger: 'change' }],
}

// ==================== チェックアウトダイアログ ====================
const checkoutDialogVisible = ref(false)
const checkoutRecordId = ref(null)
const checkoutEmployeeName = ref('')
const checkoutFormRef = ref(null)
const checkoutLoading = ref(false)
const checkoutDate = ref('')

const checkoutRules = {
  checkoutDate: [{ required: true, message: '退寮日を選択してください', trigger: 'change' }],
}

async function fetchList() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (searchForm.employeeId) params.employeeId = searchForm.employeeId
    if (searchForm.roomId) params.roomId = searchForm.roomId
    if (searchForm.isActive !== null && searchForm.isActive !== '') params.isActive = searchForm.isActive

    const res = await getResidentRecordList(params)
    if (res && res.success) {
      const body = res.body || {}
      tableData.value = body.list || []
      total.value = body.total || 0
    } else ElMessage.error(res?.message || '検索に失敗しました')
  } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗') }
  finally { loading.value = false }
}

function handleSearch() { pageNum.value = 1; fetchList() }
function handleReset() {
  searchForm.employeeId = null; searchForm.roomId = null; searchForm.isActive = null
  handleSearch()
}
function handleSizeChange(size) { pageSize.value = size; fetchList() }
function handleCurrentChange(page) { pageNum.value = page; fetchList() }
function handleSelectionChange(val) { selectedRows.value = val }
function handleRowClick(row) { router.push(`/resident/records/${row.id}`) }

// ==================== チェックイン ====================
function openCheckinDialog() {
  checkinForm.employeeId = null; checkinForm.roomId = null
  checkinForm.checkinDate = ''; checkinForm.notes = ''
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
        fetchList()
      } else {
        ElMessage.error(res?.message || '入居登録に失敗しました')
      }
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || '入居登録失敗'
      ElMessage.error(msg)
    } finally {
      checkinLoading.value = false
    }
  })
}

// ==================== チェックアウト ====================
function openCheckoutDialog(row) {
  checkoutRecordId.value = row.id
  checkoutEmployeeName.value = `記録ID: ${row.id}`
  checkoutDate.value = new Date().toISOString().split('T')[0] // デフォルト: 当日
  if (checkoutFormRef.value) checkoutFormRef.value.resetFields()
  checkoutDialogVisible.value = true
}

async function handleCheckout() {
  if (!checkoutFormRef.value) return
  await checkoutFormRef.value.validate(async (valid) => {
    if (!valid) return
    checkoutLoading.value = true
    try {
      const params = {
        checkoutDate: checkoutDate.value,
        updateBy: authStore.userid || authStore.username || 'system',
      }
      const res = await checkout(checkoutRecordId.value, params)
      if (res && res.success) {
        ElMessage.success('退寮処理しました')
        checkoutDialogVisible.value = false
        fetchList()
      } else {
        ElMessage.error(res?.message || '退寮処理に失敗しました')
      }
    } catch (err) {
      ElMessage.error(err?.response?.data?.message || err?.message || '退寮処理失敗')
    } finally {
      checkoutLoading.value = false
    }
  })
}

// ==================== 部屋移動 ====================
const transferDialogVisible = ref(false)
const transferRecordId = ref(null)
const transferFormRef = ref(null)
const transferLoading = ref(false)
const transferForm = reactive({
  newRoomId: null,
  transferDate: '',
})

const transferRules = {
  newRoomId: [{ required: true, message: '新しい部屋を選択してください', trigger: 'change' }],
  transferDate: [{ required: true, message: '移動日を選択してください', trigger: 'change' }],
}

function openTransferDialog(row) {
  transferRecordId.value = row.id
  transferForm.newRoomId = null
  transferForm.transferDate = new Date().toISOString().split('T')[0]
  if (transferFormRef.value) transferFormRef.value.resetFields()
  transferDialogVisible.value = true
}

async function handleTransfer() {
  if (!transferFormRef.value) return
  await transferFormRef.value.validate(async (valid) => {
    if (!valid) return
    transferLoading.value = true
    try {
      const params = {
        currentRecordId: transferRecordId.value,
        newRoomId: transferForm.newRoomId,
        transferDate: transferForm.transferDate,
        updateBy: authStore.userid || authStore.username || 'system',
      }
      const res = await transferRoom(params)
      if (res && res.success) {
        ElMessage.success('部屋移動しました')
        transferDialogVisible.value = false
        fetchList()
      } else {
        ElMessage.error(res?.message || '部屋移動に失敗しました')
      }
    } catch (err) {
      ElMessage.error(err?.response?.data?.message || err?.message || '部屋移動失敗')
    } finally {
      transferLoading.value = false
    }
  })
}

// ==================== 長期利用情報 ====================
const longTermDialogVisible = ref(false)
const longTermInfo = ref(null)
const longTermLoading = ref(false)

async function openLongTermDialog(row) {
  longTermDialogVisible.value = true
  longTermInfo.value = null
  longTermLoading.value = true
  try {
    const res = await getLongTermUsageInfo(row.employeeId)
    if (res && res.success) {
      longTermInfo.value = res.body
    } else {
      ElMessage.error(res?.message || '長期利用情報の取得に失敗しました')
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗')
  } finally {
    longTermLoading.value = false
  }
}

// ==================== 削除 ====================
function handleDelete(row) {
  ElMessageBox.confirm(`入居履歴(ID: ${row.id}) を削除してもよろしいですか？`, '削除確認',
    { type: 'warning', confirmButtonText: '削除', cancelButtonText: 'キャンセル' })
    .then(async () => {
      try {
        const res = await deleteResidentRecord(row.id)
        if (res && res.success) { ElMessage.success('削除しました'); fetchList() }
        else ElMessage.error(res?.message || '削除に失敗しました')
      } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗') }
    }).catch(() => {})
}

// ==================== 一括削除 ====================
function handleDeleteSelected() {
  if (!selectedRows.value.length) {
    ElMessage.warning('削除する入居履歴を選択してください')
    return
  }
  ElMessageBox.confirm(
    `選択した ${selectedRows.value.length} 件の入居履歴を削除してもよろしいですか？`,
    '一括削除確認',
    { type: 'warning', confirmButtonText: '削除', cancelButtonText: 'キャンセル' },
  )
    .then(async () => {
      let failCount = 0
      const failedNames = []
      for (const row of selectedRows.value) {
        try {
          const res = await deleteResidentRecord(row.id)
          if (!res || !res.success) {
            failCount++
            failedNames.push(`ID:${row.id}: ${res?.error || res?.message || '不明なエラー'}`)
          }
        } catch { failCount++; failedNames.push(`ID:${row.id}: 通信エラー`) }
      }
      if (failCount === 0) ElMessage.success('削除しました')
      else ElMessage.warning(`${failCount} 件の削除に失敗しました\n${failedNames.join('\n')}`)
      selectedRows.value = []
      fetchList()
    })
    .catch(() => {})
}

onMounted(() => {
  fetchList()
  fetchEmployeeOptions()
  fetchRoomOptions()
  fetchDormitoryOptions()
})
</script>

<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header-bar">
          <span class="header-title">入居履歴</span>
          <div class="header-actions">
            <el-button type="success" @click="openCheckinDialog">
              <el-icon style="margin-right:4px"><Plus /></el-icon>入居登録
            </el-button>
            <el-button type="danger" plain @click="handleDeleteSelected">一括削除</el-button>
          </div>
        </div>
      </template>

      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="社員">
            <el-select v-model="searchForm.employeeId" placeholder="すべて" filterable clearable style="width: 200px">
              <el-option v-for="emp in employeeOptions" :key="emp.value" :value="emp.value" :label="emp.label" />
            </el-select>
          </el-form-item>
          <el-form-item label="部屋">
            <el-select v-model="searchForm.roomId" placeholder="すべて" filterable clearable style="width: 200px">
              <el-option v-for="r in roomOptions" :key="r.value" :value="r.value" :label="r.label" />
            </el-select>
          </el-form-item>
          <el-form-item label="状態">
            <el-select v-model="searchForm.isActive" placeholder="すべて" clearable style="width: 130px">
              <el-option :value="1" label="入居中" />
              <el-option :value="0" label="退寮済" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">検索</el-button>
            <el-button @click="handleReset">リセット</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" border v-loading="loading"
        @selection-change="handleSelectionChange" @row-dblclick="handleRowClick">
        <el-table-column type="selection" width="50" />
        <el-table-column label="ID" width="65" prop="id" />
        <el-table-column label="社員" min-width="150">
          <template #default="{ row }">{{ row.empName || '' }}（{{ row.empNo || row.employeeId }}）</template>
        </el-table-column>
        <el-table-column label="部屋番号" width="100" prop="roomNumber" />
        <el-table-column label="寮ID" width="70" prop="dormitoryId" />
        <el-table-column label="寮名称" min-width="120" prop="dormName" />
        <el-table-column label="入寮日" width="120" prop="checkinDate" />
        <el-table-column label="退寮予定日" width="120">
          <template #default="{ row }">{{ row.plannedCheckoutDate || '-' }}</template>
        </el-table-column>
        <el-table-column label="退寮日" width="120">
          <template #default="{ row }">{{ row.checkoutDate || '入居中' }}</template>
        </el-table-column>
        <el-table-column label="状態" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isActive === 1 ? 'success' : 'info'" size="small">
              {{ row.isActive === 1 ? '入居中' : '退寮済' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="利用料金" width="140" align="right">
          <template #default="{ row }">
            <span :style="{ color: row.totalFee != null ? '#409eff' : '#c0c4cc' }">
              {{ row.totalFee != null ? '¥' + Number(row.totalFee).toLocaleString() : '未計算' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="備考" min-width="150" prop="notes" show-overflow-tooltip />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.isActive === 1" type="warning" link size="small" @click="openCheckoutDialog(row)">
              退寮
            </el-button>
            <el-button v-if="row.isActive === 1" type="primary" link size="small" @click="openTransferDialog(row)">
              部屋移動
            </el-button>
            <el-button type="info" link size="small" @click="openLongTermDialog(row)">
              長期利用
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">削除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-area">
        <el-pagination v-model:page-size="pageSize" v-model:current-page="pageNum" :total="total"
          layout="prev, pager, next, jumper, sizes, total"
          @current-change="handleCurrentChange" @size-change="handleSizeChange" />
      </div>
    </el-card>

    <!-- 入居登録ダイアログ -->
    <el-dialog v-model="checkinDialogVisible" title="入居登録（チェックイン）" width="500px" :close-on-click-modal="false">
      <el-form ref="checkinFormRef" :model="checkinForm" :rules="checkinRules" label-width="100px" status-icon>
        <el-form-item label="社員" prop="employeeId">
          <el-select v-model="checkinForm.employeeId" placeholder="社員を選択" filterable clearable style="width: 100%">
            <el-option v-for="emp in employeeOptions" :key="emp.value" :value="emp.value" :label="emp.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="部屋" prop="roomId">
          <el-select v-model="checkinForm.roomId" placeholder="部屋を選択" filterable clearable style="width: 100%">
            <el-option v-for="r in roomOptions" :key="r.value" :value="r.value" :label="r.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="入寮日" prop="checkinDate">
          <el-date-picker v-model="checkinForm.checkinDate" type="date" value-format="YYYY-MM-DD" placeholder="選択" style="width: 100%" />
        </el-form-item>
        <el-form-item label="退寮予定日">
          <el-date-picker v-model="checkinForm.plannedCheckoutDate" type="date" value-format="YYYY-MM-DD"
            placeholder="任意（省略可）" style="width: 100%" :min="checkinForm.checkinDate || undefined" />
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

    <!-- 退寮処理ダイアログ -->
    <el-dialog v-model="checkoutDialogVisible" title="退寮処理（チェックアウト）" width="450px" :close-on-click-modal="false">
      <el-form ref="checkoutFormRef" :model="{ checkoutDate }" :rules="checkoutRules" label-width="100px" status-icon>
        <el-form-item label="対象記録">
          <el-tag type="info">{{ checkoutEmployeeName }}</el-tag>
        </el-form-item>
        <el-form-item label="退寮日" prop="checkoutDate">
          <el-date-picker v-model="checkoutDate" type="date" value-format="YYYY-MM-DD" placeholder="選択（省略時は当日）" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkoutDialogVisible = false">キャンセル</el-button>
        <el-button type="warning" :loading="checkoutLoading" @click="handleCheckout">退寮処理</el-button>
      </template>
    </el-dialog>

    <!-- 部屋移動ダイアログ -->
    <el-dialog v-model="transferDialogVisible" title="部屋移動" width="500px" :close-on-click-modal="false">
      <el-form ref="transferFormRef" :model="transferForm" :rules="transferRules" label-width="120px" status-icon>
        <el-form-item label="対象記録ID">
          <el-tag type="primary">{{ transferRecordId }}</el-tag>
        </el-form-item>
        <el-form-item label="新しい部屋" prop="newRoomId">
          <el-select v-model="transferForm.newRoomId" placeholder="部屋を選択" filterable clearable style="width: 100%">
            <el-option v-for="r in roomOptions" :key="r.value" :value="r.value" :label="r.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="移動日" prop="transferDate">
          <el-date-picker v-model="transferForm.transferDate" type="date" value-format="YYYY-MM-DD" placeholder="選択" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferDialogVisible = false">キャンセル</el-button>
        <el-button type="primary" :loading="transferLoading" @click="handleTransfer">部屋移動</el-button>
      </template>
    </el-dialog>

    <!-- 長期利用情報ダイアログ -->
    <el-dialog v-model="longTermDialogVisible" title="長期利用情報" width="550px" :close-on-click-modal="false">
      <div v-loading="longTermLoading">
        <template v-if="longTermInfo">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="社員ID">{{ longTermInfo.employeeId }}</el-descriptions-item>
            <el-descriptions-item label="氏名">{{ longTermInfo.empName }}</el-descriptions-item>
            <el-descriptions-item label="初回利用日">{{ longTermInfo.firstUseDate || '未設定' }}</el-descriptions-item>
            <el-descriptions-item label="通算利用日数">{{ longTermInfo.totalUsageDays }} 日</el-descriptions-item>
            <el-descriptions-item label="通算利用月数">{{ longTermInfo.totalUsageMonths }} ヶ月</el-descriptions-item>
            <el-descriptions-item label="長期利用警告">
              <el-tag :type="longTermInfo.longTermWarning ? 'danger' : 'success'" size="small">
                {{ longTermInfo.longTermWarning ? '要警告' : '正常' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="寮費値上げ判断">
              <el-tag :type="longTermInfo.rateIncreaseJudgment ? 'warning' : 'info'" size="small">
                {{ longTermInfo.rateIncreaseJudgment ? '検討必要' : '対象外' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
          <el-alert v-if="longTermInfo.warningMessage" :title="longTermInfo.warningMessage"
            type="warning" show-icon style="margin-top: 12px" :closable="false" />
          <el-alert v-if="longTermInfo.firstUseDateUnset" title="初回利用日が設定されていません。社員詳細画面から設定してください。"
            type="info" show-icon style="margin-top: 12px" :closable="false" />
        </template>
        <el-empty v-else-if="!longTermLoading" description="情報を取得中..." />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container { max-width: 1200px; }
.header-bar { display: flex; align-items: center; justify-content: space-between; }
.header-title { font-size: 16px; font-weight: 600; }
.header-actions { display: flex; gap: 8px; }
.search-area { margin-bottom: 16px; }
.pagination-area { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
