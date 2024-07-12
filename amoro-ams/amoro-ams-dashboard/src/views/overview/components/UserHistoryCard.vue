
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
    <a-card class="user-history" title="DDL History">
      <a-table :columns="columns" :dataSource="data" :pagination="false" />
    </a-card>
  </template>
  
  <script lang="ts" setup>
  import { ref } from 'vue';
//   import { Tooltip } from 'ant-design-vue';
  
  interface DataSource {
    key: string;
    username: string;
    action: string;
    time: string;
  }
  
  const columns = [
    {
      title: 'User',
      dataIndex: 'username',
    },
    {
      title: 'DDL',
      dataIndex: 'action',
      customRender: ({ text }: { text: string }) => {
        return (
            text
        //   <Tooltip placement="top" title={text}>
        //     <span>{text.slice(0, 10)}...</span>
        //   </Tooltip>
        );
      },
    },
    {
      title: 'Action Time',
      dataIndex: 'time',
    },
  ];
  
  const data = ref<DataSource[]>([
    { key: '1', username: 'User1', action: 'Created new table named Table1', time: '2024-07-11 10:00' },
    { key: '2', username: 'User2', action: 'Deleted database DB2', time: '2024-07-11 11:00' },
    { key: '2', username: 'User2', action: 'Deleted database DB2', time: '2024-07-11 11:00' },
    { key: '2', username: 'User2', action: 'Deleted database DB2', time: '2024-07-11 11:00' },
    { key: '2', username: 'User2', action: 'Deleted database DB2', time: '2024-07-11 11:00' },
    { key: '2', username: 'User2', action: 'Deleted database DB2', time: '2024-07-11 11:00' },
    { key: '2', username: 'User2', action: 'Deleted database DB2', time: '2024-07-11 11:00' },
    { key: '2', username: 'User2', action: 'Deleted database DB2', time: '2024-07-11 11:00' },
    // Add more data here
  ]);
  </script>
  
  <style scoped>
      .user-history{
        height: 450px;
    }
  </style>