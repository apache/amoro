<template>
  <RecycleScroller
    class="scroller"
    :items="items"
    :item-size="40"
    key-field="id"
    v-slot="{ item }"
  >
    <div class="desc">
      <table-outlined v-if="iconName === 'tableOutlined'" class="g-mr-8" />
      <svg-icon v-if="iconName === 'database'" icon-class="database" class="g-mr-8" />
      <p :title="item.label" class="name g-text-nowrap">
        {{ item.label }}
      </p>
    </div>
  </RecycleScroller>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { RecycleScroller } from 'vue-virtual-scroller'
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css'
import { TableOutlined } from '@ant-design/icons-vue'

export default defineComponent ({
  components: {
    TableOutlined,
    RecycleScroller
  },
  setup() {},
  props: {
    items: {
      type: Array,
      default: () => []
    },
    itemSize: {
      type: Number,
      default: 40
    },
    iconName: {
      type: String,
      default: 'tableOutlined'
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
}
.desc {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  padding: 10px 12px;
  height: 40px;
  color: #102048;
  cursor: pointer;
  &.active,
  &:hover {
    background-color: #f6f7fa;
    color: @primary-color;
  }
  .name {
    max-width: 200px;
  }
}
</style>
