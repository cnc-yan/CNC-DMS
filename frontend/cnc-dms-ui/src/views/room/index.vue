<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoomList, getRoom, getRoomsByDormitory, createRoom, updateRoom, deleteRoom } from '@/api/room'
import { getDormitoryList } from '@/api/dormitory'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// URLパラメータから初期の寮IDを取得
const searchForm = reactive({
  dormitoryId: route.query.dormitoryId ? Number(route.query.dormitoryId) : null,
  roomNumber: '',
  status: null,
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
  id: null, dormitoryId: null, roomNumber: '', floor: 1, capacity: 1,
  dailyRate: null, roomType: 0, acType: 0,
  memo1: '', memo2: '', memo3: '',
  status: 1, version: 0,
})

const dialogTitle = computed(() => (isEdit.value ? '部屋編集' : '部屋新規作成'))

// ==================== 寮選択肢 ====================
const dormitoryOptions = ref([])
async function fetchDormitoryOptions() {
  try {
    const res = await getDormitoryList({ pageNum: 1, pageSize: 999 })
    if (res && res.success) {
      dormitoryOptions.value = (res.body?.list || []).map(d => ({
        value: d.id, label: `${d.dormName}（${d.region}）`,
      }))
    }
  } catch { /* ignore */ }
}

const rules = {
  dormitoryId: [{ required: true, message: '所属寮を選択してください', trigger: 'change' }],
  roomNumber: [{ required: true, message: '部屋番号を入力してください', trigger: 'blur' }],
}

const statusMap = { 1: '空室', 2: '満室', 0: '使用不可' }
const statusTypeMap = { 1: 'success', 2: 'warning', 0: 'info' }

async function fetchList() {
  loading.value = true
  try {
    // 寮IDが指定されている場合は by-dormitory API を使用
    if (searchForm.dormitoryId) {
      const res = await getRoomsByDormitory(searchForm.dormitoryId)
      if (res && res.success) {
        let list = res.body || []
        // クライアントサイドフィルタリング
        if (searchForm.roomNumber) {
          list = list.filter(r => (r.roomNumber || '').includes(searchForm.roomNumber))
        }
        if (searchForm.status !== null && searchForm.status !== '') {
          list = list.filter(r => r.status === searchForm.status)
        }
        tableData.value = list
        total.value = list.length
      } else ElMessage.error(res?.message || '検索に失敗しました')
    } else {
      const params = { pageNum: pageNum.value, pageSize: pageSize.value }
      if (searchForm.dormitoryId) params.dormitoryId = searchForm.dormitoryId
      if (searchForm.roomNumber) params.roomNumber = searchForm.roomNumber
      if (searchForm.status !== null && searchForm.status !== '') params.status = searchForm.status
      const res = await getRoomList(params)
      if (res && res.success) {
        const body = res.body || {}
        tableData.value = body.list || []
        total.value = body.total || 0
      } else ElMessage.error(res?.message || '検索に失敗しました')
    }
  } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗') }
  finally { loading.value = false }
}

function handleSearch() { pageNum.value = 1; fetchList() }
function handleReset() {
  searchForm.dormitoryId = route.query.dormitoryId ? Number(route.query.dormitoryId) : null
  searchForm.roomNumber = ''; searchForm.status = null
  handleSearch()
}
function handleSizeChange(size) { pageSize.value = size; fetchList() }
function handleCurrentChange(page) { pageNum.value = page; fetchList() }
function handleSelectionChange(val) { selectedRows.value = val }
function handleRowClick(row) { router.push(`/master/room/${row.id}`) }

function handleAdd() { isEdit.value = false; resetForm(); dialogVisible.value = true }

async function handleEdit(row) {
  isEdit.value = true; resetForm()
  try {
    const res = await getRoom(row.id)
    if (res && res.success) {
      const data = res.body || {}
      form.id = data.id; form.dormitoryId = data.dormitoryId
      form.roomNumber = data.roomNumber || ''; form.capacity = data.capacity ?? 1; form.dailyRate = data.dailyRate ?? null
      form.roomType = data.roomType ?? 0; form.acType = data.acType ?? 0
      form.memo1 = data.memo1 || ''; form.memo2 = data.memo2 || ''; form.memo3 = data.memo3 || ''
      form.status = data.status ?? 1; form.version = data.version ?? 0
    } else { ElMessage.error(res?.message || '部屋情報の取得に失敗しました'); return }
  } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗'); return }
  dialogVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`部屋「${row.roomNumber}」を削除してもよろしいですか？`, '削除確認',
    { type: 'warning', confirmButtonText: '削除', cancelButtonText: 'キャンセル' })
    .then(async () => {
      try {
        const res = await deleteRoom(row.id)
        if (res && res.success) { ElMessage.success('削除しました'); fetchList() }
        else ElMessage.error(res?.message || '削除に失敗しました')
      } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗') }
    }).catch(() => {})
}

function handleDeleteSelected() {
  if (!selectedRows.value.length) { ElMessage.warning('削除する部屋を選択してください'); return }
  ElMessageBox.confirm(`選択した ${selectedRows.value.length} 件の部屋を削除してもよろしいですか？`, '一括削除確認',
    { type: 'warning', confirmButtonText: '削除', cancelButtonText: 'キャンセル' })
    .then(async () => {
      let failCount = 0
      const failedNames = []
      for (const row of selectedRows.value) {
        try {
          const res = await deleteRoom(row.id)
          if (!res || !res.success) {
            failCount++
            failedNames.push(`${row.roomNumber || row.id}号室: ${res?.error || res?.message || '不明なエラー'}`)
          }
        } catch {
          failCount++
          failedNames.push(`${row.roomNumber || row.id}号室: 通信エラー`)
        }
      }
      if (failCount === 0) ElMessage.success('削除しました')
      else ElMessage.warning(`${failCount} 件の削除に失敗しました\n${failedNames.join('\n')}`)
      selectedRows.value = []; fetchList()
    }).catch(() => {})
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = {
        dormitoryId: form.dormitoryId, roomNumber: form.roomNumber,
        capacity: form.capacity ?? 1, dailyRate: form.dailyRate,
        roomType: form.roomType, acType: form.acType,
        memo1: form.memo1 || null, memo2: form.memo2 || null, memo3: form.memo3 || null,
        updateBy: authStore.userid || authStore.username || 'system',
      }
      if (isEdit.value) {
        payload.id = form.id; payload.status = form.status; payload.version = form.version
        const res = await updateRoom(payload)
        if (res && res.success) ElMessage.success('更新しました')
        else { ElMessage.error(res?.message || '更新に失敗しました'); return }
      } else {
        payload.createBy = authStore.userid || authStore.username || 'system'
        const res = await createRoom(payload)
        if (res && res.success) ElMessage.success('作成しました')
        else { ElMessage.error(res?.message || '作成に失敗しました'); return }
      }
      dialogVisible.value = false; fetchList()
    } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || '保存に失敗しました') }
    finally { submitLoading.value = false }
  })
}

function resetForm() {
  form.id = null; form.dormitoryId = searchForm.dormitoryId || null
  form.roomNumber = ''; form.capacity = 1; form.dailyRate = null
  form.roomType = 0; form.acType = 0
  form.memo1 = ''; form.memo2 = ''; form.memo3 = ''
  form.status = 1; form.version = 0
  if (formRef.value) formRef.value.resetFields()
}

onMounted(() => {
  fetchList()
  fetchDormitoryOptions()
})
</script>

<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header-bar">
          <span class="header-title">部屋管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd">新規作成</el-button>
            <el-button type="danger" plain @click="handleDeleteSelected">一括削除</el-button>
          </div>
        </div>
      </template>

      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="寮">
            <el-select v-model="searchForm.dormitoryId" placeholder="すべて" filterable clearable style="width: 220px">
              <el-option v-for="d in dormitoryOptions" :key="d.value" :value="d.value" :label="d.label" />
            </el-select>
          </el-form-item>
          <el-form-item label="部屋番号">
            <el-input v-model="searchForm.roomNumber" placeholder="部分一致" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="状態">
            <el-select v-model="searchForm.status" placeholder="すべて" clearable style="width: 120px">
              <el-option :value="1" label="空室" />
              <el-option :value="2" label="満室" />
              <el-option :value="0" label="使用不可" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">検索</el-button>
            <el-button @click="handleReset">リセット</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" border v-loading="loading" @selection-change="handleSelectionChange" @row-dblclick="handleRowClick">
        <el-table-column type="selection" width="50" />
        <el-table-column label="部屋ID" width="70" prop="id" />
        <el-table-column label="寮名称" min-width="120" prop="dormName" />
        <el-table-column label="部屋番号" width="120" prop="roomNumber" />
        <el-table-column label="定員" width="70" prop="capacity" align="center" />
        <el-table-column label="現在人数" width="80" prop="currentOccupancy" align="center" />
        <el-table-column label="1日単価" width="120">
          <template #default="{ row }">{{ row.dailyRate != null ? `¥${Number(row.dailyRate).toLocaleString()}` : '-' }}</template>
        </el-table-column>
        <el-table-column label="室区分" width="80" align="center">
          <template #default="{ row }">{{ {0:'不明',1:'和室',2:'洋室'}[row.roomType] || '-' }}</template>
        </el-table-column>
        <el-table-column label="エアコン" width="80" align="center">
          <template #default="{ row }">{{ row.acType === 1 ? 'あり' : 'なし' }}</template>
        </el-table-column>
        <el-table-column label="メモ" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ [row.memo1, row.memo2, row.memo3].filter(Boolean).join(' / ') || '-' }}</template>
        </el-table-column>
        <el-table-column label="状態" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status] || 'info'" size="small">{{ statusMap[row.status] || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">編集</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" :close-on-click-modal="false" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" status-icon>
        <el-form-item label="所属寮" prop="dormitoryId">
          <el-select v-model="form.dormitoryId" placeholder="寮を選択" filterable clearable style="width: 100%">
            <el-option v-for="d in dormitoryOptions" :key="d.value" :value="d.value" :label="d.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="部屋番号" prop="roomNumber">
          <el-input v-model="form.roomNumber" :disabled="isEdit" maxlength="20" />
        </el-form-item>
        <el-form-item label="定員">
          <el-input-number v-model="form.capacity" :min="1" :precision="0" />
        </el-form-item>
        <el-form-item label="1日単価">
          <el-input-number v-model="form.dailyRate" :min="0" :precision="2" :step="500" />
        </el-form-item>
        <el-form-item label="室区分">
          <el-radio-group v-model="form.roomType">
            <el-radio :value="0">不明</el-radio>
            <el-radio :value="1">和室</el-radio>
            <el-radio :value="2">洋室</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="エアコン">
          <el-radio-group v-model="form.acType">
            <el-radio :value="0">なし</el-radio>
            <el-radio :value="1">あり</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="メモ1">
          <el-input v-model="form.memo1" maxlength="255" />
        </el-form-item>
        <el-form-item label="メモ2">
          <el-input v-model="form.memo2" maxlength="255" />
        </el-form-item>
        <el-form-item label="メモ3">
          <el-input v-model="form.memo3" maxlength="255" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="状態">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">空室</el-radio>
            <el-radio :value="2">満室</el-radio>
            <el-radio :value="0">使用不可</el-radio>
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
