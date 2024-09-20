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
import { computed, defineComponent, nextTick, onMounted, reactive, ref, shallowReactive, toRefs, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import UDetails from './components/Details.vue'
import UFiles from './components/Files.vue'
import UOperations from './components/Operations.vue'
import USnapshots from './components/Snapshots.vue'
import UOptimizing from './components/Optimizing.vue'
import useStore from '@/store/index'
import type { IBaseDetailInfo } from '@/types/common.type'

export default defineComponent({
  name: 'Tables',
  components: {
    UDetails,
    UFiles,
    UOperations,
    USnapshots,
    UOptimizing,
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const store = useStore()

    const detailRef = ref()

    const tabConfigs = shallowReactive([
      { key: 'Snapshots', label: 'snapshots' },
      { key: 'Optimizing', label: 'optimizing' },
      { key: 'Operations', label: 'operations' },
    ])

    const state = reactive({
      activeKey: 'Details',
      isSecondaryNav: false,
      baseInfo: {
        optimizingStatus: '',
        records: '',
        tableType: '',
        tableName: '',
        createTime: '',
        tableFormat: '',
        hasPartition: false,
        healthScore: -1,
      } as IBaseDetailInfo,
      detailLoaded: false,
    })

    const isIceberg = computed(() => {
      return state.baseInfo.tableType === 'ICEBERG'
    })

    const setBaseDetailInfo = (baseInfo: IBaseDetailInfo) => {
      state.detailLoaded = true
      state.baseInfo = { ...baseInfo }
    }

    const onChangeTab = (key: string | number) => {
      const query = { ...route.query }
      query.tab = key.toString()
      router.replace({ query: { ...query } })
    }

    const hideTablesMenu = () => {
      store.updateTablesMenu(false)
    }

    const goBack = () => {
      state.isSecondaryNav = false
      router.back()
    }

    watch(
      () => route.path,
      () => {
        state.isSecondaryNav = !!(route.path.includes('create'))
      },
      { immediate: true },
    )

    watch(
      () => route.query,
      (value, oldVal) => {
        const { catalog, db, table } = value
        const { catalog: oldCatalog, db: oldDb, table: oldTable } = oldVal
        if (`${catalog}${db}${table}` !== `${oldCatalog}${oldDb}${oldTable}`) {
          state.activeKey = 'Details'
          return
        }
        state.activeKey = value.tab as string
      },
    )

    onMounted(() => {
      state.activeKey = (route.query?.tab as string) || 'Details'
      nextTick(() => {
        if (detailRef.value) {
          detailRef.value.getTableDetails()
        }
      })
    })

    return {
      ...toRefs(state),
      detailRef,
      tabConfigs,
      store,
      isIceberg,
      setBaseDetailInfo,
      hideTablesMenu,
      goBack,
      onChangeTab,
    }
  },
})
</script>

<template>
  <div class="tables-wrap">
    <div v-if="!isSecondaryNav" class="tables-content">
      <div class="g-flex-jsb">
        <div class="g-flex-col">
          <div class="g-flex">
            <span :title="baseInfo.tableName" class="table-name g-text-nowrap">{{ baseInfo.tableName }}</span>
          </div>
          <div class="table-info g-flex-ac">
            <p>{{ $t('optimizingStatus') }}: <span class="text-color">{{ baseInfo.optimizingStatus }}</span></p>
            <a-divider type="vertical" />
            <p>{{ $t('records') }}: <span class="text-color">{{ baseInfo.records }}</span></p>
            <a-divider type="vertical" />
            <template v-if="!isIceberg">
              <p>{{ $t('createTime') }}: <span class="text-color">{{ baseInfo.createTime }}</span></p>
              <a-divider type="vertical" />
            </template>
            <p>{{ $t('tableFormat') }}: <span class="text-color">{{ baseInfo.tableFormat }}</span></p>
            <a-divider type="vertical" />
            <p>
              {{ $t('healthScore') }}: <span class="text-color">{{ baseInfo.healthScore == null || baseInfo.healthScore < 0 ? 'N/A' : baseInfo.healthScore }}</span>
            </p>
          </div>
        </div>
      </div>
      <div class="content">
        <a-tabs v-model:activeKey="activeKey" destroy-inactive-tab-pane @change="onChangeTab">
          <a-tab-pane key="Details" :tab="$t('details')" force-render>
            <UDetails ref="detailRef" @set-base-detail-info="setBaseDetailInfo" />
          </a-tab-pane>
          <a-tab-pane v-if="detailLoaded" key="Files" :tab="$t('files')">
            <UFiles :has-partition="baseInfo.hasPartition" />
          </a-tab-pane>
          <a-tab-pane v-for="tab in tabConfigs" :key="tab.key" :tab="$t(tab.label)">
            <component :is="`U${tab.key}`" />
          </a-tab-pane>
        </a-tabs>
      </div>
    </div>
    <!-- Create table secondary page -->
    <router-view v-else @go-back="goBack" />
  </div>
</template>

<style lang="less" scoped>
.tables-wrap {
  font-size: 14px;
  border: 1px solid #e8e8f0;
  padding: 12px 0;
  min-height: 100%;

  .create-time {
    margin-top: 12px;
  }

  .tables-menu-wrap {
    position: fixed;
    width: 100%;
    height: 100%;
    top: 0;
    left: 200px;
    z-index: 100;
  }

  .table-name {
    font-size: 24px;
    line-height: 1.5;
    margin-right: 16px;
    max-width: 100%;
    padding-left: 24px;
  }

  .table-info {
    padding: 12px 24px 0 24px;

    .text-color {
      color: #7CB305;
    }
  }

  .table-edit {
    font-size: 18px;
    padding-right: 12px;
  }

  :deep(.ant-tabs-nav) {
    padding-left: 12px;
    margin-bottom: 0;
  }
}
</style>
