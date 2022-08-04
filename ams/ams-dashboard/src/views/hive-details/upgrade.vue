<template>
  <div class="upgrade-table">
    <div class="nav-bar">
      <left-outlined @click="goBack" />
      <span class="title g-ml-8">{{$t('upgradeHiveTable')}}</span>
    </div>
    <div class="content">
      <div class="table-attrs">
        <a-form
          :model="formState"
          name="fields"
          @finish="onFinish"
          @finishFailed="onFinishFailed"
          class="label-120"
        >
          <a-form-item
            :label="$t('field')"
            name="field"
          >
            <schema-field :fields="field"></schema-field>
          </a-form-item>
          <a-form-item
            :label="$t('partitonField')"
            name="partitonField"
          >
            <partition-field :partitionFields="partitionFields"></partition-field>
          </a-form-item>
          <a-form-item
            :label="$t('otherProperties')"
            name="otherProperties"
          >
            <other-properties :propertiesObj="propertiesObj" />
          </a-form-item>
        </a-form>
      </div>
      <div class="footer-btn">
        <a-button type="primary" class="btn g-mr-12">{{$t('ok')}}</a-button>
        <a-button type="ghost" class="btn">{{$t('cancel')}}</a-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { LeftOutlined } from '@ant-design/icons-vue'
import schemaField from './components/Field.vue'
import partitionField from './components/Partition.vue'
import otherProperties from './components/Properties.vue'
import { IField, PartitionColumnItem, DetailColumnItem, IMap } from '@/types/common.type'
import { getTableDetail } from '@/services/table.service'

const loading = ref<boolean>(false)
const field = reactive<DetailColumnItem[]>([])
const partitionFields = reactive<IField[]>([])
const propertiesObj = reactive<IMap<string>>({})

const emit = defineEmits<{
 (e: 'goBack'): void
}>()

const route = useRoute()

const params = computed(() => {
  return {
    ...route.query
  }
})
async function getDetails() {
  try {
    const { catalog, db, table } = params.value
    if (!catalog || !db || !table) {
      return
    }
    loading.value = true
    partitionFields.length = 0
    field.length = 0
    const result = await getTableDetail({
      ...params.value
    })
    const { partitionColumnList = [], schema, properties } = result;
    (partitionColumnList || []).forEach((ele: PartitionColumnItem) => {
      partitionFields.push(ele)
    });
    (schema || []).forEach((ele: DetailColumnItem) => {
      field.push(ele)
    })
    Object.assign(propertiesObj, properties)
  } catch (error) {
  } finally {
    loading.value = false
  }
}

function onFinish() {}
function onFinishFailed() {}
function goBack() {
  emit('goBack')
}

onMounted(() => {
  getDetails()
})
</script>

<style lang="less" scoped>
.upgrade-table {
  // height: 100%;
  display: flex;
  flex: 1;
  flex-direction: column;
  .nav-bar {
    padding-left: 12px;
    height: 20px;
    flex-shrink: 0;
  }
  .content {
    padding: 24px 24px 0;
    display: flex;
    flex: 1;
    flex-direction: column;
    width: 66%;
    justify-content: space-between;
    .table-attrs {
      display: flex;
      flex: 1;
      overflow-y: auto;
    }
    .footer-btn {
      height: 32px;
      .btn {
        min-width: 78px;
      }
    }
  }
}
</style>
