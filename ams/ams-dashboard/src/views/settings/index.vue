<template>
  <div class="setting-wrap">
    <div class="system-setting">
      <h1 class="g-mb-12">{{$t('systemSetting')}}</h1>
      <a-table
        rowKey="key"
        :columns="basicColumns"
        v-if="systemSettingArray.length"
        :data-source="systemSettingArray"
        :pagination="false"
      />
    </div>
    <div class="container-setting">
      <h1 class="g-mb-12">{{$t('containerSetting')}}</h1>
      <a-collapse v-model:activeKey="activeKey">
        <a-collapse-panel v-for="container in containerSetting" :key="container.name" :header="container.name">
          <ul class="content">
            <li class="item">
              <h3 class="left">{{$t('name')}}</h3>
              <span class="right">{{container.name}}</span>
            </li>
            <li class="item">
              <h3 class="left">{{$t('type')}}</h3>
              <span class="right">{{container.type}}</span>
            </li>
          </ul>
          <h3 class="g-mb-12 g-mt-12">{{$t('properties')}}</h3>
          <a-table
            rowKey="key"
            v-if="container.propertiesArray.length"
            :columns="basicColumns"
            :data-source="container.propertiesArray"
            :pagination="false"
          />
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
const containerSetting = reactive<IContainerSetting[]>([])
const optimzeGroupColumns: IColumns[] = reactive([
  { title: t('name'), dataIndex: 'name', width: 340, ellipsis: true },
  { title: t('propertiesMemory', { type: 'taskmanager' }), dataIndex: 'tmMemory', width: '50%', ellipsis: true },
  { title: t('propertiesMemory', { type: 'jobmanager' }), dataIndex: 'jmMemory', width: '50%', ellipsis: true }
])

const basicColumns: IColumns[] = reactive([
  { title: t('key'), dataIndex: 'key', width: 340, ellipsis: true },
  { title: t('value'), dataIndex: 'value' }
])
const activeKey = ref<string[]>([])

async function getSystemSettingInfo() {
  const res = await getSystemSetting()
  if (!res) { return }
  Object.keys(res).forEach(key => {
    systemSettingArray.push({
      key: key,
      value: res[key]
    })
  })
}
async function getContainersSettingInfo() {
  const res = await getContainersSetting()
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
onMounted(async() => {
  try {
    loading.value = true
    await Promise.all([getSystemSettingInfo(), getContainersSettingInfo()])
  } finally {
    loading.value = false
  }
})

</script>

<style lang="less" scoped>
.setting-wrap {
  height: 100%;
  overflow: auto;
  padding: 16px 24px;
  h1,h2,h3 {
    font-weight: 500;
  }
  h1 {
    font-size: 16px;
  }
  h3 {
    font-size: 14px;
  }
  .container-setting {
    padding-top: 16px;
    :deep(.ant-collapse) {
      > .ant-collapse-item > .ant-collapse-header {
        font-size: 16px;
        font-weight: 500;
        .ant-collapse-arrow {
          vertical-align: 1px;
        }
      }
      .ant-collapse-content > .ant-collapse-content-box {
        padding: 6px 16px 32px;
      }
      .ant-collapse-item:last-child .ant-collapse-content > .ant-collapse-content-box {
        padding-bottom: 16px;
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
      width: 320px;
      flex-shrink: 0;
      margin-right: 16px;
    }
  }
}
</style>
