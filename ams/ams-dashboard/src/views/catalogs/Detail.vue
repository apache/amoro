<template>
  <div class="detail-wrap">
    <div class="detail-content-wrap">
      <div class="content-wrap">
        <a-form ref="formRef" :model="formState" class="catalog-form">
          <a-form-item :label="$t('name')" :name="['catalog', 'name']" :rules="[{ required: isEdit && isNewCatalog, validator: validatorName }]">
            <a-input v-if="isEdit && isNewCatalog" v-model:value="formState.catalog.name" />
            <span v-else>{{formState.catalog.name}}</span>
          </a-form-item>
          <a-form-item :label="$t('metastore')" :name="['catalog', 'type']" :rules="[{ required: isEdit && isNewCatalog }]">
            <a-select
              v-if="isEdit && isNewCatalog"
              v-model:value="formState.catalog.type"
              :options="catalogTypeOps"
              :placeholder="placeholder.selectPh"
              @change="changeMetastore"
            />
            <span v-else>{{metastoreType}}</span>
          </a-form-item>
          <a-form-item :label="$t('tableFormat')" :name="['tableFormat']" :rules="[{ required: isEdit && isNewCatalog }]">
            <a-radio-group :disabled="!isEdit || !isNewCatalog" v-model:value="formState.tableFormat" name="radioGroup">
              <a-radio v-if="isHiveMetastore" :value="tableFormatMap.HIVE">Hive</a-radio>
              <a-radio :value="tableFormatMap.ICEBERG">Iceberg</a-radio>
            </a-radio-group>
          </a-form-item>
          <a-form-item>
            <p class="header">{{$t('storageConfig')}}</p>
          </a-form-item>
          <a-form-item
            v-for="config in formState.storageConfigArray"
            :key="config.label"
            :label="config.label"
            class="g-flex-ac">
            <a-upload
              v-if="isEdit"
              v-model:file-list="config.fileList"
              name="file"
              accept=".xml"
              :showUploadList="false"
              :action="uploadUrl"
              :disabled="config.uploadLoading"
              @change="(args) => uploadFile(args, config, 'STORAGE')"
            >
              <a-button type="primary" ghost :loading="config.uploadLoading" class="g-mr-12">{{$t('upload')}}</a-button>
            </a-upload>
            <span v-if="config.isSuccess || config.fileName" class="config-value" :class="{'view-active': !!config.fileUrl}" @click="viewFileDetail(config.fileUrl)">{{config.fileName}}</span>
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
          <div v-if="formState.authConfig['auth_config.type'] === 'KERBEROS'">
            <a-form-item
              v-for="config in formState.authConfigArray"
              :key="config.label"
              :label="config.label"
              class="g-flex-ac">
              <a-upload
                v-if="isEdit"
                v-model:file-list="config.fileList"
                name="file"
                :accept="config.label === 'auth_config.keytab' ? '.keytab' : '.conf'"
                :showUploadList="false"
                :action="uploadUrl"
                :disabled="config.uploadLoading"
                @change="(args) => uploadFile(args, config)"
              >
                <a-button type="primary" ghost :loading="config.uploadLoading" class="g-mr-12">{{$t('upload')}}</a-button>
              </a-upload>
              <span v-if="config.isSuccess || config.fileName" class="config-value" :class="{'view-active': !!config.fileUrl}" @click="viewFileDetail(config.fileUrl)">{{config.fileName}}</span>
            </a-form-item>
          </div>
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
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { getCatalogsTypes, getCatalogsSetting, saveCatalogsSetting, checkCatalogStatus, delCatalog } from '@/services/setting.services'
import { ILableAndValue, ICatalogItem, IMap } from '@/types/common.type'
import { Modal, message, UploadChangeParam } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import Properties from './Properties.vue'
import { usePlaceholder } from '@/hooks/usePlaceholder'
import { useRoute } from 'vue-router'

interface IStorageConfigItem {
  label: string
  value: string
  fileName: string
  fileUrl: string
  fileId: string
  fileList: string[]
  uploadLoading: boolean
  isSuccess: boolean
}

interface FormState {
  catalog: IMap<string>
  tableFormat: string
  storageConfig: IMap<string>
  authConfig: IMap<string>
  properties: IMap<string>
  storageConfigArray: IStorageConfigItem[]
  authConfigArray: IStorageConfigItem[]
}

const props = defineProps<{ isEdit: boolean }>()
const emit = defineEmits<{
 (e: 'updateEdit', val: boolean, catalog?: ICatalogItem): void,
 (e: 'updateCatalogs'): void
}>()

const { t } = useI18n()
const route = useRoute()
const placeholder = reactive(usePlaceholder())
const metastoreType = ref<string>('')
const isEdit = computed(() => {
  return props.isEdit
})
const uploadUrl = computed(() => {
  return '/ams/v1/files'
})
const isNewCatalog = computed(() => {
  const catalog = (route.query?.catalog || '').toString()
  return decodeURIComponent(catalog) === 'new catalog'
})
const isHiveMetastore = computed(() => {
  return formState.catalog.type === 'hive'
})
const loading = ref<boolean>(false)
const formRef = ref()
const propertiesRef = ref()
const tableFormatMap = {
  HIVE: 'HIVE',
  ICEBERG: 'ICEBERG'
}
const storageConfigFileNameMap = {
  'storage_config.core-site': 'core-site.xml',
  'storage_config.hdfs-site': 'hdfs-site.xml',
  'storage_config.hive-site': 'hive-site.xml'
}
const newCatalogConfig = {
  storageConfig: {
    'storage_config.core-site': '',
    'storage_config.hdfs-site': ''
  },
  authConfig: {
    'auth_config.keytab': '',
    'auth_config.krb5': ''
  }
}
const formState:FormState = reactive({
  catalog: {
    name: '',
    type: 'ams'
  },
  tableFormat: '',
  storageConfig: {},
  authConfig: {},
  properties: {},
  storageConfigArray: [],
  authConfigArray: []
})
const authConfigTypeOps = reactive<ILableAndValue[]>([{
  label: 'SIMPLE',
  value: 'SIMPLE'
}, {
  label: 'KERBEROS',
  value: 'KERBEROS'
}])

watch(() => route.query,
  (value) => {
    value && initData()
  }, {
    immediate: true,
    deep: true
  }
)
const catalogTypeOps = reactive<ILableAndValue[]>([])

function initData() {
  getConfigInfo()
}
async function getCatalogTypeOps() {
  const res = await getCatalogsTypes();
  (res || []).forEach(ele => {
    catalogTypeOps.push({
      label: ele.display,
      value: ele.value
    })
  })
  getMetastoreType()
}
function getMetastoreType() {
  metastoreType.value = (catalogTypeOps.find(ele => ele.value === formState.catalog.type) || {}).label
}
async function getConfigInfo() {
  try {
    loading.value = true
    const { catalog, type } = route.query
    if (!catalog) { return }
    if (isNewCatalog.value) {
      formState.catalog.name = ''
      formState.catalog.type = type || 'ams'
      formState.tableFormat = tableFormatMap.ICEBERG
      formState.authConfig = { ...newCatalogConfig.authConfig }
      formState.storageConfig = { ...newCatalogConfig.storageConfig }
      formState.properties = {}
      formState.storageConfigArray.length = 0
      formState.authConfigArray.length = 0
    } else {
      const res = await getCatalogsSetting(catalog)
      if (!res) { return }
      const { name, type, tableFormat, storageConfig, authConfig, properties } = res
      formState.catalog.name = name
      formState.catalog.type = type
      formState.tableFormat = tableFormat
      formState.authConfig = authConfig
      formState.storageConfig = storageConfig
      formState.properties = properties || {}
      formState.storageConfigArray.length = 0
      formState.authConfigArray.length = 0
    }
    getMetastoreType()
    const { storageConfig, authConfig } = formState
    Object.keys(storageConfig).forEach(key => {
      const configArr = ['storage_config.core-site', 'storage_config.hdfs-site']
      if (isHiveMetastore.value) {
        configArr.push('storage_config.hive-site')
      }
      if (configArr.includes(key)) {
        const item: IStorageConfigItem = {
          label: key,
          value: storageConfig[key]?.fileName,
          fileName: storageConfig[key]?.fileName,
          fileUrl: storageConfig[key]?.fileUrl,
          fileId: '',
          fileList: [],
          uploadLoading: false,
          isSuccess: false
        }
        formState.storageConfigArray.push(item)
      }
    })
    Object.keys(authConfig).forEach(key => {
      if (['auth_config.keytab', 'auth_config.krb5'].includes(key)) {
        const item: IStorageConfigItem = {
          label: key,
          value: authConfig[key]?.fileName,
          fileName: authConfig[key]?.fileName,
          fileUrl: authConfig[key]?.fileUrl,
          fileId: '',
          fileList: [],
          uploadLoading: false,
          isSuccess: false
        }
        formState.authConfigArray.push(item)
      }
    })
  } catch (error) {
  } finally {
    loading.value = false
  }
}

function changeMetastore() {
  formState.tableFormat = isHiveMetastore.value ? tableFormatMap.HIVE : tableFormatMap.ICEBERG
  if (!isNewCatalog.value) { return }
  const index = formState.storageConfigArray.findIndex(item => item.label === 'storage_config.hive-site')
  if (isHiveMetastore.value) {
    if (index > -1) {
      return
    }
    formState.storageConfigArray.push({
      label: 'storage_config.hive-site',
      value: '',
      fileName: '',
      fileUrl: '',
      fileId: '',
      fileList: [],
      uploadLoading: false,
      isSuccess: false
    })
    formState.storageConfig['storage_config.hive-site'] = ''
  } else {
    if (index > -1) {
      formState.storageConfigArray.splice(index, 1)
      delete formState.storageConfig['storage_config.hive-site']
    }
  }
}
function handleEdit() {
  emit('updateEdit', true)
}
async function handleRemove() {
  const res = await checkCatalogStatus(formState.catalog.name)
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
function getFileIdParams() {
  const { storageConfig, authConfig, storageConfigArray, authConfigArray } = formState
  Object.keys(authConfig).forEach(key => {
    if (['auth_config.keytab', 'auth_config.krb5'].includes(key)) {
      const id = (authConfigArray.find(item => item.label === key) || {}).fileId
      authConfig[key] = id
    }
  })
  Object.keys(storageConfig).forEach(key => {
    const id = (storageConfigArray.find(item => item.label === key) || {}).fileId
    storageConfig[key] = id
  })
}
function handleSave() {
  formRef.value
    .validateFields()
    .then(async() => {
      const { catalog, tableFormat, storageConfig, authConfig } = formState
      const properties = await propertiesRef.value.getProperties()
      if (!properties) {
        return
      }
      getFileIdParams()
      await saveCatalogsSetting({
        isCreate: isNewCatalog.value,
        ...catalog,
        tableFormat,
        storageConfig,
        authConfig,
        properties
      }).then(() => {
        message.success(`${t('save')} ${t('success')}`)
        emit('updateEdit', false, {
          catalogName: catalog.name,
          catalogType: catalog.type
        })
        getConfigInfo()
        formRef.value.resetFields()
      }).catch(() => {
        message.error(`${t('save')} ${t('failed')}`)
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
      await delCatalog(formState.catalog.name)
      message.success(`${t('remove')} ${t('success')}`)
      emit('updateEdit', false, {})
    }
  })
}
function uploadFile(info: UploadChangeParam, config, type?) {
  try {
    if (info.file.status === 'uploading') {
      config.uploadLoading = true
    } else {
      config.uploadLoading = false
    }
    if (info.file.status === 'done') {
      config.isSuccess = true
      config.fileName = type === 'STORAGE' ? storageConfigFileNameMap[config.label] : info.file.name
      const { url, id } = info.file.response.result || {}
      config.fileUrl = url
      config.fileId = id
      message.success(`${info.file.name} ${t('uploaded')} ${t('success')}`)
    } else if (info.file.status === 'error') {
      config.isSuccess = false
      message.error(`${info.file.name} ${t('uploaded')} ${t('failed')}`)
    }
  } catch (error) {
    message.error(`${t('uploaded')} ${t('failed')}`)
  }
}
function viewFileDetail(url: string) {
  url && window.open(url)
}
onMounted(() => {
  getCatalogTypeOps()
})

</script>

<style lang="less" scoped>
.detail-wrap {
  height: 100%;
  padding: 16px 16px 16px 24px;
  display: flex;
  flex: 1;
  flex-direction: column;
  .detail-content-wrap {
    padding: 0 200px 24px 0;
  }
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
