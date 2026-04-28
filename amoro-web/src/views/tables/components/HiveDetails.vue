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

<script lang="ts">
import { computed, defineComponent, onBeforeUnmount, onMounted, reactive, ref, toRefs, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import UDetails from '@/views/hive-details/components/Details.vue'
import ErrorMsg from '@/views/hive-details/components/ErrorMsg.vue'
import type { DetailColumnItem } from '@/types/common.type'
import { upgradeStatusMap } from '@/types/common.type'
import { getHiveTableDetail, getUpgradeStatus } from '@/services/table.service'
import { canManageTable } from '@/utils/permission'

const storageTableKey = 'easylake-menu-catalog-db-table'

export default defineComponent({
  name: 'HiveDetails',
  components: {
    UDetails,
    ErrorMsg,
  },
  setup() {
    const upgradeStatus = upgradeStatusMap
    const statusInterval = ref<number>()
    const router = useRouter()
    const route = useRoute()
    const { t } = useI18n()
    const writable = computed(() => canManageTable())

    const state = reactive({
      loading: false,
      showErrorMsg: false,
      activeKey: 'Details',
      status: '',
      displayStatus: '',
      errorMessage: '',
      tableName: 'tableName',
      partitionColumnList: [] as DetailColumnItem[],
      schema: [] as DetailColumnItem[],
    })

    const params = computed(() => ({ ...route.query }))

    const updateStoredTableType = (type: string) => {
      const { catalog, db, table } = params.value
      if (!catalog || !db || !table) {
        return
      }
      localStorage.setItem(storageTableKey, JSON.stringify({
        catalog,
        database: db,
        tableName: table,
        type,
      }))
    }

    const getHiveTableDetails = async () => {
      try {
        const { catalog, db, table } = params.value
        if (!catalog || !db || !table) {
          return
        }
        state.loading = true
        const result = await getHiveTableDetail({
          ...params.value,
        })
        const { partitionColumnList = [], schema, tableIdentifier } = result

        state.tableName = tableIdentifier?.tableName || ''
        state.partitionColumnList = partitionColumnList || []
        state.schema = schema || []
      }
      catch (error) {
        // keep current view state when the Hive detail request fails
      }
      finally {
        state.loading = false
      }
    }

    const getTableUpgradeStatus = async (hideLoading = false) => {
      try {
        statusInterval.value && clearTimeout(statusInterval.value)
        const { catalog, db, table } = params.value
        if (!catalog || !db || !table) {
          return
        }
        !hideLoading && (state.loading = true)
        const result = await getUpgradeStatus({
          ...params.value,
        })
        const { status, errorMessage } = result
        state.status = status
        state.displayStatus = status === upgradeStatusMap.upgrading ? t('upgrading') : t('upgrade')
        state.errorMessage = errorMessage || ''
        if (status === upgradeStatusMap.upgrading) {
          statusInterval.value = window.setTimeout(() => {
            getTableUpgradeStatus(true)
          }, 1500)
        }
        else if (status === upgradeStatusMap.success) {
          updateStoredTableType('ARCTIC')
          router.replace({
            path: '/tables',
            query: {
              ...route.query,
              type: 'ARCTIC',
            },
          })
        }
        else {
          getHiveTableDetails()
        }
      }
      finally {
        !hideLoading && (state.loading = false)
      }
    }

    const init = async () => {
      await getTableUpgradeStatus()
    }

    const upgradeTable = () => {
      router.push({
        path: '/tables/hive-upgrade',
        query: {
          ...route.query,
          type: 'HIVE',
        },
      })
    }

    watch(
      () => route.query,
      (val, old) => {
        const { catalog, db, table } = val
        if (route.path === '/tables' && val.type === 'HIVE' && (catalog !== old.catalog || db !== old.db || table !== old.table)) {
          init()
        }
      },
    )

    onBeforeUnmount(() => {
      clearTimeout(statusInterval.value)
    })

    onMounted(() => {
      init()
    })

    return {
      ...toRefs(state),
      upgradeStatus,
      upgradeTable,
      writable,
    }
  },
})
</script>

<template>
  <div class="hive-tables-wrap">
    <div class="tables-content">
      <div class="g-flex-jsb table-top">
        <span :title="tableName" class="table-name g-text-nowrap">{{ tableName }}</span>
        <div class="right-btn">
          <a-button v-if="writable" type="primary" :disabled="status === upgradeStatus.upgrading" @click="upgradeTable">
            {{ displayStatus }}
          </a-button>
          <p v-if="status === upgradeStatus.failed" class="fail-msg" @click="showErrorMsg = true">
            {{ $t('lastUpgradingFailed') }}
          </p>
        </div>
      </div>
      <div class="content">
        <a-tabs v-model:activeKey="activeKey">
          <a-tab-pane key="Details" tab="Details">
            <UDetails :partition-column-list="partitionColumnList" :schema="schema" />
          </a-tab-pane>
        </a-tabs>
      </div>
    </div>
    <u-loading v-if="loading" />
    <ErrorMsg v-if="showErrorMsg" :msg="errorMessage" @cancel="showErrorMsg = false" />
  </div>
</template>

<style lang="less" scoped>
.hive-tables-wrap {
  display: flex;
  height: 100%;
  flex: 1;
  flex-direction: column;
  .table-top {
    padding: 0 12px;
    .right-btn {
      position: relative;
    }
    .fail-msg {
      position: absolute;
      bottom: -30px;
      right: 0;
      z-index: 1;
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
    padding-left: 24px;
    margin-bottom: 0;
  }
}
</style>
