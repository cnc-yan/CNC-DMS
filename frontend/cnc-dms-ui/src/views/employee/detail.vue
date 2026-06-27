<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEmployee, updateEmployee, deleteEmployee, getFirstUseDate, updateFirstUseDate } from '@/api/employee'
import { getDepartmentList } from '@/api/department'
import { useAuthStore } from '@/store/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const empId = Number(route.params.id)

/** 性別値を表示用ラベルに変換（MALE/FEMALE/男/女/数値コード対応） */
function toGenderDisplay(val) {
  if (val == null || val === '') return '-'
  // 数値コード: 1=男性, 2=女性
  const num = Number(val)
  if (num === 1) return '男性'
  if (num === 2) return '女性'
  // 文字列コード
  const s = String(val)
  const u = s.toUpperCase()
  if (u === 'MALE' || s === '男') return '男性'
  if (u === 'FEMALE' || s === '女') return '女性'
  return '-'
}

/** 性別値を編集用に正規化（'男'/'1'→'MALE'、'女'/'2'→'FEMALE'） */
function normalizeGender(val) {
  if (val == null || val === '') return ''
  // 数値コード対応
  const num = Number(val)
  if (num === 1) return 'MALE'
  if (num === 2) return 'FEMALE'
  // 文字列コード対応
  const s = String(val)
  const u = s.toUpperCase()
  if (u === 'MALE' || s === '男') return 'MALE'
  if (u === 'FEMALE' || s === '女') return 'FEMALE'
  return u
}

/** 性別値をAPI送信用に変換（'MALE'/'FEMALE'→'1'/'2'） */
function genderToApi(val) {
  if (!val) return null
  const u = String(val).toUpperCase()
  if (u === 'MALE' || val === '男' || Number(val) === 1) return '1'
  if (u === 'FEMALE' || val === '女' || Number(val) === 2) return '2'
  return val
}

const loading = ref(false)
const detail = reactive({
  id: null, empNo: '', empName: '', gender: '', nationality: '', deptId: null,
  firstUseDate: null, phone: '', email: '', hireDate: '', resignDate: null,
  empStatus: 1, version: 0,
  createBy: '', createTime: '', updateBy: '', updateTime: '',
})

const dialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const form = reactive({
  empName: '', gender: '', nationality: '', deptId: null,
  phone: '', email: '', hireDate: '', empStatus: 1, version: 0,
})

const rules = {
  empName: [{ required: true, message: '社員名を入力してください', trigger: 'blur' }],
}

const statusMap = { 1: '在職', 2: '離職', 3: '休職中' }
const statusTypeMap = { 1: 'success', 2: 'info', 3: 'warning' }

async function fetchDetail() {
  loading.value = true
  try {
    const res = await getEmployee(empId)
    if (res && res.success) {
      const data = res.body || {}
      Object.assign(detail, data)
    } else {
      ElMessage.error(res?.message || '社員情報の取得に失敗しました')
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗')
  } finally {
    loading.value = false
  }
}

function goBack() { router.push('/master/employee') }

function openEditDialog() {
  form.empName = detail.empName; form.gender = normalizeGender(detail.gender)
  form.nationality = detail.nationality; form.deptId = detail.deptId
  form.phone = detail.phone; form.email = detail.email
  form.hireDate = detail.hireDate || ''
  form.empStatus = detail.empStatus; form.version = detail.version
  dialogVisible.value = true
}

async function handleEditSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = {
        id: empId, empName: form.empName, gender: genderToApi(normalizeGender(form.gender)),
        nationality: form.nationality || null, deptId: form.deptId || null,
        phone: form.phone || null, email: form.email || null,
        hireDate: form.hireDate || null,
        empStatus: form.empStatus, version: form.version,
        updateBy: authStore.userid || authStore.username || 'system',
      }
      const res = await updateEmployee(payload)
      if (res && res.success) { ElMessage.success('更新しました'); dialogVisible.value = false; fetchDetail() }
      else ElMessage.error(res?.error || res?.message || '更新に失敗しました')
    } catch (err) { ElMessage.error(err?.response?.data?.error || err?.response?.data?.message || err?.message || '更新失敗') }
    finally { submitLoading.value = false }
  })
}

function handleDelete() {
  ElMessageBox.confirm(
    `社員「${detail.empName}」を削除してもよろしいですか？`,
    '削除確認', { type: 'warning', confirmButtonText: '削除', cancelButtonText: 'キャンセル' },
  ).then(async () => {
    try {
      const res = await deleteEmployee(empId)
      if (res && res.success) { ElMessage.success('削除しました'); router.push('/master/employee') }
      else ElMessage.error(res?.message || '削除に失敗しました')
    } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || '削除失敗') }
  }).catch(() => {})
}

// ==================== 初回利用日管理 ====================
const firstUseDialogVisible = ref(false)
const firstUseFormRef = ref(null)
const firstUseLoading = ref(false)
const firstUseDateValue = ref('')

// ==================== 部門選択肢 ====================
const departmentOptions = ref([])
async function fetchDepartmentOptions() {
  try {
    const res = await getDepartmentList({ pageNum: 1, pageSize: 999 })
    if (res && res.success) {
      departmentOptions.value = (res.body?.list || []).map(d => ({
        value: d.id, label: `${d.deptName}（${d.deptCode}）`,
      }))
    }
  } catch { /* ignore */ }
}

async function openFirstUseDialog() {
  firstUseLoading.value = true
  firstUseDialogVisible.value = true
  try {
    const res = await getFirstUseDate(empId)
    if (res && res.success) {
      firstUseDateValue.value = res.body || ''
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || err?.message || '初回利用日の取得に失敗しました')
  } finally {
    firstUseLoading.value = false
  }
}

async function handleFirstUseSave() {
  firstUseLoading.value = true
  try {
    const res = await updateFirstUseDate(empId, firstUseDateValue.value,
      authStore.userid || authStore.username || 'system')
    if (res && res.success) {
      ElMessage.success('初回利用日を更新しました')
      firstUseDialogVisible.value = false
      fetchDetail()
    } else {
      ElMessage.error(res?.message || '更新に失敗しました')
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || err?.message || '更新失敗')
  } finally {
    firstUseLoading.value = false
  }
}

function resetForm() { if (formRef.value) formRef.value.resetFields() }

onMounted(() => {
  fetchDetail()
  fetchDepartmentOptions()
})
</script>

<template>
  <div class="detail-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="header-bar">
          <span class="header-title">社員詳細</span>
          <div class="header-actions">
            <el-button @click="goBack">戻る</el-button>
            <el-button type="primary" @click="openEditDialog">編集</el-button>
            <el-button type="success" @click="openFirstUseDialog">初回利用日</el-button>
            <el-button type="danger" @click="handleDelete">削除</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="社員ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="工番">{{ detail.empNo }}</el-descriptions-item>
        <el-descriptions-item label="社員名">{{ detail.empName }}</el-descriptions-item>
        <el-descriptions-item label="性別">
          {{ toGenderDisplay(detail.gender) }}
        </el-descriptions-item>
        <el-descriptions-item label="国籍">{{ detail.nationality || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属部門">
          <template v-if="detail.deptId">
            {{ departmentOptions.find(d => d.value === detail.deptId)?.label || `ID: ${detail.deptId}` }}
          </template>
          <template v-else>-</template>
        </el-descriptions-item>
        <el-descriptions-item label="初回入寮日">{{ detail.firstUseDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="電話番号">{{ detail.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="メール">{{ detail.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入社日">{{ detail.hireDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="退職日">{{ detail.resignDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状態">
          <el-tag :type="statusTypeMap[detail.empStatus] || 'info'" size="small">
            {{ statusMap[detail.empStatus] || '-' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="バージョン">{{ detail.version }}</el-descriptions-item>
        <el-descriptions-item label="作成者">{{ detail.createBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="作成日時">{{ detail.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新者">{{ detail.updateBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新日時">{{ detail.updateTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-dialog v-model="dialogVisible" title="社員編集" width="600px" :close-on-click-modal="false" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" status-icon>
        <el-form-item label="社員名" prop="empName">
          <el-input v-model="form.empName" maxlength="100" />
        </el-form-item>
        <el-form-item label="性別">
          <el-select v-model="form.gender" placeholder="選択" clearable style="width: 150px">
            <el-option label="男性" value="MALE" />
            <el-option label="女性" value="FEMALE" />
          </el-select>
        </el-form-item>
        <el-form-item label="国籍">
          <el-input v-model="form.nationality" maxlength="50" />
        </el-form-item>
        <el-form-item label="所属部門">
          <el-select v-model="form.deptId" placeholder="部門を選択" filterable clearable style="width: 100%">
            <el-option v-for="d in departmentOptions" :key="d.value" :value="d.value" :label="d.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="電話番号">
          <el-input v-model="form.phone" maxlength="20" />
        </el-form-item>
        <el-form-item label="メール">
          <el-input v-model="form.email" maxlength="100" />
        </el-form-item>
        <el-form-item label="入社日">
          <el-date-picker v-model="form.hireDate" type="date" value-format="YYYY-MM-DD" placeholder="選択" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状態">
          <el-radio-group v-model="form.empStatus">
            <el-radio :value="1">在職</el-radio>
            <el-radio :value="2">離職</el-radio>
            <el-radio :value="3">休職中</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">キャンセル</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleEditSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 初回利用日設定ダイアログ -->
    <el-dialog v-model="firstUseDialogVisible" title="初回利用日設定" width="450px" :close-on-click-modal="false">
      <div v-loading="firstUseLoading">
        <el-form ref="firstUseFormRef" label-width="120px">
          <el-form-item label="初回利用日">
            <el-date-picker v-model="firstUseDateValue" type="date" value-format="YYYY-MM-DD"
              placeholder="選択（クリアで未設定）" style="width: 100%" clearable />
          </el-form-item>
          <el-alert title="初回利用日は日本社員のみ対象です。通算利用期間の算出や長期利用警告・寮費値上げ判断に使用されます。"
            type="info" show-icon :closable="false" />
        </el-form>
      </div>
      <template #footer>
        <el-button @click="firstUseDialogVisible = false">キャンセル</el-button>
        <el-button type="success" :loading="firstUseLoading" @click="handleFirstUseSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.detail-container { max-width: 800px; }
.header-bar { display: flex; align-items: center; justify-content: space-between; }
.header-title { font-size: 16px; font-weight: 600; }
.header-actions { display: flex; gap: 8px; }
</style>
