<template>
  <div class="branch-selector">
    <a-dropdown :trigger="['click']" placement="bottomLeft" :getPopupContainer="getPopupContainer">
      <a-button class="branch-btn" :disabled="!selectedObj.value || disabled">
        <svg-icon className="branch-selector-icon" :icon-class="selectedObj.type" class="g-mr-8" />
        <span class="branch-btn-label">{{selectedObj.label}}</span>
        <down-outlined />
      </a-button>
      <template #overlay>
        <div>
          <div class="branch-selector-search">
            <a-input v-show="tabActiveKey === branchTypeMap.BRANCH" v-model:value="branchSearchKey" :placeholder="$t('Filter branches/tags')" @click="onClickInput" />
            <a-input v-show="tabActiveKey === branchTypeMap.TAG" v-model:value="tagSearchKey" :placeholder="$t('Filter branches/tags')" @click="onClickInput" />
          </div>
          <a-tabs v-model:activeKey="tabActiveKey" type="card">
            <a-tab-pane :key="branchTypeMap.BRANCH" tab="Branches">
              <template v-if="!!actualBranchList.length">
                <div class="branch-selector-item" v-for="(item, key) in actualBranchList" :key="key" @click="selectObject(item)">
                  <div class="item-icon">
                    <check-outlined v-if="item.value === selectedObj.value"  />
                  </div>
                  <span class="item-label">{{item.label}}</span>
                </div>
              </template>
              <span class="empty-tips" v-else>{{$t('nothingToShow')}}</span>
            </a-tab-pane>
            <a-tab-pane :key="branchTypeMap.TAG" tab="Tags">
              <template v-if="!!actualTagList.length">
                <div class="branch-selector-item" v-for="(item, key) in actualTagList" :key="key" @click="selectObject(item)">
                  <div class="item-icon">
                    <check-outlined v-if="item.value === selectedObj.value"  />
                  </div>
                  <span class="item-label">{{item.label}}</span>
                </div>
              </template>
              <span class="empty-tips" v-else>{{$t('nothingToShow')}}</span>
            </a-tab-pane>
          </a-tabs>
        </div>
      </template>
    </a-dropdown>
    <div>
      <svg-icon className="branch-selector-icon" icon-class="branch" class="g-mr-4 g-ml-16" />
      <span class="g-mr-4">{{branchList.length}}</span>
      <span>{{$t('branches')}}</span>
    </div>
    <div>
      <svg-icon className="branch-selector-icon" icon-class="tag" class="g-mr-4 g-ml-16" />
      <span class="g-mr-4">{{tagList.length}}</span>
      <span>{{$t('tags')}}</span>
    </div>
    <div class="g-ml-24">
      {{$t('operation')}}:
      <a-select
        class="g-ml-8"
        v-model:value="operation"
        style="width: 160px"
        :disabled="disabled"
        @change="onChange"
      >
        <a-select-option v-for="item in operationList" :value="item" :key="item">{{item}}</a-select-option>
      </a-select>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { CheckOutlined, DownOutlined } from '@ant-design/icons-vue'
import { branchTypeMap, IBranchItem, IServiceBranchItem, operationMap } from '@/types/common.type'
import { getBranches, getTags } from '@/services/table.service'

const { t } = useI18n()

const props = defineProps({ db: String, table: String, disabled: Boolean })
const disabled = computed(() => props.disabled)

const emit = defineEmits(['refChange'])

const selectedObj = ref<IBranchItem>({ value: '', type: branchTypeMap.BRANCH, label: '' })
const branchSearchKey = ref<string>('')
const tagSearchKey = ref<string>('')
const tabActiveKey = ref<string>(branchTypeMap.BRANCH)
const branchList = ref<IBranchItem[]>([])
const tagList = ref<IBranchItem[]>([])
const actualBranchList = computed(() => branchList.value.filter(item => !branchSearchKey.value || item.label.includes(branchSearchKey.value)))
const actualTagList = computed(() => tagList.value.filter(item => !tagSearchKey.value || item.label.includes(tagSearchKey.value)))
const operation = ref<string>(operationMap.ALL)
const operationList = reactive([operationMap.ALL, operationMap.OPTIMIZING, operationMap.NONOPTIMIZING])

const onClickInput = (e: MouseEvent) => {
  e.stopPropagation()
}

const getPopupContainer = (triggerNode: any) => {
  return triggerNode.parentNode || document.body
}

const selectObject = (obj: IBranchItem) => {
  selectedObj.value = obj
  operation.value = operationMap.ALL
  emit('refChange', { ref: obj.value, operation: operationMap.ALL })
}

const onChange = (val: string) => {
  emit('refChange', { ref: selectedObj.value.value, operation: val })
}

const getBranchList = async () => {
  const result = await getBranches(props)
  branchList.value = (result.list || []).map((l: IServiceBranchItem) => ({ value: l.name, label: l.name, type: branchTypeMap.BRANCH }))
  branchList.value.length && selectObject(branchList.value[0])
}

const getTagList = async () => {
  const result = await getTags(props)
  tagList.value = (result.list || []).map((l: IServiceBranchItem) => ({ value: l.name, label: l.name, type: branchTypeMap.TAG }))
}

const init = async () => {
  await Promise.all([getBranchList(), getTagList()])
}

onMounted(() => {
  tabActiveKey.value = branchTypeMap.BRANCH
  init()
})
</script>

<style lang="less">
.branch-selector {
  margin-top: 32px;
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
    box-shadow: 0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 3px 6px -4px rgba(0, 0, 0, 0.12), 0 9px 28px 8px rgba(0, 0, 0, 0.05);

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
}
</style>
