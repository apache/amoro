<template>
  <div class="setting-wrap">
    <div class="system-setting">
      <h1 class="g-mb-12">{{$t('systemSetting')}}</h1>
      <ul class="content">
        <li v-for="item in systemSettingArray" :key="item.key" class="item">
          <span class="left">{{item.key}}</span>
          <span class="right">{{item.value}}</span>
        </li>
      </ul>
    </div>
    <div class="container-setting">
      <h1 class="g-mb-12">{{$t('containerSetting')}}</h1>
      <a-collapse v-model:activeKey="activeKey">
        <a-collapse-panel v-for="container in containerSetting" :key="container.name" :header="container.name">
          <ul class="content">
            <li class="item">
              <h3 class="left">{{$t('containerName')}}</h3>
              <span class="right">{{container.name}}</span>
            </li>
            <li class="item">
              <h3 class="left">{{$t('type')}}</h3>
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
        </a-collapse-panel>
      </a-collapse>
    </div>
  </div>
  <u-loading v-if="loading" />
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { IColumns, IKeyAndValue, IContainerSetting } from '@/types/common.type'
import { useI18n } from 'vue-i18n'
import { getSystemSetting, getContainersSetting } from '@/services/setting.services'

const { t } = useI18n()
const loading = ref<boolean>(false)
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
const activeKey = ref<string[]>([])

async function getSystemSettingInfo() {
  try {
    loading.value = true
    const res = await getSystemSetting()
    if (!res) { return }
    systemSettingArray.length = 0
    Object.keys(res).forEach(key => {
      systemSettingArray.push({
        key: key,
        value: res[key]
      })
    })
  } finally {
    loading.value = false
  }
}
async function getContainersSettingInfo() {
  const res = await getContainersSetting()
  containerSetting.length = 0
  activeKey.value = [];
  (res || []).forEach((ele, index) => {
    ele.propertiesArray = []
    activeKey.value.push(ele.name)
    containerSetting.push(ele)
    Object.keys(ele.properties || {}).forEach(key => {
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
    :deep(.ant-collapse > .ant-collapse-item > .ant-collapse-header) {
      font-size: 20px;
      font-weight: 500;
      .ant-collapse-arrow {
        vertical-align: 1px;
      }
    }
  }
  .content {
    .item {
      padding: 6px 0;
      display: flex;
      word-break: break-all;
    }
    .left {
      width: 280px;
      flex-shrink: 0;
      margin-right: 16px;
    }
  }
}
</style>
