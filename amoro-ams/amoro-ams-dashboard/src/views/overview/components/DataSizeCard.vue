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

const chart = ref(null)
const timeRange = ref('24')

function updateData() {
  // This is where you'd fetch and update the chart data based on `timeRange`
}

onMounted(() => {
  const chartInstance = echarts.init(chart.value)
  const option = {
    tooltip: {
      trigger: 'axis',
    },
    xAxis: {
      type: 'category',
      data: ['00:00', '00:10', '00:20', '00:30', '00:40', '00:50'],
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        name: '数据量',
        type: 'line',
        data: [820, 932, 901, 934, 1290, 1330],
      },
    ],
  }
  chartInstance.setOption(option)
  watch(timeRange, updateData)
})
</script>

<template>
  <a-card>
    <template #title>
      <a-row justify="space-between">
        <span class="card-title">Data Size</span>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <a-select v-model:value="timeRange" style="width: 120px" @change="updateData">
            <a-select-option value="0.5">
              Last 30 min
            </a-select-option>
            <a-select-option value="8">
              Last 8 h
            </a-select-option>
            <a-select-option value="24">
              Last 24 h
            </a-select-option>
            <a-select-option value="168">
              Last 7 day
            </a-select-option>
          </a-select>
        </div>
      </a-row>
    </template>

    <div ref="chart" style="height: 300px;" />
  </a-card>
</template>

<style scoped>
.card-title {
  font-size: 18px;
}
</style>
