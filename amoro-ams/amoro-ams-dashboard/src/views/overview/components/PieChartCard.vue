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
import * as echarts from 'echarts'

interface PieData {
  value: number
  name: string
}

interface Props {
  title: string
  data: PieData[]
}

const props = defineProps<Props>()

const pieChart = ref<HTMLDivElement | null>(null)

function renderChart() {
  if (pieChart.value) {
    const chart = echarts.init(pieChart.value, null, { height: 300 })
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
    }
    chart.setOption(option)
  }
}

onMounted(renderChart)
watch(() => props.data, renderChart)
</script>

<template>
  <a-card :title="title">
    <div ref="pieChart" style="width: 100%; height: 300px;" />
  </a-card>
</template>

<style scoped>
</style>
