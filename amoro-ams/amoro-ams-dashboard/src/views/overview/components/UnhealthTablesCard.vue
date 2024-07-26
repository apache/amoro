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
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import type { TableProps } from 'ant-design-vue'
import { usePagination } from '@/hooks/usePagination'
import type { UnhealthTableItem } from '@/types/common.type'
import { getUnhealthTableList } from '@/services/overview.service'
import { bytesToSize } from '@/utils'

const { t } = useI18n()
const router = useRouter()
const pagination = reactive(usePagination())
const loading = ref<boolean>(false)
const catalogs = reactive<string[]>([])
const catalogFilter = reactive<{ text: string, value: string }[]>([])
const dataSource = reactive<UnhealthTableItem[]>([])

const columns: TableProps['columns'] = [
  {
    title: t('table'),
    dataIndex: 'tableName',
    filterSearch: true,
    filters: catalogFilter,
    filterMode: 'tree',
    onFilter: (value, record: UnhealthTableItem) => record.tableIdentifier.catalog === value,
  },
  {
    title: t('healthScore'),
    dataIndex: 'healthScore',
    sorter: true,
  },
  {
    title: t('size'),
    dataIndex: 'totalSize',
    sorter: true,
  },
  {
    title: t('fileCount'),
    dataIndex: 'fileCount',
    sorter: true,
  },
  {
    title: t('averageFileSize'),
    dataIndex: 'averageFileSize',
    sorter: true,
  },
]

function goTableDetail(record: UnhealthTableItem) {
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

async function getUnhealthTables() {
  try {
    loading.value = true
    dataSource.length = 0
    const result = await getUnhealthTableList({
      page: pagination.current,
      pageSize: pagination.pageSize,
    })
    const { total, list } = result
    pagination.total = total;
    (list || []).forEach((ele: UnhealthTableItem) => {
      if (!catalogs.includes(ele.tableIdentifier.catalog)) {
        catalogs.push(ele.tableIdentifier.catalog)
      }
      dataSource.push(ele)
    })
    catalogs.forEach((catalog) => {
      catalogFilter.push({ text: catalog, value: catalog })
    })
  }
  catch (error) {
  }
  finally {
    loading.value = false
  }
}

const filteredInfo = ref()
const sortedInfo = ref()

const handleChange: TableProps['onChange'] = (filters, sorter) => {
  filteredInfo.value = filters
  sortedInfo.value = sorter
}

onMounted(() => {
  getUnhealthTables()
})
</script>

<template>
  <a-card class="unhealth-tables-card" title="Unhealth Tables">
    <div class="list-wrap">
      <a-table
        class="ant-table-common" :columns="columns" :data-source="dataSource" :pagination="pagination"
        :loading="loading" @change="handleChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'tableName'">
            <span :title="record.tableName" class="primary-link" @click="goTableDetail(record)">
              {{ record.tableName }}
            </span>
          </template>
          <template v-if="column.dataIndex === 'totalSize'">
            {{ bytesToSize(record.totalSize) }}
          </template>
          <template v-if="column.dataIndex === 'averageFileSize'">
            {{ bytesToSize(record.averageFileSize) }}
          </template>
        </template>
      </a-table>
    </div>
  </a-card>
</template>

<style lang="less" scoped>
.unhealth-tables-card {
  height: 350px;
}

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
}
</style>
