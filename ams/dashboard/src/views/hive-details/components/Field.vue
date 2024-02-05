
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
  <div class="field-wrap">
    <a-table
      :loading="loading"
      class="ant-table-common"
      :columns="fieldsColumns"
      :data-source="props.fields"
      :pagination="false"
      >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'primaryKey'">
          <a-checkbox v-model:checked="record.checked"></a-checkbox>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { shallowReactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { DetailColumnItem } from '@/types/common.type'

const { t } = useI18n()

const props = defineProps<{ fields: DetailColumnItem[], loading: boolean }>()

const fieldsColumns = shallowReactive([
  { dataIndex: 'field', title: t('field'), ellipsis: true },
  { dataIndex: 'type', title: t('type'), ellipsis: true },
  { dataIndex: 'comment', title: t('description'), ellipsis: true },
  { dataIndex: 'primaryKey', title: t('primaryKey'), scopedSlots: { customRender: 'primaryKey' } }
])

defineExpose({
  getPkname() {
    return props.fields.filter((ele: DetailColumnItem) => ele.checked)
      .map((ele: DetailColumnItem) => ({ fieldName: ele.field || '' }))
  }
})

</script>
<style lang="less" scoped>
.field-wrap {
}
</style>
