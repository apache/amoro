
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
  <div class="sql-log">
    <div v-html="outputLog" style="white-space: pre-wrap;font-size: 12px;"></div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'

const outputLog = ref<string>('')

defineExpose({
  initData(log: string) {
    outputLog.value = log
  }
})

</script>
<style lang="less" scoped>
.sql-log {
  padding: 16px 16px 0;
  overflow: auto;
  height: 100%;
}
</style>
