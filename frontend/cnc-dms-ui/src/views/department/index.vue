<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDepartmentList, getDepartment, createDepartment, updateDepartment, deleteDepartment } from '@/api/department'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

// ==================== 検索 ====================
const searchForm = reactive({
  deptCode: '',
  deptName: '',
  region: '',
  status: null,
})

// ==================== テーブル ====================
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const selectedRows = ref([])

// ==================== ダイアログ ====================
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const form = reactive({
  id: null,
  deptCode: '',
  deptName: '',
  region: '',
  parentId: null,
  sortOrder: 0,
  status: 1,
  version: 0,
})

const dialogTitle = computed(() => (isEdit.value ? '部門編集' : '部門新規作成'))

const rules = {
  deptCode: [
    { required: true, message: '部門コードを入力してください', trigger: 'blur' },
  ],
  deptName: [
    { required: true, message: '部門名を入力してください', trigger: 'blur' },
  ],
}

// ==================== データ取得 ====================
async function fetchList() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (searchForm.deptCode) params.deptCode = searchForm.deptCode
    if (searchForm.deptName) params.deptName = searchForm.deptName
    if (searchForm.region) params.region = searchForm.region
    if (searchForm.status !== null && searchForm.status !== '') params.status = searchForm.status

    const res = await getDepartmentList(params)
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

function handleSearch() {
  pageNum.value = 1
  fetchList()
}

function handleReset() {
  searchForm.deptCode = ''
  searchForm.deptName = ''
  searchForm.region = ''
  searchForm.status = null
  handleSearch()
}

function handleSizeChange(size) {
  pageSize.value = size
  fetchList()
}

function handleCurrentChange(page) {
  pageNum.value = page
  fetchList()
}

function handleSelectionChange(val) {
  selectedRows.value = val
}

function handleRowClick(row) {
  router.push(`/master/department/${row.id}`)
}

// ==================== 新規作成 ====================
function handleAdd() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// ==================== 編集 ====================
async function handleEdit(row) {
  isEdit.value = true
  resetForm()
  try {
    const res = await getDepartment(row.id)
    if (res && res.success) {
      const data = res.body || {}
      form.id = data.id
      form.deptCode = data.deptCode || ''
      form.deptName = data.deptName || ''
      form.region = data.region || ''
      form.parentId = data.parentId || null
      form.sortOrder = data.sortOrder ?? 0
      form.status = data.status ?? 1
      form.version = data.version ?? 0
    } else {
      ElMessage.error(res?.message || '部門情報の取得に失敗しました')
      return
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗')
    return
  }
  dialogVisible.value = true
}

// ==================== 削除 ====================
function handleDelete(row) {
  ElMessageBox.confirm(
    `部門「${row.deptName}」(${row.deptCode}) を削除してもよろしいですか？`,
    '削除確認',
    { type: 'warning', confirmButtonText: '削除', cancelButtonText: 'キャンセル' },
  )
    .then(async () => {
      try {
        const res = await deleteDepartment(row.id)
        if (res && res.success) {
          ElMessage.success('削除しました')
          fetchList()
        } else {
          ElMessage.error(res?.message || '削除に失敗しました')
        }
      } catch (err) {
        ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗')
      }
    })
    .catch(() => {})
}

// ==================== 一括削除 ====================
function handleDeleteSelected() {
  if (!selectedRows.value.length) {
    ElMessage.warning('削除する部門を選択してください')
    return
  }
  const names = selectedRows.value.map(r => r.deptName).join(', ')
  ElMessageBox.confirm(
    `選択した ${selectedRows.value.length} 件の部門を削除してもよろしいですか？\n(${names})`,
    '一括削除確認',
    { type: 'warning', confirmButtonText: '削除', cancelButtonText: 'キャンセル' },
  )
    .then(async () => {
      let failCount = 0
      const failedNames = []
      for (const row of selectedRows.value) {
        try {
          const res = await deleteDepartment(row.id)
          if (!res || !res.success) {
            failCount++
            failedNames.push(`${row.deptName || row.deptCode}: ${res?.error || res?.message || '不明なエラー'}`)
          }
        } catch { failCount++; failedNames.push(`${row.deptName || row.id}: 通信エラー`) }
      }
      if (failCount === 0) ElMessage.success('削除しました')
      else ElMessage.warning(`${failCount} 件の削除に失敗しました\n${failedNames.join('\n')}`)
      selectedRows.value = []
      fetchList()
    })
    .catch(() => {})
}

// ==================== 保存 ====================
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = {
        deptCode: form.deptCode,
        deptName: form.deptName,
        region: form.region,
        parentId: form.parentId || null,
        sortOrder: form.sortOrder ?? 0,
        updateBy: authStore.userid || authStore.username || 'system',
      }
      if (isEdit.value) {
        payload.id = form.id
        payload.status = form.status
        payload.version = form.version
        const res = await updateDepartment(payload)
        if (res && res.success) {
          ElMessage.success('更新しました')
        } else {
          ElMessage.error(res?.message || '更新に失敗しました')
          return
        }
      } else {
        payload.createBy = authStore.userid || authStore.username || 'system'
        const res = await createDepartment(payload)
        if (res && res.success) {
          ElMessage.success('作成しました')
        } else {
          ElMessage.error(res?.message || '作成に失敗しました')
          return
        }
      }
      dialogVisible.value = false
      fetchList()
    } catch (err) {
      ElMessage.error(err?.response?.data?.message || err?.message || '保存に失敗しました')
    } finally {
      submitLoading.value = false
    }
  })
}

function resetForm() {
  form.id = null
  form.deptCode = ''
  form.deptName = ''
  form.region = ''
  form.parentId = null
  form.sortOrder = 0
  form.status = 1
  form.version = 0
  if (formRef.value) formRef.value.resetFields()
}

onMounted(() => fetchList())
</script>

<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header-bar">
          <span class="header-title">部門管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd">新規作成</el-button>
            <el-button type="danger" plain @click="handleDeleteSelected">一括削除</el-button>
          </div>
        </div>
      </template>

      <!-- 検索 -->
      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="部門コード">
            <el-input v-model="searchForm.deptCode" placeholder="部分一致" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="部門名">
            <el-input v-model="searchForm.deptName" placeholder="部分一致" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="地域">
            <el-input v-model="searchForm.region" placeholder="完全一致" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="状態">
            <el-select v-model="searchForm.status" placeholder="すべて" clearable style="width: 120px">
              <el-option :value="1" label="有効" />
              <el-option :value="0" label="無効" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">検索</el-button>
            <el-button @click="handleReset">リセット</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- テーブル -->
      <el-table
        :data="tableData"
        border
        v-loading="loading"
        @selection-change="handleSelectionChange"
        @row-dblclick="handleRowClick"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column label="部門コード" width="150" prop="deptCode" />
        <el-table-column label="部門名" min-width="180" prop="deptName" />
        <el-table-column label="地域" width="120" prop="region" />
        <el-table-column label="表示順" width="80" prop="sortOrder" align="center" />
        <el-table-column label="状態" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '有効' : '無効' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">編集</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">削除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- ページネーション -->
      <div class="pagination-area">
        <el-pagination
          v-model:page-size="pageSize"
          v-model:current-page="pageNum"
          :total="total"
          layout="prev, pager, next, jumper, sizes, total"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 新規/編集ダイアログ -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" status-icon>
        <el-form-item label="部門コード" prop="deptCode">
          <el-input v-model="form.deptCode" :disabled="isEdit" placeholder="例: DEPT-001" maxlength="50" />
        </el-form-item>
        <el-form-item label="部門名" prop="deptName">
          <el-input v-model="form.deptName" placeholder="部門名を入力" maxlength="100" />
        </el-form-item>
        <el-form-item label="地域">
          <el-input v-model="form.region" placeholder="地域を入力" maxlength="50" />
        </el-form-item>
        <el-form-item label="親部門ID">
          <el-input-number v-model="form.parentId" :min="0" :precision="0" placeholder="なし" />
        </el-form-item>
        <el-form-item label="表示順">
          <el-input-number v-model="form.sortOrder" :min="0" :precision="0" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="状態">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">有効</el-radio>
            <el-radio :value="0">無効</el-radio>
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
.page-container { max-width: 1100px; }
.header-bar { display: flex; align-items: center; justify-content: space-between; }
.header-title { font-size: 16px; font-weight: 600; }
.header-actions { display: flex; gap: 8px; }
.search-area { margin-bottom: 16px; }
.pagination-area { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
