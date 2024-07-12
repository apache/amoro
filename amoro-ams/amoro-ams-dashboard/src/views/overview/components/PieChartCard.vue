<template>
    <a-card :title="title">
      <div ref="pieChart" style="width: 100%; height: 400px;"></div>
    </a-card>
  </template>
  
  <script lang="ts" setup>
  import { ref, onMounted, watch } from 'vue';
  import * as echarts from 'echarts';
  
  interface PieData {
    value: number;
    name: string;
  }
  
  interface Props {
    title: string;
    data: PieData[];
  }
  
  const props = defineProps<Props>();
  
  const pieChart = ref<HTMLDivElement | null>(null);
  
  const renderChart = () => {
    if (pieChart.value) {
      const chart = echarts.init(pieChart.value);
      const option = {
        tooltip: {
          trigger: 'item',
        },
        legend: {
          orient: 'vertical',
          left: 'left',
        },
        series: [
          {
            name: '数据类型占比',
            type: 'pie',
            radius: '50%',
            data: props.data,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)',
              },
            },
          },
        ],
      };
      chart.setOption(option);
    }
  };
  
  onMounted(renderChart);
  watch(() => props.data, renderChart);
  </script>
  
  <style scoped>
  </style>