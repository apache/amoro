<template>
  <div class="catalogs-wrap g-flex">
    <div class="catalog-list-left">
      <div class="catalog-header">{{`${$t('catalog')} ${$t('list')}`}}</div>
      <ul v-if="catalogs.length && !loading" class="catalog-list">
        <li v-for="item in catalogs" :key="item.catalogName" class="catalog-item g-text-nowrap" :class="{'active': item.catalogName === curCatalog.catalogName}" @click="handleClick(item)">
          {{ item.catalogName }}
        </li>
      </ul>
      <a-empty v-if="!catalogs.length && !loading" :image="simpleImage"></a-empty>
      <a-button @click="addCatalog" :disabled="curCatalog.catalogName === NEW_CATALOG" class="add-btn">+</a-button>
    </div>
    <div class="catalog-detail">
      <Detail :catalog="curCatalog" :isEdit="isEdit" @updateEdit="updateEdit" @updateCatalogs="updateCatalogs" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ICatalogItem } from '@/types/common.type'
import { getCatalogList } from '@/services/table.service'
import Detail from './Detail.vue'
import { Modal, Empty } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const catalogs = reactive<ICatalogItem[]>([])
const curCatalog = reactive<ICatalogItem>({})
const isEdit = ref<boolean>(false)
const NEW_CATALOG = 'new catalog'
const loading = ref<boolean>(false)
const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE

async function getCatalogs() {
  try {
    loading.value = true
    const res = await getCatalogList()
    catalogs.length = 0;
    (res || []).forEach((ele: ICatalogItem) => {
      catalogs.push({
        catalogName: ele.catalogName,
        catalogType: ele.catalogType
      })
    })
    const { catalog = '', type } = route.query
    const item: ICatalogItem = {}
    if (decodeURIComponent(catalog as string) === NEW_CATALOG) {
      addCatalog()
      return
    }
    if (catalog) {
      item.catalogName = catalog
      item.catalogType = type
    } else {
      item.catalogName = catalogs[0]?.catalogName
      item.catalogType = catalogs[0]?.catalogType
    }
    selectCatalog(item)
  } finally {
    loading.value = false
  }
}
function handleClick(item: ICatalogItem) {
  if (isEdit.value) {
    leaveConfirm(() => {
      selectCatalog(item)
      isEdit.value = false
    })
  } else {
    selectCatalog(item)
  }
}
function selectCatalog(item: ICatalogItem) {
  const { catalogName, catalogType } = item
  curCatalog.catalogName = catalogName
  curCatalog.catalogType = catalogType
  router.replace({
    path: '/catalogs',
    query: {
      catalog: encodeURIComponent(curCatalog.catalogName),
      type: curCatalog.catalogType
    }
  })
}

function updateEdit(val, catalog?) {
  isEdit.value = val
  const index = catalogs.findIndex((ele: ICatalogItem) => ele.catalogName === NEW_CATALOG)
  if (index > -1) {
    catalogs.splice(index)
    const item: ICatalogItem = {
      catalogName: catalogs[0]?.catalogName,
      catalogType: catalogs[0]?.catalogType
    }
    selectCatalog(item)
    return
  }
  catalog && selectCatalog(catalog)
}
function updateCatalogs() {
  getCatalogs()
}
function addCatalog() {
  const item: ICatalogItem = {
    catalogName: NEW_CATALOG,
    catalogType: ''
  }
  catalogs.push(item)
  selectCatalog(item)
  isEdit.value = true
}
onMounted(() => {
  getCatalogs()
})
function leaveConfirm(cb?) {
  Modal.confirm({
    title: t('leavePageModalTitle'),
    content: t('leavePageModalContent'),
    okText: t('leave'),
    onOk: async() => {
      cb && await cb()
    }
  })
}
onBeforeRouteLeave((to, form, next) => {
  if (isEdit.value) {
    leaveConfirm(() => {
      next()
    })
  } else {
    next()
  }
})

</script>

<style lang="less" scoped>
.catalogs-wrap {
  height: 100%;
  .catalog-list-left {
    width: 200px;
    height: 100%;
    // border: 1px solid #e8e8f0;
    box-shadow: 0 1px 4px rgb(0 21 41 / 8%);
    text-align: center;
  }
  .catalog-header {
    height: 48px;
    line-height: 48px;
    font-size: 14px;
    border-bottom: 1px solid #e8e8f0;
  }
  .catalog-list {
    background-color: #fff;
    max-height: calc(100% - 88px);
    overflow-y: auto;
    .catalog-item {
      height: 40px;
      line-height: 40px;
      border-bottom: 1px solid #e8e8f0;
      padding: 0 12px;
      &.active {
        color: #fff;
        background-color: @primary-color;
      }
      &:hover {
        cursor: pointer;
        color: #fff;
        background-color: @primary-color;
      }
      &:last-child {
        border-bottom: 0;
      }
    }
  }
  .add-btn {
    height: 40px;
    line-height: 40px;
    width: 100%;
  }
  .catalog-detail {
    display: flex;
    flex: 1;
  }
}
</style>
