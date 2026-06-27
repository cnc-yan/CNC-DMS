<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoom, updateRoom, deleteRoom } from '@/api/room'
import { getDormitory } from '@/api/dormitory'
import { useAuthStore } from '@/store/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const roomId = Number(route.params.id)

const loading = ref(false)
const dormitoryName = ref('')

async function fetchDormitoryName(id) {
  if (!id) return
  try {
    const res = await getDormitory(id)
    if (res?.success) dormitoryName.value = res.body?.dormName || ''
  } catch { /* ignore */ }
}

const detail = reactive({
  id: null, dormitoryId: null, roomNumber: '', floor: null, capacity: 1,
  currentOccupancy: 0, dailyRate: null,
  roomType: 0, acType: 0, memo1: '', memo2: '', memo3: '',
  status: 1, version: 0,
  createBy: '', createTime: '', updateBy: '', updateTime: '',
})

const dialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const form = reactive({
  roomNumber: '', floor: null, capacity: 1, dailyRate: null,
  roomType: 0, acType: 0, memo1: '', memo2: '', memo3: '',
  status: 1, version: 0,
})

const rules = {
  roomNumber: [{ required: true, message: '部屋番号を入力してください', trigger: 'blur' }],
}

const statusMap = { 1: '空室', 2: '満室', 0: '使用不可' }
const statusTypeMap = { 1: 'success', 2: 'warning', 0: 'info' }

async function fetchDetail() {
  loading.value = true
  try {
    const res = await getRoom(roomId)
    if (res && res.success) {
      const data = res.body || {}
      Object.assign(detail, data)
      fetchDormitoryName(data.dormitoryId)
    } else ElMessage.error(res?.message || '部屋情報の取得に失敗しました')
  } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗') }
  finally { loading.value = false }
}

function goBack() { router.push('/master/room') }

function openEditDialog() {
  form.roomNumber = detail.roomNumber; form.floor = detail.floor; form.capacity = detail.capacity
  form.dailyRate = detail.dailyRate
  form.roomType = detail.roomType; form.acType = detail.acType
  form.memo1 = detail.memo1 || ''; form.memo2 = detail.memo2 || ''; form.memo3 = detail.memo3 || ''
  form.status = detail.status; form.version = detail.version
  dialogVisible.value = true
}

async function handleEditSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = {
        id: roomId, roomNumber: form.roomNumber, floor: form.floor, capacity: form.capacity,
        dailyRate: form.dailyRate,
        roomType: form.roomType, acType: form.acType,
        memo1: form.memo1 || null, memo2: form.memo2 || null, memo3: form.memo3 || null,
        status: form.status, version: form.version,
        updateBy: authStore.userid || authStore.username || 'system',
      }
      const res = await updateRoom(payload)
      if (res && res.success) { ElMessage.success('更新しました'); dialogVisible.value = false; fetchDetail() }
      else ElMessage.error(res?.message || '更新に失敗しました')
    } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || '更新失敗') }
    finally { submitLoading.value = false }
  })
}

function handleDelete() {
  ElMessageBox.confirm(`部屋「${detail.roomNumber}」を削除してもよろしいですか？`, '削除確認',
    { type: 'warning', confirmButtonText: '削除', cancelButtonText: 'キャンセル' })
    .then(async () => {
      try {
        const res = await deleteRoom(roomId)
        if (res && res.success) { ElMessage.success('削除しました'); router.push('/master/room') }
        else ElMessage.error(res?.message || '削除に失敗しました')
      } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || '削除失敗') }
    }).catch(() => {})
}

function resetForm() { if (formRef.value) formRef.value.resetFields() }

onMounted(() => fetchDetail())
</script>

<template>
  <div class="detail-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="header-bar">
          <span class="header-title">部屋詳細</span>
          <div class="header-actions">
            <el-button @click="goBack">戻る</el-button>
            <el-button type="primary" @click="openEditDialog">編集</el-button>
            <el-button type="danger" @click="handleDelete">削除</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="部屋ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="所属寮">
          {{ dormitoryName || `ID: ${detail.dormitoryId}` }}
        </el-descriptions-item>
        <el-descriptions-item label="部屋番号">{{ detail.roomNumber }}</el-descriptions-item>
        <el-descriptions-item label="階数">{{ detail.floor ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="定員">{{ detail.capacity }}</el-descriptions-item>
        <el-descriptions-item label="現在入居者数">{{ detail.currentOccupancy }}</el-descriptions-item>
        <el-descriptions-item label="1日単価">{{ detail.dailyRate != null ? `¥${Number(detail.dailyRate).toLocaleString()}` : '-' }}</el-descriptions-item>
        <el-descriptions-item label="室区分">{{ {0:'不明',1:'和室',2:'洋室'}[detail.roomType] || '-' }}</el-descriptions-item>
        <el-descriptions-item label="エアコン">{{ detail.acType === 1 ? 'あり' : 'なし' }}</el-descriptions-item>
        <el-descriptions-item label="メモ1">{{ detail.memo1 || '-' }}</el-descriptions-item>
        <el-descriptions-item label="メモ2">{{ detail.memo2 || '-' }}</el-descriptions-item>
        <el-descriptions-item label="メモ3">{{ detail.memo3 || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状態">
          <el-tag :type="statusTypeMap[detail.status] || 'info'" size="small">{{ statusMap[detail.status] || '-' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="バージョン">{{ detail.version }}</el-descriptions-item>
        <el-descriptions-item label="作成者">{{ detail.createBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="作成日時">{{ detail.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新者">{{ detail.updateBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新日時">{{ detail.updateTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-dialog v-model="dialogVisible" title="部屋編集" width="520px" :close-on-click-modal="false" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" status-icon>
        <el-form-item label="部屋番号" prop="roomNumber"><el-input v-model="form.roomNumber" maxlength="20" /></el-form-item>
        <el-form-item label="階数"><el-input-number v-model="form.floor" :min="0" :precision="0" /></el-form-item>
        <el-form-item label="定員"><el-input-number v-model="form.capacity" :min="1" :precision="0" /></el-form-item>
        <el-form-item label="1日単価"><el-input-number v-model="form.dailyRate" :min="0" :precision="2" :step="500" /></el-form-item>
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
        <el-form-item label="メモ1"><el-input v-model="form.memo1" maxlength="255" /></el-form-item>
        <el-form-item label="メモ2"><el-input v-model="form.memo2" maxlength="255" /></el-form-item>
        <el-form-item label="メモ3"><el-input v-model="form.memo3" maxlength="255" /></el-form-item>
        <el-form-item label="状態">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">空室</el-radio>
            <el-radio :value="2">満室</el-radio>
            <el-radio :value="0">使用不可</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">キャンセル</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleEditSubmit">保存</el-button>
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
