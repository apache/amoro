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

<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue'
import Chart from '@/components/echarts/Chart.vue'

const props = defineProps<{
  title: string
  data: {
    value: number
    name: string
  }[]
}>()

const pieChartOption = ref({})

function renderChart() {
  pieChartOption.value = {
    tooltip: {
      trigger: 'item',
    },
    legend: {
      orient: 'vertical',
      left: 'left',
    },
    series: [
      {
        name: props.title,
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
  }
}

onMounted(renderChart)
watch(() => props.data, renderChart)
</script>

<template>
  <a-card class="pie-chart-card">
    <template #title>
      <span class="card-title" v-text="title" />
    </template>
    <Chart :options="pieChartOption" />
  </a-card>
</template>

<style scoped>
.pie-chart-card {
  height: 500px;
}

.card-title {
  font-size: 18px;
}
</style>
