<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDepartment, updateDepartment, deleteDepartment } from '@/api/department'
import { useAuthStore } from '@/store/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const deptId = Number(route.params.id)

const loading = ref(false)
const detail = reactive({
  id: null,
  deptCode: '',
  deptName: '',
  region: '',
  parentId: null,
  sortOrder: 0,
  status: 1,
  version: 0,
  createBy: '',
  createTime: '',
  updateBy: '',
  updateTime: '',
})

const dialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const form = reactive({
  deptName: '',
  region: '',
  parentId: null,
  sortOrder: 0,
  status: 1,
  version: 0,
})

const rules = {
  deptName: [{ required: true, message: '部門名を入力してください', trigger: 'blur' }],
}

async function fetchDetail() {
  loading.value = true
  try {
    const res = await getDepartment(deptId)
    if (res && res.success) {
      const data = res.body || {}
      Object.assign(detail, {
        id: data.id,
        deptCode: data.deptCode || '',
        deptName: data.deptName || '',
        region: data.region || '',
        parentId: data.parentId ?? null,
        sortOrder: data.sortOrder ?? 0,
        status: data.status ?? 1,
        version: data.version ?? 0,
        createBy: data.createBy || '-',
        createTime: data.createTime || '-',
        updateBy: data.updateBy || '-',
        updateTime: data.updateTime || '-',
      })
    } else {
      ElMessage.error(res?.message || '部門情報の取得に失敗しました')
    }
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || err?.message || 'リクエスト失敗')
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/master/department')
}

function openEditDialog() {
  form.deptName = detail.deptName
  form.region = detail.region
  form.parentId = detail.parentId
  form.sortOrder = detail.sortOrder
  form.status = detail.status
  form.version = detail.version
  dialogVisible.value = true
}

async function handleEditSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = {
        id: deptId,
        deptName: form.deptName,
        region: form.region,
        parentId: form.parentId || null,
        sortOrder: form.sortOrder,
        status: form.status,
        version: form.version,
        updateBy: authStore.userid || authStore.username || 'system',
      }
      const res = await updateDepartment(payload)
      if (res && res.success) {
        ElMessage.success('更新しました')
        dialogVisible.value = false
        fetchDetail()
      } else {
        ElMessage.error(res?.message || '更新に失敗しました')
      }
    } catch (err) {
      ElMessage.error(err?.response?.data?.message || err?.message || '更新失敗')
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDelete() {
  ElMessageBox.confirm(
    `部門「${detail.deptName}」を削除してもよろしいですか？`,
    '削除確認',
    { type: 'warning', confirmButtonText: '削除', cancelButtonText: 'キャンセル' },
  )
    .then(async () => {
      try {
        const res = await deleteDepartment(deptId)
        if (res && res.success) {
          ElMessage.success('削除しました')
          router.push('/master/department')
        } else {
          ElMessage.error(res?.message || '削除に失敗しました')
        }
      } catch (err) {
        ElMessage.error(err?.response?.data?.message || err?.message || '削除失敗')
      }
    })
    .catch(() => {})
}

function resetForm() {
  if (formRef.value) formRef.value.resetFields()
}

onMounted(() => fetchDetail())
</script>

<template>
  <div class="detail-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="header-bar">
          <span class="header-title">部門詳細</span>
          <div class="header-actions">
            <el-button @click="goBack">戻る</el-button>
            <el-button type="primary" @click="openEditDialog">編集</el-button>
            <el-button type="danger" @click="handleDelete">削除</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="部門ID" :span="1">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="部門コード" :span="1">{{ detail.deptCode }}</el-descriptions-item>
        <el-descriptions-item label="部門名" :span="1">{{ detail.deptName }}</el-descriptions-item>
        <el-descriptions-item label="地域" :span="1">{{ detail.region || '-' }}</el-descriptions-item>
        <el-descriptions-item label="親部門ID" :span="1">{{ detail.parentId ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="表示順" :span="1">{{ detail.sortOrder }}</el-descriptions-item>
        <el-descriptions-item label="状態" :span="1">
          <el-tag :type="detail.status === 1 ? 'success' : 'info'" size="small">
            {{ detail.status === 1 ? '有効' : '無効' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="バージョン" :span="1">{{ detail.version }}</el-descriptions-item>
        <el-descriptions-item label="作成者" :span="1">{{ detail.createBy }}</el-descriptions-item>
        <el-descriptions-item label="作成日時" :span="1">{{ detail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新者" :span="1">{{ detail.updateBy }}</el-descriptions-item>
        <el-descriptions-item label="更新日時" :span="1">{{ detail.updateTime }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 編集ダイアログ -->
    <el-dialog
      v-model="dialogVisible"
      title="部門編集"
      width="520px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" status-icon>
        <el-form-item label="部門名" prop="deptName">
          <el-input v-model="form.deptName" maxlength="100" />
        </el-form-item>
        <el-form-item label="地域">
          <el-input v-model="form.region" maxlength="50" />
        </el-form-item>
        <el-form-item label="親部門ID">
          <el-input-number v-model="form.parentId" :min="0" :precision="0" />
        </el-form-item>
        <el-form-item label="表示順">
          <el-input-number v-model="form.sortOrder" :min="0" :precision="0" />
        </el-form-item>
        <el-form-item label="状態">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">有効</el-radio>
            <el-radio :value="0">無効</el-radio>
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
