<template>
  <div class="partition-field-wrap">
    <a-table
      class="ant-table-common"
      :columns="partitionFieldsColumns"
      :data-source="props.partitionFields"
      :pagination="false"
      >
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { shallowReactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { IField } from '@/types/common.type'

const { t } = useI18n()

const props = defineProps<{ partitionFields: IField[] }>()

const partitionFieldsColumns = shallowReactive([
  { dataIndex: 'field', title: t('field'), ellipsis: true },
  { dataIndex: 'type', title: t('type'), ellipsis: true },
  { dataIndex: 'description', title: t('description'), ellipsis: true }
])

</script>
<style lang="less" scoped>
.partition-field-wrap {
}
</style>
