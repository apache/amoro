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
import useClipboard from 'vue-clipboard3'
import { Modal as AModal, message } from 'ant-design-vue'
import { usePagination } from '@/hooks/usePagination'
import type { IColumns, OperationItem } from '@/types/common.type'
import { getOperations } from '@/services/table.service'
import { dateFormat } from '@/utils'

const { toClipboard } = useClipboard()
const { t } = useI18n()
const columns: IColumns[] = shallowReactive([
  { title: t('time'), dataIndex: 'ts', width: '30%' },
  { title: t('operation'), dataIndex: 'operation', scopedSlots: { customRender: 'operation' } },
])
const visible = ref<boolean>(false)
const activeCopyText = ref<string>('')

const dataSource = reactive<OperationItem[]>([])

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

async function getOperationInfo() {
  try {
    loading.value = true
    dataSource.length = 0
    const result = await getOperations({
      ...sourceData,
      page: pagination.current,
      pageSize: pagination.pageSize,
    })
    const { total, list } = result
    pagination.total = total;
    (list || []).forEach((ele: OperationItem) => {
      ele.ts = ele.ts ? dateFormat(ele.ts) : ''
      dataSource.push(ele)
    })
  }
  catch (error) {
  }
  finally {
    loading.value = false
  }
}

function change({ current = 1, pageSize = 25 } = pagination) {
  pagination.current = current
  if (pageSize !== pagination.pageSize) {
    pagination.current = 1
  }
  pagination.pageSize = pageSize
  getOperationInfo()
}

function viewDetail(record: OperationItem) {
  visible.value = true
  activeCopyText.value = record.operation
}

function cancel() {
  visible.value = false
}

async function onCopy() {
  try {
    await toClipboard(activeCopyText.value)
    message.success(t('copySuccess'))
    cancel()
  }
  catch (error) {}
}

onMounted(() => {
  getOperationInfo()
})
</script>

<template>
  <div class="table-operations">
    <a-table
      row-key="partiton"
      :columns="columns"
      :data-source="dataSource"
      :pagination="pagination"
      :loading="loading"
      @change="change"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'operation'">
          <span class="text-active g-max-line-3" @click="viewDetail(record)">
            {{ record.operation }}
          </span>
        </template>
      </template>
    </a-table>
  </div>
  <AModal
    :open="visible"
    :width="560"
    :title="$t('operationDetails')"
    class="operation-wrap"
    @cancel="cancel"
  >
    {{ activeCopyText }}
    <template #footer>
      <a-button type="primary" @click="onCopy">
        {{ $t('copy') }}
      </a-button>
    </template>
  </AModal>
</template>

<style lang="less">
.table-operations {
  padding: 12px;
  .ant-table-tbody > tr > td {
    white-space: pre;
  }
  .text-active {
    color: #1890ff;
    cursor: pointer;
  }
}
.operation-wrap .ant-modal-body {
  max-height: 360px;
  overflow-y: auto;
  white-space: pre;
}
</style>
