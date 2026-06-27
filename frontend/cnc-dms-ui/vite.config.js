import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // 加载对应 mode 的环境变量（.env + .env.[mode] + .env.[mode].local）
  const env = loadEnv(mode, process.cwd(), '')

  return {
    // 部署基础路径，由各 .env.[mode] 中的 VITE_BASE 控制（默认 /）
    base: env.VITE_BASE || '/',
    plugins: [vue()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src'),
      },
    },
    server: {
      proxy: {
        '/api': {
          // 开发时后端地址，由 .env.development 中的 VITE_PROXY_TARGET 控制
          // 未设置时回退 localhost:8080
          target: env.VITE_PROXY_TARGET || 'http://localhost:8080',
          changeOrigin: true,
        },
      },
    },
  }
})
