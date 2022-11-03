<template>
  <div class="time-select-wrap">
    <a-select
      v-model:value="time"
      :options="options"
      @change="changeTime"
    />
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref, shallowReactive } from 'vue'
import { useI18n } from 'vue-i18n'
import dayjs, { Dayjs } from 'dayjs'

const emit = defineEmits<{
 (e: 'timeChange', value: { start: string, end: string }): void
}>()
const { t } = useI18n()

const options = shallowReactive([
  { label: t('lastTime', { time: '1h' }), value: '1' },
  { label: t('lastTime', { time: '6h' }), value: '6' },
  { label: t('lastTime', { time: '12h' }), value: '12' },
  { label: t('lastTime', { time: '24h' }), value: '24' },
  { label: t('lastTime', { time: '72h' }), value: '72' },
  { label: t('lastTime', { time: '7d' }), value: 7 * 24 + '' }
])

const time = ref<string>('24')

function changeTime () {
  emitValue()
}

function emitValue() {
  const end = dayjs().format('YYYY-MM-DD HH:mm:ss')
  const start = dayjs()
    .subtract(+time.value, 'hours')
    .format('YYYY-MM-DD HH:mm:ss')
  emit('timeChange', { start, end })
}

onMounted(() => {
  emitValue()
})
</script>
<style lang="less" scoped>
</style>
