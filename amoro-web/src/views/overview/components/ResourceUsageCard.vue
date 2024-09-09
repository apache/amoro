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
import { useI18n } from 'vue-i18n'
import { onMounted, ref } from 'vue'
import Chart from '@/components/echarts/Chart.vue'
import { bytesToSize, dateFormat } from '@/utils'
import { getResourceUsageList } from '@/services/overview.service'
import type { ResourceUsageItem } from '@/types/common.type'

const { t } = useI18n()
const timeRange = ref('24')
const loading = ref<boolean>(false)

function resourceFormatter(params: any[]): string {
  const cpuParam = params[0]
  const memoryParam = params[1]
  const memorySize = bytesToSize(memoryParam.value)
  let str = `<span style="font-size: 12px">${params[0].axisValue}</span><br/>`
  str += `<span style="display: inline-block;background-color:${cpuParam.color}; margin-right: 6px; width: 6px;height: 6px;"></span>${t('cpu')}: ${cpuParam.value} Core<br/>`
  str += `<span style="display: inline-block;background-color:${memoryParam.color}; margin-right: 6px; width: 6px;height: 6px;"></span>${t('memory')}: ${memorySize}<br/>`
  return str
}

const option = ref({
  tooltip: {
    trigger: 'axis',
    formatter: resourceFormatter,
  },
  legend: { data: [t('cpu'), t('memory')] },
  xAxis: { type: 'category', data: [''] },
  yAxis: [
    { type: 'value', name: t('cpuCores'), axisLabel: { formatter: '{value}' } },
    { type: 'value', name: t('memory'), axisLabel: { formatter: (value: number) => { return `${bytesToSize(value)}` } } },
  ],
  series: [
    { name: t('cpu'), type: 'line', yAxisIndex: 0, data: [-1] },
    { name: t('memory'), type: 'line', yAxisIndex: 1, data: [-1] },
  ],
})

async function updateData() {
  try {
    loading.value = true
    const times: string[] = []
    const cpu: number[] = []
    const memory: number[] = []

    const startTime = new Date().getTime() - Number.parseFloat(timeRange.value) * 60 * 60 * 1000
    const result: ResourceUsageItem[] = await getResourceUsageList(startTime)
    result.forEach((item) => {
      times.push(dateFormat(item.ts))
      cpu.push(item.totalCpu)
      memory.push(item.totalMemory)
    })
    option.value.xAxis.data = times
    option.value.series[0].data = cpu
    option.value.series[1].data = memory
  }
  catch (error) {
  }
  finally {
    loading.value = false
  }
}

onMounted(() => {
  updateData()
})
</script>

<template>
  <a-card>
    <template #title>
      <a-row justify="space-between">
        <span class="card-title" v-text="t('resourceUsage')" />
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <a-select v-model:value="timeRange" style="width: 120px" @change="updateData">
            <a-select-option value="1">
              {{ t('last1h') }}
            </a-select-option>
            <a-select-option value="12">
              {{ t('last12h') }}
            </a-select-option>
            <a-select-option value="24">
              {{ t('last24h') }}
            </a-select-option>
            <a-select-option value="168">
              {{ t('last7day') }}
            </a-select-option>
          </a-select>
        </div>
      </a-row>
    </template>
    <Chart :loading="loading" :options="option" />
  </a-card>
</template>

<style scoped>
.card-title {
  font-size: 18px;
}
</style>
