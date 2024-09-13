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
import { getDataSizeList } from '@/services/overview.service'
import { bytesToSize, dateFormat } from '@/utils'
import type { DataSizeItem } from '@/types/common.type'
import Chart from '@/components/echarts/Chart.vue'

const { t } = useI18n()
const timeRange = ref('24')
const loading = ref<boolean>(false)

function dataSizeFormatter(params: any[]): string {
  const dataParam = params[0]
  const dataSize = bytesToSize(dataParam.value)
  let str = `<span style="font-size: 12px">${params[0].axisValue}</span><br/>`
  str += `<span style="display: inline-block;background-color:${dataParam.color}; margin-right: 6px; width: 6px;height: 6px;"></span>${t('dataSize')}: ${dataSize}<br/>`
  return str
}

const dataSizeChartOption = ref({
  tooltip: {
    trigger: 'axis',
    formatter: dataSizeFormatter,
  },
  legend: { data: [t('dataSize')] },
  xAxis: {
    type: 'category',
    data: [''],
  },
  yAxis: {
    type: 'value',
    axisLabel: { formatter: (value: number) => { return `${bytesToSize(value)}` } },
  },
  series: [
    {
      name: t('dataSize'),
      type: 'line',
      data: [-1],
    },
  ],
})

async function updateData() {
  try {
    loading.value = true
    const times: string[] = []
    const dataSizeList: number[] = []

    const startTime = new Date().getTime() - Number.parseFloat(timeRange.value) * 60 * 60 * 1000
    const result: DataSizeItem[] = await getDataSizeList(startTime)
    result.forEach((item) => {
      times.push(dateFormat(item.ts))
      dataSizeList.push(item.dataSize)
    })
    dataSizeChartOption.value.xAxis.data = times
    dataSizeChartOption.value.series[0].data = dataSizeList
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
        <span class="card-title" v-text="t('dataSize')" />
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

    <Chart :loading="loading" :options="dataSizeChartOption" />
  </a-card>
</template>

<style scoped>
.card-title {
  font-size: 18px;
}
</style>
