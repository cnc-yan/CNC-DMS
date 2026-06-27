<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { uploadExcel, getImportLogList, getImportLog } from '@/api/importLog'
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()

const searchForm = reactive({
  importType: '',
  importStatus: null,
})

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const uploadDialogVisible = ref(false)
const uploadLoading = ref(false)
const selectedFile = ref(null)
const selectedImportType = ref('DORMITORY')

const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)

const importTypeOptions = [
  { value: 'DORMITORY', label: '寮マスタ' },
  { value: 'ROOM', label: '部屋情報' },
  { value: 'EMPLOYEE', label: '社員情報' },
  { value: 'RESIDENT', label: '入居履歴' },
]

const importStatusMap = { 1: '成功', 2: '一部失敗', 3: '失敗' }
const importStatusTypeMap = { 1: 'success', 2: 'warning', 3: 'danger' }

async function fetchList() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (searchForm.importType) params.importType = searchForm.importType
    if (searchForm.importStatus !== null && searchForm.importStatus !== '') params.importStatus = searchForm.importStatus
    const res = await getImportLogList(params)
    if (res && res.success) {
      const body = res.body || {}
      tableData.value = body.list || []
      total.value = body.total || 0
    } else ElMessage.error(res?.message || '検索に失敗しました')
  } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗') }
  finally { loading.value = false }
}

function handleSearch() { pageNum.value = 1; fetchList() }
function handleReset() { searchForm.importType = ''; searchForm.importStatus = null; handleSearch() }
function handleSizeChange(size) { pageSize.value = size; fetchList() }
function handleCurrentChange(page) { pageNum.value = page; fetchList() }

function handleFileChange(file) {
  selectedFile.value = file.raw
}

function openUploadDialog() {
  selectedFile.value = null
  selectedImportType.value = 'DORMITORY'
  uploadDialogVisible.value = true
}

async function handleUpload() {
  if (!selectedFile.value) {
    ElMessage.warning('ファイルを選択してください')
    return
  }
  uploadLoading.value = true
  try {
    const res = await uploadExcel(
      selectedFile.value,
      selectedImportType.value,
      authStore.userid || authStore.username || 'system',
    )
    if (res && res.success) {
      ElMessage.success('インポート処理が完了しました')
      uploadDialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res?.message || 'インポートに失敗しました')
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || err?.message || 'アップロード失敗')
  } finally {
    uploadLoading.value = false
  }
}

async function handleViewDetail(row) {
  detailLoading.value = true
  detailDialogVisible.value = true
  try {
    const res = await getImportLog(row.id)
    if (res && res.success) {
      detailData.value = res.body
    } else {
      ElMessage.error(res?.message || 'インポートログの取得に失敗しました')
      detailDialogVisible.value = false
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗')
    detailDialogVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => fetchList())
</script>

<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header-bar">
          <span class="header-title">Excelインポート</span>
          <div class="header-actions">
            <el-button type="primary" @click="openUploadDialog">
              <el-icon style="margin-right:4px"><Upload /></el-icon>ファイルアップロード
            </el-button>
          </div>
        </div>
      </template>

      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="インポート種別">
            <el-select v-model="searchForm.importType" placeholder="すべて" clearable style="width: 160px">
              <el-option v-for="t in importTypeOptions" :key="t.value" :value="t.value" :label="t.label" />
            </el-select>
          </el-form-item>
          <el-form-item label="状態">
            <el-select v-model="searchForm.importStatus" placeholder="すべて" clearable style="width: 120px">
              <el-option :value="1" label="成功" />
              <el-option :value="2" label="一部失敗" />
              <el-option :value="3" label="失敗" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">検索</el-button>
            <el-button @click="handleReset">リセット</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" border v-loading="loading">
        <el-table-column label="ID" width="65" prop="id" />
        <el-table-column label="ファイル名" min-width="200" prop="fileName" show-overflow-tooltip />
        <el-table-column label="種別" width="120" prop="importType" />
        <el-table-column label="総件数" width="70" prop="totalCount" align="center" />
        <el-table-column label="成功" width="70" prop="successCount" align="center" />
        <el-table-column label="エラー" width="70" prop="errorCount" align="center" />
        <el-table-column label="状態" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="importStatusTypeMap[row.importStatus] || 'info'" size="small">
              {{ importStatusMap[row.importStatus] || row.importStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="作成日時" width="160" prop="createTime" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">詳細</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-area">
        <el-pagination v-model:page-size="pageSize" v-model:current-page="pageNum" :total="total"
          layout="prev, pager, next, jumper, sizes, total"
          @current-change="handleCurrentChange" @size-change="handleSizeChange" />
      </div>
    </el-card>

    <!-- アップロードダイアログ -->
    <el-dialog v-model="uploadDialogVisible" title="Excelファイルアップロード" width="500px" :close-on-click-modal="false">
      <el-form label-width="120px">
        <el-form-item label="インポート種別">
          <el-select v-model="selectedImportType" style="width: 200px">
            <el-option v-for="t in importTypeOptions" :key="t.value" :value="t.value" :label="t.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="ファイル">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :show-file-list="true"
            accept=".xlsx,.xls"
            :on-change="handleFileChange"
            :limit="1"
          >
            <el-button type="primary" plain>ファイルを選択</el-button>
            <template #tip>
              <div style="font-size: 12px; color: #909399; margin-top: 4px;">
                .xlsx / .xls 形式のみ対応
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">キャンセル</el-button>
        <el-button type="primary" :loading="uploadLoading" @click="handleUpload">
          アップロード
        </el-button>
      </template>
    </el-dialog>

    <!-- 詳細ダイアログ -->
    <el-dialog v-model="detailDialogVisible" title="インポートログ詳細" width="700px" :close-on-click-modal="false">
      <div v-loading="detailLoading">
        <template v-if="detailData">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="ログID">{{ detailData.id }}</el-descriptions-item>
            <el-descriptions-item label="ファイル名">{{ detailData.fileName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="種別">{{ detailData.importType || '-' }}</el-descriptions-item>
            <el-descriptions-item label="作成者">{{ detailData.createBy || '-' }}</el-descriptions-item>
            <el-descriptions-item label="状態">
              <el-tag :type="importStatusTypeMap[detailData.importStatus] || 'info'" size="small">
                {{ importStatusMap[detailData.importStatus] || detailData.importStatus }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="作成日時">{{ detailData.createTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="総件数">{{ detailData.totalCount ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="成功件数">{{ detailData.successCount ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="エラー件数">{{ detailData.errorCount ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="エラー詳細" :span="2" v-if="detailData.errorMessages && detailData.errorMessages.length">
              <div v-for="(errMsg, idx) in detailData.errorMessages" :key="idx" style="color: #f56c6c; font-size: 13px; margin-bottom: 2px;">
                {{ idx + 1 }}. {{ errMsg }}
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </template>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">閉じる</el-button>
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
