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
import { computed, onMounted, reactive, shallowReactive, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import type { ColumnProps } from 'ant-design-vue/es/table'
import type { DetailColumnItem, IBaseDetailInfo, IColumns, IMap, PartitionColumnItem } from '@/types/common.type'
import { getTableDetail } from '@/services/table.service'
import { dateFormat } from '@/utils'

const emit = defineEmits<{
  (e: 'setBaseDetailInfo', data: IBaseDetailInfo): void
}>()
const { t } = useI18n()
const route = useRoute()

const params = computed(() => {
  return {
    ...route.query,
  }
})

watch(
  () => route.query,
  (val) => {
    val?.catalog && route.path === '/tables' && getTableDetails()
  },
)
const commonMetricMap = {
  fileCount: 'File Count',
  totalSize: 'Total Size',
  averageFileSize: 'Average File Size',
  lastCommitTime: 'Last Commit Time',
}

const baseMetricsMap: IMap<string | number> = {
  ...commonMetricMap,
  baseWatermark: 'Base Watermark',
}
const changeMetricsMap: IMap<string | number> = {
  ...commonMetricMap,
  tableWatermark: 'Table Watermark',
}

const state = reactive({
  detailLoading: false,
  baseDetailInfo: {
    optimizingStatus: '',
    tableType: '',
    tableName: '',
    createTime: '',
    tableFormat: '',
    hasPartition: false, // Whether there is a partition, if there is no partition, the file list will be displayed
    comment: ''
  } as IBaseDetailInfo,
  pkList: [] as DetailColumnItem[],
  partitionColumnList: [] as PartitionColumnItem[],
  properties: [] as IMap<string>[],
  changeMetrics: [] as IMap<string | number>[],
  baseMetrics: [] as IMap<string | number>[],
  schema: [] as DetailColumnItem[],
})

async function getTableDetails() {
  try {
    const { catalog, db, table } = params.value
    if (!catalog || !db || !table) {
      return
    }
    state.detailLoading = true
    const result = await getTableDetail({
      ...params.value,
    })
    const { pkList = [], tableType, partitionColumnList = [], properties, changeMetrics, schema, createTime, tableIdentifier, baseMetrics, tableSummary, comment } = result
    state.baseDetailInfo = {
      ...tableSummary,
      tableType,
      tableName: `${tableIdentifier?.catalog || ''}.${tableIdentifier?.database || ''}.${tableIdentifier?.tableName || ''}`,
      createTime: createTime ? dateFormat(createTime) : '',
      hasPartition: !!(partitionColumnList?.length),
      comment: comment || ''
    }

    state.pkList = pkList || []
    state.partitionColumnList = partitionColumnList || []
    state.schema = schema || []

    state.changeMetrics = Object.keys(changeMetricsMap || {}).map((key) => {
      return {
        metric: changeMetricsMap[key],
        value: key === 'lastCommitTime' || key === 'tableWatermark' ? ((changeMetrics || {})[key] ? dateFormat((changeMetrics || {})[key]) : '') : (changeMetrics || {})[key],
      }
    }).filter(ele => ele.value)

    state.baseMetrics = Object.keys(baseMetricsMap || {}).map((key) => {
      return {
        metric: baseMetricsMap[key],
        value: key === 'lastCommitTime' || key === 'baseWatermark' ? ((baseMetrics || {})[key] ? dateFormat((baseMetrics || {})[key]) : '') : (baseMetrics || {})[key],
      }
    })
    state.properties = Object.keys(properties || {}).map((key) => {
      return {
        key,
        value: properties[key],
      }
    })
    setBaseDetailInfo()
  }
  catch (error) {
  }
  finally {
    state.detailLoading = false
  }

  function setBaseDetailInfo() {
    emit('setBaseDetailInfo', state.baseDetailInfo)
  }
}

onMounted(() => {
  getTableDetails()
})

defineExpose({ getTableDetails })

const primaryColumns: ColumnProps[] = shallowReactive([
  { title: t('field'), dataIndex: 'field', width: '30%' },
  { title: t('type'), dataIndex: 'type', width: '20%' },
  { title: t('required'), dataIndex: 'required', width: '20%', customRender: text => String(text?.value) },
  { title: t('description'), dataIndex: 'comment', ellipsis: true },
])
const partitionColumns: IColumns[] = shallowReactive([
  { title: t('field'), dataIndex: 'field', width: '30%' },
  { title: t('sourceField'), dataIndex: 'sourceField', width: '30%' },
  { title: t('transform'), dataIndex: 'transform', ellipsis: true },
])
const metricsColumns: IColumns[] = shallowReactive([
  { title: t('metric'), dataIndex: 'metric', width: '50%', ellipsis: true },
  { title: t('value'), dataIndex: 'value', ellipsis: true },
])
const propertiesColumns: IColumns[] = shallowReactive([
  { title: t('key'), dataIndex: 'key', width: '50%', ellipsis: true },
  { title: t('value'), dataIndex: 'value', ellipsis: true },
])
</script>

<template>
  <div class="table-detail g-flex">
    <div class="left-content">
      <div v-if="state.pkList && state.pkList.length" class="table-attrs">
        <p class="attr-title">
          {{ $t('primaryKey') }}
        </p>
        <a-table
          row-key="field"
          :columns="primaryColumns"
          :data-source="state.pkList"
          :pagination="false"
        />
      </div>
      <div v-if="state.partitionColumnList && state.partitionColumnList.length" class="table-attrs">
        <p class="attr-title">
          {{ $t('partitionKey') }}
        </p>
        <a-table
          row-key="field"
          :columns="partitionColumns"
          :data-source="state.partitionColumnList"
          :pagination="false"
        />
      </div>
      <div class="table-attrs">
        <p class="attr-title">
          {{ $t('schema') }}
        </p>
        <a-table
          row-key="field"
          :columns="primaryColumns"
          :data-source="state.schema"
          :pagination="false"
        />
      </div>
    </div>
    <div class="right-content">
      <div v-if="state.changeMetrics && state.changeMetrics.length" class="table-attrs">
        <p class="attr-title">
          {{ $t('changeTableMetrics') }}
        </p>
        <a-table
          :columns="metricsColumns"
          :data-source="state.changeMetrics"
          :pagination="false"
        />
      </div>
      <div class="table-attrs">
        <p class="attr-title">
          {{ $t('baseTableMetrics') }}
        </p>
        <a-table
          :columns="metricsColumns"
          :data-source="state.baseMetrics"
          :pagination="false"
        />
      </div>
      <div class="table-attrs">
        <p class="attr-title">
          {{ $t('properties') }}
        </p>
        <a-table
          :columns="propertiesColumns"
          :data-source="state.properties"
          :pagination="false"
        />
      </div>
    </div>
    <u-loading v-if="state.detailLoading" />
  </div>
</template>

<style lang="less" scoped>
.table-detail {
  .left-content,
  .right-content {
    padding: 0 24px 12px;
    flex-shrink: 0;
    flex-direction: column;
  }
  .left-content {
    flex: 2;
  }
  .right-content {
    flex: 1;
    padding-left: 0;
  }
  .table-attrs {
    margin-top: 16px;
  }
  .attr-title {
    font-size: 16px;
    line-height: 24px;
    font-weight: bold;
    color: #102048;
    padding-bottom: 12px;
  }
}
</style>
