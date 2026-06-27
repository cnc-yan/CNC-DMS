<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDormitoryList, getDormitory, createDormitory, updateDormitory, deleteDormitory } from '@/api/dormitory'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

const searchForm = reactive({ dormName: '', region: '', status: null })

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
  id: null, dormName: '', region: '', address: '', dormCondition: '', totalRooms: 0, status: 1, version: 0,
})

const dialogTitle = computed(() => (isEdit.value ? '寮編集' : '寮新規作成'))
const rules = {
  dormName: [{ required: true, message: '寮名を入力してください', trigger: 'blur' }],
  region: [{ required: true, message: '地域を入力してください', trigger: 'blur' }],
}

const conditionMap = { MALE: '男性のみ', FEMALE: '女性のみ', ANY: '制限なし' }

async function fetchList() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (searchForm.dormName) params.dormName = searchForm.dormName
    if (searchForm.region) params.region = searchForm.region
    if (searchForm.status !== null && searchForm.status !== '') params.status = searchForm.status
    const res = await getDormitoryList(params)
    if (res && res.success) {
      const body = res.body || {}
      tableData.value = body.list || []
      total.value = body.total || 0
    } else ElMessage.error(res?.message || '検索に失敗しました')
  } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗') }
  finally { loading.value = false }
}

function handleSearch() { pageNum.value = 1; fetchList() }
function handleReset() { searchForm.dormName = ''; searchForm.region = ''; searchForm.status = null; handleSearch() }
function handleSizeChange(size) { pageSize.value = size; fetchList() }
function handleCurrentChange(page) { pageNum.value = page; fetchList() }
function handleSelectionChange(val) { selectedRows.value = val }
function handleRowClick(row) { router.push(`/master/dormitory/${row.id}`) }

function handleAdd() { isEdit.value = false; resetForm(); dialogVisible.value = true }

async function handleEdit(row) {
  isEdit.value = true; resetForm()
  try {
    const res = await getDormitory(row.id)
    if (res && res.success) {
      const data = res.body || {}
      form.id = data.id; form.dormName = data.dormName || ''; form.region = data.region || ''
      form.address = data.address || ''; form.dormCondition = data.dormCondition || ''
      form.totalRooms = data.totalRooms ?? 0; form.status = data.status ?? 1; form.version = data.version ?? 0
    } else { ElMessage.error(res?.message || '寮情報の取得に失敗しました'); return }
  } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗'); return }
  dialogVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`寮「${row.dormName}」を削除してもよろしいですか？`, '削除確認',
    { type: 'warning', confirmButtonText: '削除', cancelButtonText: 'キャンセル' })
    .then(async () => {
      try {
        const res = await deleteDormitory(row.id)
        if (res && res.success) { ElMessage.success('削除しました'); fetchList() }
        else ElMessage.error(res?.message || '削除に失敗しました')
      } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗') }
    }).catch(() => {})
}

function handleDeleteSelected() {
  if (!selectedRows.value.length) { ElMessage.warning('削除する寮を選択してください'); return }
  ElMessageBox.confirm(`選択した ${selectedRows.value.length} 件の寮を削除してもよろしいですか？`, '一括削除確認',
    { type: 'warning', confirmButtonText: '削除', cancelButtonText: 'キャンセル' })
    .then(async () => {
      let failCount = 0
      const failedNames = []
      for (const row of selectedRows.value) {
        try {
          const res = await deleteDormitory(row.id)
          if (!res || !res.success) {
            failCount++
            failedNames.push(`${row.dormName}: ${res?.error || res?.message || '不明なエラー'}`)
          }
        } catch { failCount++; failedNames.push(`${row.dormName || row.id}: 通信エラー`) }
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
        dormName: form.dormName, region: form.region, address: form.address || null,
        dormCondition: form.dormCondition || null, totalRooms: form.totalRooms ?? 0,
        updateBy: authStore.userid || authStore.username || 'system',
      }
      if (isEdit.value) {
        payload.id = form.id; payload.status = form.status; payload.version = form.version
        const res = await updateDormitory(payload)
        if (res && res.success) ElMessage.success('更新しました')
        else { ElMessage.error(res?.message || '更新に失敗しました'); return }
      } else {
        payload.createBy = authStore.userid || authStore.username || 'system'
        const res = await createDormitory(payload)
        if (res && res.success) ElMessage.success('作成しました')
        else { ElMessage.error(res?.message || '作成に失敗しました'); return }
      }
      dialogVisible.value = false; fetchList()
    } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || '保存に失敗しました') }
    finally { submitLoading.value = false }
  })
}

function resetForm() {
  form.id = null; form.dormName = ''; form.region = ''; form.address = ''
  form.dormCondition = ''; form.totalRooms = 0; form.status = 1; form.version = 0
  if (formRef.value) formRef.value.resetFields()
}

onMounted(() => fetchList())
</script>

<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header-bar">
          <span class="header-title">寮管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd">新規作成</el-button>
            <el-button type="danger" plain @click="handleDeleteSelected">一括削除</el-button>
          </div>
        </div>
      </template>

      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="寮名">
            <el-input v-model="searchForm.dormName" placeholder="部分一致" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="地域">
            <el-input v-model="searchForm.region" placeholder="完全一致" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="状態">
            <el-select v-model="searchForm.status" placeholder="すべて" clearable style="width: 120px">
              <el-option :value="1" label="運用中" />
              <el-option :value="0" label="停止中" />
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
        <el-table-column label="寮名" min-width="160" prop="dormName" />
        <el-table-column label="地域" width="120" prop="region" />
        <el-table-column label="住所" min-width="200" prop="address" show-overflow-tooltip />
        <el-table-column label="入居条件" width="100" align="center">
          <template #default="{ row }">{{ conditionMap[row.dormCondition] || row.dormCondition || '-' }}</template>
        </el-table-column>
        <el-table-column label="総部屋数" width="80" prop="totalRooms" align="center" />
        <el-table-column label="メモ1" min-width="120" prop="memo1" show-overflow-tooltip />
        <el-table-column label="メモ2" min-width="120" prop="memo2" show-overflow-tooltip />
        <el-table-column label="メモ3" min-width="120" prop="memo3" show-overflow-tooltip />
        <el-table-column label="状態" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '運用中' : '停止中' }}</el-tag>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" :close-on-click-modal="false" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" status-icon>
        <el-form-item label="寮名" prop="dormName">
          <el-input v-model="form.dormName" :disabled="isEdit" maxlength="100" />
        </el-form-item>
        <el-form-item label="地域" prop="region">
          <el-input v-model="form.region" maxlength="50" />
        </el-form-item>
        <el-form-item label="住所">
          <el-input v-model="form.address" type="textarea" :rows="2" maxlength="200" />
        </el-form-item>
        <el-form-item label="入居条件">
          <el-select v-model="form.dormCondition" placeholder="選択" clearable style="width: 160px">
            <el-option label="男性のみ" value="MALE" />
            <el-option label="女性のみ" value="FEMALE" />
            <el-option label="制限なし" value="ANY" />
          </el-select>
        </el-form-item>
        <el-form-item label="総部屋数">
          <el-input-number v-model="form.totalRooms" :min="0" :precision="0" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="状態">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">運用中</el-radio>
            <el-radio :value="0">停止中</el-radio>
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
