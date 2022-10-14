<template>
  <div class="resource-occupation g-mr-16">
    <div class="common-header">
      <span class="name">{{`${$t('resourceUsage')} (%)`}}</span>
      <a-select :options="timeOptions"></a-select>
    </div>
    <Chart :options="lineChartOptions" :loading="loading" />
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import Chart from '@/components/echarts/Chart.vue'

const loading = ref<boolean>(false)
const lineChartOptions = reactive([])

function getLineChartData() {}

onMounted(() => {
  getLineChartData()
})
</script>
<style lang="less" scoped>
.resource-occupation {
  padding: 0 24px 16px;
  display: flex;
  flex: 1;
  flex-direction: column;
  box-shadow: 0 1px 4px rgb(0 21 41 / 8%);
  .name {
    font-weight: 500;
    font-size: 16px;
    line-height: 24px;
    color: @header-color;
  }
}
</style>
