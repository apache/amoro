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
import { usePagination } from '@/hooks/usePagination'
import type { IColumns, ILableAndValue } from '@/types/common.type'
import { getTableProcesses } from '@/services/table.service'
import { dateFormat } from '@/utils/index'

const statusMap = {
  UNKNOWN: { title: 'UNKNOWN', color: '#c9cdd4' },
  PENDING: { title: 'PENDING', color: '#ffcc00' },
  SUBMITTED: { title: 'SUBMITTED', color: '#4169E1' },
  RUNNING: { title: 'RUNNING', color: '#1890ff' },
  CANCELING: { title: 'CANCELING', color: '#faad14' },
  SUCCESS: { title: 'SUCCESS', color: '#0ad787' },
  CANCELED: { title: 'CANCELED', color: '#c9cdd4' },
  CLOSED: { title: 'CLOSED', color: '#c9cdd4' },
  KILLED: { title: 'KILLED', color: '#c9cdd4' },
  FAILED: { title: 'FAILED', color: '#f5222d' },
}
const STATUS_CONFIG = shallowReactive(statusMap)

const { t } = useI18n()
const columns: IColumns[] = shallowReactive([
  { title: t('processId'), dataIndex: 'processId' },
  { title: t('status'), dataIndex: 'status', width: 140 },
  { title: t('processType'), dataIndex: 'processType' },
  { title: t('processStage'), dataIndex: 'processStage' },
  { title: t('executionEngine'), dataIndex: 'executionEngine' },
  { title: t('externalProcessId'), dataIndex: 'externalProcessIdentifier', ellipsis: true },
  { title: t('retry'), dataIndex: 'retryNumber', width: 80 },
  { title: t('startTime'), dataIndex: 'createTimeFormatted', width: 172 },
  { title: t('finishTime'), dataIndex: 'finishTimeFormatted', width: 172 },
])

const dataSource = reactive<any[]>([])
const loading = ref<boolean>(false)
const pagination = reactive(usePagination())
const route = useRoute()
const query = route.query
const sourceData = reactive({
  catalog: '',
  db: '',
  table: '',
  ...query,
})

const processTypeFilter = ref<string>('')
const statusType = ref<string>()
const statusTypeList = ref<ILableAndValue[]>([])

function initFilters() {
  const statusList = Object.entries(statusMap).map(([key, value]) => ({
    label: value.title,
    value: key,
  }))
  statusTypeList.value = statusList
}

async function refreshProcesses() {
  try {
    loading.value = true
    dataSource.length = 0
    const result = await getTableProcesses({
      ...sourceData,
      type: processTypeFilter.value || '',
      status: statusType.value || '',
      page: pagination.current,
      pageSize: pagination.pageSize,
    } as any)
    const { list, total = 0 } = result
    pagination.total = total

    dataSource.push(
      ...[...(list || [])].map((item) => {
        return {
          ...item,
          createTimeFormatted: item.createTime ? dateFormat(item.createTime) : '-',
          finishTimeFormatted: item.finishTime ? dateFormat(item.finishTime) : '-',
        }
      }),
    )
  }
  catch (error) {
  }
  finally {
    loading.value = false
  }
}

function change({ current = 1, pageSize = 25 }) {
  pagination.current = current
  if (pageSize !== pagination.pageSize) {
    pagination.current = 1
  }
  pagination.pageSize = pageSize
  refreshProcesses()
}

function handleFilterChange() {
  pagination.current = 1
  refreshProcesses()
}

onMounted(() => {
  initFilters()
  refreshProcesses()
})
</script>

<template>
  <div class="table-process">
    <a-space class="filter-form">
      <a-input
        v-model:value="processTypeFilter" allow-clear :placeholder="t('processType')"
        style="min-width: 150px;" @change="handleFilterChange"
      />
      <a-select
        v-model:value="statusType" allow-clear :placeholder="t('status')" :options="statusTypeList"
        style="min-width: 150px;" @change="handleFilterChange"
      />
    </a-space>
    <a-table
      row-key="processId" :columns="columns" :data-source="dataSource" :pagination="pagination"
      :loading="loading" @change="change"
    >
      <template #bodyCell="{ record, column }">
        <template v-if="column.dataIndex === 'status'">
          <div class="g-flex-ac">
            <span
              :style="{ 'background-color': (STATUS_CONFIG[record.status as keyof typeof STATUS_CONFIG] || {} as any).color }"
              class="status-icon"
            />
            <span>{{ record.status }}</span>
            <a-tooltip
              v-if="record.status === 'FAILED' && record.failMessage" placement="topRight" class="g-ml-4"
              overlay-class-name="table-failed-tip"
            >
              <template #title>
                <div class="tip-title">
                  {{ record.failMessage }}
                </div>
              </template>
              <question-circle-outlined />
            </a-tooltip>
          </div>
        </template>
      </template>
      <template #expandedRowRender="{ record }">
        <div class="expanded-content">
          <template v-if="record.processParameters && Object.keys(record.processParameters).length">
            <h4 class="expanded-section-title">
              {{ t('processParameters') }}
            </h4>
            <div class="kv-list">
              <div v-for="(value, key) in record.processParameters" :key="key" class="kv-row">
                <span class="kv-key">{{ key }}</span>
                <span class="kv-value">{{ value }}</span>
              </div>
            </div>
          </template>
          <template v-if="record.summary && Object.keys(record.summary).length">
            <h4 class="expanded-section-title">
              {{ t('summary') }}
            </h4>
            <div class="kv-list">
              <div v-for="(value, key) in record.summary" :key="key" class="kv-row">
                <span class="kv-key">{{ key }}</span>
                <span class="kv-value">{{ value }}</span>
              </div>
            </div>
          </template>
        </div>
      </template>
    </a-table>
  </div>
</template>

<style lang="less" scoped>
.table-process {
  padding: 18px 0;

  :deep(.ant-table-thead > tr > th:not(:last-child):not(.ant-table-selection-column):not(.ant-table-row-expand-icon-cell):not([colspan])::before) {
    height: 100% !important;
  }

  :deep(.ant-table-thead > tr:not(:last-child) > th[colspan]) {
    border-bottom: 1px solid #e8e8f0;
  }

  :deep(.ant-table-thead > tr > th) {
    padding: 4px 16px !important;
  }

  :deep(.ant-table-row-expand-icon) {
    border-radius: 0 !important;
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

.expanded-content {
  padding: 4px 0;
}

.expanded-section-title {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;

  & + .kv-list {
    margin-bottom: 16px;
  }
}

.kv-list {
  display: table;
  border-spacing: 0;
}

.kv-row {
  display: table-row;
  line-height: 28px;
}

.kv-key {
  display: table-cell;
  text-align: right;
  padding-right: 8px;
  white-space: nowrap;
  color: rgba(0, 0, 0, 0.65);
  font-weight: 500;

  &::after {
    content: ' :';
  }
}

.kv-value {
  display: table-cell;
  text-align: left;
  padding-left: 8px;
  word-break: break-all;
}
</style>

<style lang="less">
.table-process {

  .filter-form {
    width: 100%;
    margin-bottom: 16px;
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
