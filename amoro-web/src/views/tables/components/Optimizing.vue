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

<script lang="ts" setup>
import { onMounted, reactive, ref, shallowReactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { Modal } from 'ant-design-vue'
import { usePagination } from '@/hooks/usePagination'
import type { BreadcrumbOptimizingItem, IColumns, ILableAndValue } from '@/types/common.type'
import { cancelOptimizingProcess, getOptimizingProcesses, getTasksByOptimizingProcessId, getTableOptimizingTypes } from '@/services/table.service'
import { bytesToSize, dateFormat, formatMS2Time } from '@/utils/index'

const hasBreadcrumb = ref<boolean>(false)

const statusMap = {
  PENDING: { title: 'PENDING', color: '#ffcc00'},
  ACTIVE: { title: 'ACTIVE', color: '#1890ff' },
  CLOSED: { title: 'CLOSED', color: '#c9cdd4' },
  SUCCESS: { title: 'SUCCESS', color: '#0ad787' },
  FAILED: { title: 'FAILED', color: '#f5222d' },
}
const STATUS_CONFIG = shallowReactive(statusMap)

const TASK_STATUS_CONFIG = shallowReactive({
  PLANNED: { title: 'PLANNED', color: '#ffcc00' },
  SCHEDULED: { title: 'SCHEDULED', color: '#4169E1' },
  ACKED: { title: 'ACKED', color: '#1890ff' },
  FAILED: { title: 'FAILED', color: '#f5222d' },
  SUCCESS: { title: 'SUCCESS', color: '#0ad787' },
  CANCELED: { title: 'CANCELED', color: '#c9cdd4' },
})

const { t } = useI18n()
const columns: IColumns[] = shallowReactive([
  { title: t('processId'), dataIndex: 'processId' },
  { title: t('startTime'), dataIndex: 'startTime', width: 172 },
  { title: t('type'), dataIndex: 'optimizingType' },
  { title: t('status'), dataIndex: 'status' },
  { title: t('duration'), dataIndex: 'duration', width: 120 },
  { title: t('tasks'), dataIndex: 'tasks' },
  { title: t('finishTime'), dataIndex: 'finishTime', width: 172 },
  { title: t('input'), dataIndex: 'inputFiles' },
  { title: t('output'), dataIndex: 'outputFiles' },
])

const breadcrumbColumns = shallowReactive([
  { title: t('taskId'), dataIndex: 'taskId', width: 82 },
  { title: t('partition'), dataIndex: 'partitionData', ellipsis: true },
  { title: t('startTime'), dataIndex: 'startTime', width: 172 },
  { title: t('status'), dataIndex: 'status', width: 124 },
  { title: t('costTime'), dataIndex: 'formatCostTime', width: 120 },
  { title: t('finishTime'), dataIndex: 'endTime', width: 172 },
  { title: t('input'), dataIndex: 'inputFilesDesc' },
  { title: t('output'), dataIndex: 'outputFilesDesc' },
])

const dataSource = reactive<any[]>([])
const processId = ref<number>(0)
const breadcrumbDataSource = reactive<BreadcrumbOptimizingItem[]>([])

const loading = ref<boolean>(false)
const cancelDisabled = ref(true)
const pagination = reactive(usePagination())
const breadcrumbPagination = reactive(usePagination())
const route = useRoute()
const query = route.query
const sourceData = reactive({
  catalog: '',
  db: '',
  table: '',
  ...query,
})
const actionType = ref<ILableAndValue>()
const actionTypeList = ref<ILableAndValue[]>([])
const statusType = ref<ILableAndValue>()
const statusTypeList = ref<ILableAndValue[]>([])

async function getQueryDataDictList() {
  const tableProcessTypes = await getTableOptimizingTypes({...sourceData})
  const typesList = Object.entries(tableProcessTypes).map(([typeName, displayName]) => ({ label: displayName as string, value: typeName}))
  const status = Object.entries(statusMap).map(([key, value]) => ({ label: value.title , value: key }));

  actionTypeList.value = typesList
  statusTypeList.value = status
}

async function refreshOptimizingProcesses() {
  try {
    loading.value = true
    dataSource.length = 0
    const result = await getOptimizingProcesses({
      ...sourceData,
      type: actionType.value || '',
      status: statusType.value || '',
      page: pagination.current,
      pageSize: pagination.pageSize,
    } as any)
    const { list, total = 0 } = result
    pagination.total = total
    dataSource.push(...[...list || []].map((item) => {
      const { inputFiles = {}, outputFiles = {} } = item
      return {
        ...item,
        // recordId,
        // startTime: item.commitTime ? d(new Date(item.commitTime), 'long') : '',
        // commitTime: item.commitTime ? dateFormat(item.commitTime) : '',
        startTime: item.startTime ? dateFormat(item.startTime) : '-',
        finishTime: item.finishTime ? dateFormat(item.finishTime) : '-',
        optimizingType: item.optimizingType ? item.optimizingType : '-',
        duration: item.duration ? formatMS2Time(item.duration) : '-',
        inputFiles: `${bytesToSize(inputFiles.totalSize)} / ${inputFiles.fileCnt}`,
        outputFiles: `${bytesToSize(outputFiles.totalSize)} / ${outputFiles.fileCnt}`,
        tasks: `${item.successTasks || '0'} / ${item.totalTasks || '0'}${item.runningTasks ? ` (${item.runningTasks} running)` : ''}`,
      }
    }))
  }
  catch (error) {
  }
  finally {
    loading.value = false
  }
}

async function cancel() {
  Modal.confirm({
    title: t('cancelOptimizingProcessOptModalTitle'),
    onOk: async () => {
      try {
        loading.value = true
        await cancelOptimizingProcess({
          ...sourceData,
          processId: processId.value?.toString(),
        })
        cancelDisabled.value = true
        refresh()
      }
      catch (error) {
      }
      finally {
        loading.value = false
      }
    },
  })
}

function change({ current = 1, pageSize = 25 }) {
  if (hasBreadcrumb.value) {
    breadcrumbPagination.current = current
    if (pageSize !== breadcrumbPagination.pageSize) {
      breadcrumbPagination.current = 1
    }
    breadcrumbPagination.pageSize = pageSize
  }
  else {
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
    refreshOptimizingTasks()
  }
  else {
    refreshOptimizingProcesses()
  }
}

async function refreshOptimizingTasks() {
  try {
    breadcrumbDataSource.length = 0
    loading.value = true
    const params = {
      ...sourceData,
      processId: processId.value,
      page: breadcrumbPagination.current,
      pageSize: breadcrumbPagination.pageSize,
    }
    const result = await getTasksByOptimizingProcessId(params)
    const { list, total } = result
    breadcrumbPagination.total = total
    list.forEach((p: BreadcrumbOptimizingItem) => {
      p.startTime = p.startTime ? dateFormat(p.startTime) : '-'
      p.endTime = p.endTime ? dateFormat(p.endTime) : '-'
      p.formatCostTime = p.costTime ? formatMS2Time(p.costTime) : '-'
      p.thread = p.optimizerToken ? `(${p.threadId})${p.optimizerToken}` : '-'
      p.partitionData = p.partitionData ? p.partitionData : '-'
      p.inputFilesDesc = `${bytesToSize(p.inputFiles.totalSize)} / ${p.inputFiles.fileCnt}`
      p.outputFilesDesc = `${bytesToSize(p.outputFiles.totalSize)} / ${p.outputFiles.fileCnt}`
      breadcrumbDataSource.push(p)
    })
  }
  catch (error) {
  }
  finally {
    loading.value = false
  }
}

function toggleBreadcrumb(rowProcessId: number, status: string) {
  processId.value = rowProcessId
  cancelDisabled.value = status !== 'RUNNING'
  hasBreadcrumb.value = !hasBreadcrumb.value
  if (hasBreadcrumb.value) {
    breadcrumbPagination.current = 1
  }
  refresh()
}

onMounted(() => {
  hasBreadcrumb.value = false
  refresh()
  getQueryDataDictList()
})
</script>

<template>
  <div class="table-optimizing">
    <template v-if="!hasBreadcrumb">
      <a-space class="filter-form">
        <a-select
          v-model:value="actionType" allow-clear placeholder="Type" :options="actionTypeList"
          style="min-width: 150px;" @change="refresh"
        />
        <a-select
          v-model:value="statusType" allow-clear placeholder="Status" :options="statusTypeList"
          style="min-width: 150px;" @change="refresh"
        />
      </a-space>
      <a-table
        row-key="processId" :columns="columns" :data-source="dataSource" :pagination="pagination"
        :loading="loading" @change="change"
      >
        <template #headerCell="{ column }">
          <template v-if="column.dataIndex === 'tasks'">
            <div class="">
              {{ column.title }}
            </div>
            <div class="">
              success / total
            </div>
          </template>
          <template v-if="column.dataIndex === 'inputFiles'">
            <div class="">
              {{ column.title }}
            </div>
            <div class="">
              size / count
            </div>
          </template>
          <template v-if="column.dataIndex === 'outputFiles'">
            <div class="">
              {{ column.title }}
            </div>
            <div class="">
              size / count
            </div>
          </template>
        </template>
        <template #bodyCell="{ record, column }">
          <template v-if="column.dataIndex === 'processId'">
            <a-button type="link" @click="toggleBreadcrumb(record.processId, record.status)">
              {{ record.processId }}
            </a-button>
          </template>
          <template v-if="column.dataIndex === 'status'">
            <div class="g-flex-ac">
              <span
                :style="{ 'background-color': (STATUS_CONFIG[record.status as keyof typeof STATUS_CONFIG] || {} as any).color }"
                class="status-icon"
              />
              <span>{{ record.status }}</span>
              <a-tooltip
                v-if="record.status === 'FAILED'" placement="topRight" class="g-ml-4"
                overlay-class-name="table-failed-tip"
              >
                <template #title>
                  <div class="tip-title">
                    {{ record.failReason }}
                  </div>
                </template>
                <question-circle-outlined />
              </a-tooltip>
            </div>
          </template>
        </template>
        <template #expandedRowRender="{ record }">
          <a-row v-for="(value, key) in record.summary" :key="key" type="flex" :gutter="16">
            <a-col flex="420px" style="text-align: right;">
              {{ key }} :
            </a-col>
            <a-col flex="auto">
              {{ value }}
            </a-col>
          </a-row>
        </template>
      </a-table>
    </template>
    <template v-else>
      <a-row>
        <a-col :span="18">
          <a-breadcrumb separator=">">
            <a-breadcrumb-item class="text-active" @click="toggleBreadcrumb">
              All
            </a-breadcrumb-item>
            <a-breadcrumb-item>{{ `${$t('processId')} ${processId}` }}</a-breadcrumb-item>
          </a-breadcrumb>
        </a-col>
        <a-col :span="6">
          <a-button
            v-model:disabled="cancelDisabled" type="primary" class="g-mb-16" style="float: right"
            @click="cancel"
          >
            {{ t("cancelProcess") }}
          </a-button>
        </a-col>
      </a-row>
      <a-table
        row-key="taskId" :columns="breadcrumbColumns" :data-source="breadcrumbDataSource"
        :pagination="breadcrumbPagination" :loading="loading" class="g-mt-8" @change="change"
      >
        <template #headerCell="{ column }">
          <template v-if="column.dataIndex === 'inputFilesDesc'">
            <div class="">
              {{ column.title }}
            </div>
            <div class="">
              size / count
            </div>
          </template>
          <template v-if="column.dataIndex === 'outputFilesDesc'">
            <div class="">
              {{ column.title }}
            </div>
            <div class="">
              size / count
            </div>
          </template>
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'partitionData'">
            <a-tooltip>
              <template #title>
                {{ record.partitionData }}
              </template>
              <span>{{ record.partitionData }}</span>
            </a-tooltip>
          </template>
          <template v-if="column.dataIndex === 'status'">
            <div class="g-flex-ac">
              <span
                :style="{ 'background-color': (TASK_STATUS_CONFIG[record.status as keyof typeof TASK_STATUS_CONFIG] || {} as any).color }"
                class="status-icon"
              />
              <span>{{ record.status }}</span>
              <a-tooltip
                v-if="record.status === 'FAILED'" placement="topRight" class="g-ml-4"
                overlay-class-name="table-failed-tip"
              >
                <template #title>
                  <div class="tip-title">
                    {{ record.failReason }}
                  </div>
                </template>
                <question-circle-outlined />
              </a-tooltip>
            </div>
          </template>
        </template>
        <template #expandedRowRender="{ record }">
          <a-row v-for="(value, key) in record.summary" :key="key" type="flex" :gutter="16">
            <a-col flex="420px" style="text-align: right;">
              {{ key }} :
            </a-col>
            <a-col flex="auto">
              {{ value }}
            </a-col>
          </a-row>
        </template>
      </a-table>
    </template>
  </div>
</template>

<style lang="less" scoped>
.table-optimizing {
  padding: 18px 24px;

  :deep(.ant-table-thead > tr > th:not(:last-child):not(.ant-table-selection-column):not(.ant-table-row-expand-icon-cell):not([colspan])::before) {
    height: 100% !important;
  }

  :deep(.ant-table-thead > tr:not(:last-child) > th[colspan]) {
    border-bottom: 1px solid #e8e8f0;
  }

  :deep(.ant-table-thead > tr > th) {
    padding: 4px 16px !important;
  }

  :deep(.ant-table-thead > tr > th) {
    padding: 4px 16px !important;
  }
}

.status-icon {
  width: 8px;
  height: 8px;
  border-radius: 8px;
  background-color: #c9cdd4;
  display: inline-block;
  margin-right: 8px;
}
</style>

<style lang="less">
.table-optimizing {

  .filter-form {
    width: 100%;
    margin-bottom: 16px;
  }

  .text-active {
    color: #1890ff;
    cursor: pointer;
  }

  .ant-btn-link {
    padding: 0;
  }
}

.table-failed-tip {
  .ant-tooltip-content {
    width: 800px;
  }

  .tip-title {
    display: block;
    max-height: 700px;
    overflow: auto;
    white-space: pre-wrap;
  }
}
</style>
