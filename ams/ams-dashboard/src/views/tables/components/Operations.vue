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

const { t } = useI18n()
const columns: IColumns[] = shallowReactive([
  { title: t('time'), dataIndex: 'ts' },
  { title: t('operation'), dataIndex: 'operation' }
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

async function getTableInfo() {
  try {
    loading.value = true
    dataSource.length = 0
    const result = await getOperations({
      ...sourceData,
      page: pagination.current,
      pageSize: pagination.pageSize
    })
    dataSource.push(...[result || []])
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
  getTableInfo()
}

onMounted(() => {
  getTableInfo()
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
