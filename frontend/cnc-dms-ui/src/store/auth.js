import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getCurrentUser } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  // --- state ---
  const token = ref(localStorage.getItem('token') || '')
  const userid = ref('')    // 当前登录的用户ID（登录标识）
  const username = ref('')  // 用于显示的用户名

  // --- getters ---
  const isAuthenticated = computed(() => !!token.value)

  // --- actions ---
  async function login(useridInput, passwordInput) {
    const res = await loginApi(useridInput, passwordInput)
    // 后端统一返回 Result<T> 包装：{ success, message, body: T }
    // loginApi 已解包一层 (res.data)，Result 结构在 res 上，token 在 res.body.token
    if (res.success && res.body?.token) {
      token.value = res.body.token
      localStorage.setItem('token', res.body.token)
      return true
    }
    return false
  }

  async function fetchCurrentUser() {
    if (!token.value) return
    try {
      const data = await getCurrentUser()
      // 后端统一返回 Result<T> 包装，实际数据在 data.body
      if (data.success && data.body) {
        userid.value = data.body.userid || data.body.username || ''
        username.value = data.body.userName || data.body.username || ''
      }
    } catch {
      // token invalid — force logout
      logout()
    }
  }

  function logout() {
    token.value = ''
    userid.value = ''
    username.value = ''
    localStorage.removeItem('token')
  }

  return {
    token,
    userid,
    username,
    isAuthenticated,
    login,
    fetchCurrentUser,
    logout,
  }
})
