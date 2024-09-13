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
import { useI18n } from 'vue-i18n'
import SingleDataCard from './components/SingleDataCard.vue'
import MultipleDataCard from './components/MultipleDataCard.vue'
import Top10TablesCard from './components/Top10TablesCard.vue'
import ResourceUsageCard from './components/ResourceUsageCard.vue'
import DataSizeCard from './components/DataSizeCard.vue'
import PieChartCard from './components/PieChartCard.vue'
import { getOverviewOptimizingStatus, getOverviewSummary } from '@/services/overview.service'
import { bytesToSize } from '@/utils'
import type { IKeyAndValue } from '@/types/common.type'

const { t } = useI18n()

const singleData = ref<IKeyAndValue[]>([])
const multipleData = ref<IKeyAndValue[]>([])
const optimizingStatusData = ref<{ value: number, name: string }[]>([])

async function getCurOverviewData() {
  const summaryResult = await getOverviewSummary()
  const tableSize = bytesToSize(summaryResult.tableTotalSize)
  const memorySize = bytesToSize(summaryResult.totalMemory)
  singleData.value.push({ key: 'catalog', value: summaryResult.catalogCnt })
  singleData.value.push({ key: 'table', value: summaryResult.tableCnt })
  singleData.value.push({ key: 'data', value: tableSize })
  multipleData.value.push({ key: 'cpu', value: summaryResult.totalCpu })
  multipleData.value.push({ key: 'memory', value: memorySize })

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
        <SingleDataCard :title="t(card.key)" :value="card.value" />
      </a-col>
      <a-col :span="6">
        <MultipleDataCard :title="t('resource')" :data="multipleData" />
      </a-col>
      <a-col :span="12">
        <ResourceUsageCard />
      </a-col>
      <a-col :span="12">
        <DataSizeCard />
      </a-col>
      <a-col :span="12">
        <PieChartCard :title="t('optimizingStatus')" :data="optimizingStatusData" />
      </a-col>
      <a-col :span="12">
        <Top10TablesCard />
      </a-col>
    </a-row>
  </div>
</template>

<style lang="less" scoped>
</style>
