<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 / -->

<script lang="ts">
import { defineComponent, onMounted, reactive } from 'vue'
import { Modal } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'

import useStore from '@/store'
import { getVersionInfo } from '@/services/global.service'
import loginService from '@/services/login.service'

interface IVersion {
  version: string
  commitTime: string
}

export default defineComponent ({
  name: 'Topbar',
  setup() {
    const verInfo = reactive<IVersion>({
      version: '',
      commitTime: '',
    })

    const { t, locale } = useI18n()

    const getVersion = async () => {
      const res = await getVersionInfo()
      if (res) {
        verInfo.version = res.version
        verInfo.commitTime = res.commitTime
      }
    }

    const handleLogout = async () => {
      Modal.confirm({
        title: t('logoutModalTitle'),
        content: '',
        okText: '',
        cancelText: '',
        onOk: async () => {
          try {
            await loginService.logout()
          }
          catch (error) {
          }
          finally {
            const store = useStore()
            store.updateUserInfo({
              userName: '',
            })
            window.location.href = '/login'
          }
        },
      })
    }

    const goDocs = () => {
      window.open('https://amoro.apache.org/docs/latest/')
    }

    const setLocale = ({ key }: { key: string }) => {
      if(locale.value !== key) {
        locale.value = key
      }
    };

    onMounted(() => {
      getVersion()
    })

    return {
      verInfo,
      goDocs,
      handleLogout,
      setLocale,
    }
  },
})
</script>

<template>
  <div class="custom-top-bar">
    <div class="version-info">
      <span class="g-mr-8">{{ `${$t('version')}:  ${verInfo.version}` }}</span>
      <span class="g-mr-8">{{ `${$t('commitTime')}:  ${verInfo.commitTime}` }}</span>
    </div>
    <a-tooltip placement="bottomRight" arrow-point-at-center overlay-class-name="topbar-tooltip">
      <template #title>
        {{ $t('userGuide') }}
      </template>
      <question-circle-outlined class="question-icon" @click="goDocs" />
    </a-tooltip>
    <a-dropdown>
      <TranslationOutlined class="g-ml-8" />
      <template #overlay>
        <a-menu @click="setLocale">
          <a-menu-item key="en">English</a-menu-item>
          <a-menu-item key="zh">中文</a-menu-item>
        </a-menu>
      </template>
    </a-dropdown>
    <a-tooltip>
      <template #title>
        {{ $t('logout') }}
      </template>
      <a-button class="logout-button" @click="handleLogout">
        <LogoutOutlined style="font-size: 1.2em" />
      </a-button>
    </a-tooltip>
  </div>
</template>

<style lang="less">
  .custom-top-bar {
    height: 48px;
    display: flex;
    flex: 1;
    align-items: center;
    justify-content: flex-end;
    background: #fff;
    border-bottom: 1px solid #e8e8e8;
    box-shadow: 0 1px 2px rgb(0 21 41 / 5%);
    padding: 0 12px 0 0;
    font-size: 12px;
    .question-icon {
      font-size: 12px;
      margin-top: -2px;
    }
  }
  .topbar-tooltip .ant-tooltip-inner {
    font-size: 12px;
  }
  .logout-button.ant-btn {
    border: none;
  }
  .logout-button:hover {
    border-color: unset;
  }
</style>
