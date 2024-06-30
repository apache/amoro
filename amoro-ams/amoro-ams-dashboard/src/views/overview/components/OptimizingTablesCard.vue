<script setup lang="ts">
import { defineProps } from 'vue';

const props = defineProps<{ title: string; tables: { name: string; status: string; duration: string }[] }>();
</script>

<template>
  <a-card>
    <template #title>
      {{ props.title }}
    </template>
    <a-table :dataSource="props.tables" :columns="columns" rowKey="name" />
  </a-card>
</template>

<script lang="ts">
const columns = [
  { title: 'Table Name', dataIndex: 'name', key: 'name' },
  { title: 'Status', dataIndex: 'status', key: 'status' },
  { title: 'Duration', dataIndex: 'duration', key: 'duration' },
];
</script>