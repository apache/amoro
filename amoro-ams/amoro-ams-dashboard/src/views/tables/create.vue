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
import { reactive, ref } from 'vue'
import type { SelectProps } from 'ant-design-vue'
import type { TableBasicInfo } from '@/types/common.type'
import { usePlaceholder } from '@/hooks/usePlaceholder'

const emit = defineEmits<{
  (e: 'goBack'): void
}>()

const formRef = ref()
const catalogOptions = ref<SelectProps['options']>([
  {
    value: 'catalog1',
    label: 'catalog1',
  },
  {
    value: 'catalog2',
    label: 'catalog2',
  },
])
const databaseOptions = ref<SelectProps['options']>([
  {
    value: 'database1',
    label: 'database1',
  },
  {
    value: 'database2',
    label: 'database2',
  },
])
const formState: TableBasicInfo = reactive({
  catalog: 'catalog1',
  database: '',
  tableName: '',
})
const placeholder = reactive(usePlaceholder())

function changeCatalog() {}
function changeDatabase() {}
function goBack() {
  emit('goBack')
}
</script>

<template>
  <div class="create-table">
    <div class="nav-bar">
      <left-outlined @click="goBack" />
      <span class="title g-ml-8">{{ $t('createTable') }}</span>
    </div>
    <div class="content">
      <div class="basic">
        <p class="title">
          {{ $t('basicInformation') }}
        </p>
        <a-form ref="formRef" :model="formState" class="label-120">
          <a-form-item name="catalog" label="Catalog" :rules="[{ required: true, message: `${placeholder.selectClPh}` }]">
            <a-select
              v-model:value="formState.catalog"
              :options="catalogOptions"
              show-search
              :placeholder="placeholder.selectClPh"
              @change="changeCatalog"
            />
          </a-form-item>
          <a-form-item name="database" label="Database" :rules="[{ required: true, message: `${placeholder.selectDBPh}` }]">
            <a-select
              v-model:value="formState.database"
              :options="databaseOptions"
              show-search
              :placeholder="placeholder.selectDBPh"
              @change="changeDatabase"
            />
          </a-form-item>
          <a-form-item name="tableName" label="Table" :rules="[{ required: true, message: `${placeholder.inputTNPh}` }]">
            <a-input v-model:value="formState.tableName" :placeholder="placeholder.inputTNPh" />
          </a-form-item>
        </a-form>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.create-table {
  .nav-bar {
    padding-left: 12px;
  }
}
</style>
