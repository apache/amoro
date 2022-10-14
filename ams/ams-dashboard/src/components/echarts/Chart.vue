<template>
  <a-spin :spinning="loading">
    <div ref="echart" :style="{ width: width, height: height }"></div>
  </a-spin>
</template>
<script lang="ts">
import { defineComponent, onMounted, onBeforeUnmount, watch, ref, toRefs, reactive } from 'vue'
import echarts from './index'
// import * as echarts from 'echarts'

export default defineComponent({
  props: {
    width: {
      type: String,
      default: 'auto'
    },
    height: {
      type: String,
      default: '350px'
    },
    loading: {
      type: Boolean,
      default: false
    },
    options: {
      type: Object,
      default: () => {}
    }
  },
  setup(props) {
    let echartsInst: any = null
    const { options } = toRefs(props)
    const state = reactive({
      echart: ref()
    })
    const echartsInit = () => {
      echartsInst = echarts.init(state.echart)
      echartsInst.setOption({
        ...props.options
      })
      echartsInst.on('mouseover', {}, () => {

      })
      echartsInst.on('mouseout', {}, () => {

      })
    }
    const echartsOptionsUpdate = () => {
      echartsInst.setOption({
        ...props.options
      })
      echartsInst.resize()
    }

    const resize = () => {
      if (echartsInst) {
        echartsInst.resize()
      }
    }

    watch(
      () => options.value,
      (options) => {
        if (options) {
          echartsOptionsUpdate()
        }
      }
    )

    onBeforeUnmount(() => {
      window.removeEventListener('resize', resize)
    })

    onMounted(() => {
      window.addEventListener('resize', resize)
      echartsInit()
    })

    return {
      ...toRefs(state)
    }
  }
})
</script>
