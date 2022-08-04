
<template>
  <div class="config-properties">
    <div class="config-header g-flex-ac">
      <div class="td">{{$t('key')}}</div>
      <div class="td">{{$t('value')}}</div>
    </div>
    <div class="config-row g-flex-ac" v-for="(item, index) in propertiesArray" :key="item.uuid">
      <a-auto-complete
        placeholder=""
        :name="item.uuid + '_key'"
        :data-source="propertiesList"
        :filter-option="filterOption"
        v-model="item.key"
        @change="changeProperty(item)"
      />
      <a-input
        type="text"
        placeholder=""
        :name="item.uuid + '_value'"
        @change="changeProperty(item, 'INPUT')"
        v-model="item.value"
        :maxLength="64"
      />
      <close-outlined class="icon-close" @click="removeRule(item, index)"  />
    </div>
    <a-button class="config-btn" @click="addRule">+</a-button>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { IMap, IKeyAndValue } from '@/types/common.type'
import { CloseOutlined } from '@ant-design/icons-vue'

// interface IItem {
//   key: string
//   value: string
//   uuid: string
// }

// const props = defineProps<{ propertiesObj: IMap<string>, hasSelectOps: boolean, }>()

const propertiesArray = reactive<IMap<string>[]>([])

const propertiesList = reactive<string[]>([]) // 属性 只包含key
const propertiesIncludeValueList = reactive<IKeyAndValue[]>([]) // 属性可选值，包含key value

function changeProperty() {}
function removeRule() {}
function addRule() {}

</script>

<style lang="less">
  .config-properties {
    width: 100%;
    display: flex;
    flex-direction: column;
    line-height: 32px;
    .config-header {
      width: 100%;
      border-bottom: 1px solid #e5e5e5;
      .td {
        width: 49%;
      }
    }
    .config-row {
      height: 40px;
      margin-bottom: 8px;
      position: relative;
      .icon-close {
        cursor: pointer;
        position: absolute;
        right: -6px;
        font-size: 12px;
        &.disabled {
          cursor: not-allowed;
        }
      }
    }
    .config-btn {
      border-radius: 4px;
      width: 100%;
      border: 1px solid #e5e5e5;
      text-align: center;
      margin-top: 8px;
      color: #102048;
      box-shadow: none;
    }
    // .ant-btn {
    //   color: #102048;
    // }
    // .ant-btn[disabled] {
    //   color: #a9a9b8 !important;
    // }
  }
</style>
