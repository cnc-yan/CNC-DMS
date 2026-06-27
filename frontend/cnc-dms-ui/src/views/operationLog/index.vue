<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOperationLogList, getOperationLog } from '@/api/operationLog'

const searchForm = reactive({
  operatorId: '',
  operationType: '',
  targetType: '',
  resultStatus: null,
})

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)

const resultMap = { 1: '成功', 0: '失敗' }
const resultTypeMap = { 1: 'success', 0: 'danger' }

async function fetchList() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (searchForm.operatorId) params.operatorId = searchForm.operatorId
    if (searchForm.operationType) params.operationType = searchForm.operationType
    if (searchForm.targetType) params.targetType = searchForm.targetType
    if (searchForm.resultStatus !== null && searchForm.resultStatus !== '') params.resultStatus = searchForm.resultStatus
    const res = await getOperationLogList(params)
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
  searchForm.operatorId = ''; searchForm.operationType = ''
  searchForm.targetType = ''; searchForm.resultStatus = null
  handleSearch()
}
function handleSizeChange(size) { pageSize.value = size; fetchList() }
function handleCurrentChange(page) { pageNum.value = page; fetchList() }

async function handleViewDetail(row) {
  detailLoading.value = true
  detailDialogVisible.value = true
  try {
    const res = await getOperationLog(row.id)
    if (res && res.success) {
      detailData.value = res.body
    } else {
      ElMessage.error(res?.message || '操作ログの取得に失敗しました')
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
          <span class="header-title">操作ログ</span>
        </div>
      </template>

      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="操作者">
            <el-input v-model="searchForm.operatorId" placeholder="部分一致" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="操作タイプ">
            <el-input v-model="searchForm.operationType" placeholder="完全一致" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="対象種別">
            <el-input v-model="searchForm.targetType" placeholder="完全一致" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="結果">
            <el-select v-model="searchForm.resultStatus" placeholder="すべて" clearable style="width: 120px">
              <el-option :value="1" label="成功" />
              <el-option :value="0" label="失敗" />
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
        <el-table-column label="操作者" width="120" prop="operatorId" />
        <el-table-column label="操作タイプ" width="120" prop="operationType" />
        <el-table-column label="対象種別" width="100" prop="targetType" />
        <el-table-column label="対象ID" width="80" prop="targetId" />
        <el-table-column label="説明" min-width="200" prop="description" show-overflow-tooltip />
        <el-table-column label="結果" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="resultTypeMap[row.resultStatus] || 'info'" size="small">
              {{ resultMap[row.resultStatus] || row.resultStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="IPアドレス" width="130" prop="clientIp" />
        <el-table-column label="操作日時" width="160" prop="createTime" />
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

    <!-- 詳細ダイアログ -->
    <el-dialog v-model="detailDialogVisible" title="操作ログ詳細" width="800px" :close-on-click-modal="false">
      <div v-loading="detailLoading">
        <template v-if="detailData">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="ログID">{{ detailData.id }}</el-descriptions-item>
            <el-descriptions-item label="操作者">{{ detailData.operatorId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="操作タイプ">{{ detailData.operationType || '-' }}</el-descriptions-item>
            <el-descriptions-item label="対象種別">{{ detailData.targetType || '-' }}</el-descriptions-item>
            <el-descriptions-item label="対象ID">{{ detailData.targetId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="結果">
              <el-tag :type="resultTypeMap[detailData.resultStatus] || 'info'" size="small">
                {{ resultMap[detailData.resultStatus] || detailData.resultStatus }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="IPアドレス">{{ detailData.clientIp || '-' }}</el-descriptions-item>
            <el-descriptions-item label="トレースID">{{ detailData.traceId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="URL" :span="2">{{ detailData.requestUrl || '-' }}</el-descriptions-item>
            <el-descriptions-item label="HTTPメソッド">{{ detailData.httpMethod || '-' }}</el-descriptions-item>
            <el-descriptions-item label="説明" :span="2">{{ detailData.description || '-' }}</el-descriptions-item>
            <el-descriptions-item label="リクエストパラメータ" :span="2">
              <pre style="white-space: pre-wrap; word-break: break-all; max-height: 150px; overflow-y: auto; background: #f5f7fa; padding: 8px; border-radius: 4px;">{{ detailData.requestParams || '-' }}</pre>
            </el-descriptions-item>
            <el-descriptions-item label="変更前データ" :span="2">
              <pre style="white-space: pre-wrap; word-break: break-all; max-height: 200px; overflow-y: auto; background: #f5f7fa; padding: 8px; border-radius: 4px;">{{ detailData.beforeJson || '-' }}</pre>
            </el-descriptions-item>
            <el-descriptions-item label="変更後データ" :span="2">
              <pre style="white-space: pre-wrap; word-break: break-all; max-height: 200px; overflow-y: auto; background: #f5f7fa; padding: 8px; border-radius: 4px;">{{ detailData.afterJson || '-' }}</pre>
            </el-descriptions-item>
            <el-descriptions-item label="作成日時">{{ detailData.createTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="更新日時">{{ detailData.updateTime || '-' }}</el-descriptions-item>
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
.search-area { margin-bottom: 16px; }
.pagination-area { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
