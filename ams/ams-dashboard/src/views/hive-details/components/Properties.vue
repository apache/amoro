
<template>
  <div class="config-properties">
    <div class="config-header g-flex">
      <div class="td g-flex-ac">{{$t('key')}}</div>
      <div class="td g-flex-ac bd-left">{{$t('value')}}</div>
    </div>
    <a-form ref="propertiesFormRef" :model="propertiesArray" layout="inline" name="propertiesForm">
      <div class="config-row g-flex-ac" v-for="item in propertiesArray" :key="item.uuid">
        <a-form-item
          :name="item.uuid + '_key'"
        >
          <a-auto-complete
            v-model:value="item.key"
            :name="item.uuid + '_key'"
            :options="options"
            @select="onSelect"
            :filter-option="filterOption"
            style="width: 40%"
            class="g-mr-12"
          />
        </a-form-item>
        <a-form-item
          :name="item.uuid + '_value'"
        >
          <a-input
            v-model:value="item.value"
            :name="item.uuid + '_value'"
            :maxlength="64"
            style="width: 40%; margin-right: 8px"
          />
          <!-- <close-outlined class="icon-close" @click="removeRule(item, index)"  /> -->
        </a-form-item>
        <!-- <a-form-item :rules="[{ required: true, message: `${placeholder.selectClPh}` }]">
          <a-input
            :name="item.uuid + '_value'"
            v-model:value="item.value"
            :maxlength="64"
          />
        </a-form-item> -->
        <!-- <a-form-item> -->
          <close-outlined class="icon-close" @click="removeRule(item, index)"  />
        <!-- </a-form-item> -->
      </div>
    </a-form>
    <!-- <div class="config-row g-flex-ac" v-for="(item, index) in propertiesArray" :key="item.uuid">
      <a-auto-complete
        v-model:value="item.key"
        :name="item.uuid + '_key'"
        :options="options"
        @select="onSelect"
        :filter-option="filterOption"
        class="g-mr-12"
      />
      <a-input
        :name="item.uuid + '_value'"
        v-model:value="item.value"
        :maxlength="64"
      />
      <close-outlined class="icon-close" @click="removeRule(item, index)"  />
    </div> -->
    <a-button class="config-btn" @click="addRule">+</a-button>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { IMap, IKeyAndValue } from '@/types/common.type'
import { CloseOutlined } from '@ant-design/icons-vue'
import { getUpgradeProperties } from '@/services/table.service'
import { getUUid } from '@/utils/index'
// import { usePlaceholder } from '@/hooks/usePlaceholder'

interface IItem {
  key: string
  value: string
  uuid: string
}

const props = defineProps<{ propertiesObj: IMap<string> }>()
const propertiesArray = reactive<IItem[]>([])
const options = ref<IMap<string>[]>()
const propertiesIncludeValueList = reactive<IKeyAndValue[]>([]) // includes key value
const propertiesFormRef = ref()

// const placeholder = reactive(usePlaceholder())

// async function validatePass(rule: Rule, value: string) {
//   if (value === '') {
//     return Promise.reject(Error('Please input'))
//   } else {
//     // if (formState.checkPass !== '') {
//     //   formRef.value.validateFields('checkPass');
//     // }
//     return Promise.resolve()
//   }
// }

watch(() => props.propertiesObj, () => {
  initPropertiesArray()
}, {
  immediate: true,
  deep: true
})

function initPropertiesArray() {
  propertiesArray.length = 0
  Object.keys(props.propertiesObj).forEach(key => {
    propertiesArray.push({
      key: key,
      value: props.propertiesObj[key],
      uuid: getUUid()
    })
  })
}

async function getPropertiesList() {
  propertiesIncludeValueList.length = 0
  options.value = []
  const result = await getUpgradeProperties()
  Object.keys(result).forEach(key => {
    const item = {
      key: result[key],
      value: key
    }
    propertiesIncludeValueList.push(item)
    options.value.push(item)
  })
}

function filterOption(input: string, option: IMap<string>) {
  return option.value.toUpperCase().indexOf(input.toUpperCase()) >= 0
}

function onSelect(value: string) {
  const selected = propertiesIncludeValueList.find((ele: IKeyAndValue) => ele.value === value)
  const selectVal = propertiesArray.find((ele: IItem) => ele.key === value)
  selectVal && (selectVal.value = selected.key)
}
function removeRule(item, index) {
  propertiesArray.splice(index, 1)
}
function addRule() {
  propertiesArray.push({
    key: '',
    value: '',
    uuid: getUUid()
  })
}
function validateForm() {
  propertiesFormRef.value
    .validateFields()
    .then(() => {
      propertiesFormRef.value.resetFields()
      return Promise.resolve()
    })
    .catch((info: Error) => {
      console.log('Validate Failed:', info)
      return Promise.reject(Error('Please input'))
    })
}
defineExpose({
  getProperties() {
    const propObj: IMap<string> = {}
    propertiesArray.forEach(e => {
      propObj[e.key] = e.value
    })
    return propObj
  }
})

onMounted(() => {
  getPropertiesList()
})

</script>

<style lang="less">
  .config-properties {
    width: 100%;
    display: flex;
    flex-direction: column;
    line-height: 32px;
    .config-header {
      width: 100%;
      padding-right: 32px;
      background: #fafafa;
      border-bottom: 1px solid #e8e8f0;
      .td {
        width: 50%;
        height: 40px;
        padding: 8px 12px;
        color: #102048;
        font-weight: 500;
      }
      .bd-left {
        position: relative;
        &:before {
          position: absolute;
          top: 50%;
          left: 0;
          width: 1px;
          height: 1.6em;
          background-color: rgba(0, 0, 0, 0.06);
          transform: translateY(-50%);
          transition: background-color 0.3s;
          content: '';
        }
      }
    }
    .config-row {
      height: 40px;
      margin-bottom: 8px;
      position: relative;
      padding-right: 32px;
      width: 100%;
      .ant-form-item {
        width: 50%;
      }
      .ant-select-auto-complete {
        width: 50%;
        input {
          color: #79809a;
        }
      }
      .ant-input {
        width: 50%;
      }
      .icon-close {
        cursor: pointer;
        position: absolute;
        right: 10px;
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
