<template>
  <div class="top-list-wrap">
    <div class="common-header">
      <span class="name">{{`${$t('top10')} ${$t('tables')}`}}</span>
      <a-select
        v-model:value="filterType"
        :options="filterOps"
        @change="onChangeFilter"
      />
    </div>
    <a-table
      :columns="columns"
      :data-source="topList"
      :pagination="false"
      :loading="loading"
      :scroll="{ y: scrollY }"
      rowKey="tableName"
      >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'tableNameOnly'">
          <span class="primary-link" :title="record.tableName" @click="goTableDetail(record)">
            {{ record.tableNameOnly }}
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
import { ITopTableItem } from '@/types/common.type'
import { getTopTables } from '@/services/overview.service'
import { useI18n } from 'vue-i18n'
import { bytesToSize } from '@/utils'
import { useRouter } from 'vue-router'

const { t } = useI18n()
const router = useRouter()

const scrollY = ref<number>(302)
const loading = ref<boolean>(false)
const filterType = ref<string>('size')
const filterOps = reactive([
  { value: 'size', label: t('size') },
  { value: 'files', label: t('files') }
])
const columns = shallowReactive([
  { dataIndex: 'index', title: t('numIndex'), ellipsis: true, width: 60 },
  { dataIndex: 'tableNameOnly', title: t('table'), ellipsis: true, scopedSlots: { customRender: 'tableNameOnly' } },
  { dataIndex: 'displaySize', title: t('size'), ellipsis: true, width: '20%' },
  { dataIndex: 'fileCnt', title: t('fileCnt'), ellipsis: true, width: '20%' }
])
const topList = reactive<ITopTableItem[]>([])

async function getTables () {
  try {
    topList.length = 0
    loading.value = true
    const params = {
      orderBy: filterType.value
    }
    const result = await getTopTables(params);
    (result || []).forEach((p: ITopTableItem, index: number) => {
      p.index = index + 1
      p.tableNameOnly = (p.tableName || '').split('.')[2]
      p.displaySize = bytesToSize(p.size)
      topList.push(p)
    })
  } catch (error) {
  } finally {
    loading.value = false
  }
}

function onChangeFilter() {
  getTables()
}

function goTableDetail (record: ITopTableItem) {
  try {
    const { catalog, database, tableName } = (record.tableName || '').split('.')
    router.push({
      path: '/tables',
      query: {
        catalog,
        db: database,
        table: tableName
      }
    })
  } catch (error) {
  }
}

onMounted(() => {
  getTables()
})
</script>
<style lang="less" scoped>
.top-list-wrap {
  display: flex;
  flex: 1;
  flex-direction: column;
  padding: 0 24px 16px;
  box-shadow: 0 1px 4px rgb(0 21 41 / 8%);
  .primary-link {
    color: @primary-color;
    &:hover {
      cursor: pointer;
    }
  }
}
</style>
