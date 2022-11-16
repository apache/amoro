<template>
  <div class="resource-occupation g-mr-16">
    <div class="common-header">
      <span class="name">{{`${$t('resourceUsage')} (%)`}}</span>
      <TimeSelect @timeChange="handleTimeChange" />
    </div>
    <LineChart :data="chartLineData" :dataInfo="dataInfo" :tipFormat="cpuFormat" :loading="loading" />
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import LineChart from './LineChart.vue'
import TimeSelect from './TimeSelect.vue'
import { getOptimizeResource } from '@/services/overview.service'
import { ITimeInfo, IChartLineData } from '@/types/common.type'

const { t } = useI18n()
const loading = ref<boolean>(false)
const chartLineData = reactive<IChartLineData>(
  {
    timeLine: [],
    data1: [],
    data2: []
  }
)
const dataInfo = reactive<ITimeInfo>({
  yTitle: '',
  colors: ['#5B8FF9', '#5AD8A6'],
  name: [t('cpu'), t('memory')]
})

const startTime = ref<string>()
const endTime = ref<string>()

const hoverDatas = reactive([])

function handleTimeChange(val) {
  const { start, end } = val
  startTime.value = start
  endTime.value = end
  getLineChartData()
}

async function getLineChartData() {
  const res = await getOptimizeResource({
    startTime: startTime.value,
    endTime: endTime.value
  })
  hoverDatas.length = 0
  const { timeLine, usedCpu, usedMem, usedCpuPercent, usedMemPercent, usedCpuDivision = [], usedMemDivision = [] } = res
  chartLineData.timeLine = timeLine
  chartLineData.data1 = usedCpu
  chartLineData.data2 = usedMem
  hoverDatas.push([usedCpuPercent, usedCpuDivision], [usedMemPercent, usedMemDivision])
}

function cpuFormat(data) {
  let str = `<span style="font-size: 10px">${data[0].axisValue}</span><br/>`
  for (let i = 0; i < data.length; i++) {
    str += `<span style="display: inline-block;background-color:${data[i].color}; margin-right: 6px; width: 6px;height: 6px;"></span>${data[i].seriesName}: ${hoverDatas[i][0][data[i].dataIndex]}<br/><span style="line-height: 16px;margin-left: 12px;">${hoverDatas[i][1][data[i].dataIndex]}</span><br/>`
  }
  return str
}

onMounted(() => {
})
</script>
<style lang="less" scoped>
.resource-occupation {
  padding: 0 24px 16px;
  display: flex;
  width: 50%;
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
