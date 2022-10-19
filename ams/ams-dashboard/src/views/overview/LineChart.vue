<template>
  <Chart :options="lineChartOptions" :loading="props.loading" :style="{ width: props.width, height: props.height }"/>
</template>

<script lang="ts" setup>
import { onMounted, reactive, watch } from 'vue'
import Chart from '@/components/echarts/Chart.vue'
import { ITimeInfo, IChartLineData } from '@/types/common.type'

const props = withDefaults(defineProps<{
  width: string,
  height: string,
  loading: boolean,
  data: IChartLineData,
  dataInfo: ITimeInfo,
  tipFormat: () => ''
}>(), {
  width: 'auto',
  height: '350px',
  data: {
    timeLine: [],
    data1: [],
    data2: []
  }
})

watch(
  () => props.data,
  (value) => {
    value && setOptionData()
  }, {
    immediate: true,
    deep: true
  }
)

watch(
  () => props.dataInfo,
  (value) => {
    value && setOptionData()
  }
)

const lineChartOptions = reactive({})

const seriesData = []

function initData() {
  const { data1 = [], data2 = [] } = props.data
  const { name, colors } = props.dataInfo
  const result = []
  if (!data1.length || !data2.length) {
    return result
  }
  seriesData.push(data1)
  seriesData.push(data2)
  for (let i = 0; i < seriesData.length; i++) {
    result.push({
      name: name[i],
      type: 'line',
      data: seriesData[i],
      lineStyle: {
        color: colors[i]
      },
      itemStyle: {
        color: colors[i]
      }
    })
  }
  return result
}
function getLegend(data = seriesData || []) {
  return {
    show: data.length > 1,
    selectedMode: 'series',
    bottom: '16',
    icon: 'rect',
    itemWidth: 10,
    itemHeight: 10,
    itemGap: 32,
    padding: [8, 0, 0, 0]
  }
}

function getGrid(data = seriesData || []) {
  return { left: 50, top: props.dataInfo.yTitle ? 52 : 20, right: 50, bottom: data.length > 1 ? 68 : 35 }
}

function setOptionData() {
  const series = initData()
  const legend = getLegend()
  const grid = getGrid()
  Object.assign(lineChartOptions || {}, {
    tooltip: {
      trigger: 'axis',
      padding: 8,
      className: 'echarts-tooltip-dark',
      textStyle: {
        color: '#fff',
        fontSize: 12,
        lineHeight: 20,
        fontWeight: 'normal'
      },
      formatter: props.tipFormat
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: props.data.timeLine,
      axisLine: {
        lineStyle: {
          color: 'rgba(0, 0, 0, 0.3)'
        }
      },
      axisLabel: {
        color: 'rgba(0, 0, 0, 0.65)',
        lineHeight: 24
      },
      axisTick: {
        show: false
      }
    },
    yAxis: {
      type: 'value',
      name: props.dataInfo.yTitle || '',
      nameTextStyle: {
        color: '#102048',
        fontWeight: 'normal',
        fontSize: 12,
        lineHeight: 20
      },
      axisLabel: {
        color: 'rgba(0, 0, 0, 0.65)'
      }
    },
    axisPointer: {
      lineStyle: { type: 'solid', color: 'rgba(0, 0, 0, 0.3)' }
    },
    legend,
    grid,
    series
  })
}

onMounted(() => {
})

</script>
