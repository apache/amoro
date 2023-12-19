import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'node:path'

const ENV = 'DEV'
const ENV_HOST = {
  DEV: 'http://127.0.0.1:1630/', // Change it to the address of your development server
  TEST: '',
  ONLINE: ''
}

const devServer = {
  port: '8080',
  headers: {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Credentials': 'true',
    'Access-Control-Allow-Headers':
      'Content-Type, Content-Length, Authorization, Accept, X-Requested-With , yourHeaderFeild',
    'Access-Control-Allow-Methods': 'PUT,POST,GET,DELETE,OPTIONS'
  },
  proxy: {
    '^/ams': {
      target: ENV_HOST[ENV],
      changeOrigin: true,
      onProxyReq (proxyReq) {
        proxyReq.setHeader('cookie', 'JSESSIONID=node07rhpm05aujgi1amdr8stpj9xa4.node0')
      }
    }
  }
}

const css = {
  preprocessorOptions: {
    less: {
      modifyVars: {
        'primary-color': '#1890ff',
        'link-color': '#1890ff',
        'border-color-base': '#e8e8f0',
        'border-radius-base': '2px',
        'border-color-split': '#e8e8f0',
        'header-color': 'rgba(0, 0, 0, 0.85)',
        'text-color': '#79809a',
        'text-color-secondary': '#c0c0ca',
        'font-size-base': '14px',
        'dark-gray-color': '#2b354a',
        'dark-bg-color': '#202a40',
        'dark-bg-primary-color': '#1a2232'
      },
      javascriptEnabled: true
    }
  }
}

// https://vitejs.dev/config/
export default defineConfig({
  css,
  plugins: [vue()],
  server: {
    port: 8080
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src') // 路径别名
    }
  }
})
