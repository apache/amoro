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

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Modal, message } from 'ant-design-vue'
import type { IBranchItem, IServiceBranchItem } from '@/types/common.type'
import { branchTypeMap, operationMap } from '@/types/common.type'
import { getBranches, getConsumers, getTags, createTag as createTagApi, deleteTag as deleteTagApi } from '@/services/table.service'

const props = defineProps({ catalog: String, db: String, table: String, disabled: Boolean })
const emit = defineEmits(['refChange', 'consumerChange'])

const { t } = useI18n()
const disabled = computed(() => props.disabled)

const selectedObj = ref<IBranchItem>({ value: '', type: branchTypeMap.BRANCH, label: '' })
const branchSearchKey = ref<string>('')
const tagSearchKey = ref<string>('')
const consumerSearchKey = ref<string>('')
const tabActiveKey = ref<string>(branchTypeMap.BRANCH)
const branchList = ref<IBranchItem[]>([])
const tagList = ref<IBranchItem[]>([])
const consumerList = ref<IBranchItem[]>([])
const actualBranchList = computed(() => branchList.value.filter(item => !branchSearchKey.value || item.label.includes(branchSearchKey.value)))
const actualTagList = computed(() => tagList.value.filter(item => !tagSearchKey.value || item.label.includes(tagSearchKey.value)))
const actualConsumerList = computed(() => consumerList.value.filter(item => !consumerSearchKey.value
  || item.label.includes(consumerSearchKey.value)))

const operation = ref<string>(operationMap.ALL)
const operationList = reactive([operationMap.ALL, operationMap.OPTIMIZING, operationMap.NONOPTIMIZING])

// Create Tag modal state
const createTagModalVisible = ref<boolean>(false)
const createTagLoading = ref<boolean>(false)
const createTagForm = reactive({
  tagName: '',
  snapshotId: '' as string,
  maxRefAgeMs: undefined as number | undefined,
})

function onClickInput(e: MouseEvent) {
  e.stopPropagation()
}

function getPopupContainer(triggerNode: any) {
  return triggerNode.parentNode || document.body
}

function selectObject(obj: IBranchItem) {
  selectedObj.value = obj
  operation.value = operationMap.ALL
  emit('refChange', { ref: obj.value, operation: operationMap.ALL })
}
function selectConsumer(obj: IBranchItem) {
  if (obj.value === selectedObj.value.value)
    return
  selectedObj.value = obj
  const amoroCurrentSnapshotsItem = { ...obj.amoroCurrentSnapshotsOfTable }
  emit('consumerChange', { ref: obj.value, amoroCurrentSnapshotsItem, operation: operationMap.ALL })
}
function onChange(val: string) {
  emit('refChange', { ref: selectedObj.value.value, operation: val })
}

async function getBranchList() {
  const result = await getBranches(props as any)
  branchList.value = (result.list || []).map((l: IServiceBranchItem) => ({ value: l.name, label: l.name, type: branchTypeMap.BRANCH }))
  branchList.value.length && selectObject(branchList.value[0])
}

async function getTagList() {
  const result = await getTags(props as any)
  tagList.value = (result.list || []).map((l: IServiceBranchItem) => ({ value: l.name, label: l.name, type: branchTypeMap.TAG }))
}
async function getConsumerList() {
  const result = await getConsumers(props as any)
  consumerList.value = (result.list || []).map((l: IServiceBranchItem) => ({ value: l.consumerId, label: l.consumerId, type: branchTypeMap.CONSUMER, amoroCurrentSnapshotsOfTable: l.amoroCurrentSnapshotsOfTable }))
}

// Open the Create Tag modal
function openCreateTagModal(e: MouseEvent) {
  e.stopPropagation()
  createTagForm.tagName = ''
  createTagForm.snapshotId = ''
  createTagForm.maxRefAgeMs = undefined
  createTagModalVisible.value = true
}

// Submit Create Tag
async function handleCreateTag() {
  if (!createTagForm.tagName) {
    message.warning(t('tagNameRequired'))
    return
  }
  if (!createTagForm.snapshotId || !createTagForm.snapshotId.trim()) {
    message.warning(t('snapshotIdRequired'))
    return
  }
  try {
    createTagLoading.value = true
    await createTagApi({
      catalog: props.catalog!,
      db: props.db!,
      table: props.table!,
      tagName: createTagForm.tagName,
      snapshotId: createTagForm.snapshotId.trim(),
      maxRefAgeMs: createTagForm.maxRefAgeMs,
    })
    message.success(t('createTagSuccess'))
    createTagModalVisible.value = false
    await getTagList()
  } catch (error) {
    // Error is handled by the global interceptor
  } finally {
    createTagLoading.value = false
  }
}

// Delete Tag
function handleDeleteTag(item: IBranchItem, e: MouseEvent) {
  e.stopPropagation()
  Modal.confirm({
    title: t('deleteTag'),
    content: t('deleteTagConfirm', { name: item.label }),
    okText: t('confirm'),
    cancelText: t('cancel'),
    onOk: async () => {
      try {
        await deleteTagApi({
          catalog: props.catalog!,
          db: props.db!,
          table: props.table!,
          tagName: item.value,
        })
        message.success(t('deleteTagSuccess'))
        // If the deleted tag is currently selected, switch back to the first branch
        if (selectedObj.value.value === item.value && selectedObj.value.type === branchTypeMap.TAG) {
          if (branchList.value.length) {
            selectObject(branchList.value[0])
          }
        }
        await getTagList()
      } catch (error) {
        // Error is handled by the global interceptor
      }
    },
  })
}
async function init() {
  await Promise.all([getBranchList(), getTagList(), getConsumerList()])
}

onMounted(() => {
  tabActiveKey.value = branchTypeMap.BRANCH
  init()
})
</script>

<template>
  <div class="branch-selector">
    <a-dropdown :trigger="['click']" placement="bottomLeft" :get-popup-container="getPopupContainer">
      <a-button class="branch-btn" :disabled="!selectedObj.value || disabled">
        <svg-icon class-name="branch-selector-icon" :icon-class="selectedObj.type" class="g-mr-8" />
        <span class="branch-btn-label">{{ selectedObj.label }}</span>
        <down-outlined />
      </a-button>
      <template #overlay>
        <div>
          <div class="branch-selector-search">
            <a-input v-show="tabActiveKey === branchTypeMap.BRANCH" v-model:value="branchSearchKey" :placeholder="$t('filterBranchesOrTagsOrConsumers')" @click="onClickInput" />
            <a-input v-show="tabActiveKey === branchTypeMap.TAG" v-model:value="tagSearchKey" :placeholder="$t('filterBranchesOrTagsOrConsumers')" @click="onClickInput" />
            <a-input
              v-show="tabActiveKey === branchTypeMap.CONSUMER" v-model:value="consumerSearchKey" :placeholder="$t('filterBranchesOrTagsOrConsumers')"
              @click="onClickInput"
            />
          </div>
          <a-tabs v-model:activeKey="tabActiveKey" type="card">
            <a-tab-pane :key="branchTypeMap.BRANCH" :tab="$t('branches')">
              <template v-if="!!actualBranchList.length">
                <div v-for="(item, key) in actualBranchList" :key="key" class="branch-selector-item" @click="selectObject(item)">
                  <div class="item-icon">
                    <check-outlined v-if="item.value === selectedObj.value" />
                  </div>
                  <span class="item-label">{{ item.label }}</span>
                </div>
              </template>
              <span v-else class="empty-tips">{{ $t('nothingToShow') }}</span>
            </a-tab-pane>
            <a-tab-pane :key="branchTypeMap.TAG" :tab="$t('tags')">
              <template v-if="!!actualTagList.length">
                <div v-for="(item, key) in actualTagList" :key="key" class="branch-selector-item" @click="selectObject(item)">
                  <div class="item-icon">
                    <check-outlined v-if="item.value === selectedObj.value" />
                  </div>
                  <span class="item-label">{{ item.label }}</span>
                  <a-button type="link" size="small" danger class="tag-delete-btn" @click="handleDeleteTag(item, $event)">
                    <delete-outlined />
                  </a-button>
                </div>
              </template>
              <span v-else class="empty-tips">{{ $t('nothingToShow') }}</span>
              <div class="create-tag-btn-wrapper">
                <a-button type="link" size="small" block @click="openCreateTagModal">
                  <plus-outlined />
                  <span>{{ $t('createTag') }}</span>
                </a-button>
              </div>
            </a-tab-pane>
            <a-tab-pane v-if="consumerList.length !== 0" :key="branchTypeMap.CONSUMER" :tab="$t('consumers')">
              <template v-if="!!actualConsumerList.length">
                <div v-for="(item, key) in actualConsumerList" :key="key" class="branch-selector-item" @click="selectConsumer(item)">
                  <div class="item-icon">
                    <check-outlined v-if="item.value === selectedObj.value" />
                  </div>
                  <span class="item-label">{{ item.label }}</span>
                </div>
              </template>
              <span v-else class="empty-tips">{{ $t('nothingToShow') }}</span>
            </a-tab-pane>
          </a-tabs>
        </div>
      </template>
    </a-dropdown>
    <div>
      <svg-icon class-name="branch-selector-icon" icon-class="branch" class="g-mr-4 g-ml-16" />
      <span class="g-mr-4">{{ branchList.length }}</span>
      <span>{{ $t('branches') }}</span>
    </div>
    <div>
      <svg-icon class-name="branch-selector-icon" icon-class="tag" class="g-mr-4 g-ml-16" />
      <span class="g-mr-4">{{ tagList.length }}</span>
      <span>{{ $t('tags') }}</span>
    </div>
    <div class="g-ml-24">
      {{ $t('operation') }}:
      <a-select
        v-model:value="operation"
        class="g-ml-8"
        style="width: 160px"
        :disabled="disabled || tabActiveKey === branchTypeMap.CONSUMER"
        @change="onChange"
      >
        <a-select-option v-for="item in operationList" :key="item" :value="item">
          {{ item }}
        </a-select-option>
      </a-select>
    </div>
    <div class="selector-extra">
      <slot name="extra" />
    </div>

    <!-- Create Tag Modal -->
    <a-modal
      v-model:visible="createTagModalVisible"
      :title="$t('createTag')"
      :ok-text="$t('confirm')"
      :cancel-text="$t('cancel')"
      :confirm-loading="createTagLoading"
      @ok="handleCreateTag"
    >
      <a-form layout="vertical">
        <a-form-item :label="$t('tagName')" required>
          <a-input v-model:value="createTagForm.tagName" :placeholder="$t('inputPlaceholder', { inputPh: $t('tagName') })" />
        </a-form-item>
        <a-form-item :label="$t('snapshotIdLabel')" required>
          <a-input v-model:value="createTagForm.snapshotId" :placeholder="$t('inputPlaceholder', { inputPh: $t('snapshotIdLabel') })" />
        </a-form-item>
        <a-form-item :label="$t('maxRefAgeMs')">
          <a-input-number v-model:value="createTagForm.maxRefAgeMs" :placeholder="$t('inputPlaceholder', { inputPh: $t('maxRefAgeMs') })" :min="0" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style lang="less">
.branch-selector {
  display: flex;
  align-items: center;

  .branch-btn {
    display: flex;
    align-items: center;
    color: #102048;

    .branch-btn-label {
      line-height: 32px;
      max-width: 125px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .ant-dropdown {
    background: #fff;
    width: 300px;
    height: auto;
    max-height: 480px;
    padding: 8px 0 16px 0;
    font-size: 12px;
    border-radius: 6px;
    box-shadow: 0 6px 16px 0 rgba(0, 0, 0, 0.08),0 3px 6px -4px rgba(0, 0, 0, 0.12), 0 9px 28px 8px rgba(0, 0, 0, 0.05);

    .branch-selector-search {
      margin: 0 12px 8px 12px;
    }

    .ant-tabs {
      margin: 0 12px;

      .ant-tabs-tabpane {
        height: 240px;
        overflow-y: auto;
      }

      .branch-selector-item {
        display: flex;
        align-items: center;
        width: 100%;
        padding: 7px 16px;
        overflow: hidden;
        text-align: left;
        cursor: pointer;

        &:hover {
          background-color: rgba(0, 0, 0, 0.04);
        }

        .item-icon {
          margin-right: 4px;
          width: 16px;
        }

        .item-label {
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          color: #102048;
        }
      }
    }
  }

  .empty-tips {
    margin-top: 12px;
    display: block;
    width: 100%;
    text-align: center;
    color: #102048;
  }

  .selector-extra {
    display: flex;
    align-items: center;
    padding-left: 18px;
  }

  .create-tag-btn-wrapper {
    padding: 8px 16px;
    border-top: 1px solid #f0f0f0;
  }

  .tag-delete-btn {
    opacity: 0;
    transition: opacity 0.2s;
    padding: 0 4px;
    font-size: 12px;
  }

  .branch-selector-item:hover .tag-delete-btn {
    opacity: 1;
  }
}
</style>
