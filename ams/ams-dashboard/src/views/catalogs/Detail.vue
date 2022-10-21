<template>
  <div class="detail-wrap">
    <div class="content-wrap">
      <a-form ref="formRef" :model="formState" class="catalog-form">
        <a-form-item :label="$t('name')" :name="['catalog', 'name']" :rules="[{ required: isEdit }]">
          <a-input v-if="isEdit" v-model:value="formState.catalog.name" />
          <span v-else>{{formState.catalog.name}}</span>
        </a-form-item>
        <a-form-item :label="$t('catalogSettingType')" :name="['catalog', 'type']" :rules="[{ required: isEdit }]">
          <a-select
            v-if="isEdit"
            v-model:value="formState.catalog.type"
            :options="catalogTypeOps"
          />
          <span v-else>{{formState.catalog.type}}</span>
        </a-form-item>
        <a-form-item>
          <p class="header">{{$t('storageConfig')}}</p>
        </a-form-item>
        <a-form-item label="storage_config.storage.type" :name="['storageConfig', 'storage_config.storage.type']" :rules="[{ required: isEdit }]">
          <a-input v-if="isEdit" v-model:value="formState.storageConfig['storage_config.storage.type']" />
          <span v-else class="config-value">hdfs</span>
        </a-form-item>
        <a-form-item label="storage_config.core-site" class="g-flex-ac">
          <a-button v-if="!formState.storageConfig['storage_config.core-site']" type="ghost" @click="uploadFile" class="g-mr-12">{{$t('upload')}}</a-button>
          <span class="config-value" :class="{'view-active': formState.storageConfig['storage_config.core-site']}" @click="downLoad(formState.storageConfig['storage_config.core-site'])">core-site.xml</span>
        </a-form-item>
        <a-form-item label="storage_config.hdfs-site" class="g-flex-ac">
          <a-button v-if="!formState.storageConfig['storage_config.hdfs-site']" type="ghost" class="g-mr-12">{{$t('upload')}}</a-button>
          <span class="config-value" :class="{'view-active': formState.storageConfig['storage_config.hdfs-site']}">hdfs-site.xml</span>
        </a-form-item>
        <a-form-item label="storage_config.hive-site" class="g-flex-ac">
          <a-button v-if="!formState.storageConfig['storage_config.hive-site']" type="ghost" class="g-mr-12">{{$t('upload')}}</a-button>
          <span class="config-value" :class="{'view-active': formState.storageConfig['storage_config.hive-site']}">hive-site.xml</span>
        </a-form-item>
        <a-form-item>
          <p class="header">{{$t('authConfig')}}</p>
        </a-form-item>
        <a-form-item label="auth_config.type" :name="['authConfig', 'auth_config.type']" :rules="[{ required: isEdit }]">
          <a-select
            v-if="isEdit"
            v-model:value="formState.authConfig['auth_config.type']"
            :options="authConfigTypeOps"
          />
          <span v-else class="config-value">{{formState.authConfig['auth_config.type']}}</span>
        </a-form-item>
        <a-form-item v-if="formState.authConfig['auth_config.type'] === 'simole'" label="auth_config.hadoop_username" :name="['authConfig', 'auth_config.hadoop_username']" :rules="[{ required: isEdit }]">
          <a-input v-if="isEdit" v-model:value="formState.authConfig['auth_config.hadoop_username']" />
          <span v-else class="config-value">{{formState.authConfig['auth_config.hadoop_username']}}</span>
        </a-form-item>
        <a-form-item v-if="formState.authConfig['auth_config.type'] === 'kerberos'" label="auth_config.principal" :name="['authConfig', 'auth_config.principal']" :rules="[{ required: isEdit }]">
          <a-input v-if="isEdit" v-model:value="formState.authConfig['auth_config.principal']" />
          <span v-else class="config-value">{{formState.authConfig['auth_config.principal']}}</span>
        </a-form-item>
        <a-form-item v-if="formState.authConfig['auth_config.type'] === 'kerberos'" label="auth_config.keytab" :name="['authConfig', 'auth_config.keytab']" :rules="[{ required: isEdit }]">
          <a-input v-if="isEdit" v-model:value="formState.authConfig['auth_config.keytab']" />
          <span v-else class="config-value">{{formState.authConfig['auth_config.keytab']}}</span>
        </a-form-item>
        <a-form-item v-if="formState.authConfig['auth_config.type'] === 'kerberos'" label="auth_config.krb5" :name="['authConfig', 'auth_config.krb5']" :rules="[{ required: isEdit }]">
          <a-input v-if="isEdit" v-model:value="formState.authConfig['auth_config.krb5']" />
          <span v-else class="config-value">{{formState.authConfig['auth_config.krb5']}}</span>
        </a-form-item>
        <a-form-item>
          <p class="header">{{$t('properties')}}</p>
        </a-form-item>
        <a-form-item>
          <Properties :propertiesObj="formState.properties" :isEdit="isEdit" ref="propertiesRef" />
        </a-form-item>
      </a-form>
    </div>
    <div v-if="isEdit" class="footer-btn">
      <a-button type="primary" @click="handleSave" class="g-mr-12">{{$t('save')}}</a-button>
      <a-button @click="handleCancle">{{$t('cancle')}}</a-button>
    </div>
    <div v-if="!isEdit" class="footer-btn">
      <a-button type="primary" @click="handleEdit" class="g-mr-12">{{$t('edit')}}</a-button>
      <a-button @click="handleRemove">{{$t('remove')}}</a-button>
    </div>
    <u-loading v-if="loading" />
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { getCatalogsTypes, getCatalogsSetting, saveCatalogsSetting, checkCatalogStatus, delCatalog } from '@/services/setting.services'
import { ILableAndValue, ICatalogItem, IMap } from '@/types/common.type'
import { Modal, message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import Properties from './Properties.vue'

interface FormState {
  catalog: IMap<string>
  storageConfig: IMap<string>
  authConfig: IMap<string>
  properties: IMap<string>
}

const props = defineProps<{ catalog: ICatalogItem, isEdit: boolean }>()
const emit = defineEmits<{
 (e: 'updateEdit', val: boolean): void,
 (e: 'updateCatalogs'): void
}>()

const { t } = useI18n()
const isEdit = computed(() => {
  return props.isEdit
})
const loading = ref<boolean>(false)
const formRef = ref()
const propertiesRef = ref()
const formState:FormState = reactive({
  catalog: {
    name: '',
    type: ''
  },
  storageConfig: {
    'storage_config.storage.type': '',
    'storage_config.core-site': '',
    'storage_config.hdfs-site': '',
    'storage_config.hive-site': ''
  },
  authConfig: {
    'auth_config.type': '',
    'auth_config.hadoop_username': '',
    'auth_config.principal': '',
    'auth_config.keytab': '',
    'auth_config.krb5': ''
  },
  properties: {}
})
const authConfigTypeOps = reactive<ILableAndValue[]>([{
  label: 'simple',
  value: 'simple'
}, {
  label: 'kerberos',
  value: 'kerberos'
}])

watch(() => props.catalog,
  (value) => {
    value && initData()
  }, {
    immediate: true,
    deep: true
  }
)
const catalogTypeOps = reactive<ILableAndValue[]>([])

function initData() {
  const { catalogName, catalogType } = props.catalog
  formState.catalog.name = catalogName || ''
  formState.catalog.type = catalogType || ''
  getConfigInfo()
}
async function getCatalogTypeOps() {
  const res = await getCatalogsTypes();
  (res || []).forEach((ele: string) => {
    catalogTypeOps.push({
      label: ele,
      value: ele
    })
  })
}
async function getConfigInfo() {
  try {
    loading.value = true
    const catalogName = props.catalog.catalogName
    if (!catalogName) { return }
    const res = await getCatalogsSetting(catalogName)
    if (!res) { return }
    const { name, type, storageConfig, authConfig, properties } = res
    formState.catalog.name = name
    formState.catalog.type = type
    formState.authConfig = authConfig
    formState.storageConfig = storageConfig
    formState.properties = properties
  } catch (error) {
  } finally {
    loading.value = false
  }
}

function handleEdit() {
  emit('updateEdit', true)
}
async function handleRemove() {
  const res = await checkCatalogStatus(props.catalog.catalogName)
  if (res?.code === 200) {
    deleteCatalogModal()
    return
  }
  if (res?.code === 400) {
    Modal.confirm({
      title: t('cannotDeleteModalTitle'),
      content: t('cannotDeleteModalContent'),
      wrapClassName: 'not-delete-modal'
    })
  }
}
function handleSave() {
  formRef.value
    .validateFields()
    .then(async() => {
      const { catalog, storageConfig, authConfig } = formState
      const properties = await propertiesRef.value.getProperties()
      if (!properties) {
        return
      }
      await saveCatalogsSetting({
        ...catalog,
        storageConfig,
        authConfig,
        properties
      }).then(() => {
        message.success(`${t('save')}${t('success')}`)
        formRef.value.resetFields()
        handleCancle()
      }).catch(() => {
      })
    })
    .catch(() => {
    })
}
function handleCancle() {
  formRef.value.resetFields()
  emit('updateEdit', false)
  getConfigInfo()
}
async function deleteCatalogModal() {
  Modal.confirm({
    title: t('deleteCatalogModalTitle'),
    onOk: async() => {
      await delCatalog(props.catalog.catalogName)
      message.success(`${t('remove')}${t('success')}`)
      emit('updateCatalogs')
    }
  })
}
function uploadFile() {}
function downLoad() {}
onMounted(() => {
  getCatalogTypeOps()
})

</script>

<style lang="less" scoped>
.detail-wrap {
  height: 100%;
  padding: 16px 200px 16px 24px;
  display: flex;
  flex: 1;
  flex-direction: column;
  .content-wrap {
    display: flex;
    flex: 1;
    overflow: auto;
    flex-direction: column;
    :deep(.ant-form-item-label) {
      width: 280px;
    }
    .header {
      font-size: 20px;
      font-weight: 600;
      color: #102048;
    }
    .view-active {
      color: @primary-color;
      cursor: pointer;
    }
  }
  .footer-btn {
    height: 32px;
    flex-shrink: 0;
  }
}
</style>
<style lang="less">
.not-delete-modal {
  .ant-modal-confirm-btns .ant-btn:first-child {
    display: none;
  }
}
</style>
