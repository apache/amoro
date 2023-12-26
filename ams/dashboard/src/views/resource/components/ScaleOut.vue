
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
  <a-modal
    :visible="true"
    :title="$t('scaleOut')"
    :confirmLoading="confirmLoading"
    :closable="false"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-form ref="formRef" :model="formState" class="label-120">
      <a-form-item name="resourceGroup" :label="$t('resourceGroup')">
        {{ formState.resourceGroup }}
      </a-form-item>
      <a-form-item
        name="parallelism"
        :label="$t('parallelism')"
        :rules="[{ required: true, message: `${placeholder.parallelismPh}` }]"
      >
        <a-input
          v-model:value="formState.parallelism"
          type="number"
          :placeholder="placeholder.parallelismPh"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { usePlaceholder } from '@/hooks/usePlaceholder'
import { IIOptimizeGroupItem } from '@/types/common.type'
import { scaleoutResource } from '@/services/optimize.service'
// import { message } from 'ant-design-vue'

interface FormState {
  resourceGroup: undefined | string;
  parallelism: number;
}

const emit = defineEmits<{
  (e: 'cancel'): void;
  (e: 'refresh'): void;
}>()

const props = defineProps<{ groupRecord: IIOptimizeGroupItem }>()
const confirmLoading = ref<boolean>(false)
const placeholder = reactive(usePlaceholder())
const formRef = ref()
const formState: FormState = reactive({
  resourceGroup: props.groupRecord?.name || '',
  parallelism: 1
})

function handleOk() {
  formRef.value
    .validateFields()
    .then(async () => {
      confirmLoading.value = true
      await scaleoutResource({
        optimizerGroup: formState.resourceGroup || '',
        parallelism: Number(formState.parallelism)
      })
      formRef.value.resetFields()
      emit('cancel')
      emit('refresh')
      confirmLoading.value = false
    })
    .catch(() => {
      confirmLoading.value = false
    })
}

function handleCancel() {
  formRef.value.resetFields()
  emit('cancel')
}
onMounted(() => {
})
</script>
