<template>
  <div class="resource-card g-flex g-mr-16">
    <div class="card g-mr-16">
      <span class="card-name">{{$t('catalog')}}</span>
      <p class="card-value">3</p>
    </div>
    <div class="card g-mr-16">
      <span class="card-name">{{$t('table')}}</span>
      <p class="card-value">3</p>
    </div>
    <div class="card g-mr-16">
      <span class="card-name">{{$t('data')}}</span>
      <p class="card-value">300.00<span class="unit">G</span></p>
    </div>
    <div class="card g-mr-16 card-resource">
      <div class="g-flex-row">
        <div class="card-inner card-left">
          <span class="card-name">{{$t('resourceCpu')}}</span>
          <p class="card-value">33<span class="unit">Core</span></p>
        </div>
        <div class="card-inner card-right">
          <span class="card-name">{{$t('resourceMemory')}}</span>
          <p class="card-value">123<span class="unit">Core</span></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'

</script>
<style lang="less" scoped>
.resource-card {
  .card {
    display: flex;
    flex: 1;
    flex-direction: column;
    background-color: #fff;
    border-radius: 2px;
    padding: 16px 24px;
    box-shadow: 0 1px 4px rgb(0 21 41 / 8%);
    .card-name {
      font-size: 14px;
      line-height: 22px;
    }
    .card-value {
      font-weight: 500;
      font-size: 30px;
      line-height: 40px;
      color: @header-color;
      .unit {
        font-size: 12px;
        line-height: 20px;
        margin-left: 8px;
      }
    }
    .card-left {
      border-right: 1px solid rgba(0, 0, 0, 0.06);
    }
    .card-right {
      padding-left: 16px;
    }
    &.card-resource {
      flex: 1.5;
      margin-right: 0;
      flex-shrink: 0;
      min-width: 382px;
      padding: 16px 24px;
    }
    .card-inner {
      flex: 1;
    }
  }
}
</style>
