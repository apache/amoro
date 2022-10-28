<template>
  <div class="detail-wrap">
    <div class="content-wrap">
      <a-form ref="formRef" :model="formState" class="catalog-form">
        <a-form-item :label="$t('name')" :name="['catalog', 'name']" :rules="[{ required: isEdit, validator: validatorName }]">
          <a-input v-if="isEdit" v-model:value="formState.catalog.name" />
          <span v-else>{{formState.catalog.name}}</span>
        </a-form-item>
        <a-form-item :label="$t('tableFormat')" :name="['catalog', 'type']" :rules="[{ required: isEdit }]">
          <a-select
            v-if="isEdit"
            v-model:value="formState.catalog.type"
            :options="catalogTypeOps"
            :placeholder="placeholder.selectPh"
          />
          <span v-else>{{formState.catalog.type}}</span>
        </a-form-item>
        <a-form-item>
          <p class="header">{{$t('storageConfig')}}</p>
        </a-form-item>
        <a-form-item label="storage_config.storage.type">
          <span class="config-value">{{formState.storageConfig['storage_config.storage.type']}}</span>
        </a-form-item>
        <a-form-item
          v-for="config in formState.storageConfigArray"
          :key="config.label"
          :label="config.label"
          class="g-flex-ac">
          <a-upload
            v-if="!config.value"
            v-model:file-list="config.fileList"
            name="file"
            accept=".xml"
            :showUploadList="false"
            :action="uploadUrl"
            :disabled="config.uploadLoading"
            @change="(args) => uploadFile(args, config)"
          >
            <a-button type="primary" ghost class="g-mr-12">{{$t('upload')}}</a-button>
          </a-upload>
          <span v-if="config.isSuccess" class="config-value" :class="{'view-active': config.value}" @click="downLoadFile(config.value)">{{config.fileName}}</span>
        </a-form-item>
        <a-form-item>
          <p class="header">{{$t('authConfig')}}</p>
        </a-form-item>
        <a-form-item label="auth_config.type" :name="['authConfig', 'auth_config.type']" :rules="[{ required: isEdit }]">
          <a-select
            v-if="isEdit"
            v-model:value="formState.authConfig['auth_config.type']"
            :placeholder="placeholder.selectPh"
            :options="authConfigTypeOps"
          />
          <span v-else class="config-value">{{formState.authConfig['auth_config.type']}}</span>
        </a-form-item>
        <a-form-item v-if="formState.authConfig['auth_config.type'] === 'SIMPLE'" label="auth_config.hadoop_username" :name="['authConfig', 'auth_config.hadoop_username']" :rules="[{ required: isEdit }]">
          <a-input v-if="isEdit" v-model:value="formState.authConfig['auth_config.hadoop_username']" />
          <span v-else class="config-value">{{formState.authConfig['auth_config.hadoop_username']}}</span>
        </a-form-item>
        <a-form-item v-if="formState.authConfig['auth_config.type'] === 'KERBEROS'" label="auth_config.principal" :name="['authConfig', 'auth_config.principal']" :rules="[{ required: isEdit }]">
          <a-input v-if="isEdit" v-model:value="formState.authConfig['auth_config.principal']" />
          <span v-else class="config-value">{{formState.authConfig['auth_config.principal']}}</span>
        </a-form-item>
        <a-form-item v-if="formState.authConfig['auth_config.type'] === 'KERBEROS'" label="auth_config.keytab" :name="['authConfig', 'auth_config.keytab']" :rules="[{ required: isEdit }]">
          <a-input v-if="isEdit" v-model:value="formState.authConfig['auth_config.keytab']" />
          <span v-else class="config-value">{{formState.authConfig['auth_config.keytab']}}</span>
        </a-form-item>
        <a-form-item v-if="formState.authConfig['auth_config.type'] === 'KERBEROS'" label="auth_config.krb5" :name="['authConfig', 'auth_config.krb5']" :rules="[{ required: isEdit }]">
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
      <a-button @click="handleCancle">{{$t('cancel')}}</a-button>
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
import { Modal, message, UploadChangeParam } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import Properties from './Properties.vue'
import { download } from '@/utils/request'
import { usePlaceholder } from '@/hooks/usePlaceholder'
import { useRoute } from 'vue-router'

interface IStorageConfigItem {
  label: string
  value: string
  fileName: string
  fileList: string[],
  uploadLoading: boolean
  isSuccess: boolean
}

interface FormState {
  catalog: IMap<string>
  storageConfig: IMap<string>
  authConfig: IMap<string>
  properties: IMap<string>
  storageConfigArray: IStorageConfigItem[]
}

const props = defineProps<{ catalog: ICatalogItem, isEdit: boolean }>()
const emit = defineEmits<{
 (e: 'updateEdit', val: boolean, catalog?: ICatalogItem): void,
 (e: 'updateCatalogs'): void
}>()

const { t } = useI18n()
const route = useRoute()
const placeholder = reactive(usePlaceholder())
const isEdit = computed(() => {
  return props.isEdit
})
const uploadUrl = computed(() => {
  return '/ams/v1/files'
})
const isNewCatalog = computed(() => {
  return props.catalog.catalogName === 'new catalog'
})
const loading = ref<boolean>(false)
const formRef = ref()
const propertiesRef = ref()
const storageConfigFileNameMap = {
  'storage_config.core-site': 'core-site.xml',
  'storage_config.hdfs-site': 'hdfs-site.xml',
  'storage_config.hive-site': 'hive-site.xml'
}
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
  properties: {},
  storageConfigArray: []
})
const authConfigTypeOps = reactive<ILableAndValue[]>([{
  label: 'SIMPLE',
  value: 'SIMPLE'
}, {
  label: 'KERBEROS',
  value: 'KERBEROS'
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
  const { catalog, type } = route.query
  formState.catalog.name = catalog || ''
  formState.catalog.type = type || undefined
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
    const { catalogName, catalogType } = props.catalog
    if (!catalogName) { return }
    if (isNewCatalog.value) {
      formState.catalog.name = catalogName
      formState.catalog.type = catalogType
      formState.authConfig = {}
      formState.storageConfig = {}
      formState.properties = {}
      formState.storageConfigArray.length = 0
    }
    const res = await getCatalogsSetting(catalogName)
    if (!res) { return }
    const { name, type, storageConfig, authConfig, properties } = res
    formState.catalog.name = name
    formState.catalog.type = type
    formState.authConfig = authConfig
    formState.storageConfig = storageConfig
    formState.properties = properties || {}
    formState.storageConfigArray.length = 0
    Object.keys(storageConfig).forEach(key => {
      if (key !== 'storage_config.storage.type') {
        const item: IStorageConfigItem = {
          label: key,
          value: storageConfig[key],
          fileName: storageConfigFileNameMap[key],
          fileList: [],
          uploadLoading: false,
          isSuccess: false
        }
        formState.storageConfigArray.push(item)
      }
    })
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
  if (res) {
    deleteCatalogModal()
    return
  }
  Modal.confirm({
    title: t('cannotDeleteModalTitle'),
    content: t('cannotDeleteModalContent'),
    wrapClassName: 'not-delete-modal'
  })
}
async function validatorName(rule, value) {
  if (!value) {
    return Promise.reject(new Error(t('inputPlaceholder')))
  }
  if ((/^[a-zA-Z][\w-]*$/.test(value))) {
    return Promise.resolve()
  } else {
    return Promise.reject(new Error(t('invalidInput')))
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
        message.success(`${t('save')} ${t('success')}`)
        formRef.value.resetFields()
        emit('updateCatalogs')
        emit('updateEdit', false, {
          catalogName: catalog.name,
          catalogType: catalog.type
        })
        getConfigInfo()
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
      message.success(`${t('remove')} ${t('success')}`)
      emit('updateCatalogs')
    }
  })
}
function uploadFile(info: UploadChangeParam, config) {
  if (info.file.status === 'uploading') {
    config.uploadLoading = true
  } else {
    console.log(info.file, info.fileList)
    config.uploadLoading = false
  }
  if (info.file.status === 'done') {
    config.isSuccess = true
    message.success(`${info.file.name} ${t('uploaded')} ${t('success')}`)
  } else if (info.file.status === 'error') {
    config.isSuccess = false
    message.error(`${info.file.name} ${t('uploaded')} ${t('failed')}`)
  }
}
function downLoadFile(id: string) {
  if (!id) return
  const url = `/ams/v1/files/${id}`
  download(url)
}
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
      > label {
        word-break: break-all;
        white-space: pre-wrap;
      }
      width: 280px;
      margin-right: 16px;
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
