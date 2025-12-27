import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],

  // 路径别名配置
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },

  // 开发服务器配置
  server: {
    port: 5173,
    host: true,
    // 代理配置：将 /api 请求转发到后端服务
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 后端服务地址
        changeOrigin: true,              // 开启跨域
        secure: false,                   // 允许非HTTPS
        // rewrite: (path) => path.replace(/^\/api/, '/api') // 保持 /api 前缀
      }
    }
  }
})
