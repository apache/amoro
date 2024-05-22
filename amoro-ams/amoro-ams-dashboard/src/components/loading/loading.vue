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

<script setup lang="ts">
import { onBeforeUnmount, ref } from 'vue'

// name: 'ULoading',
// props: {
//   loadingText: {
//     type: String,
//     default: 'Loading'
//   },
//   fullscreen: {
//     type: Boolean,
//     default: false
//   }
// },
const props = defineProps({
  loadingText: {
    type: String,
    default: 'Loading'
  },
  fullscreen: {
    type: Boolean,
    default: false
  }
})
const visible = ref<boolean>(true)

const hide = () => {
  visible.value = false
}

onBeforeUnmount(() => {
  visible.value = false;
})

</script>

<template>
  <div v-show="visible" v-if="visible" ref="loadingRef" class="u-loading" :class="{ 'fullscreen': props.fullscreen }">
    <a-spin :tip="props.loadingText" />
  </div>
</template>

<style lang="less" scoped>
.u-loading {
  position: absolute;
  z-index: 999;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, .2);

  &.fullscreen {
    position: fixed;
  }

  &-img {
    width: 30px;
    height: 30px;
  }

  &-text {
    margin-top: 8px;
    user-select: none;
    color: #fff;
  }
}</style>
