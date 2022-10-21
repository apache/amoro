<template>
  <div class="catalogs-wrap g-flex">
    <div class="catalog-list-left">
      <div class="catalog-header">{{`${$t('catalog')} ${$t('list')}`}}</div>
      <ul class="catalog-list">
        <li v-for="item in catalogs" :key="item.catalogName" class="catalog-item g-text-nowrap" :class="{'active': item.catalogName === curCatalog.catalogName}" @click="handleClick(item)">
          {{ item.catalogName }}
        </li>
      </ul>
      <a-button type="primary" class="add-btn">+</a-button>
    </div>
    <div class="catalog-detail">
      <Detail :catalog="curCatalog" :isEdit="isEdit" @updateEdit="updateEdit" @updateCatalogs="updateCatalogs" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { ICatalogItem } from '@/types/common.type'
import { getCatalogList } from '@/services/table.service'
import Detail from './Detail.vue'

const catalogs = reactive<ICatalogItem[]>([])
const curCatalog = reactive<ICatalogItem>({})
const isEdit = ref<boolean>(false)

async function getCatalogs() {
  const res = await getCatalogList()
  catalogs.length = 0;
  (res || []).forEach((ele: ICatalogItem) => {
    catalogs.push({
      catalogName: ele.catalogName,
      catalogType: ele.catalogType
    })
  })
  curCatalog.catalogName = catalogs[0]?.catalogName
  curCatalog.catalogType = catalogs[0]?.catalogType
}
function handleClick(item: ICatalogItem) {
  const { catalogName, catalogType } = item
  curCatalog.catalogName = catalogName
  curCatalog.catalogType = catalogType
}

function updateEdit(val) {
  isEdit.value = val
  console.log('isEdit.value', isEdit.value)
}
function updateCatalogs() {
  getCatalogs()
}
onMounted(() => {
  getCatalogs()
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
    }
  }
  .add-btn {
    height: 40px;
    line-height: 40px;
    width: 100%;
    border: 0;
  }
  .catalog-detail {
    display: flex;
    flex: 1;
  }
}
</style>
