/**
 * 集中式 Axios 实例
 *
 * 所有 API 请求统一走此实例，避免在业务模块中散落 baseURL / 拦截器等配置。
 * 基础路径由环境变量 VITE_API_BASE_URL 控制，运行时注入，无需修改代码。
 */
import axios from 'axios'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

// ==================== 请求拦截：自动附带 Token ====================
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// ==================== 响应拦截：统一处理 401 / 403 ====================
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const url = error.config?.url || ''
    const method = (error.config?.method || '').toUpperCase()

    // --- 调试日志：便于排查后端权限问题 ---
    console.warn(
      `[API Error] ${method} ${url} → HTTP ${status}`,
      error.response?.data || '(无响应体)',
    )

    if (status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }

    if (status === 403) {
      // 根据请求的 API 路径生成更精准的提示
      const backendMsg = error.response?.data?.message || ''
      let defaultMsg = '没有权限执行此操作，请联系管理员'

      if (url.includes('/user/list')) {
        defaultMsg = '没有权限查看用户列表，请联系管理员分配权限'
      } else if (url.includes('/user/update')) {
        defaultMsg = '没有权限修改用户信息，请联系管理员'
      } else if (url.includes('/user')) {
        defaultMsg = '没有权限访问该用户信息，请联系管理员'
      } else if (url.includes('/auth/')) {
        defaultMsg = '认证失败或权限不足，请重新登录'
        // 认证相关 403 可能是 token 问题，清理并跳转
        localStorage.removeItem('token')
        window.location.href = '/login'
        return Promise.reject(error)
      }

      error.message = backendMsg || defaultMsg
    }

    return Promise.reject(error)
  },
)

export default apiClient
