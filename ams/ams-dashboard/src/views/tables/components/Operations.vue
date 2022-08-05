<template>
  <div class="table-operations">
    <a-table
      rowKey="partiton"
      :columns="columns"
      :data-source="dataSource"
      :pagination="pagination"
      @change="change"
      :loading="loading"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'operation'">
          <span class="text-active g-max-line-3" @click="viewDetail(record)">
            {{ record.operation }}
          </span>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref, shallowReactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { usePagination } from '@/hooks/usePagination'
import { IColumns, OperationItem } from '@/types/common.type'
import { getOperations } from '@/services/table.service'
import { useRoute } from 'vue-router'
import { dateFormat } from '@/utils'

const { t } = useI18n()
const columns: IColumns[] = shallowReactive([
  { title: t('time'), dataIndex: 'ts', width: '30%' },
  { title: t('operation'), dataIndex: 'operation', scopedSlots: { customRender: 'operation' } }
])

const dataSource = reactive<OperationItem[]>([])

const loading = ref<boolean>(false)
const pagination = reactive(usePagination())
const route = useRoute()
const query = route.query
const sourceData = reactive({
  catalog: '',
  db: '',
  table: '',
  ...query
})

async function getOperationInfo() {
  try {
    loading.value = true
    dataSource.length = 0
    const result = await getOperations({
      ...sourceData,
      page: pagination.current,
      pageSize: pagination.pageSize
    });
    (result?.list || []).forEach((ele: OperationItem) => {
      ele.ts = ele.ts ? dateFormat(ele.ts) : ''
      dataSource.push(ele)
    })
  } catch (error) {
  } finally {
    loading.value = false
  }
}

function change({ current = 1, pageSize = 25 } = pagination) {
  pagination.current = current
  if (pageSize !== pagination.pageSize) {
    pagination.current = 1
  }
  pagination.pageSize = pageSize
  getOperationInfo()
}

function viewDetail() {}

onMounted(() => {
  getOperationInfo()
})

</script>

<style lang="less" scoped>
.table-operations {
  padding: 12px;
  .text-active {
    color: #1890ff;
    cursor: pointer;
  }
}
</style>
