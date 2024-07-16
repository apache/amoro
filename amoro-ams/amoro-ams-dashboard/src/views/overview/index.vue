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
import { bytesToSize } from '@/utils'

interface SingleData {
  title: string
  data: string
  precision: number
  suffix: string
}

const singleData = ref<SingleData[]>([])
const multipleData = ref<SingleData[]>([])
const tableFormatData = ref<{ value: number, name: string }[]>([])
const optimizingStatusData = ref<{ value: number, name: string }[]>([])

async function getCurOverviewData() {
  const summaryResult = await getOverviewSummary()
  const tableSize = bytesToSize(summaryResult.tableTotalSize)
  const memorySize = bytesToSize(summaryResult.totalMemory)
  singleData.value.push({ title: 'Catalog', data: summaryResult.catalogCnt, precision: 0, suffix: '' })
  singleData.value.push({ title: 'Table', data: summaryResult.tableCnt, precision: 0, suffix: '' })
  singleData.value.push({ title: 'Data', data: tableSize, precision: 0, suffix: '' })
  multipleData.value.push({ title: 'CPU', data: summaryResult.totalCpu, precision: 0, suffix: '' })
  multipleData.value.push({ title: 'Memory', data: memorySize, precision: 0, suffix: '' })

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
        <SingleDataCard :title="card.title" :data="card.data" :precision="card.precision" :suffix="card.suffix" />
      </a-col>
      <a-col :span="6">
        <MultipleDataCard title="Resource" :items="multipleData" />
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
.home-section {
  margin: 0 auto;
  display: flex;
  justify-content: center;
  background-color: #fff;

  .content {
    width: 100%;
    max-width: 1182px;
    padding: 64px 30px;

    .img {
      max-width: 100%;
    }
  }
}

.home-section .content .title,
.home-feature .content .title {
  font-size: 20px;
  font-weight: bold;
  line-height: 24px;
  text-align: center;
  margin-bottom: 40px;
}

.home-feature {
  background: #f5f6fa;
  margin: 0 auto;
  display: flex;
  justify-content: center;

  .content {
    width: 100%;
    max-width: 1182px;
    padding: 64px 41px 80px;

    .title {
      font-size: 32px;
    }

    .features {
      display: grid;
      grid-row-gap: 24px;
      grid-column-gap: 24px;
      max-width: 1100px;
      grid-template-columns: auto auto;

      .feature-item {
        background-color: #fff;
        padding: 32px;
        border-radius: 5px;
        position: relative;

        .fix-icon {
          position: absolute;
          top: 0px;
          right: 20px;
        }
      }
    }
  }
}

@media screen and (max-width: 800px) {
  .home-feature .features {
    grid-template-columns: auto;
  }
}

.home-feature .features .item-title {
  font-size: 24px;
  font-weight: bold;
  line-height: 28px;
  display: flex;
  align-items: center;
}

.home-feature .features .item-title img {
  margin-right: 13px;
}

.home-feature .features .item-desc {
  margin-top: 24px;
  font-size: 14px;
  font-weight: normal;
  line-height: 22px;
  color: #53576a;
}
</style>
