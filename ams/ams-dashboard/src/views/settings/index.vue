<template>
  <div class="tables-wrap">settings
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted } from 'vue'
// import { useI18n } from 'vue-i18n'

export default defineComponent({
  name: 'Settings',
  components: {
  },
  setup() {
    // const { t, d } = useI18n()

    onMounted(() => {
    })
    return {

    }
  }
})

</script>

<style lang="less" scoped>
.echarts-wrap {
  display: flex;
  align-items: center;
  .line-chart {
    flex: 1;
  }
}
  .top-list {
    padding: 0 12px;
    .filter-options {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
    }
  }
</style>
