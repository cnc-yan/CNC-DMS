<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

const appTitle = import.meta.env.VITE_APP_TITLE

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const ok = await authStore.login(form.username, form.password)
    if (ok) {
      ElMessage.success('登录成功')
      router.push('/home')
    } else {
      ElMessage.error('用户名或密码错误')
    }
  } catch {
    ElMessage.error('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-wrapper">
    <el-card class="login-card">
      <template #header>
        <h2 class="login-title">{{ appTitle }}</h2>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        size="default"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}

.login-card {
  width: 400px;
  max-width: 90vw;
}

.login-title {
  margin: 0;
  text-align: center;
  font-size: 20px;
  font-weight: 600;
}
</style>
