<template>
  <RecycleScroller
    class="scroller"
    :items="items"
    :item-size="40"
    key-field="id"
    v-slot="{ item }"
    v-if="items.length && !loading"
  >
    <div :class="{'active': activeItem === item.label, 'hive-table': item.type === 'HIVE'}" @mouseenter="handleMouseEnter(item)" @click="handleClickTable(item)" class="desc">
      <table-outlined v-if="iconName === 'tableOutlined'" class="g-mr-8" />
      <svg-icon v-if="iconName === 'database'" icon-class="database" class="g-mr-8" />
      <p :title="item.label" class="name g-text-nowrap">
        {{ item.label }}
      </p>
    </div>
  </RecycleScroller>
  <a-empty class="theme-dark" v-if="!items.length && !loading" :image="simpleImage"></a-empty>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { RecycleScroller } from 'vue-virtual-scroller'
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css'
import { TableOutlined } from '@ant-design/icons-vue'
import { IMap } from '@/types/common.type'
import { Empty } from 'ant-design-vue'

export default defineComponent ({
  components: {
    TableOutlined,
    RecycleScroller
  },
  emits: ['mouseEnter', 'handleClickTable'],
  setup(props, { emit }) {
    const handleMouseEnter = (item: IMap<string>) => {
      emit('mouseEnter', item.label)
    }

    const handleClickTable = (item: IMap<string>) => {
      emit('handleClickTable', item)
    }

    return {
      simpleImage: Empty.PRESENTED_IMAGE_SIMPLE,
      handleMouseEnter,
      handleClickTable
    }
  },
  props: {
    items: {
      type: Array,
      default: () => []
    },
    activeItem: {
      type: String,
      default: ''
    },
    itemSize: {
      type: Number,
      default: 40
    },
    iconName: {
      type: String,
      default: 'tableOutlined'
    },
    loading: {
      type: Boolean,
      default: false
    }
  }
})
</script>

<style lang="less" scoped>
.scroller {
  height: calc(100% - 80px);
  padding: 4px 0 0 4px;
  margin-top: 4px;
  box-sizing: border-box;
  :deep(.vue-recycle-scroller__item-view) {
    padding-right: 4px;
  }
}
.desc {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  padding: 10px 12px;
  height: 40px;
  color: rgba(255,255,255,0.8);
  cursor: pointer;
  &.active,
  &:hover {
    .name {
      color: #fff !important;
    }
    background-color: @dark-gray-color;
    color: #fff;
  }
  &.hive-table {
    .name {
      color: #9ea4aa;
    }
    &:hover {
      .name {
        color: #fff;
      }
      background-color: @dark-gray-color;
    }
  }
  .name {
    max-width: 200px;
  }
}
</style>
