<template>
  <div class="hive-tables-wrap">
    <div v-if="!isSecondaryNav" class="tables-content">
      <div class="g-flex-jsb table-top">
        <span class="table-name g-text-nowrap">{{tableName}}</span>
        <div class="right-btn">
          <a-button type="primary" :disabled="status === upgradeStatus.upgrading" @click="upgradeTable">{{status}}</a-button>
          <p v-if="status === upgradeStatus.failed" class="fail-msg">Last Upgrading Failed</p>
        </div>
      </div>
      <div class="content">
        <a-tabs v-model:activeKey="activeKey">
          <a-tab-pane key="Details" tab="Details">
            <u-details :partitionColumnList="partitionColumnList" :schema="schema" />
          </a-tab-pane>
        </a-tabs>
      </div>
    </div>
     <u-loading v-if="loading" />
    <!-- upgrade table secondary page -->
    <router-view v-else @goBack="goBack"></router-view>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, reactive, toRefs, watch } from 'vue'
import UDetails from './Details.vue'
import { useRoute, useRouter } from 'vue-router'
import { DetailColumnItem, PartitionColumnItem, upgradeStatusMap } from '@/types/common.type'
import { getTableDetail, getUpgradeStatus } from '@/services/table.service'

export default defineComponent({
  name: 'Tables',
  components: {
    UDetails
  },
  setup() {
    const upgradeStatus = upgradeStatusMap
    const router = useRouter()
    const route = useRoute()

    const state = reactive({
      loading: false,
      activeKey: 'Details',
      status: '', // failed、upgrading、success、none
      errorMessage: '',
      isSecondaryNav: false,
      tableName: 'tableName',
      partitionColumnList: [] as PartitionColumnItem[],
      schema: [] as DetailColumnItem[]
    })

    const goBack = () => {
      state.isSecondaryNav = false
      router.back()
    }

    const params = computed(() => {
      return {
        ...route.query
      }
    })

    const getTableUpgradeStatus = async() => {
      try {
        const { catalog, db, table } = params.value
        if (!catalog || !db || !table) {
          return
        }
        state.loading = true
        const result = await getUpgradeStatus({
          ...params.value
        })
        const { status, errorMessage } = result
        state.status = status
        state.errorMessage = errorMessage || ''
      } catch (error) {
      } finally {
        state.loading = false
      }
    }

    const getHiveTableDetails = async() => {
      try {
        const { catalog, db, table } = params.value
        if (!catalog || !db || !table) {
          return
        }
        state.loading = true
        const result = await getTableDetail({
          ...params.value
        })
        const { partitionColumnList = [], schema, tableIdentifier } = result

        state.tableName = tableIdentifier?.tableName || ''

        state.partitionColumnList = partitionColumnList || []
        state.schema = schema || []
      } catch (error) {
      } finally {
        state.loading = false
      }
    }

    const init = async() => {
      await getTableUpgradeStatus()
      getHiveTableDetails()
    }

    const upgradeTable = () => {
      router.push({
        path: '/hive-tables/upgrade',
        query: {
          ...route.query
        }
      })
    }

    watch(
      () => route.query,
      (val) => {
        val?.catalog && init()
      }
    )

    watch(
      () => route.path,
      () => {
        state.isSecondaryNav = !!(route.path.indexOf('upgrade') > -1)
      }, { immediate: true }
    )

    onMounted(async() => {
      init()
    })

    return {
      ...toRefs(state),
      upgradeStatus,
      upgradeTable,
      goBack
    }
  }
})

</script>

<style lang="less" scoped>
.hive-tables-wrap {
  font-size: 14px;
  border: 1px solid #e8e8f0;
  padding: 12px 0;
  min-height: 100%;
  .table-top {
    padding: 0 12px;
    .fail-msg {
      font-size: 12px;
      width: 90px;
      color: #ff4d4f;
      text-align: center;
      text-decoration-line: underline;
      cursor: pointer;
    }
  }
  .table-name {
    font-size: 24px;
    line-height: 1.5;
    margin-right: 16px;
    max-width: 400px;
    padding-left: 12px;
  }
  :deep(.ant-tabs-nav) {
    padding-left: 12px;
    margin-bottom: 0;
  }
}
</style>
