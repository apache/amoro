
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

<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { Modal as AModal } from 'ant-design-vue'
import { usePlaceholder } from '@/hooks/usePlaceholder'
import { createOptimizerResource, getOptimizerGroups } from '@/services/optimize.service'

interface FormState {
  resourceGroup: string
  parallelism: number
}

const emit = defineEmits<{ (e: 'cancel'): void; (e: 'refresh'): void }>()

const confirmLoading = ref<boolean>(false)
const placeholder = reactive(usePlaceholder())
const formRef = ref()
const formState: FormState = reactive({
  resourceGroup: '',
  parallelism: 1,
})

const resourceGroupOptions = ref<{ label: string, value: string }[]>([])

async function loadResourceGroups() {
  try {
    const response = await getOptimizerGroups()
    resourceGroupOptions.value = response.map((group: any) => ({
      label: group.optimizerGroupName,
      value: group.optimizerGroupName,
    }))
    console.log('Parsed resourceGroupOptions:', resourceGroupOptions.value)
  } catch (error) {
    console.error('Failed to load resource groups:', error)
  }
}

function handleOk() {
  formRef.value
      .validateFields()
      .then(async () => {
        confirmLoading.value = true
        await createOptimizerResource({
          optimizerGroup: formState.resourceGroup || '',
          parallelism: Number(formState.parallelism),
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
  loadResourceGroups()
})
</script>

<template>
  <AModal
      :open="true"
      :title="$t('createOptimizer')"
      :confirm-loading="confirmLoading"
      :closable="false"
      @ok="handleOk"
      @cancel="handleCancel"
  >
    <a-form ref="formRef" :model="formState" class="label-120">
      <a-form-item
          name="resourceGroup"
          :label="$t('resourceGroup')"
          :rules="[{ required: true, message: `${placeholder.resourceGroupPh}` }]"
      >
        <a-select
            v-model:value="formState.resourceGroup"
            :placeholder="placeholder.resourceGroupPh"
        >
          <a-select-option
              v-for="option in resourceGroupOptions"
              :key="option.value"
              :value="option.value"
          >
            {{ option.label }}
          </a-select-option>
        </a-select>
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
  </AModal>
</template>