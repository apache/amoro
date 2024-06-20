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

<script lang="ts">
import { defineComponent, onBeforeUnmount, onMounted, ref, toRefs, watch } from 'vue'
import echarts from './index'

export default defineComponent({
  props: {
    width: {
      type: String,
      default: 'auto',
    },
    height: {
      type: String,
      default: '350px',
    },
    loading: {
      type: Boolean,
      default: false,
    },
    options: {
      type: Object,
      default: () => {},
    },
  },
  setup(props) {
    let echartsInst: any = null
    const { options } = toRefs(props)
    const echart = ref()

    const echartsInit = () => {
      echartsInst = echarts.init(echart.value)
      echartsInst.setOption({
        ...options.value,
      })
    }
    const echartsOptionsUpdate = () => {
      echartsInst.setOption({
        ...options.value,
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
      (value) => {
        value && echartsOptionsUpdate()
      },
      {
        deep: true,
      },
    )

    onBeforeUnmount(() => {
      window.removeEventListener('resize', resize)
    })

    onMounted(() => {
      window.addEventListener('resize', resize)
      echartsInit()
    })

    return {
      echart,
    }
  },
})
</script>

<template>
  <a-spin :spinning="loading" class="echarts-loading">
    <div ref="echart" :style="{ width, height }" class="timeline-echarts" />
  </a-spin>
</template>

<style lang="less">
.echarts-loading {
  width: 100% !important;
}
.timeline-echarts {
  .echarts-tooltip-dark {
    background-color: rgba(0,0,0,.7) !important;
    line-height: 20px !important;
    border: 1px solid #E9EBF1 !important;
  }
}
</style>
