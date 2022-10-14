<template>
  <Chart :options="lineChartOptions" :loading="loading" :style="{ width: props.width, height: props.height }"/>
</template>

<script lang="ts" setup>
import { onMounted, reactive } from 'vue'
import Chart from '@/components/echarts/Chart.vue'
// import echarts from '@/components/echarts'
import { ITimeInfo, IChartLineData } from '@/types/common.type'

const props = withDefaults(defineProps<{ 
  width: string, 
  height: string,
  loading: boolean,
  data: IChartLineData,
  dataInfo: ITimeInfo;
}>(), {
  width: 'auto',
  height: '350px',
  data: {
    timeLine: [],
    data1: [],
    data2: []
  }
})

const state = reactive({
  lineChartOptions: {},
  loading: true
})

const seriesData = []

function initData() {
  const { data1 = [], data2 = [] } = props.data;
  const { name, colors } = props.dataInfo;
  const result: any = [];
  if (!data1.length || !data2.length) {
    return result;
  }
  // seriesData = [data1, data2];
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
    });
  }
  return result;
}
function getLegend(data = seriesData) {
  return {
    show: data.length > 1,
    selectedMode: 'series',
    bottom: '16',
    icon: 'rect',
    itemWidth: 10,
    itemHeight: 10,
    itemGap: 32,
    padding: [8, 0, 0, 0]
  };
}

function getGrid(data = seriesData) {
  return { left: 50, top: this.dataInfo.yTitle ? 52 : 20, right: 50, bottom: data.length > 1 ? 68 : 35 };
}

function setOptionData() {
  state.loading = false
  const series = initData();
  const legend = getLegend();
  const grid = getGrid();
  state.lineChartOptions = {
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
      // formatter: undefined,
      // valueFormatter: undefined
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
        color: 'var(--heading-color)',
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
  };
}

onMounted(() => {
  setOptionData()
})

</script>
