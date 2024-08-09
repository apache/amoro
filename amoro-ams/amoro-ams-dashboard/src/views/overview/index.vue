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
import UnhealthTablesCard from './components/UnhealthTablesCard.vue'
import OperationsCard from './components/OperationsCard.vue'
import PieChartCard from './components/PieChartCard.vue'
import { getOverviewFormat, getOverviewOptimizingStatus, getOverviewSummary } from '@/services/overview.service'
import { bytesToSize, mbToSize } from '@/utils'
import type { IKeyAndValue } from '@/types/common.type'

const { t } = useI18n()

const singleData = ref<IKeyAndValue[]>([])
const multipleData = ref<IKeyAndValue[]>([])
const tableFormatData = ref<{ value: number, name: string }[]>([])
const optimizingStatusData = ref<{ value: number, name: string }[]>([])

async function getCurOverviewData() {
  const summaryResult = await getOverviewSummary()
  const tableSize = bytesToSize(summaryResult.tableTotalSize)
  const memorySize = mbToSize(summaryResult.totalMemory)
  singleData.value.push({ key: t('catalog'), value: summaryResult.catalogCnt })
  singleData.value.push({ key: t('table'), value: summaryResult.tableCnt })
  singleData.value.push({ key: t('data'), value: tableSize })
  multipleData.value.push({ key: t('cpu'), value: summaryResult.totalCpu })
  multipleData.value.push({ key: t('memory'), value: memorySize })

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
        <SingleDataCard :title="card.key" :value="card.value" />
      </a-col>
      <a-col :span="6">
        <MultipleDataCard :title="t('resource')" :data="multipleData" />
      </a-col>
      <a-col :span="12">
        <PieChartCard :title="t('tableFormat')" :data="tableFormatData" />
      </a-col>
      <a-col :span="12">
        <PieChartCard :title="t('optimizingStatus')" :data="optimizingStatusData" />
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
