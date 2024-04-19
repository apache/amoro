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
 /-->

<template>
  <svg :class="svgClass" aria-hidden="true" @click="handleClick" :stroke="stroke" @mouseover="onMouseover" @mouseout="onMouseout">
    <use :xlink:href="iconName"></use>
  </svg>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue'

const isHover = ref<boolean>(false)
const props = defineProps<{ iconClass?: string, className?: string, isStroke?: boolean, disabled?: boolean }>()
const emit = defineEmits<{
 (e: 'click'): void
}>()

const stroke = computed(() => {
  if (props.isStroke) {
    if (props.disabled) {
      return '#999'
    }
    if (isHover.value) {
      return '#1890ff'
    } else {
      return '#333'
    }
  }
  return ''
})

const iconName = computed(() => {
  return `#icon-${props.iconClass}`
})

const svgClass = computed(() => {
  let str = 'svg-icon '
  if (props.disabled) {
    str += 'disabled '
  }
  if (props.className) {
    str += props.className
  }
  return str
})

function handleClick() {
  if (props.disabled) {
    return
  }
  emit('click')
}

function onMouseover() {
  isHover.value = true
}

function onMouseout() {
  isHover.value = false
}

</script>

<style scoped lang="less">
.svg-icon {
  width: 1em;
  height: 1em;
  vertical-align: -0.15em;
  fill: currentColor;
  overflow: hidden;
  outline-color: transparent;
  &.disabled{
    cursor: not-allowed !important;
    color: #999 !important;
  }
}
</style>
