<script setup lang="ts">
import { ref, watch } from 'vue';
import { defineProps } from 'vue';
// import { Line } from '@antv/g2plot';

const props = defineProps<{ title: string; data: { time: string; size: number }[] }>();

const selectedRange = ref('30 minutes');
const chartContainer = ref<HTMLElement | null>(null);

watch(selectedRange, (newValue) => {
  // Update data based on selected range
});

// onMounted(() => {
//   if (chartContainer.value) {
//     new Line(chartContainer.value, {
//       data: props.data,
//       xField: 'time',
//       yField: 'size',
//       tooltip: {
//         showMarkers: false,
//       },
//     }).render();
//   }
// });
</script>

<template>
  <a-card>
    <template #title>
      {{ props.title }}
      <a-select v-model:value="selectedRange" style="float: right; width: 120px;">
        <a-select-option value="30 minutes">30 minutes</a-select-option>
        <a-select-option value="6 hours">6 hours</a-select-option>
        <a-select-option value="24 hours">24 hours</a-select-option>
      </a-select>
    </template>
    <div ref="chartContainer" style="height: 300px;"></div>
  </a-card>
</template>