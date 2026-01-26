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
import { computed, defineComponent, nextTick, onBeforeUnmount, onMounted, reactive, ref, shallowReactive, toRefs, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import UDetails from './components/Details.vue'
import UFiles from './components/Files.vue'
import UOperations from './components/Operations.vue'
import USnapshots from './components/Snapshots.vue'
import UOptimizing from './components/Optimizing.vue'
import UHealthScore from './components/HealthScoreDetails.vue'
import TableExplorer from './components/TableExplorer.vue'
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
    UHealthScore,
    TableExplorer,
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const store = useStore()

    const detailRef = ref()

    const SIDEBAR_WIDTH_STORAGE_KEY = 'tables_sidebar_width'
    const SIDEBAR_MIN_WIDTH = 320
    const SIDEBAR_MAX_WIDTH = 800
    const sidebarWidth = ref(512)

    let isResizing = false
    let startX = 0
    let startWidth = sidebarWidth.value

    const clampSidebarWidth = (width: number) => {
      if (width < SIDEBAR_MIN_WIDTH) {
        return SIDEBAR_MIN_WIDTH
      }
      if (width > SIDEBAR_MAX_WIDTH) {
        return SIDEBAR_MAX_WIDTH
      }
      return width
    }

    const initSidebarWidth = () => {
      const stored = localStorage.getItem(SIDEBAR_WIDTH_STORAGE_KEY)
      const parsed = stored ? Number.parseInt(stored, 10) : Number.NaN
      const base = Number.isFinite(parsed) ? clampSidebarWidth(parsed) : 512
      sidebarWidth.value = base
      startWidth = base
    }

    const onSidebarResize = (event: MouseEvent) => {
      if (!isResizing) {
        return
      }
      const deltaX = event.clientX - startX
      const nextWidth = clampSidebarWidth(startWidth + deltaX)
      sidebarWidth.value = nextWidth
    }

    const stopSidebarResize = () => {
      if (!isResizing) {
        return
      }
      isResizing = false
      document.removeEventListener('mousemove', onSidebarResize)
      document.removeEventListener('mouseup', stopSidebarResize)
      localStorage.setItem(SIDEBAR_WIDTH_STORAGE_KEY, String(sidebarWidth.value))
    }

    const startSidebarResize = (event: MouseEvent) => {
      isResizing = true
      startX = event.clientX
      startWidth = sidebarWidth.value
      document.addEventListener('mousemove', onSidebarResize)
      document.addEventListener('mouseup', stopSidebarResize)
    }

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
        smallFileScore: 0,
        equalityDeleteScore: 0,
        positionalDeleteScore: 0,
        comment: '',
      } as IBaseDetailInfo,
      detailLoaded: false,
    })

    const isIceberg = computed(() => {
      return state.baseInfo.tableType === 'ICEBERG'
    })

    const hasSelectedTable = computed(() => !!(route.query?.catalog && route.query?.db && route.query?.table))

    const setBaseDetailInfo = (baseInfo: IBaseDetailInfo & { comment?: string }) => {
      state.detailLoaded = true
      state.baseInfo = { ...baseInfo }
    }

    const onChangeTab = (key: string | number) => {
      const query = { ...route.query }
      query.tab = key.toString()
      router.replace({ query: { ...query } })
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

    watch(
      hasSelectedTable,
      (value, oldVal) => {
        if (value && !oldVal && detailRef.value) {
          detailRef.value.getTableDetails()
        }
      },
    )

    onMounted(() => {
      initSidebarWidth()
      state.activeKey = (route.query?.tab as string) || 'Details'
      nextTick(() => {
        if (detailRef.value && hasSelectedTable.value) {
          detailRef.value.getTableDetails()
        }
      })
    })

    onBeforeUnmount(() => {
      document.removeEventListener('mousemove', onSidebarResize)
      document.removeEventListener('mouseup', stopSidebarResize)
    })

    return {
      ...toRefs(state),
      detailRef,
      tabConfigs,
      store,
      isIceberg,
      hasSelectedTable,
      setBaseDetailInfo,
      goBack,
      onChangeTab,
      sidebarWidth,
      startSidebarResize,
    }
  },
})
</script>

<template>
  <div class="tables-wrap">
    <div v-if="!isSecondaryNav" class="tables-content">
      <div
        class="tables-sidebar"
        :style="{ width: `${sidebarWidth}px`, flex: `0 0 ${sidebarWidth}px` }"
      >
        <TableExplorer />
      </div>
      <div class="tables-divider" aria-hidden="true" @mousedown="startSidebarResize" />
      <div class="tables-main">
        <template v-if="hasSelectedTable">
          <div class="tables-main-header g-flex-jsb">
            <div class="g-flex-col">
              <div class="g-flex">
                <span :title="baseInfo.tableName" class="table-name g-text-nowrap">{{ baseInfo.tableName }}</span>
              </div>
              <div v-if="baseInfo.comment" class="table-info g-flex-ac">
                <p>{{ $t('Comment') }}: <span class="text-color">{{ baseInfo.comment }}</span></p>
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
                  {{ $t('healthScore') }}:
                  <UHealthScore :base-info="baseInfo" />
                </p>
              </div>
            </div>
          </div>
          <div class="tables-main-body">
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
        </template>
        <div v-else class="empty-page" />
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
  height: 100%;
  min-height: 100%;

  .tables-content {
    display: flex;
    height: 100%;
    align-items: stretch;
  }

  .tables-sidebar {
    flex: 0 0 auto;
    height: 100%;
    background-color: #fff;
    position: relative;
  }

  .tables-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-width: 0;
    height: 100%;
  }

  .tables-divider {
    position: relative;
    flex: 0 0 8px;
    width: 8px;
    height: 100%;
    cursor: col-resize;
    z-index: 2; // Ensure divider is above sidebar and main content so drag area is not blocked
  }

  .tables-divider::before {
    content: '';
    position: absolute;
    top: 0;
    bottom: 0;
    left: 50%;
    width: 1px;
    transform: translateX(-50%);
    background: #e8e8f0;
  }

  .tables-main-body {
    flex: 1;
    min-height: 0;
    padding: 0 24px 24px;
    overflow: auto;
  }

  .tables-main .empty-page {
    height: 100%;
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

    .clickable-score {
      cursor: pointer;
      text-decoration: underline;
    }
  }

  .table-edit {
    font-size: 18px;
    padding-right: 12px;
  }

  :deep(.ant-tabs-nav) {
    padding-left: 0;
    margin-bottom: 0;
  }
}
</style>
