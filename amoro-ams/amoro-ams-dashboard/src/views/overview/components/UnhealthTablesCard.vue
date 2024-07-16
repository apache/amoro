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
import { ref } from 'vue'
import type { TableProps } from 'ant-design-vue'

interface DataItem {
  key: string
  table: string
  healthScore: string
  size: string
  fileCount: string
  averageFileSize: string
}

const filteredInfo = ref()
const sortedInfo = ref()

const columns: TableProps['columns'] = [
  {
    title: 'Table',
    dataIndex: 'table',
    key: 'table',
    filterSearch: true,
    filters: [
      {
        text: 'test_catalog',
        value: 'test_catalog.db.school',
      },
      {
        text: 'test_catalog2',
        value: 'test_catalog.db.course',
      },
      {
        text: 'test_catalog3',
        value: 'test_catalog.db.viedo',
      },
    ],
    filterMode: 'tree',
    onFilter: (value, record: DataItem) => record.table === value,
  },
  {
    title: 'Health Score',
    dataIndex: 'healthScore',
    sorter: true,
  },
  {
    title: 'Size',
    dataIndex: 'size',
    sorter: true,
  },
  {
    title: 'File Count',
    dataIndex: 'fileCount',
    sorter: true,
  },
  {
    title: 'Average File Size',
    dataIndex: 'averageFileSize',
    sorter: true,
  },
]

const data = ref<DataItem[]>([
  { key: '1', table: 'test_catalog.db.school', healthScore: '47', size: '10 MB', fileCount: '10', averageFileSize: '1 MB' },
  { key: '2', table: 'test_catalog.db.course', healthScore: '70', size: '20 MB', fileCount: '2', averageFileSize: '10 MB' },
  { key: '3', table: 'test_catalog.db.viedo', healthScore: '88', size: '50 MB', fileCount: '5', averageFileSize: '10 MB' },
  // Add more data here
])

const handleChange: TableProps['onChange'] = (filters, sorter) => {
  filteredInfo.value = filters
  sortedInfo.value = sorter
}
</script>

<template>
  <a-card class="unhealth-tables-card" title="Unhealth Tables">
    <a-table
      :columns="columns"
      :data-source="data"
      row-key="table"
      @change="handleChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'table'">
          <a>
            {{ record.table }}
          </a>
        </template>
      </template>
    </a-table>
  </a-card>
</template>

<style scoped>
.unhealth-tables-card {
  height: 350px;
}
</style>
