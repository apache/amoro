<script setup lang="ts">
import { defineProps } from 'vue';
const props = defineProps<{ title: string; items: { subtitle: string; data: number }[] }>();
</script>

<template>
  <a-card>
    <template #title>
      {{ props.title }}
    </template>
    <div v-for="item in props.items" :key="item.subtitle">
      <p>{{ item.subtitle }}</br> {{ item.data }}</p>
    </div>
  </a-card>
</template>

<style scoped>
p {
  margin: 0;
  font-size: 18px;
}
</style>