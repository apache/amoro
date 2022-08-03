<template>
  <div class="upgrade-table">
    <div class="nav-bar">
      <left-outlined @click="goBack" />
      <span class="title g-ml-8">{{$t('upgradeHiveTable')}}</span>
    </div>
    <div class="content">
      <div class="table-attrs"></div>
      <div class="footer-btn">
        <a-button type="primary" class="btn g-mr-12">{{$t('ok')}}</a-button>
        <a-button type="ghost" class="btn">{{$t('cancel')}}</a-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
// import { reactive, ref } from 'vue'
// import type { SelectProps } from 'ant-design-vue'
import { LeftOutlined } from '@ant-design/icons-vue'

const emit = defineEmits<{
 (e: 'goBack'): void
}>()

function goBack() {
  emit('goBack')
}
</script>

<style lang="less" scoped>
.upgrade-table {
  .nav-bar {
    padding-left: 12px;
  }
  .content {
    padding: 0 24px;
    display: flex;
    flex-direction: column;
  }
  .table-attrs {}
  .footer-btn {
    .btn {
      min-width: 78px;
    }
  }
}
</style>
