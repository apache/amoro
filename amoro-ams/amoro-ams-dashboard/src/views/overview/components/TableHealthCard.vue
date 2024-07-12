<template>
    <a-card class="table-health" title="Table Health">
      <a-table
        :columns="columns"
        :dataSource="data"
        :pagination="false"
        @change="handleTableChange"
      />
    </a-card>
  </template>
  
  <script lang="ts" setup>
  import { ref } from 'vue';
  
  interface DataSource {
    key: string;
    catalog: string;
    database: string;
    tableName: string;
    health: string;
    size: string;
  }
  
  const columns = [
    {
      title: 'Catalog',
      dataIndex: 'catalog',
      sorter: true,
    },
    {
      title: 'Database',
      dataIndex: 'database',
      sorter: true,
    },
    {
      title: 'Table Name',
      dataIndex: 'tableName',
      sorter: true,
    },
    {
      title: 'Health',
      dataIndex: 'health',
      sorter: true,
    },
    {
      title: 'Table Size',
      dataIndex: 'size',
      sorter: true,
    },
  ];
  
  const data = ref<DataSource[]>([
    { key: '1', catalog: 'Cat1', database: 'DB1', tableName: 'Table1', health: '73', size: '10MB' },
    { key: '2', catalog: 'Cat2', database: 'DB2', tableName: 'Table2', health: '90', size: '20MB' },
    // Add more data here
  ]);
  
  const handleTableChange = (pagination: any, filters: any, sorter: any) => {
    console.log('params', pagination, filters, sorter);
    // Implement sorting logic here
  };
  </script>
  
  <style scoped>
    .table-health{
        height: 450px;
    }

  </style>