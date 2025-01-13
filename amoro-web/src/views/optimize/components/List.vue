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
import { computed, onMounted, reactive, ref, shallowReactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { Modal } from 'ant-design-vue'
import type { IIOptimizeGroupItem, ILableAndValue, IOptimizeResourceTableItem, IOptimizeTableItem } from '@/types/common.type'
import { getOptimizerAction, getOptimizerTableList, getResourceGroupsListAPI, releaseResource } from '@/services/optimize.service'
import { usePagination } from '@/hooks/usePagination'
import { usePlaceholder } from '@/hooks/usePlaceholder'
import { bytesToSize, formatMS2DisplayTime, formatMS2Time } from '@/utils'
import { getTableMaxWidth } from '@/utils/table'

const { t } = useI18n()
const router = useRouter()

const STATUS_CONFIG = shallowReactive({
  pending: { title: 'pending', color: '#ffcc00' },
  planning: { title: 'planning', color: '#076de3' },
  idle: { title: 'idle', color: '#c9cdd4' },
  minor: { title: 'minor', color: '#0ad787' },
  major: { title: 'major', color: '#0ad787' },
  full: { title: 'full', color: '#0ad787' },
  committing: { title: 'committing', color: '#0ad787' },
})

const loading = ref<boolean>(false)
const releaseLoading = ref<boolean>(false)
const optimizerGroupList = ref<ILableAndValue[]>([])

const columns = computed(() => [
  { dataIndex: 'tableName', title: t('table'), width: 300, scopedSlots: { customRender: 'tableName' } },
  { dataIndex: 'groupName', title: t('optimizerGroup'), width: 180, ellipsis: true },
  { dataIndex: 'optimizeStatus', title: t('optimizingStatus'), width: 150, ellipsis: true },
  { dataIndex: 'duration', title: t('duration'), width: 150, ellipsis: true },
  { dataIndex: 'fileCount', title: t('fileCount'), width: 150, ellipsis: true },
  { dataIndex: 'fileSizeDesc', title: t('fileSize'), width: 150, ellipsis: true },
  { dataIndex: 'quota', title: t('quota'), width: 150, ellipsis: true },
  { dataIndex: 'quotaOccupation', title: t('occupation'), width: 120, ellipsis: true },
])

const pagination = reactive(usePagination())
const dataSource = ref<IOptimizeTableItem[]>([])
const optimizerGroup = ref<ILableAndValue>()
const actions = ref<string[]>()
const dbSearchInput = ref<ILableAndValue>()
const tableSearchInput = ref<ILableAndValue>()
const placeholder = reactive(usePlaceholder())

const actionOptions = ref<string[]>([])
async function fetchOptimizerAction() {
  try {
    const res = await getOptimizerAction()
    actionOptions.value = (res || []).map((value: string) => ({ label: value, value }))
  }
  catch (error) {
  }
}

async function getOptimizerGroupList() {
  const res = await getResourceGroupsListAPI()
  const list = (res || []).map((item: IIOptimizeGroupItem) => ({ lable: item.resourceGroup.name, value: item.resourceGroup.name }))
  optimizerGroupList.value = list
}

function refresh(resetPage?: boolean) {
  if (resetPage) {
    pagination.current = 1
  }
  getTableList()
}

async function getTableList() {
  try {
    loading.value = true
    const params = {
      optimizerGroup: optimizerGroup.value ? encodeURIComponent(optimizerGroup.value) : 'all',
      dbSearchInput: dbSearchInput.value || '',
      tableSearchInput: tableSearchInput.value || '',
      page: pagination.current,
      pageSize: pagination.pageSize,
      actions: actions.value,
    }
    const result = await getOptimizerTableList(params as any)
    const { list, total } = result
    pagination.total = total
    dataSource.value = (list || []).map((p: IOptimizeTableItem) => {
      return {
        ...p,
        quotaOccupationDesc: p.quotaOccupation - 0.0005 > 0 ? `${(p.quotaOccupation * 100).toFixed(1)}%` : '0',
        durationDesc: p.duration ? formatMS2Time(p.duration) : '-',
        fileSizeDesc: bytesToSize(p.fileSize),
      }
    })
  }
  catch (error) {
  }
  finally {
    loading.value = false
  }
}

function releaseModal(record: any) {
  if (record.container === 'external') {
    return
  }
  Modal.confirm({
    title: t('releaseOptModalTitle'),
    onOk: () => {
      releaseJob(record)
    },
  })
}
async function releaseJob(record: IOptimizeResourceTableItem) {
  try {
    releaseLoading.value = true
    await releaseResource({
      optimizerGroup: record.groupName,
      jobId: record.jobId as unknown as string,
    })
    refresh(true)
  }
  finally {
    releaseLoading.value = false
  }
}
function changeTable({ current = pagination.current, pageSize = pagination.pageSize }) {
  pagination.current = current
  const resetPage = pageSize !== pagination.pageSize
  pagination.pageSize = pageSize

  refresh(resetPage)
}

function goTableDetail(record: IOptimizeTableItem) {
  const { catalog, database, tableName } = record.tableIdentifier
  router.push({
    path: '/tables',
    query: {
      catalog,
      db: database,
      table: tableName,
    },
  })
}

function reset() {
  optimizerGroup.value = undefined
  dbSearchInput.value = undefined
  tableSearchInput.value = undefined
  actions.value = undefined
  refresh(true)
}

onMounted(async () => {
  refresh()
  await getOptimizerGroupList()
  await fetchOptimizerAction()
})
</script>

<template>
  <div class="list-wrap">
    <a-space class="filter-form">
      <a-select
        v-model:value="optimizerGroup" allow-clear placeholder="Optimizer group" :options="optimizerGroupList"
        style="min-width: 150px;"
      />

      <a-input
        v-model:value="dbSearchInput"
        :placeholder="placeholder.filterDBPh"
      />

      <a-input
        v-model:value="tableSearchInput"
        :placeholder="placeholder.filterTablePh"
      />

      <a-select
        v-model:value="actions" allow-clear placeholder="Action" :options="actionOptions" mode="multiple"
        style="min-width: 150px;"
      />

      <a-button type="primary" @click="refresh">
        {{ t('search') }}
      </a-button>
      <a-button @click="reset">
        {{ t('reset') }}
      </a-button>
    </a-space>
    <a-table
      :columns="columns"
      :data-source="dataSource"
      :pagination="pagination"
      :loading="loading"
      :scroll="{ x: getTableMaxWidth(columns) }"
      class="ant-table-common"
      @change="changeTable"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'tableName'">
          <a-typography-text
            style="width: 300px"
            :ellipsis="{
              tooltip: record.tableName,
            }"
            class="primary-link"
            :content="record.tableName"
            @click="goTableDetail(record)"
          />
        </template>
        <template v-if="column.dataIndex === 'duration'">
          <span :title="record.durationDesc">
            {{ formatMS2DisplayTime(record.duration || 0) }}
          </span>
        </template>
        <template v-if="column.dataIndex === 'quotaOccupation'">
          <span :title="record.quotaOccupationDesc">
            {{ record.quotaOccupationDesc }}
          </span>
        </template>
        <template v-if="column.dataIndex === 'optimizeStatus'">
          <span
            :style="{ 'background-color': (STATUS_CONFIG[record.optimizeStatus as keyof typeof STATUS_CONFIG] as any)?.color }"
            class="status-icon"
          />
          <span>{{ record.optimizeStatus }}</span>
        </template>
        <template v-if="column.dataIndex === 'operation'">
          <span class="primary-link" :class="{ disabled: record.container === 'external' }" @click="releaseModal(record)">
            {{ t('release') }}
          </span>
        </template>
      </template>
    </a-table>
  </div>
  <u-loading v-if="releaseLoading" />
</template>

<style lang="less" scoped>
.list-wrap {

  .filter-form {
    width: 100%;
    margin-bottom: 16px;
  }

  .primary-link {
    color: @primary-color;

    &:hover {
      cursor: pointer;
    }

    &.disabled {
      color: #999;

      &:hover {
        cursor: not-allowed;
      }
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
}
</style>
