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

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { Modal } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import { DownOutlined, LogoutOutlined, QuestionCircleOutlined, TranslationOutlined } from '@ant-design/icons-vue'
import useStore from '@/store'
import { getVersionInfo } from '@/services/global.service'
import loginService from '@/services/login.service'

interface IVersion {
  version: string
  commitTime: string
}

const verInfo = reactive<IVersion>({
  version: '',
  commitTime: '',
})

const { t, locale } = useI18n()
const router = useRouter()
const store = useStore()

async function getVersion() {
  const res = await getVersionInfo()
  if (res) {
    verInfo.version = res.version
    verInfo.commitTime = res.commitTime
  }
}

function goLoginPage() {
  router.push({ path: '/login' })
}

async function handleLogout() {
  Modal.confirm({
    title: t('logoutModalTitle'),
    onOk: async () => {
      try {
        await loginService.logout()
      }
      catch (error) {
      }
      finally {
        store.updateUserInfo({
          userName: '',
        })
        goLoginPage()
      }
    },
  })
}

function goDocs() {
  window.open('https://amoro.apache.org/docs/latest/')
}

function setLocale() {
  if (locale.value === 'zh') {
    locale.value = 'en'
  }
  else {
    locale.value = 'zh'
  }
}

onMounted(() => {
  getVersion()
})
</script>

<template>
  <div class="custom-top-bar">
    <div class="version-info">
      <span class="g-mr-8">{{ `${$t('version')}:  ${verInfo.version}` }}</span>
      <span class="g-mr-8">{{ `${$t('commitTime')}:  ${verInfo.commitTime}` }}</span>
    </div>
    <a-dropdown>
      <span>{{ store.userInfo.userName }} <DownOutlined /></span>
      <template #overlay>
        <a-menu>
          <a-menu-item key="userGuide" @click="goDocs">
            <QuestionCircleOutlined /> {{ $t('userGuide') }}
          </a-menu-item>
          <a-menu-item key="locale" @click="setLocale">
            <TranslationOutlined /> {{ locale === 'zh' ? '切换至英文' : 'Switch To Chinese' }}
          </a-menu-item>
          <a-menu-item key="logout" @click="handleLogout">
            <LogoutOutlined /> {{ $t('logout') }}
          </a-menu-item>
        </a-menu>
      </template>
    </a-dropdown>
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
