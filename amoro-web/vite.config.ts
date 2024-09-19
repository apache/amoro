/*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

import path from 'node:path'
import process from 'node:process'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { vitePluginFakeServer } from 'vite-plugin-fake-server'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import ViteComponents from 'unplugin-vue-components/vite'
import { AntDesignVueResolver } from 'unplugin-vue-components/resolvers'
import vueDevTools from 'vite-plugin-vue-devtools'

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
        'dark-bg-primary-color': '#1a2232',
      },
      javascriptEnabled: true,
    },
  },
}

// https://vitejs.dev/config/
export default defineConfig({
  css,
  base: './',
  build: {
    outDir: './src/main/resources/static',
    rollupOptions: {
      output: {
        manualChunks: {
          vue: ['vue', 'vue-i18n', 'vue-router', 'vue-virtual-scroller'],
          dayjs: ['dayjs'],
          axios: ['axios'],
          antd: ['ant-design-vue', '@ant-design/icons-vue'],
          pinia: ['pinia'],
          echarts: ['echarts'],
          monaco: ['monaco-editor'],
          sql: ['sql-formatter'],
        },
      },
    },
  },
  plugins: [
    vue(),
    vitePluginFakeServer({
      logger: false,
      include: 'mock',
      infixName: false,
      enableProd: true,
    }),
    createSvgIconsPlugin({
      iconDirs: [path.resolve(process.cwd(), 'src/assets/icons/svg')],
    }),
    ViteComponents({
      resolvers: [
        AntDesignVueResolver({
          importStyle: false,
          resolveIcons: true,
        }),
      ],
    }),
    vueDevTools(),
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
    //     target: 'http://127.0.0.1:xxx',
    //     changeOrigin: true,
    //     configure(_, options) {
    //       // configure proxy header here
    //       options.headers = {
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
      '@': path.resolve(__dirname, './src'), // Path alias
    },
  },
})
