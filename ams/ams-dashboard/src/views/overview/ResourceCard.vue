<template>
  <div class="resource-card g-flex g-mr-16">
    <div class="card g-mr-16">
      <span class="card-name">{{$t('catalog')}}</span>
      <p class="card-value">{{summary.catalogCnt}}</p>
    </div>
    <div class="card g-mr-16">
      <span class="card-name">{{$t('table')}}</span>
      <p class="card-value">{{summary.tableCnt}}</p>
    </div>
    <div class="card g-mr-16">
      <span class="card-name">{{$t('data')}}</span>
      <p class="card-value">{{summary.displayTotalSize}}<span class="unit">{{summary.totalSizeUnit}}</span></p>
    </div>
    <div class="card g-mr-16 card-resource">
      <div class="g-flex-row">
        <div class="card-inner card-left">
          <span class="card-name">{{$t('resourceCpu')}}</span>
          <p class="card-value">{{summary.totalCpu}}<span class="unit">Core</span></p>
        </div>
        <div class="card-inner card-right">
          <span class="card-name">{{$t('resourceMemory')}}</span>
          <p class="card-value">{{summary.displayTotalMemory}}<span class="unit">{{summary.totalMemoryUnit}}</span></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive } from 'vue'
import { getOverviewSummary } from '@/services/overview.service'
import { IOverviewSummary } from '@/types/common.type'
import { bytesToSize, mbToSize } from '@/utils'

const summary = reactive<IOverviewSummary>({})

async function getSummaryData() {
  const result = await getOverviewSummary()
  Object.assign(summary, { ...result })
  const { size: displayTotalSize, unit: totalSizeUnit } = convertUnit(result.tableTotalSize)
  summary.displayTotalSize = displayTotalSize
  summary.totalSizeUnit = totalSizeUnit
  const { size: displayTotalMemory, unit: totalMemoryUnit } = convertUnit(result.totalMemory, true)
  summary.displayTotalMemory = displayTotalMemory
  summary.totalMemoryUnit = totalMemoryUnit
}

function convertUnit(value: number, isMbToSize = false) {
  const item = isMbToSize ? mbToSize(value) : bytesToSize(value)
  const itemArr = item.split(' ')
  return {
    size: itemArr[0],
    unit: itemArr[1] || ''
  }
}

onMounted(() => {
  getSummaryData()
})

</script>
<style lang="less" scoped>
.resource-card {
  .card {
    display: flex;
    flex: 1;
    flex-shrink: 0;
    flex-direction: column;
    background-color: #fff;
    border-radius: 2px;
    padding: 16px 24px;
    box-shadow: 0 1px 4px rgb(0 21 41 / 8%);
    .card-name {
      font-size: 14px;
      line-height: 22px;
    }
    .card-value {
      font-weight: 500;
      font-size: 30px;
      line-height: 40px;
      color: @header-color;
      .unit {
        font-size: 12px;
        line-height: 20px;
        margin-left: 8px;
      }
    }
    .card-left {
      border-right: 1px solid rgba(0, 0, 0, 0.06);
    }
    .card-right {
      padding-left: 16px;
    }
    &.card-resource {
      flex: 1.5;
      margin-right: 0;
      flex-shrink: 0;
      min-width: 382px;
      padding: 16px 24px;
    }
    .card-inner {
      flex: 1;
    }
  }
}
</style>
