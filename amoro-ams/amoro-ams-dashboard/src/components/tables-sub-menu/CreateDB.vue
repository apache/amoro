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
  <a-modal :visible="visible" :title="$t('createDatabase')" @ok="handleOk" @cancel="handleCancel">
    <a-form ref="formRef" :model="formState" class="label-120">
      <a-form-item name="catalog" :label="$t('catalog')" :rules="[{ required: true, message: `${placeholder.selectClPh}` }]">
        <a-select
          v-model:value="formState.catalog"
          :options="catalogOptions"
          :placeholder="placeholder.selectClPh"
        />
      </a-form-item>
      <a-form-item name="dbname" :label="$t('databaseName')" :rules="[{ required: true, message: `${placeholder.inputDBPh}` }]">
        <a-input v-model:value="formState.dbname" :placeholder="placeholder.inputDBPh" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script lang="ts">
import { defineComponent, reactive, ref } from 'vue'
import { usePlaceholder } from '@/hooks/usePlaceholder'

interface FormState {
  catalog: string | undefined
  dbname: string
}

export default defineComponent({
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    catalogOptions: {
      type: Array,
      default: () => []
    }
  },
  emits: ['cancel'],
  setup(props, { emit }) {
    const placeholder = reactive(usePlaceholder())

    const formRef = ref()
    const formState:FormState = reactive({
      catalog: undefined,
      dbname: ''
    })
    const handleOk = () => {
      formRef.value
        .validateFields()
        .then(() => {
          formRef.value.resetFields()
          emit('cancel')
        })
        .catch((info: Error) => {
          console.log('Validate Failed:', info)
        })
    }
    const handleCancel = () => {
      formRef.value.resetFields()
      emit('cancel')
    }

    return {
      formRef,
      formState,
      placeholder,
      handleOk,
      handleCancel
    }
  }
})
</script>
