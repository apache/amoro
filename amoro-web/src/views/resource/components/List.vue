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
import { Modal, message } from 'ant-design-vue'
import type { IIOptimizeGroupItem, IOptimizeResourceTableItem } from '@/types/common.type'
import { getOptimizerResourceList, getResourceGroupsListAPI, groupDeleteAPI, groupDeleteCheckAPI, releaseResource } from '@/services/optimize.service'
import { usePagination } from '@/hooks/usePagination'
import { dateFormat, mbToSize } from '@/utils'

const props = defineProps<{ curGroupName?: string, type: string }>()

const emit = defineEmits<{
  (e: 'editGroup', record: IIOptimizeGroupItem): void
  (e: 'refresh'): void
  (e: 'refreshCurGroupInfo'): void
}>()

const { t } = useI18n()

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
const tableColumns = shallowReactive([
  { dataIndex: 'name', title: t('name'), ellipsis: true },
  { dataIndex: 'container', title: t('container'), width: '23%', ellipsis: true },
  { dataIndex: 'resourceOccupation', title: t('resourceOccupation'), width: '23%', ellipsis: true },
  { dataIndex: 'operationGroup', title: t('operation'), key: 'operationGroup', ellipsis: true, width: 230, scopedSlots: { customRender: 'operationGroup' } },
])
const optimizerColumns = shallowReactive([
  { dataIndex: 'jobId', title: t('optimizerId'), width: '15%', ellipsis: true },
  { dataIndex: 'token', title: t('token'), width: '10%', ellipsis: true },
  { dataIndex: 'groupName', title: t('optimizerGroup'), ellipsis: true },
  { dataIndex: 'container', title: t('container'), ellipsis: true },
  { dataIndex: 'jobStatus', title: t('status'), ellipsis: true },
  { dataIndex: 'resourceAllocation', title: t('resourceAllocation'), width: '10%', ellipsis: true },
  { dataIndex: 'startTime', title: t('startTime'), width: 172, ellipsis: true },
  { dataIndex: 'touchTime', title: t('touchTime'), width: 172, ellipsis: true },
  { dataIndex: 'operation', title: t('operation'), key: 'operation', ellipsis: true, width: 160, scopedSlots: { customRender: 'operationGroup' } },
])
const pagination = reactive(usePagination())
const optimizersList = reactive<IOptimizeResourceTableItem[]>([])
const groupList = reactive<IIOptimizeGroupItem[]>([])

const columns = computed(() => {
  return props.type === 'optimizers' ? optimizerColumns : tableColumns
})

const dataSource = computed(() => {
  return props.type === 'optimizers' ? optimizersList : groupList
})

function refresh(resetPage?: boolean) {
  if (resetPage) {
    pagination.current = 1
  }
  if (props.type === 'optimizers') {
    getOptimizersList()
  }
  else {
    getResourceGroupsList()
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
      jobId: (record.jobId) as unknown as string,
    })
    refresh(true)
    emit('refreshCurGroupInfo')
  }
  finally {
    releaseLoading.value = false
  }
}

async function getOptimizersList() {
  try {
    optimizersList.length = 0
    loading.value = true
    const params = {
      optimizerGroup: 'all',
      page: pagination.current,
      pageSize: pagination.pageSize,
    }
    const result = await getOptimizerResourceList(params)
    const { list, total } = result
    pagination.total = total;
    (list || []).forEach((p: IOptimizeResourceTableItem, index: number) => {
      p.resourceAllocation = `${p.coreNumber} ${t('core')} ${mbToSize(p.memory)}`
      p.index = (pagination.current - 1) * pagination.pageSize + index + 1
      p.startTime = p.startTime ? dateFormat(p.startTime) : '-'
      p.touchTime = p.touchTime ? dateFormat(p.touchTime) : '-'
      optimizersList.push(p)
    })
  }
  catch (error) {
  }
  finally {
    loading.value = false
  }
}

async function getResourceGroupsList() {
  try {
    groupList.length = 0
    loading.value = true
    const result = await getResourceGroupsListAPI()
    pagination.total = result.length;
    (result || []).forEach((p: IIOptimizeGroupItem) => {
      p.name = p.resourceGroup.name
      p.container = p.resourceGroup.container
      p.resourceOccupation = `${p.occupationCore} ${t('core')} ${mbToSize(p.occupationMemory)}`
      groupList.push(p)
    })
  }
  catch (error) {
  }
  finally {
    loading.value = false
  }
}

function editGroup(record: IIOptimizeGroupItem) {
  emit('editGroup', record)
}
async function removeGroup(record: IIOptimizeGroupItem) {
  const res = await groupDeleteCheckAPI({ name: record.name })
  if (res) {
    Modal.confirm({
      title: t('deleteGroupModalTitle'),
      onOk: async () => {
        await groupDeleteAPI({ name: record.name })
        message.success(`${t('remove')} ${t('success')}`)
        refresh()
      },
    })
    return
  }
  Modal.warning({
    title: t('cannotDeleteGroupModalTitle'),
    content: t('cannotDeleteGroupModalContent'),
  })
}

const groupRecord = ref<IIOptimizeGroupItem>({
  resourceGroup: {
    name: '',
    container: '',
    properties: {},
  },
  occupationCore: 0,
  occupationMemory: 0,
  name: '',
  container: '',
  resourceOccupation: '',
})
const scaleOutVisible = ref<boolean>(false)
function scaleOutGroup(record: IIOptimizeGroupItem) {
  if (record.container === 'external') {
    return
  }
  groupRecord.value = { ...record }
  scaleOutVisible.value = true
}

function changeTable({ current = pagination.current, pageSize = pagination.pageSize }) {
  pagination.current = current
  const resetPage = pageSize !== pagination.pageSize
  pagination.pageSize = pageSize
  refresh(resetPage)
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="list-wrap">
    <a-table
      class="ant-table-common"
      :columns="columns"
      :data-source="dataSource"
      :pagination="pagination"
      :loading="loading" @change="changeTable"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'durationDisplay'">
          <span :title="record.durationDesc">
            {{ record.durationDisplay }}
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
          <span
            class="primary-link" :class="{ disabled: record.container === 'external' }"
            @click="releaseModal(record)"
          >
            {{ t('release') }}
          </span>
        </template>
        <template v-if="column.dataIndex === 'operationGroup'">
          <span class="primary-link g-mr-12" @click="editGroup(record)">
            {{ t('edit') }}
          </span>
          <span class="primary-link" @click="removeGroup(record)">
            {{ t('remove') }}
          </span>
        </template>
      </template>
    </a-table>
  </div>
  <u-loading v-if="releaseLoading" />
</template>

<style lang="less" scoped>
.list-wrap {
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
