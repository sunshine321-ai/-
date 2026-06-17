import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/ai-tutor': 'http://localhost:8000',
      '/api': 'http://localhost:8000',
      '/video': 'http://localhost:8000',
      '/study-raw': 'http://localhost:8000',
      '/calculator-raw': 'http://localhost:8000',
      '/health': 'http://localhost:8000',
      '/static': 'http://localhost:8000',
    },
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
  },
})
