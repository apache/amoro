import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { viteMockServe } from 'vite-plugin-mock'
import path from 'node:path'

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
  build: {
    outDir: './src/main/resources/static',
  },
  plugins: [
    vue(),
    viteMockServe({
      mockPath: 'mock',
      enable: true,
    })
  ],
  server: {
    port: 8080,

    /**
     * If you run the server on you local backend
     * Maybe you need to open the Proxy
     */
    // proxy: {
    //   '^/ams': {
    //     // change the target to your backend server
    //     // Such as target: 'http://127.0.0.1:xxx',
    //     target: 'http://127.0.0.1:8080',
    //     changeOrigin: true,
    //     configure(_, options) {
    //       // configure proxy header here
    //       options.headers = {
    //         'cookie': 'JSESSIONID=node07rhpm05aujgi1amdr8stpj9xa4.node0',
    //         'Access-Control-Allow-Origin': '*',
    //         'Access-Control-Allow-Credentials': 'true',
    //         'Access-Control-Allow-Headers':
    //           'Content-Type, Content-Length, Authorization, Accept, X-Requested-With , yourHeaderFeild',
    //         'Access-Control-Allow-Methods': 'PUT,POST,GET,DELETE,OPTIONS'
    //       }
    //     }
    //   }
    // }
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src') // 路径别名
    }
  }
})
