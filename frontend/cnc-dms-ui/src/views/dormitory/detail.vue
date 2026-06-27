<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDormitory, updateDormitory, deleteDormitory } from '@/api/dormitory'
import { useAuthStore } from '@/store/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const dormId = Number(route.params.id)

const loading = ref(false)
const detail = reactive({
  id: null, dormName: '', region: '', address: '', dormCondition: '',
  totalRooms: 0, status: 1, version: 0,
  createBy: '', createTime: '', updateBy: '', updateTime: '',
})

const dialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const form = reactive({
  dormName: '', region: '', address: '', dormCondition: '', totalRooms: 0, status: 1, version: 0,
})

const rules = {
  dormName: [{ required: true, message: '寮名を入力してください', trigger: 'blur' }],
  region: [{ required: true, message: '地域を入力してください', trigger: 'blur' }],
}

const conditionMap = { MALE: '男性のみ', FEMALE: '女性のみ', ANY: '制限なし' }

async function fetchDetail() {
  loading.value = true
  try {
    const res = await getDormitory(dormId)
    if (res && res.success) {
      const data = res.body || {}
      Object.assign(detail, data)
    } else ElMessage.error(res?.message || '寮情報の取得に失敗しました')
  } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗') }
  finally { loading.value = false }
}

function goBack() { router.push('/master/dormitory') }

function openEditDialog() {
  form.dormName = detail.dormName; form.region = detail.region; form.address = detail.address
  form.dormCondition = detail.dormCondition; form.totalRooms = detail.totalRooms
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
        id: dormId, dormName: form.dormName, region: form.region, address: form.address || null,
        dormCondition: form.dormCondition || null, totalRooms: form.totalRooms, status: form.status,
        version: form.version, updateBy: authStore.userid || authStore.username || 'system',
      }
      const res = await updateDormitory(payload)
      if (res && res.success) { ElMessage.success('更新しました'); dialogVisible.value = false; fetchDetail() }
      else ElMessage.error(res?.message || '更新に失敗しました')
    } catch (err) { ElMessage.error(err?.response?.data?.message || err?.message || '更新失敗') }
    finally { submitLoading.value = false }
  })
}

function handleDelete() {
  ElMessageBox.confirm(`寮「${detail.dormName}」を削除してもよろしいですか？`, '削除確認',
    { type: 'warning', confirmButtonText: '削除', cancelButtonText: 'キャンセル' })
    .then(async () => {
      try {
        const res = await deleteDormitory(dormId)
        if (res && res.success) { ElMessage.success('削除しました'); router.push('/master/dormitory') }
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
          <span class="header-title">寮詳細</span>
          <div class="header-actions">
            <el-button @click="goBack">戻る</el-button>
            <el-button type="primary" @click="openEditDialog">編集</el-button>
            <el-button type="danger" @click="handleDelete">削除</el-button>
            <el-button type="success" @click="router.push(`/master/room?dormitoryId=${dormId}`)">部屋一覧</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="寮ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="寮名">{{ detail.dormName }}</el-descriptions-item>
        <el-descriptions-item label="地域">{{ detail.region }}</el-descriptions-item>
        <el-descriptions-item label="入居条件">{{ conditionMap[detail.dormCondition] || detail.dormCondition || '-' }}</el-descriptions-item>
        <el-descriptions-item label="住所" :span="2">{{ detail.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="メモ1">{{ detail.memo1 || '-' }}</el-descriptions-item>
        <el-descriptions-item label="メモ2">{{ detail.memo2 || '-' }}</el-descriptions-item>
        <el-descriptions-item label="メモ3">{{ detail.memo3 || '-' }}</el-descriptions-item>
        <el-descriptions-item label="総部屋数">{{ detail.totalRooms }}</el-descriptions-item>
        <el-descriptions-item label="状態">
          <el-tag :type="detail.status === 1 ? 'success' : 'info'" size="small">{{ detail.status === 1 ? '運用中' : '停止中' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="バージョン">{{ detail.version }}</el-descriptions-item>
        <el-descriptions-item label="作成者">{{ detail.createBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="作成日時">{{ detail.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新者">{{ detail.updateBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新日時">{{ detail.updateTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-dialog v-model="dialogVisible" title="寮編集" width="560px" :close-on-click-modal="false" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" status-icon>
        <el-form-item label="寮名" prop="dormName"><el-input v-model="form.dormName" maxlength="100" /></el-form-item>
        <el-form-item label="地域" prop="region"><el-input v-model="form.region" maxlength="50" /></el-form-item>
        <el-form-item label="住所"><el-input v-model="form.address" type="textarea" :rows="2" maxlength="200" /></el-form-item>
        <el-form-item label="入居条件">
          <el-select v-model="form.dormCondition" placeholder="選択" clearable style="width: 160px">
            <el-option label="男性のみ" value="MALE" />
            <el-option label="女性のみ" value="FEMALE" />
            <el-option label="制限なし" value="ANY" />
          </el-select>
        </el-form-item>
        <el-form-item label="総部屋数"><el-input-number v-model="form.totalRooms" :min="0" :precision="0" /></el-form-item>
        <el-form-item label="状態">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">運用中</el-radio>
            <el-radio :value="0">停止中</el-radio>
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
