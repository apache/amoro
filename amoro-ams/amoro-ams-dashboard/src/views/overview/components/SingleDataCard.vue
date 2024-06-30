<script setup lang="ts">
import { defineProps } from 'vue';
const props = defineProps<{ title: string; data: number }>();
</script>

<template>
  <a-card>
    <template #title>
      {{ props.title }}
    </template>
    <p>{{ props.data }}</p>
  </a-card>
</template>

<style scoped>
p {
  margin: 0;
  font-size: 18px;
}
</style>