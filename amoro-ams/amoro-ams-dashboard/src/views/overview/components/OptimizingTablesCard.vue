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
import { computed, ref } from 'vue'

const columns = [
  { title: 'Table Name', dataIndex: 'name', key: 'name' },
  { title: 'Status', dataIndex: 'status', key: 'status' },
  { title: 'Duration', dataIndex: 'duration', key: 'duration' },
]

const current = ref(1)
const pageSize = ref(10)

const pagination = computed(() => ({
  total: 1,
  current: current.value,
  pageSize: pageSize.value,
}))

const dataSource = ref<{ name: string, status: string, duration: string }[]>([
])

const loading = ref(true)
function fetchData() {
  loading.value = false
  dataSource.value = [
    { name: 'Table 1', status: 'Optimizing', duration: '1h 20m' },
    { name: 'Table 2', status: 'Idle', duration: '0h 45m' },
  ]
}

fetchData()
</script>

<template>
  <a-card>
    <template #title>
      Optimizing Tables
    </template>
    <a-table
      :data-source="dataSource"
      :columns="columns"
      row-key="name"
      :pagination="pagination"
      :loading="loading"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'name'">
          <a>
            {{ record.name }}
          </a>
        </template>

        <template v-else-if="column.key === 'status'">
          <span>
            <a-tag
              :color="record.status === 'Optimizing' ? 'green' : 'geekblue'"
            >
              {{ record.status.toUpperCase() }}
            </a-tag>
          </span>
        </template>
      </template>
    </a-table>
  </a-card>
</template>
