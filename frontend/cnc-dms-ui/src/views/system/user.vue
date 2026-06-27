<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, getUser, deleteUser, updateUser, createUser } from '@/api/user'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

// ==================== 搜索筛选 ====================
const searchForm = reactive({ username: '' })

// ==================== 表格数据 ====================
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const selectedRows = ref([])

// ==================== 弹窗（新增/编辑） ====================
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const form = reactive({
  userid: '',
  user_name: '',
  password: '',
  enabled: true,
})

const dialogTitle = computed(() => (isEdit.value ? '编辑用户' : '新增用户'))

// 表单校验规则
const rules = computed(() => ({
  userid: [
    { required: true, message: '请输入用户ID', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户ID只能包含字母、数字和下划线', trigger: 'blur' },
  ],
  user_name: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度为 2~50 个字符', trigger: 'blur' },
  ],
  password: isEdit.value
    ? []
    : [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 32, message: '密码长度为 6~32 个字符', trigger: 'blur' },
      ],
}))

// ==================== 数据请求 ====================
async function fetchList() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (searchForm.username) params.username = searchForm.username
    const res = await getUserList(params)
    if (res && res.success) {
      const body = res.body || {}
      tableData.value = body.list || []
      total.value = body.total || 0
    } else {
      ElMessage.error(res?.message || '查询失败')
    }
  } catch (err) {
    const status = err?.response?.status
    const msg = err?.response?.data?.message || err?.message || '请求失败'

    if (status === 403) {
      // 403 权限不足：给出明确提示并提供返回入口
      ElMessageBox.alert(
        msg,
        '权限不足',
        {
          confirmButtonText: '返回主菜单',
          type: 'warning',
          callback: () => router.push('/home'),
        },
      )
    } else {
      ElMessage.error(msg)
    }
    console.error('获取用户列表失败:', err)
  } finally {
    loading.value = false
  }
}

// ==================== 搜索/分页 ====================
function handleSearch() {
  pageNum.value = 1
  fetchList()
}

function handleReset() {
  searchForm.username = ''
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

// ==================== 选择 ====================
function handleSelectionChange(val) {
  selectedRows.value = val
}

// ==================== 用户ID链接 → 详情页 ====================
function handleIdClick(row) {
  const userid = row.userid || row.id
  router.push({ path: `/system/user/${userid}` })
}

// ==================== 新增用户 ====================
function handleAdd() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// ==================== 编辑用户 ====================
async function handleEdit(row) {
  isEdit.value = true
  resetForm()
  const userid = row.userid || row.id
  try {
    const res = await getUser(userid)
    if (res && res.success) {
      const data = res.body || {}
      form.userid = data.userid || data.id || ''
      form.user_name = data.userName || data.user_name || ''
      form.enabled = data.enabled !== undefined ? data.enabled : true
      form.password = ''
    } else {
      ElMessage.error(res?.message || '获取用户信息失败')
      return
    }
  } catch (err) {
    const msg = err?.response?.data?.message || err?.message || '请求失败'
    ElMessage.error(msg)
    console.error('获取用户详情失败:', err)
    return
  }
  dialogVisible.value = true
}

// ==================== 弹窗提交 ====================
async function handleSubmit() {
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
      if (!isEdit.value) {
        payload.password = form.password
      } else if (form.password) {
        // 编辑时如果填写了密码则更新
        payload.password = form.password
      }

      let res
      if (isEdit.value) {
        res = await updateUser(payload)
      } else {
        res = await createUser(payload)
      }

      if (res && res.success) {
        ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
        dialogVisible.value = false
        fetchList()
      } else {
        ElMessage.error(res?.message || '保存失败')
      }
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || '请求失败'
      ElMessage.error(msg)
      console.error('保存请求失败:', err)
    } finally {
      submitLoading.value = false
    }
  })
}

// ==================== 删除选中 ====================
function handleDeleteSelected() {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择要删除的用户')
    return
  }
  const userIds = selectedRows.value.map((r) => r.userid || r.id)
  ElMessageBox.confirm(
    `确定删除选中的 ${selectedRows.value.length} 条用户吗？（用户ID: ${userIds.join(', ')}）`,
    '删除确认',
    { type: 'warning' },
  )
    .then(async () => {
      let failCount = 0
      for (const r of selectedRows.value) {
        const uid = r.userid || r.id
        try {
          const res = await deleteUser(uid)
          if (!res || !res.success) {
            ElMessage.error(res?.message || `删除用户 ${uid} 失败`)
            failCount++
          }
        } catch (err) {
          const msg = err?.response?.data?.message || err?.message || '删除失败'
          ElMessage.error(msg)
          console.error('删除请求失败:', err)
          failCount++
        }
      }
      if (failCount === 0) {
        ElMessage.success('删除成功')
      }
      selectedRows.value = []
      fetchList()
    })
    .catch(() => {})
}

// ==================== 返回主菜单 ====================
function handleBackToHome() {
  router.push('/home')
}

// ==================== 重置表单 ====================
function resetForm() {
  form.userid = ''
  form.user_name = ''
  form.password = ''
  form.enabled = true
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="user-list-wrapper">
    <el-card>
      <!-- 头部操作栏 -->
      <template #header>
        <div class="header-bar">
          <span class="header-title">用户信息一览</span>
          <div class="header-actions">
            <el-button @click="handleBackToHome">返回主菜单</el-button>
            <el-button type="primary" @click="handleAdd">新增用户</el-button>
            <el-button type="danger" plain @click="handleDeleteSelected">删除</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索筛选区域 -->
      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="用户ID">
            <el-input
              v-model="searchForm.username"
              placeholder="请输入用户ID"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <el-table
        :data="tableData"
        style="width: 100%"
        border
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="用户ID" width="180">
          <template #default="{ row }">
            <a href="javascript:;" class="user-id-link" @click="() => handleIdClick(row)">
              {{ row.userid || row.username || row.id }}
            </a>
          </template>
        </el-table-column>
        <el-table-column label="用户名" min-width="160">
          <template #default="{ row }">
            {{ row.userName || row.user_name }}
          </template>
        </el-table-column>
        <el-table-column label="启用" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ row.create_time || row.createTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="() => handleEdit(row)">
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-area">
        <el-pagination
          :page-size="pageSize"
          :current-page="pageNum"
          :total="total"
          layout="prev, pager, next, jumper, sizes, total"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      :close-on-click-modal="false"
      @close="resetForm"
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
            :disabled="isEdit"
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
        <el-form-item
          v-if="!isEdit"
          label="密码"
          prop="password"
        >
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            maxlength="32"
          />
        </el-form-item>
        <el-form-item v-if="isEdit" label="新密码">
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
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.user-list-wrapper {
  padding: 24px;
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

.search-area {
  margin: 12px 0;
}

.user-id-link {
  color: #409eff;
  text-decoration: none;
}

.user-id-link:hover {
  text-decoration: underline;
}

.pagination-area {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
