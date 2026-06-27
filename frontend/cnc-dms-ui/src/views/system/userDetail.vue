<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUser, deleteUser, updateUser } from '@/api/user'
import { useAuthStore } from '@/store/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const userid = route.params.id

// ==================== 详情数据 ====================
const detail = reactive({
  userid: '',
  user_name: '',
  enabled: false,
  createBy: '',
  createTime: '',
  updateBy: '',
  updateTime: '',
})
const loading = ref(false)
const isCreate = userid === 'new'

// ==================== 编辑弹窗 ====================
const dialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const form = reactive({
  userid: '',
  user_name: '',
  password: '',
  enabled: true,
})

const rules = {
  userid: [
    { required: true, message: '请输入用户ID', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户ID只能包含字母、数字和下划线', trigger: 'blur' },
  ],
  user_name: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度为 2~50 个字符', trigger: 'blur' },
  ],
}

// ==================== 获取详情 ====================
async function fetchDetail() {
  if (isCreate) return
  loading.value = true
  try {
    const res = await getUser(userid)
    if (res && res.success) {
      const data = res.body || {}
      detail.userid = data.userid || data.id || ''
      detail.user_name = data.userName || data.user_name || ''
      detail.enabled = data.enabled !== undefined ? data.enabled : false
      detail.createBy = data.createBy || data.create_by || '-'
      detail.createTime = data.createTime || data.create_time || '-'
      detail.updateBy = data.updateBy || data.update_by || '-'
      detail.updateTime = data.updateTime || data.update_time || '-'
    } else {
      ElMessage.error(res?.message || '获取用户信息失败')
    }
  } catch (err) {
    const status = err?.response?.status
    const msg = err?.response?.data?.message || err?.message || '请求失败'

    if (status === 403) {
      ElMessageBox.alert(
        msg,
        '权限不足',
        {
          confirmButtonText: '返回列表',
          type: 'warning',
          callback: () => router.push('/system/user'),
        },
      )
    } else {
      ElMessage.error(msg)
    }
    console.error('获取用户详情失败:', err)
  } finally {
    loading.value = false
  }
}

// ==================== 返回 ====================
function goBack() {
  router.back()
}

// ==================== 删除 ====================
function handleDelete() {
  if (isCreate) return
  ElMessageBox.confirm(
    `确定删除用户「${detail.userid}」吗？此操作不可恢复。`,
    '删除确认',
    { type: 'warning' },
  )
    .then(async () => {
      try {
        const res = await deleteUser(userid)
        if (res && res.success) {
          ElMessage.success('删除成功')
          router.push('/system/user')
        } else {
          ElMessage.error(res?.message || '删除失败')
        }
      } catch (err) {
        const msg = err?.response?.data?.message || err?.message || '请求失败'
        ElMessage.error(msg)
        console.error('删除请求失败:', err)
      }
    })
    .catch(() => {})
}

// ==================== 打开编辑弹窗 ====================
function openEditDialog() {
  form.userid = detail.userid
  form.user_name = detail.user_name
  form.password = ''
  form.enabled = detail.enabled
  dialogVisible.value = true
}

// ==================== 编辑提交 ====================
async function handleEditSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = {
        userid: form.userid,
        userName: form.user_name,
        enabled: form.enabled,
        updateBy: authStore.userid || authStore.username || 'system',
      }
      if (form.password) {
        payload.password = form.password
      }

      const res = await updateUser(payload)
      if (res && res.success) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        // 刷新详情
        fetchDetail()
      } else {
        ElMessage.error(res?.message || '更新失败')
      }
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || '请求失败'
      ElMessage.error(msg)
      console.error('更新请求失败:', err)
    } finally {
      submitLoading.value = false
    }
  })
}

// ==================== 重置编辑表单 ====================
function resetEditForm() {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="detail-wrapper">
    <el-card v-loading="loading">
      <!-- 头部 -->
      <template #header>
        <div class="header-bar">
          <span class="header-title">用户详情</span>
          <div class="header-actions">
            <el-button @click="goBack">返回</el-button>
            <el-button type="warning" @click="openEditDialog">编辑</el-button>
            <el-button type="danger" @click="handleDelete">删除</el-button>
          </div>
        </div>
      </template>

      <!-- 详情内容 -->
      <el-descriptions :column="2" border size="default">
        <el-descriptions-item label="用户ID" :span="1">
          <strong>{{ detail.userid || '-' }}</strong>
        </el-descriptions-item>
        <el-descriptions-item label="用户名" :span="1">
          {{ detail.user_name || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="启用状态" :span="1">
          <el-tag :type="detail.enabled ? 'success' : 'info'" size="small">
            {{ detail.enabled ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建人" :span="1">
          {{ detail.createBy }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="1">
          {{ detail.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="更新人" :span="1">
          {{ detail.updateBy }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="2">
          {{ detail.updateTime }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑用户"
      width="520px"
      :close-on-click-modal="false"
      @close="resetEditForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="default"
        status-icon
      >
        <el-form-item label="用户ID" prop="userid">
          <el-input
            v-model="form.userid"
            placeholder="请输入用户ID"
            disabled
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="用户名" prop="user_name">
          <el-input
            v-model="form.user_name"
            placeholder="请输入用户名"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="留空则不修改密码"
            show-password
            maxlength="32"
          />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleEditSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.detail-wrapper {
  padding: 24px;
  max-width: 900px;
  margin: 0 auto;
}

.header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 8px;
}
</style>
