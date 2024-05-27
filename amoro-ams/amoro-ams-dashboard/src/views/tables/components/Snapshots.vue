
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
/-->

<template>
  <div class="table-snapshots">
    <template v-if="!hasBreadcrumb">
      <a-row>
        <a-col :span="12">
          <Chart :loading="loading" :options="recordChartOption" />
        </a-col>
        <a-col :span="12">
          <Chart :loading="loading" :options="fileChartOption" />
        </a-col>
      </a-row>
      <selector :catalog = "sourceData.catalog" :db="sourceData.db" :table="sourceData.table" :disabled="loading" @ref-change="onRefChange" />
      <a-table
        rowKey="snapshotId"
        :columns="columns"
        :data-source="dataSource"
        :pagination="pagination"
        :loading="loading"
        @change="change"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'snapshotId'">
            <a-button type="link" @click="toggleBreadcrumb(record)">
              {{ record.snapshotId }}
            </a-button>
          </template>
        </template>
        <template #expandedRowRender="{ record }">
          <a-row type="flex" :gutter="16" v-for="(value, key) in record.summary" :key="key">
            <a-col flex="220px" style="text-align: right;">{{ key }} :</a-col>
            <a-col flex="auto">{{ value }}</a-col>
          </a-row>
        </template>
      </a-table>
    </template>
    <template v-else>
      <a-breadcrumb separator=">">
        <a-breadcrumb-item @click="toggleBreadcrumb" class="text-active">All</a-breadcrumb-item>
        <a-breadcrumb-item>{{ `${$t('snapshotId')} ${snapshotId}`}}</a-breadcrumb-item>
      </a-breadcrumb>
      <a-table
        rowKey="file"
        :columns="breadcrumbColumns"
        :data-source="breadcrumbDataSource"
        :pagination="breadcrumbPagination"
        :loading="loading"
        @change="change"
        class="g-mt-8"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'path'">
            <a-tooltip>
              <template #title>{{record.path}}</template>
              <span>{{record.path}}</span>
            </a-tooltip>
          </template>
          <template v-if="column.dataIndex === 'file'">
            <a-tooltip>
              <template #title>{{record.file}}</template>
              <span>{{record.file}}</span>
            </a-tooltip>
          </template>
        </template>
      </a-table>
    </template>

  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref, shallowReactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { usePagination } from '@/hooks/usePagination'
import { BreadcrumbSnapshotItem, IColumns, ILineChartOriginalData, SnapshotItem } from '@/types/common.type'
import { getDetailBySnapshotId, getSnapshots } from '@/services/table.service'
import { useRoute } from 'vue-router'
import { dateFormat } from '@/utils'
import Chart from '@/components/echarts/Chart.vue'
import { ECOption } from '@/components/echarts'
import { generateLineChartOption } from '@/utils/chart'
import Selector from './Selector.vue'

const hasBreadcrumb = ref<boolean>(false)
const { t } = useI18n()
const columns: IColumns[] = shallowReactive([
  { title: t('snapshotId'), dataIndex: 'snapshotId', ellipsis: true },
  { title: t('operation'), dataIndex: 'operation' },
  { title: t('records'), dataIndex: 'records' },
  { title: t('fileCount'), dataIndex: 'fileCount' },
  { title: t('commitTime'), dataIndex: 'commitTime' }
])
const breadcrumbColumns = shallowReactive([
  { title: t('operation'), dataIndex: 'operation', width: 120, ellipsis: true },
  { title: t('file'), dataIndex: 'file', ellipsis: true },
  // { title: t('fsn'), dataIndex: 'fsn' },
  { title: t('partition'), dataIndex: 'partition', width: 120 },
  { title: t('fileType'), dataIndex: 'fileType', width: 120, ellipsis: true },
  { title: t('size'), dataIndex: 'size', width: 120 },
  { title: t('commitTime'), dataIndex: 'commitTime', width: 200, ellipsis: true },
  { title: t('path'), dataIndex: 'path', ellipsis: true }
])
const dataSource = reactive<SnapshotItem[]>([])
const breadcrumbDataSource = reactive<BreadcrumbSnapshotItem[]>([])
const snapshotId = ref<string>('')
const loading = ref<boolean>(false)
const pagination = reactive(usePagination())
const breadcrumbPagination = reactive(usePagination())
const route = useRoute()
const query = route.query
const sourceData = reactive({
  catalog: '',
  db: '',
  table: '',
  ...query
})
const recordChartOption = ref<ECOption>({})
const fileChartOption = ref<ECOption>({})
const tblRef = ref<string>('')
const operation = ref<string>('')

const onRefChange = (params: { ref: string, operation: string }) => {
  tblRef.value = params.ref
  operation.value = params.operation
  getTableInfo()
}

async function getTableInfo() {
  try {
    loading.value = true
    dataSource.length = 0
    const result = await getSnapshots({
      ...sourceData,
      ref: tblRef.value,
      operation: operation.value,
      page: pagination.current,
      pageSize: pagination.pageSize
    })
    const { list = [], total } = result
    const rcData: ILineChartOriginalData = {}
    const fcData: ILineChartOriginalData = {}
    list.forEach((p: SnapshotItem) => {
      // Assume that the time will not conflict and use the time as the unique key without formatting it.
      const { recordsSummaryForChart, filesSummaryForChart, commitTime } = p
      rcData[commitTime] = recordsSummaryForChart || {}
      fcData[commitTime] = filesSummaryForChart || {}
      if (p.producer === 'OPTIMIZE') {
        p.operation = p.operation + '(optimizing)'
      }
      p.commitTime = p.commitTime ? dateFormat(p.commitTime) : '-'
      dataSource.push(p)
    })
    recordChartOption.value = generateLineChartOption(t('recordChartTitle'), rcData)
    fileChartOption.value = generateLineChartOption(t('fileChartTitle'), fcData)
    pagination.total = total
  } catch (error) {
  } finally {
    loading.value = false
  }
}

function change({ current = 1, pageSize = 25 }) {
  if (hasBreadcrumb.value) {
    breadcrumbPagination.current = current
    if (pageSize !== breadcrumbPagination.pageSize) {
      breadcrumbPagination.current = 1
    }
    breadcrumbPagination.pageSize = pageSize
  } else {
    pagination.current = current
    if (pageSize !== pagination.pageSize) {
      pagination.current = 1
    }
    pagination.pageSize = pageSize
  }
  refresh()
}

function refresh() {
  if (hasBreadcrumb.value) {
    getBreadcrumbTable()
  } else {
    getTableInfo()
  }
}

async function getBreadcrumbTable() {
  try {
    breadcrumbDataSource.length = 0
    loading.value = true
    const params = {
      ...sourceData,
      snapshotId: snapshotId.value,
      page: breadcrumbPagination.current,
      pageSize: breadcrumbPagination.pageSize
    }
    const result = await getDetailBySnapshotId(params)
    const { list, total } = result
    breadcrumbPagination.total = total
    list.forEach((p: BreadcrumbSnapshotItem) => {
      p.commitTime = p.commitTime ? dateFormat(p.commitTime) : ''
      breadcrumbDataSource.push(p)
    })
  } catch (error) {
  } finally {
    loading.value = false
  }
}

function toggleBreadcrumb(record: SnapshotItem) {
  snapshotId.value = record.snapshotId
  hasBreadcrumb.value = !hasBreadcrumb.value
  if (hasBreadcrumb.value) {
    breadcrumbPagination.current = 1
    getBreadcrumbTable()
  }
}

onMounted(() => {
  hasBreadcrumb.value = false
  // getTableInfo()
})

</script>

<style lang="less" scoped>
.table-snapshots {
  padding: 18px 24px;
  .text-active {
    color: #1890ff;
    cursor: pointer;
  }
  :deep(.ant-btn-link) {
    padding: 0;
  }
  .ant-table-wrapper {
    margin-top: 24px;
  }
}
</style>
