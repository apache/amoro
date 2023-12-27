
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
  <div class="table-partitons">
    <template v-if="!hasBreadcrumb && hasPartition">
      <div class="filter-wrap">
        <a-input-search
          v-model:value="searchKey"
          :placeholder="$t('fileSearchPlaceholder')"
          @search="(val) => handleSearch(val)"
          style="width: 350px"
        >
          <template #prefix>
            <SearchOutlined />
          </template>
          <template #suffix v-if="searchKey">
            <CloseCircleOutlined @click="handleSearch('')" class="input-clear-icon" />
          </template>
        </a-input-search>
      </div>
      <a-table
        rowKey="partiton"
        :columns="columns"
        :data-source="dataSource"
        :pagination="pagination"
        @change="change"
        :loading="loading"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'partition'">
            <a-button type="link" @click="toggleBreadcrumb(record)">
              {{ record.partition }}
            </a-button>
          </template>
        </template>
      </a-table>
    </template>
    <template v-else>
      <a-breadcrumb separator=">" v-if="hasPartition">
        <a-breadcrumb-item @click="toggleBreadcrumb" class="text-active">All</a-breadcrumb-item>
        <a-breadcrumb-item>{{ `${$t('partition')} ${partitionId}`}}</a-breadcrumb-item>
      </a-breadcrumb>
      <a-table
        rowKey="file"
        :columns="breadcrumbColumns"
        :data-source="breadcrumbDataSource"
        :pagination="breadcrumbPagination"
        @change="change"
        :loading="loading"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'file'">
            <a-tooltip>
              <template #title>{{record.file}}</template>
              <span>{{record.file}}</span>
            </a-tooltip>
          </template>
          <template v-if="column.dataIndex === 'path'">
            <a-tooltip>
              <template #title>{{record.path}}</template>
              <span>{{record.path}}</span>
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
import { BreadcrumbPartitionItem, IColumns, PartitionItem } from '@/types/common.type'
import { getPartitionFiles, getPartitionTable } from '@/services/table.service'
import { useRoute } from 'vue-router'
import { dateFormat } from '@/utils'
import { SearchOutlined, CloseCircleOutlined } from '@ant-design/icons-vue'

const hasBreadcrumb = ref<boolean>(false)
const { t } = useI18n()
const columns: IColumns[] = shallowReactive([
  { title: t('partition'), dataIndex: 'partition', ellipsis: true },
  { title: t('fileCount'), dataIndex: 'fileCount', width: 120, ellipsis: true },
  { title: t('size'), dataIndex: 'size', width: 120, ellipsis: true },
  { title: t('lastCommitTime'), dataIndex: 'lastCommitTime', width: 200, ellipsis: true }
])
const breadcrumbColumns = shallowReactive([
  { title: t('file'), dataIndex: 'file', ellipsis: true },
  // { title: t('fsn'), dataIndex: 'fsn' },
  { title: t('fileType'), dataIndex: 'fileType', width: 120, ellipsis: true },
  { title: t('size'), dataIndex: 'size', width: 120, ellipsis: true },
  { title: t('commitTime'), dataIndex: 'commitTime', width: 200, ellipsis: true },
  { title: t('commitId'), dataIndex: 'commitId', width: 200, ellipsis: true },
  { title: t('path'), dataIndex: 'path', ellipsis: true, scopedSlots: { customRender: 'path' } }
])

const props = defineProps<{ hasPartition: boolean}>()
const dataSource = reactive<PartitionItem[]>([])
const breadcrumbDataSource = reactive<BreadcrumbPartitionItem[]>([])
const partitionId = ref<string>('')
const specId = ref<number>(0)
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
const searchKey = ref<string>('')

async function handleSearch(val: string) {
  searchKey.value = val
  await getTableInfo()
}

async function getTableInfo() {
  try {
    loading.value = true
    dataSource.length = 0
    const result = await getPartitionTable({
      ...sourceData,
      filter: searchKey.value,
      page: pagination.current,
      pageSize: pagination.pageSize
    })
    const { list, total } = result
    pagination.total = total;
    (list || []).forEach((p: PartitionItem) => {
      p.lastCommitTime = p.lastCommitTime ? dateFormat(p.lastCommitTime) : ''
      dataSource.push(p)
    })
  } catch (error) {
  } finally {
    loading.value = false
  }
}
function change({ current = 1, pageSize = 25 }) {
  if (!hasBreadcrumb.value && props.hasPartition) {
    pagination.current = current
    if (pageSize !== pagination.pageSize) {
      pagination.current = 1
    }
    pagination.pageSize = pageSize
  } else {
    breadcrumbPagination.current = current
    if (pageSize !== breadcrumbPagination.pageSize) {
      breadcrumbPagination.current = 1
    }
    breadcrumbPagination.pageSize = pageSize
  }
  refresh()
}

function refresh() {
  if (!props.hasPartition) {
    getFiles()
    return
  }
  if (hasBreadcrumb.value) {
    getFiles()
  } else {
    getTableInfo()
  }
}

async function getFiles() {
  try {
    breadcrumbDataSource.length = 0
    loading.value = true
    const params = {
      ...sourceData,
      partition: props.hasPartition ? encodeURIComponent(partitionId.value) : null,
      specId: specId.value,
      page: breadcrumbPagination.current,
      pageSize: breadcrumbPagination.pageSize
    }
    const result = await getPartitionFiles(params)
    const { list, total } = result
    breadcrumbPagination.total = total;
    (list || []).forEach((p: BreadcrumbPartitionItem) => {
      p.commitTime = p.commitTime ? dateFormat(p.commitTime) : ''
      breadcrumbDataSource.push(p)
    })
  } catch (error) {
  } finally {
    loading.value = false
  }
}

function toggleBreadcrumb(record: PartitionItem) {
  partitionId.value = record.partition
  specId.value = record.specId
  hasBreadcrumb.value = !hasBreadcrumb.value
  if (hasBreadcrumb.value) {
    breadcrumbPagination.current = 1
    getFiles()
  }
}

onMounted(() => {
  hasBreadcrumb.value = false

  if (props.hasPartition) {
    getTableInfo()
  } else {
    getFiles()
  }
})

</script>

<style lang="less" scoped>
.table-partitons {
  padding: 18px 24px;
  .text-active {
    color: #1890ff;
    cursor: pointer;
  }

  .filter-wrap {
    margin-bottom: 12px;
    .input-clear-icon {
      font-size: 12px;
    }
  }
  :deep(.ant-input-group-addon) {
    display: none;
  }
}
</style>
