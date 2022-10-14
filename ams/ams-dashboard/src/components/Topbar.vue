<template>
  <div class="top-bar">
    <div class="version-info">
      <span class="g-mr-8">{{`${$t('version')}:  ${verInfo.version}`}}</span>
      <span class="g-mr-8">{{`${$t('commitTime')}:  ${verInfo.commitTime}`}}</span>
    </div>
    <a-tooltip placement="bottomRight" arrow-point-at-center>
      <template #title>{{$t('userGuide')}}</template>
      <question-circle-outlined class="question-icon" @click="goDocs" />
    </a-tooltip>
  </div>
</template>

<script lang="ts">

import { defineComponent, onMounted, reactive } from 'vue'
import { QuestionCircleOutlined } from '@ant-design/icons-vue'
import { getVersionInfo } from '@/services/global.service'

interface IVersion {
  version: string
  commitTime: string
}

export default defineComponent ({
  name: 'Topbar',
  components: {
    QuestionCircleOutlined
  },
  setup() {
    const verInfo = reactive<IVersion>({
      version: '',
      commitTime: ''
    })
    const getVersion = async() => {
      const res = await getVersionInfo()
      if (res) {
        verInfo.version = res.version
        verInfo.commitTime = res.commitTime
      }
    }

    const goDocs = () => {
      window.open('https://arctic.netease.com/')
    }
    onMounted(() => {
      getVersion()
    })

    return {
      verInfo,
      goDocs
    }
  }
})
</script>

<style lang="less" scoped>
  .top-bar {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    width: 100%;
    background: #fff;
    border-bottom: 1px solid #e8e8e8;
    box-shadow: 0 1px 4px rgb(0 21 41 / 8%);
    padding: 0 12px 0 0;
    .question-icon {
      font-size: 14px;
    }
  }
</style>
