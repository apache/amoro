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
import { shallowReactive } from 'vue'
import { useI18n } from 'vue-i18n'
import type { DetailColumnItem } from '@/types/common.type'

const props = defineProps<{ partitionFields: DetailColumnItem[], loading: boolean }>()

const { t } = useI18n()

const partitionFieldsColumns = shallowReactive([
  { dataIndex: 'field', title: t('field'), ellipsis: true },
  { dataIndex: 'type', title: t('type'), ellipsis: true },
  { dataIndex: 'comment', title: t('description'), ellipsis: true },
])
</script>

<template>
  <div class="partition-field-wrap">
    <a-table
      :loading="loading"
      class="ant-table-common"
      :columns="partitionFieldsColumns"
      :data-source="props.partitionFields"
      :pagination="false"
    />
  </div>
</template>
