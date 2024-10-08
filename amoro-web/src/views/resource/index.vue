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
import {
  defineComponent,
  onMounted,
  reactive,
  shallowReactive,
  toRefs,
  watch,
  ref,
} from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import TableList from '../optimize/components/List.vue'
import List from './components/List.vue'
import { usePlaceholder } from '@/hooks/usePlaceholder'
import { usePagination } from '@/hooks/usePagination'
import type { IIOptimizeGroupItem, ILableAndValue } from '@/types/common.type'
import GroupModal from '@/views/resource/components/GroupModal.vue'
import CreateOptimizerModal from '@/views/resource/components/CreateOptimizerModal.vue'

export default defineComponent({
  name: 'Resource',
  components: {
    List,
    GroupModal,
    TableList,
    CreateOptimizerModal,
  },
  setup() {
    const { t } = useI18n()
    const router = useRouter()
    const route = useRoute()
    const tabConfig: ILableAndValue[] = shallowReactive([
      { label: t('optimizergroup'), value: 'optimizergroup' },
      { label: t('optimizers'), value: 'optimizers' },
    ])
    const placeholder = reactive(usePlaceholder())
    const pagination = reactive(usePagination())
    const state = reactive({
      activeTab: 'optimizergroup' as string,
      showGroupModal: false as boolean,
      groupEdit: false,
      groupEditRecord: {
        resourceGroup: {
          name: '',
          container: '',
          properties: {},
        },
        occupationCore: 0,
        occupationMemory: 0,
        name: '',
        container: '',
        resourceOccupation: '',
      },
      groupKeyCount: 1,
      showTab: false as boolean,
      showCreateOptimizer: false as boolean,
      optimizerEdit: false,
      optimizerEditRecord: null,
    })

    watch(
      () => route.query,
      (value) => {
        state.activeTab = (value.tab as string) || 'tables'
      },
      {
        immediate: true,
      },
    )

    const editGroup = (editRecord: IIOptimizeGroupItem | null) => {
      if (editRecord) {
        state.groupEdit = true
        state.groupEditRecord = { ...editRecord }
      }
      else {
        state.groupEdit = false
      }
      state.showGroupModal = true
    }

    const createOptimizer = (editRecord: any | null) => {
      if (editRecord) {
        state.optimizerEdit = true
        state.optimizerEditRecord = { ...editRecord }
      } else {
        state.optimizerEdit = false
      }
      state.showCreateOptimizer = true
    }

    const onChangeTab = (key: string) => {
      const query = { ...route.query }
      query.tab = key
      router.replace({ query: { ...query } })
    }

    onMounted(() => {
      state.showTab = true
    })

    let createOptimizer1 = createOptimizer;
    return {
      placeholder,
      pagination,
      ...toRefs(state),
      tabConfig,
      onChangeTab,
      editGroup,
      createOptimizer,
      t,
    }
  },
})
</script>

<template>
  <div class="border-wrap">
    <div class="resource-wrap">
      <div class="content">
        <a-tabs
          v-model:activeKey="activeTab"
          destroy-inactive-tab-pane
          @change="onChangeTab"
        >
          <a-tab-pane
            key="tables"
            :tab="t('tables')"
            :class="[activeTab === 'tables' ? 'active' : '']"
          >
            <TableList />
          </a-tab-pane>
          <a-tab-pane
            key="optimizers"
            :tab="t('optimizers')"
            :class="[activeTab === 'optimizers' ? 'active' : '']"
          >
            <a-button type="primary" class="g-mb-16" @click="createOptimizer(null)">
              {{ t("createOptimizer") }}
            </a-button>
            <List type="optimizers" />
          </a-tab-pane>
          <a-tab-pane
            key="optimizergroup"
            :tab="t('optimizergroup')"
            :class="[activeTab === 'optimizergroup' ? 'active' : '']"
          >
            <a-button type="primary" class="g-mb-16" @click="editGroup(null)">
              {{ t("addgroup") }}
            </a-button>
            <List
              :key="groupKeyCount"
              type="optimizergroup"
              @edit-group="editGroup"
            />
          </a-tab-pane>
        </a-tabs>
      </div>
    </div>
    <GroupModal
      v-if="showGroupModal"
      :edit="groupEdit"
      :edit-record="groupEditRecord"
      @cancel="showGroupModal = false"
      @refresh="
        groupKeyCount++;
        showGroupModal = false;
      "
    />
    <CreateOptimizerModal
        v-if="showCreateOptimizer"
        @cancel="showCreateOptimizer = false"
        @refresh="showCreateOptimizer = false"
    />
  </div>
</template>

<style lang="less" scoped>
.border-wrap {
  padding: 16px 24px;
  height: 100%;
}
.resource-wrap {
  height: 100%;
  overflow-y: auto;
  .status-icon {
    width: 8px;
    height: 8px;
    border-radius: 8px;
  }
  .tabs {
    height: 32px;
    display: flex;
    align-items: center;
    margin-bottom: 12px;
    padding: 0 12px;
    border: 1px solid #e5e5e5;
  }
  .table-name {
    color: @primary-color;
    &:hover {
      cursor: pointer;
    }
  }
}
</style>
