<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 / -->

<script lang="ts">
import { defineComponent } from 'vue'
import { RecycleScroller } from 'vue-virtual-scroller'
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css'
import { Empty as AEmpty } from 'ant-design-vue'
import type { IMap } from '@/types/common.type'
import { tableTypeIconMap } from '@/types/common.type'

export default defineComponent ({
  components: {
    RecycleScroller,
  },
  props: {
    items: {
      type: Array,
      default: () => [],
    },
    activeItem: {
      type: String,
      default: '',
    },
    itemSize: {
      type: Number,
      default: 40,
    },
    iconName: {
      type: String,
      default: 'tableOutlined',
    },
    loading: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['mouseEnter', 'handleClickTable'],
  setup(_, { emit }) {
    const handleMouseEnter = (item: IMap<string>) => {
      emit('mouseEnter', item.label)
    }

    const handleClickTable = (item: IMap<string>) => {
      emit('handleClickTable', item)
    }

    return {
      simpleImage: AEmpty.PRESENTED_IMAGE_SIMPLE,
      tableTypeIconMap,
      handleMouseEnter,
      handleClickTable,
    }
  },
})
</script>

<template>
  <RecycleScroller
    v-if="items.length && !loading"
    v-slot="{ item }"
    class="scroller"
    :items="items"
    :item-size="40"
    key-field="id"
  >
    <div :class="{ 'active': activeItem === item.label, 'hive-table': item.type === 'HIVE' }" class="desc" @mouseenter="handleMouseEnter(item)" @click="handleClickTable(item)">
      <svg-icon v-if="iconName === 'database'" icon-class="database" class="table-icon g-mr-8" />
      <svg-icon v-else :icon-class="tableTypeIconMap[item.type as keyof typeof tableTypeIconMap]" class="table-icon g-mr-8" />
      <p :title="item.label" class="name g-text-nowrap">
        {{ item.label }}
      </p>
    </div>
  </RecycleScroller>
  <a-empty v-if="!items.length && !loading" class="theme-dark" :image="simpleImage" />
</template>

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
  color: rgba(255,255,255,0.8);
  cursor: pointer;
  &.active,
  &:hover {
    background-color: @dark-gray-color;
    color: #fff;
  }
  .name {
    max-width: 200px;
    margin-top: -2px;
  }
  .table-icon {
    font-size: 14px;
  }
}
</style>
