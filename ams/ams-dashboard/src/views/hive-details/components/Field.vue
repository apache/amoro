<template>
  <div class="field-wrap">
    <a-table
      class="ant-table-common"
      :columns="fieldsColumns"
      :data-source="props.fields"
      :pagination="false"
      >
      <template #bodyCell="{ column }">
        <template v-if="column.dataIndex === 'primaryKey'">
          <a-checkbox @click="onChangeCheckBox"></a-checkbox>
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

const props = defineProps<{ fields: DetailColumnItem[] }>()

const fieldsColumns = shallowReactive([
  { dataIndex: 'field', title: t('field'), ellipsis: true },
  { dataIndex: 'type', title: t('type'), ellipsis: true },
  { dataIndex: 'description', title: t('description'), ellipsis: true },
  { dataIndex: 'primaryKey', title: t('primaryKey'), scopedSlots: { customRender: 'primaryKey' } }
])

function onChangeCheckBox() {}

</script>
<style lang="less" scoped>
.field-wrap {
}
</style>
