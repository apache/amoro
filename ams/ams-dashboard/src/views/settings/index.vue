<template>
  <div class="setting-wrap">
    <div class="system-setting">
      <h1 class="g-mb-12">System Setting</h1>
      <ul class="content">
        <li v-for="item in systemSettingArray" :key="item.key" class="item">
          <span class="left">{{item.key}}</span>
          <span class="right">{{item.value}}</span>
        </li>
      </ul>
    </div>
    <div class="container-setting">
      <h1 class="g-mb-12">Container Setting</h1>
      <div v-for="container in containerSetting" :key="container.name" class="container-setting-item">
        <h2 class="g-mb-12 g-mt-12">{{container.name}}</h2>
        <ul class="content">
          <li class="item">
            <span class="left">name</span>
            <span class="right">{{container.name}}</span>
          </li>
          <li class="item">
            <span class="left">type</span>
            <span class="right">{{container.type}}</span>
          </li>
        </ul>
        <h3 class="g-mb-12 g-mt-12">{{$t('properties')}}</h3>
        <ul class="content">
          <li v-for="item in container.propertiesArray" :key="item.key" class="item">
            <span class="left">{{item.key}}</span>
            <span class="right">{{item.value}}</span>
          </li>
        </ul>
        <h3 class="g-mb-12 g-mt-12">{{$t('optimzeGroup')}}</h3>
        <a-table
          rowKey="name"
          :columns="optimzeGroupColumns"
          :data-source="container.optimizeGroup"
          :pagination="false"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive } from 'vue'
import { IColumns, IKeyAndValue, IContainerSetting } from '@/types/common.type'
import { useI18n } from 'vue-i18n'
import { getSystemSetting, getContainersSetting } from '@/services/setting.services'

const { t } = useI18n()
const systemSettingArray = reactive<IKeyAndValue>([])
const containerSetting = reactive<IContainerSetting[]>([
  {
    name: '',
    type: '',
    properties: {},
    propertiesArray: [],
    optimizeGroup: []
  }
])
const optimzeGroupColumns: IColumns[] = reactive([
  { title: t('name'), dataIndex: 'name', ellipsis: true },
  { title: t('propertiesMemory', { type: 'taskmanager' }), dataIndex: 'tmMemory', ellipsis: true },
  { title: t('propertiesMemory', { type: 'jobmanager' }), dataIndex: 'jmMemory', ellipsis: true }
])

async function getSystemSettingInfo() {
  const res = await getSystemSetting()
  if (!res) { return }
  systemSettingArray.length = 0
  Object.keys(res).forEach(key => {
    systemSettingArray.push({
      key: key,
      value: res[key]
    })
  })
}
async function getContainersSettingInfo() {
  const res = await getContainersSetting()
  containerSetting.length = 0;
  (res || []).forEach((ele, index) => {
    ele.propertiesArray = []
    containerSetting.push(ele)
    Object.keys(ele.properties).forEach(key => {
      containerSetting[index].propertiesArray.push({
        key: key,
        value: ele.properties[key]
      })
    })
  })
}
onMounted(() => {
  getSystemSettingInfo()
  getContainersSettingInfo()
})

</script>

<style lang="less" scoped>
.setting-wrap {
  height: 100%;
  overflow: auto;
  h1,h2,h3 {
    font-weight: 500;
  }
  .container-setting {
    padding-top: 12px;
  }
  .content {
    .item {
      padding: 6px 0;
      display: flex;
    }
    .left {
      width: 280px;
    }
  }
}
</style>
