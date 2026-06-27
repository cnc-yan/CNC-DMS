<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getResidentRecord, checkout, deleteResidentRecord, transferRoom, getLongTermUsageInfo } from '@/api/residentRecord'
import { getEmployee, getEmployeeList } from '@/api/employee'
import { getRoom, getRoomList } from '@/api/room'
import { getDormitory, getDormitoryList } from '@/api/dormitory'
import { useAuthStore } from '@/store/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const recordId = Number(route.params.id)

const loading = ref(false)
const detail = reactive({
  id: null, employeeId: null, roomId: null,
  checkinDate: '', checkoutDate: null, isActive: 1,
  notes: '', version: 0,
  createBy: '', createTime: '', updateBy: '', updateTime: '',
})

// 関連情報
const employeeInfo = ref(null)
const roomInfo = ref(null)
const dormitoryInfo = ref(null)

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

// ==================== チェックアウト ====================
const checkoutDialogVisible = ref(false)
const checkoutFormRef = ref(null)
const checkoutLoading = ref(false)
const checkoutDate = ref('')

// ==================== 部屋移動 ====================
const transferDialogVisible = ref(false)
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

// ==================== 長期利用情報 ====================
const longTermDialogVisible = ref(false)
const longTermInfo = ref(null)
const longTermLoading = ref(false)

async function fetchDetail() {
  loading.value = true
  try {
    const res = await getResidentRecord(recordId)
    if (res && res.success) {
      const data = res.body || {}
      Object.assign(detail, data)
      // 関連情報を取得
      await loadRelatedInfo(data)
    } else ElMessage.error(res?.message || '入居履歴の取得に失敗しました')
  } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗') }
  finally { loading.value = false }
}

async function loadRelatedInfo(data) {
  // 社員情報
  if (data.employeeId) {
    try {
      const empRes = await getEmployee(data.employeeId)
      if (empRes?.success) employeeInfo.value = empRes.body
    } catch { /* ignore */ }
  }
  // 部屋情報
  if (data.roomId) {
    try {
      const roomRes = await getRoom(data.roomId)
      if (roomRes?.success) {
        roomInfo.value = roomRes.body
        // 寮情報
        if (roomRes.body.dormitoryId) {
          const dorRes = await getDormitory(roomRes.body.dormitoryId)
          if (dorRes?.success) dormitoryInfo.value = dorRes.body
        }
      }
    } catch { /* ignore */ }
  }
}

function goBack() { router.push('/resident/records') }

// ==================== チェックアウト ====================
function openCheckoutDialog() {
  checkoutDate.value = new Date().toISOString().split('T')[0]
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
      const res = await checkout(recordId, params)
      if (res && res.success) {
        ElMessage.success('退寮処理しました')
        checkoutDialogVisible.value = false
        fetchDetail()
      } else ElMessage.error(res?.message || '退寮処理に失敗しました')
    } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || '退寮処理失敗') }
    finally { checkoutLoading.value = false }
  })
}

// ==================== 部屋移動 ====================
function openTransferDialog() {
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
        currentRecordId: recordId,
        newRoomId: transferForm.newRoomId,
        transferDate: transferForm.transferDate,
        updateBy: authStore.userid || authStore.username || 'system',
      }
      const res = await transferRoom(params)
      if (res && res.success) {
        ElMessage.success('部屋移動しました')
        transferDialogVisible.value = false
        fetchDetail()
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
async function openLongTermDialog() {
  longTermDialogVisible.value = true
  longTermInfo.value = null
  longTermLoading.value = true
  try {
    const res = await getLongTermUsageInfo(detail.employeeId)
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

function handleDelete() {
  ElMessageBox.confirm(`入居履歴(ID: ${recordId}) を削除してもよろしいですか？`, '削除確認',
    { type: 'warning', confirmButtonText: '削除', cancelButtonText: 'キャンセル' })
    .then(async () => {
      try {
        const res = await deleteResidentRecord(recordId)
        if (res && res.success) { ElMessage.success('削除しました'); router.push('/resident/records') }
        else ElMessage.error(res?.message || '削除に失敗しました')
      } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || '削除失敗') }
    }).catch(() => {})
}

onMounted(() => {
  fetchDetail()
  fetchEmployeeOptions()
  fetchRoomOptions()
  fetchDormitoryOptions()
})
</script>

<template>
  <div class="detail-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="header-bar">
          <span class="header-title">入居履歴詳細</span>
          <div class="header-actions">
            <el-button @click="goBack">戻る</el-button>
            <el-button v-if="detail.isActive === 1" type="warning" @click="openCheckoutDialog">退寮処理</el-button>
            <el-button v-if="detail.isActive === 1" type="primary" @click="openTransferDialog">部屋移動</el-button>
            <el-button type="info" @click="openLongTermDialog">長期利用</el-button>
            <el-button type="danger" @click="handleDelete">削除</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="記録ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="状態">
          <el-tag :type="detail.isActive === 1 ? 'success' : 'info'" size="small">
            {{ detail.isActive === 1 ? '入居中' : '退寮済' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="社員">
          <div>ID: {{ detail.employeeId }}</div>
          <div v-if="employeeInfo" style="color: #606266; font-size: 13px;">
            {{ employeeInfo.empName }}（{{ employeeInfo.empNo }}）
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="部屋">
          <div>ID: {{ detail.roomId }}</div>
          <div v-if="roomInfo && dormitoryInfo" style="color: #606266; font-size: 13px;">
            {{ dormitoryInfo.dormName }} - {{ roomInfo.roomNumber }}号室
          </div>
          <div v-else-if="roomInfo" style="color: #606266; font-size: 13px;">
            部屋番号: {{ roomInfo.roomNumber }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="入寮日">{{ detail.checkinDate }}</el-descriptions-item>
        <el-descriptions-item label="退寮予定日">{{ detail.plannedCheckoutDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="退寮日">{{ detail.checkoutDate || '入居中' }}</el-descriptions-item>
        <el-descriptions-item label="備考" :span="2">{{ detail.notes || '-' }}</el-descriptions-item>
        <el-descriptions-item label="ステータス">{{ detail.status || '-' }}</el-descriptions-item>
        <el-descriptions-item label="バージョン">{{ detail.version }}</el-descriptions-item>
        <el-descriptions-item label="作成者">{{ detail.createBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="作成日時">{{ detail.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新者">{{ detail.updateBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新日時">{{ detail.updateTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 退寮処理ダイアログ -->
    <el-dialog v-model="checkoutDialogVisible" title="退寮処理（チェックアウト）" width="500px" :close-on-click-modal="false">
      <el-form ref="checkoutFormRef" label-width="100px" status-icon>
        <el-form-item label="対象記録">
          <div>
            <el-tag type="info">ID: {{ recordId }}</el-tag>
            <span v-if="employeeInfo" style="margin-left: 8px;">{{ employeeInfo.empName }}</span>
          </div>
        </el-form-item>
        <el-form-item label="入居状況">
          <el-tag type="success">入居中</el-tag>
          <span style="margin-left: 8px;">入居日: {{ detail.checkinDate }}</span>
        </el-form-item>
        <el-form-item label="退寮日" required prop="checkoutDate">
          <el-date-picker v-model="checkoutDate" type="date" value-format="YYYY-MM-DD"
            placeholder="選択（省略時は当日）" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkoutDialogVisible = false">キャンセル</el-button>
        <el-button type="warning" :loading="checkoutLoading" @click="handleCheckout">退寮処理</el-button>
      </template>
    </el-dialog>

    <!-- 部屋移動ダイアログ -->
    <el-dialog v-model="transferDialogVisible" title="部屋移動" width="520px" :close-on-click-modal="false">
      <el-form ref="transferFormRef" :model="transferForm" :rules="transferRules" label-width="120px" status-icon>
        <el-form-item label="現在の部屋">
          <div>
            <el-tag type="info">ID: {{ detail.roomId }}</el-tag>
            <span v-if="roomInfo && dormitoryInfo" style="margin-left: 8px;">
              {{ dormitoryInfo.dormName }} - {{ roomInfo.roomNumber }}号室
            </span>
          </div>
        </el-form-item>
        <el-form-item label="入居者">
          <span v-if="employeeInfo">{{ employeeInfo.empName }}（{{ employeeInfo.empNo }}）</span>
          <span v-else>社員ID: {{ detail.employeeId }}</span>
        </el-form-item>
        <el-form-item label="新しい部屋" prop="newRoomId">
          <el-select v-model="transferForm.newRoomId" placeholder="部屋を選択" filterable clearable style="width: 100%">
            <el-option v-for="r in roomOptions" :key="r.value" :value="r.value" :label="r.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="移動日" prop="transferDate">
          <el-date-picker v-model="transferForm.transferDate" type="date" value-format="YYYY-MM-DD"
            placeholder="選択" style="width: 100%" />
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
.detail-container { max-width: 800px; }
.header-bar { display: flex; align-items: center; justify-content: space-between; }
.header-title { font-size: 16px; font-weight: 600; }
.header-actions { display: flex; gap: 8px; }
</style>
