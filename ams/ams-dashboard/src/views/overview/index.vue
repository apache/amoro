<template>
  <div class="overview-wrap">
    <div class="echarts-wrap">
      <div class="line-chart">
        <line-chart key='chart1' />
      </div>
      <div class="line-chart">
        <line-chart key='chart2' />
      </div>
      <div class="line-chart">
        <line-chart key='chart3' />
      </div>
      <div class="line-chart">
        <line-chart key='chart4' />
      </div>
    </div>
    <div class="top-list">
      <div class="filter-options">
        <a-select
          v-model:value="filterValue"
          style="width: 220px"
          @change="handleSelectChange"
        >
          <a-select-option value="size">{{$t('size')}} TOP 10</a-select-option>
          <a-select-option value="time">{{$t('lastCommitTime')}} TOP 10</a-select-option>
        </a-select>
        <a-button type="primary">Create</a-button>
      </div>
      <a-table
        rowKey="order"
        :columns="columns"
        :data-source="dataSource"
        :pagination="pagination"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { usePagination } from '@/hooks/usePagination'
import { IColumns } from '@/types/common.type'
import LineChart from './LineChart.vue'

export default defineComponent({
  name: 'Overview',
  components: {
    LineChart
  },
  setup() {
    const { t, d } = useI18n()
    const columns: IColumns[] = [
      {
        title: t('order'),
        dataIndex: 'order'
      },
      {
        title: t('catalog'),
        dataIndex: 'catalog'
      },
      {
        title: t('database'),
        dataIndex: 'database'
      },
      {
        title: t('table'),
        dataIndex: 'table'
      },
      {
        title: t('size'),
        dataIndex: 'size'
      },
      {
        title: t('file'),
        dataIndex: 'file'
      },
      {
        title: t('averageFileSize'),
        dataIndex: 'averageFileSize'
      },
      {
        title: t('lastCommitTime'),
        dataIndex: 'lastCommitTime'
      },
      {
        title: t('quota'),
        dataIndex: 'quota'
      }
    ]
    const dataSource = [
      {
        key: '1',
        order: '1',
        catalog: 'catalog1',
        database: 'database1',
        table: 'table1',
        size: '10G',
        file: 'file1',
        averageFileSize: '100MB',
        lastCommitTime: d(new Date(1649903533281), 'long'),
        quota: 2
      }
    ]
    const pagination = reactive(usePagination())
    const filterValue = ref('size')

    onMounted(() => {
    })
    const handleSelectChange = () => {
    }
    return {
      columns,
      dataSource,
      pagination,
      filterValue,
      handleSelectChange
    }
  }
})

</script>

<style lang="less" scoped>
.echarts-wrap {
  display: flex;
  align-items: center;
  .line-chart {
    flex: 1;
  }
}
  .top-list {
    padding: 0 12px;
    .filter-options {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
    }
  }
</style>
