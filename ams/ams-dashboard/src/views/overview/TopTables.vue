<template>
  <div class="top-list-wrap">
    <div class="common-header">
      <span class="name">{{`${$t('resourceUsage')} ${$t('tables')}`}}</span>
      <a-select :options="timeOptions"></a-select>
    </div>
    <a-table
      class="ant-table-common"
      :columns="optimizerColumns"
      :data-source="optimizersList"
      :pagination="false"
      :loading="loading"
      >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'tableName'">
          <span class="primary-link" :title="record.tableName" @click="goTableDetail(record)">
            {{ record.tableName }}
          </span>
        </template>
        <template v-if="column.dataIndex === 'durationDisplay'">
          <span>
            {{ record.durationDisplay }}
          </span>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref, shallowReactive } from 'vue'
import { IOptimizeResourceTableItem, IOptimizeTableItem } from '@/types/common.type'
import { getOptimizerResourceList } from '@/services/optimize.service'
import { useI18n } from 'vue-i18n'
import { usePagination } from '@/hooks/usePagination'
import { formatMS2DisplayTime } from '@/utils'
import { useRouter } from 'vue-router'

const { t } = useI18n()
const router = useRouter()

const props = defineProps<{ curGroupName: string, type: string, needFresh: boolean }>()

const loading = ref<boolean>(false)

const optimizerColumns = shallowReactive([
  { dataIndex: 'tableName', title: t('table'), ellipsis: true },
  { dataIndex: 'jobStatus', title: t('status'), ellipsis: true },
  { dataIndex: 'durationDisplay', title: t('duration'), ellipsis: true }
])
const pagination = reactive(usePagination())
const optimizersList = reactive<IOptimizeResourceTableItem[]>([])

async function getOptimizersList () {
  try {
    optimizersList.length = 0
    loading.value = true
    const params = {
      optimizerGroup: props.curGroupName,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const result = await getOptimizerResourceList(params)
    const { list } = result;
    (list || []).forEach((p: IOptimizeResourceTableItem) => {
      p.durationDisplay = formatMS2DisplayTime(p.duration || 0)
      optimizersList.push(p)
    })
  } catch (error) {
  } finally {
    loading.value = false
  }
}

function goTableDetail (record: IOptimizeTableItem) {
  const { catalog, database, tableName } = record.tableIdentifier
  router.push({
    path: '/tables',
    query: {
      catalog,
      db: database,
      table: tableName
    }
  })
}

onMounted(() => {
  getOptimizersList()
})
</script>
<style lang="less" scoped>
.optimizing-list-wrap {
  display: flex;
  flex: 1;
  .primary-link {
    color: @primary-color;
    &:hover {
      cursor: pointer;
    }
  }
}
</style>
