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
  <a-card class="operations-card" title="Latest Operations">
    <a-table :columns="columns" :dataSource="data" :pagination="false"  rowKey="key" >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'table'">
          <a>
            {{ record.table }}
          </a>
        </template>

        <!-- <template v-else-if="column.key === 'status'">
        <span>
          <a-tag
            :color="record.status == 'Optimizing' ? 'green' : 'geekblue'"
          >
            {{ record.status.toUpperCase() }}
          </a-tag>
        </span>
        </template> -->
      </template>
    </a-table>
  </a-card>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
//   import { Tooltip } from 'ant-design-vue';

interface DataSource {
  key: string;
  table: string;
  operation: string;
  time: string;
}

const columns = [
  {
    title: 'Table',
    dataIndex: 'table',
    key: 'table',
  },
  {
    title: 'Operation',
    dataIndex: 'operation',
    ellipsis: true,
  },
  {
    title: 'Time',
    dataIndex: 'time',
  },
];

const data = ref<DataSource[]>([
  {
    key: '1', table: 'test_catalog.db.school', operation: `create external table school
(
    school_id   int,
    school_name string,
    school_code string,
    school_note string
)
    row format delimited
        fields terminated by '|'
    stored as textfile
    location '/edu1/school`, time: '2024-07-10 10:53'
  },
  { key: '2', table: 'test_catalog.db.teacher', operation: 'drop table if exists teacher;', time: '2024-07-11 11:00' },
  { key: '3', table: 'test_catalog.db.video', operation: 'drop table if exists video;', time: '2024-07-11 12:00' },
  // Add more data here
]);
</script>

<style scoped>
.operations-card {
  height: 350px;
}
</style>