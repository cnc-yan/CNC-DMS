<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEmployeeList, getEmployee, createEmployee, updateEmployee, deleteEmployee } from '@/api/employee'
import { getDepartmentList } from '@/api/department'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

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

const searchForm = reactive({
  empNo: '',
  empName: '',
  deptId: null,
  empStatus: null,
})

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const selectedRows = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const form = reactive({
  id: null,
  empNo: '',
  empName: '',
  gender: '',
  nationality: '',
  deptId: null,
  phone: '',
  email: '',
  hireDate: '',
  empStatus: 1,
  version: 0,
})

const dialogTitle = computed(() => (isEdit.value ? '社員編集' : '社員新規作成'))

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

const rules = {
  empNo: [{ required: true, message: '工番を入力してください', trigger: 'blur' }],
  empName: [{ required: true, message: '社員名を入力してください', trigger: 'blur' }],
}

async function fetchList() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (searchForm.empNo) params.empNo = searchForm.empNo
    if (searchForm.empName) params.empName = searchForm.empName
    if (searchForm.deptId) params.deptId = searchForm.deptId
    if (searchForm.empStatus !== null && searchForm.empStatus !== '') params.empStatus = searchForm.empStatus

    const res = await getEmployeeList(params)
    if (res && res.success) {
      const body = res.body || {}
      tableData.value = body.list || []
      total.value = body.total || 0
    } else {
      ElMessage.error(res?.message || '検索に失敗しました')
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗')
  } finally {
    loading.value = false
  }
}

function handleSearch() { pageNum.value = 1; fetchList() }
function handleReset() {
  searchForm.empNo = ''; searchForm.empName = ''; searchForm.deptId = null; searchForm.empStatus = null
  handleSearch()
}
function handleSizeChange(size) { pageSize.value = size; fetchList() }
function handleCurrentChange(page) { pageNum.value = page; fetchList() }
function handleSelectionChange(val) { selectedRows.value = val }

function handleRowClick(row) {
  router.push(`/master/employee/${row.id}`)
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

async function handleEdit(row) {
  isEdit.value = true
  resetForm()
  try {
    const res = await getEmployee(row.id)
    if (res && res.success) {
      const data = res.body || {}
      form.id = data.id
      form.empNo = data.empNo || ''
      form.empName = data.empName || ''
      form.gender = normalizeGender(data.gender)
      form.nationality = data.nationality || ''
      form.deptId = data.deptId || null
      form.phone = data.phone || ''
      form.email = data.email || ''
      form.hireDate = data.hireDate || ''
      form.empStatus = data.empStatus ?? 1
      form.version = data.version ?? 0
    } else {
      ElMessage.error(res?.message || '社員情報の取得に失敗しました')
      return
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗')
    return
  }
  dialogVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(
    `社員「${row.empName}」(${row.empNo}) を削除してもよろしいですか？`,
    '削除確認',
    { type: 'warning', confirmButtonText: '削除', cancelButtonText: 'キャンセル' },
  )
    .then(async () => {
      try {
        const res = await deleteEmployee(row.id)
        if (res && res.success) { ElMessage.success('削除しました'); fetchList() }
        else ElMessage.error(res?.message || '削除に失敗しました')
      } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗') }
    })
    .catch(() => {})
}

function handleDeleteSelected() {
  if (!selectedRows.value.length) { ElMessage.warning('削除する社員を選択してください'); return }
  ElMessageBox.confirm(
    `選択した ${selectedRows.value.length} 件の社員を削除してもよろしいですか？`,
    '一括削除確認',
    { type: 'warning', confirmButtonText: '削除', cancelButtonText: 'キャンセル' },
  )
    .then(async () => {
      let failCount = 0
      const failedNames = []
      for (const row of selectedRows.value) {
        try {
          const res = await deleteEmployee(row.id)
          if (!res || !res.success) {
            failCount++
            failedNames.push(`${row.empName || row.empNo}: ${res?.error || res?.message || '不明なエラー'}`)
          }
        } catch { failCount++; failedNames.push(`${row.empName || row.id}: 通信エラー`) }
      }
      if (failCount === 0) ElMessage.success('削除しました')
      else ElMessage.warning(`${failCount} 件の削除に失敗しました\n${failedNames.join('\n')}`)
      selectedRows.value = []
      fetchList()
    })
    .catch(() => {})
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = {
        empNo: form.empNo,
        empName: form.empName,
        gender: genderToApi(normalizeGender(form.gender)),
        nationality: form.nationality || null,
        deptId: form.deptId || null,
        phone: form.phone || null,
        email: form.email || null,
        hireDate: form.hireDate || null,
        updateBy: authStore.userid || authStore.username || 'system',
      }
      if (isEdit.value) {
        payload.id = form.id
        payload.empStatus = form.empStatus
        payload.version = form.version
        const res = await updateEmployee(payload)
        if (res && res.success) { ElMessage.success('更新しました') }
        else { ElMessage.error(res?.error || res?.message || '更新に失敗しました'); return }
      } else {
        payload.createBy = authStore.userid || authStore.username || 'system'
        const res = await createEmployee(payload)
        if (res && res.success) { ElMessage.success('作成しました') }
        else { ElMessage.error(res?.error || res?.message || '作成に失敗しました'); return }
      }
      dialogVisible.value = false
      fetchList()
    } catch (err) {
      ElMessage.error(err?.response?.data?.error || err?.response?.data?.message || err?.message || '保存に失敗しました')
    } finally {
      submitLoading.value = false
    }
  })
}

function resetForm() {
  form.id = null; form.empNo = ''; form.empName = ''; form.gender = ''
  form.nationality = ''; form.deptId = null; form.phone = ''; form.email = ''
  form.hireDate = ''; form.empStatus = 1; form.version = 0
  if (formRef.value) formRef.value.resetFields()
}

const statusMap = { 1: '在職', 2: '離職', 3: '休職中' }
const statusTypeMap = { 1: 'success', 2: 'info', 3: 'warning' }

onMounted(() => {
  fetchList()
  fetchDepartmentOptions()
})
</script>

<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header-bar">
          <span class="header-title">社員管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd">新規作成</el-button>
            <el-button type="danger" plain @click="handleDeleteSelected">一括削除</el-button>
          </div>
        </div>
      </template>

      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="工番">
            <el-input v-model="searchForm.empNo" placeholder="部分一致" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="社員名">
            <el-input v-model="searchForm.empName" placeholder="部分一致" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="状態">
            <el-select v-model="searchForm.empStatus" placeholder="すべて" clearable style="width: 120px">
              <el-option :value="1" label="在職" />
              <el-option :value="2" label="離職" />
              <el-option :value="3" label="休職中" />
            </el-select>
          </el-form-item>
          <el-form-item label="所属部門">
            <el-select v-model="searchForm.deptId" placeholder="すべて" filterable clearable style="width: 190px">
              <el-option v-for="d in departmentOptions" :key="d.value" :value="d.value" :label="d.label" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">検索</el-button>
            <el-button @click="handleReset">リセット</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table
        :data="tableData" border v-loading="loading"
        @selection-change="handleSelectionChange" @row-dblclick="handleRowClick"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column label="工番" width="140" prop="empNo" />
        <el-table-column label="社員名" min-width="150" prop="empName" />
        <el-table-column label="性別" width="70" align="center">
          <template #default="{ row }">
            {{ toGenderDisplay(row.gender) }}
          </template>
        </el-table-column>
        <el-table-column label="所属部門" min-width="130" prop="deptName" show-overflow-tooltip />
        <el-table-column label="状態" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.empStatus] || 'info'" size="small">
              {{ statusMap[row.empStatus] || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="電話番号" width="140" prop="phone" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">編集</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">削除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-area">
        <el-pagination
          v-model:page-size="pageSize" v-model:current-page="pageNum" :total="total"
          layout="prev, pager, next, jumper, sizes, total"
          @current-change="handleCurrentChange" @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" status-icon>
        <el-form-item label="工番" prop="empNo">
          <el-input v-model="form.empNo" :disabled="isEdit" placeholder="例: EMP-00001" maxlength="20" />
        </el-form-item>
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
        <el-form-item v-if="isEdit" label="状態">
          <el-radio-group v-model="form.empStatus">
            <el-radio :value="1">在職</el-radio>
            <el-radio :value="2">離職</el-radio>
            <el-radio :value="3">休職中</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">キャンセル</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
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
