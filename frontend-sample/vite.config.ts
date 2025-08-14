import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
// [추가] @ 별칭을 위해 path 임포트
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  // [추가] @ → /src 별칭 설정 (경로 혼선을 원천 차단)
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
