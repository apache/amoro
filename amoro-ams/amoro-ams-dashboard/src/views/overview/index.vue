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
import { onMounted, ref } from 'vue'
import SingleDataCard from './components/SingleDataCard.vue'
import MultipleDataCard from './components/MultipleDataCard.vue'
import UnhealthTablesCard from './components/UnhealthTablesCard.vue'
import OperationsCard from './components/OperationsCard.vue'
import PieChartCard from './components/PieChartCard.vue'
import { getOverviewFormat, getOverviewOptimizingStatus, getOverviewSummary } from '@/services/overview.service'
import { bytesToSize, mbToSize } from '@/utils'

interface SingleData {
  title: string
  data: string
}

const singleData = ref<SingleData[]>([])
const multipleData = ref<SingleData[]>([])
const tableFormatData = ref<{ value: number, name: string }[]>([])
const optimizingStatusData = ref<{ value: number, name: string }[]>([])

async function getCurOverviewData() {
  const summaryResult = await getOverviewSummary()
  const tableSize = bytesToSize(summaryResult.tableTotalSize)
  const memorySize = mbToSize(summaryResult.totalMemory)
  singleData.value.push({ title: 'Catalog', data: summaryResult.catalogCnt })
  singleData.value.push({ title: 'Table', data: summaryResult.tableCnt })
  singleData.value.push({ title: 'Data', data: tableSize })
  multipleData.value.push({ title: 'CPU', data: summaryResult.totalCpu })
  multipleData.value.push({ title: 'Memory', data: memorySize })

  tableFormatData.value = await getOverviewFormat()
  optimizingStatusData.value = await getOverviewOptimizingStatus()
}

onMounted(() => {
  getCurOverviewData()
})
</script>

<template>
  <div :style="{ background: '#F8F7F8', padding: '24px', minHeight: '900px' }" class="overview-content">
    <a-row :gutter="[16, 8]">
      <a-col v-for="(card, index) in singleData" :key="index" :span="6">
        <SingleDataCard :title="card.title" :data="card.data" />
      </a-col>
      <a-col :span="6">
        <MultipleDataCard title="Resource" :data="multipleData" />
      </a-col>
      <a-col :span="12">
        <PieChartCard title="Table Format" :data="tableFormatData" />
      </a-col>
      <a-col :span="12">
        <PieChartCard title="Optimizing Status" :data="optimizingStatusData" />
      </a-col>
      <a-col :span="12">
        <UnhealthTablesCard />
      </a-col>
      <a-col :span="12">
        <OperationsCard />
      </a-col>
    </a-row>
  </div>
</template>

<style lang="less" scoped>
</style>
