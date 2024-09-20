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

<script lang="ts" setup>
import { Modal as AModal } from 'ant-design-vue'
import { ref } from 'vue'

const props = defineProps<{ msg: string }>()
const emit = defineEmits<{
  (e: 'cancel'): void
}>()
const open = ref(true)
</script>

<template>
  <AModal
    v-model:open="open"
    :width="560"
    :title="$t('errorMessage')"
    :footer="null"
    class="upgrade-error"
    @cancel="emit('cancel')"
  >
    <p class="msg">
      {{ props.msg }}
    </p>
  </AModal>
</template>

<style lang="less" scoped>
.upgrade-error {
  .msg {
    min-height: 172px;
    max-height: 320px;
    overflow-y: auto;
  }
}
</style>
