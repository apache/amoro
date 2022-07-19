<template>
  <Chart :options="lineChartOptions" :loading="loading" />
</template>

<script lang="ts">
import { defineComponent, onMounted, reactive, toRefs } from 'vue'
import { useI18n } from 'vue-i18n'
import Chart from '@/components/echarts/Chart.vue'
import echarts from '@/components/echarts'

export default defineComponent({
  components: {
    Chart
  },
  setup() {
    const { d } = useI18n()
    const state = reactive({
      lineChartOptions: {},
      loading: true
    })

    const getLineChartData = () => {
      setTimeout(() => {
        state.loading = false
        const dates = []
        for (let i = 0; i < 3; i++) {
          dates.push(d(new Date().getTime(), 'long'))
        }
        state.lineChartOptions = {
          tooltip: {
            trigger: 'axis',
            formatter: function (val: any) {
              let string = ''
              for (const k in val) {
                string += `<span style="margin-bottom:0px;">${val[k].value}</span><br/>+10%`
              }
              return `<div style="color:#7880A0; border-radius: 4px;">
              <p style="margin-bottom:0px;">${val[0].axisValue}</p>
              ${string}
            </div>`
            }
          },
          xAxis: {
            type: 'category',
            data: dates,
            // hide x-axis minor tick marks
            axisTick: {
              show: false
            },
            // hide the x-axis
            axisLine: {
              show: false,
              lineStyle: {
                color: '#B8B8C8'
              }
            },
            // hide x-axis values
            axisLabel: {
              show: false
            }
          },
          yAxis: {
            show: false,
            type: 'value',
            scale: true,
            splitLine: {
              show: false,
              lineStyle: {
                type: 'dashed',
                color: '#E8E8F0'
              }
            },
            axisTick: {
              // y-axis tick marks
              show: false
            },
            axisLine: {
              show: false,
              lineStyle: {
                color: '#B8B8C8'
              }
            },
            axisLabel: {
              show: false
            }
          },
          series: [
            {
              data: [2, 21, 25],
              type: 'line',
              areaStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: 'rgba(56, 125, 255, 0.2)' },
                  { offset: 0.8, color: 'rgba(56, 125, 255, 0.1)' },
                  { offset: 1, color: 'rgba(56, 125, 255, 0)' }
                ])
              }
            }
          ]
        }
      }, 1000)
    }

    onMounted(() => {
      getLineChartData()
    })

    return {
      ...toRefs(state)
    }
  }
})
</script>
