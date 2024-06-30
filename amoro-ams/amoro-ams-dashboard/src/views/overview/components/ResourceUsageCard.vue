<script setup lang="ts">
import { ref, watch } from 'vue';
import { defineProps } from 'vue';

const props = defineProps<{data: { time: string; memory: number; cpu: number }[] }>();

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
//       yField: 'value',
//       seriesField: 'type',
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
      Resource Usage(%)
      <a-select v-model:value="selectedRange" style="float: right; width: 120px;">
        <a-select-option value="05h">Last 30 m</a-select-option>
        <a-select-option value="6h">Last 6 h</a-select-option>
        <a-select-option value="24h">Last 24 h</a-select-option>
      </a-select>
    </template>
    <div ref="chartContainer" style="height: 300px;"></div>
  </a-card>
</template>