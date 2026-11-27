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
-->

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import {
  getTableFilterDatabaseList,
  getTableFilterMatchedTables,
  getTableFilterTableList,
} from '@/services/table.service'

interface TableOption {
  key: string
  database: string
  tableName: string
}

const props = withDefaults(defineProps<{
  catalogName: string
  modelValue: string
  disabled?: boolean
}>(), {
  disabled: false,
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const { t } = useI18n()
const visible = ref(false)
const loading = ref(false)
const tables = ref<TableOption[]>([])
const databaseSearch = ref('')
const tableSearch = ref('')
const activeDatabase = ref('')
const selectedKeys = ref(new Set<string>())
const inheritedKeys = ref(new Set<string>())
const matchedKeys = ref(new Set<string>())
const customPatterns = ref<string[]>([])

const databases = computed(() => {
  const keyword = databaseSearch.value.trim().toLowerCase()
  return [...new Set(tables.value.map(table => table.database))]
    .filter(database => !keyword || database.toLowerCase().includes(keyword))
    .sort((left, right) => left.localeCompare(right))
})

const activeDatabaseTables = computed(() => {
  const keyword = tableSearch.value.trim().toLowerCase()
  return tables.value.filter((table) => {
    return table.database === activeDatabase.value
      && (!keyword || table.tableName.toLowerCase().includes(keyword))
  })
})

const selectedCount = computed(() => selectedKeys.value.size)

const selectableActiveTableKeys = computed(() => {
  return activeDatabaseTables.value
    .filter(table => !inheritedKeys.value.has(table.key))
    .map(table => table.key)
})

const allVisibleTablesSelected = computed(() => {
  return selectableActiveTableKeys.value.length > 0
    && selectableActiveTableKeys.value.every(key => selectedKeys.value.has(key))
})

const someVisibleTablesSelected = computed(() => {
  return selectableActiveTableKeys.value.some(key => selectedKeys.value.has(key))
    && !allVisibleTablesSelected.value
})

watch(() => [props.catalogName, props.modelValue], () => {
  if (!visible.value) {
    initializeSelection()
  }
}, {
  immediate: true,
})

function escapeRegex(value: string) {
  return value.replace(/[.\\*+?^$|()[\]{}]/g, '\\$&')
}

function isOuterGroup(value: string) {
  if (!value.startsWith('(') || value.startsWith('(?') || !value.endsWith(')')) {
    return false
  }

  let depth = 0
  let escaped = false
  for (let index = 0; index < value.length; index += 1) {
    const character = value[index]
    if (escaped) {
      escaped = false
      continue
    }
    if (character === '\\') {
      escaped = true
      continue
    }
    if (character === '(') {
      depth += 1
    }
    else if (character === ')') {
      depth -= 1
      if (depth === 0 && index !== value.length - 1) {
        return false
      }
    }
  }
  return depth === 0
}

function removeOuterGroups(value: string) {
  let result = value.trim()
  while (isOuterGroup(result)) {
    result = result.slice(1, -1).trim()
  }
  return result
}

function splitAlternatives(value: string): string[] {
  const expression = removeOuterGroups(value)
  if (!expression) {
    return []
  }

  const alternatives: string[] = []
  let start = 0
  let depth = 0
  let escaped = false
  for (let index = 0; index < expression.length; index += 1) {
    const character = expression[index]
    if (escaped) {
      escaped = false
      continue
    }
    if (character === '\\') {
      escaped = true
      continue
    }
    if (character === '(') {
      depth += 1
    }
    else if (character === ')') {
      depth -= 1
    }
    else if (character === '|' && depth === 0) {
      alternatives.push(expression.slice(start, index).trim())
      start = index + 1
    }
  }
  alternatives.push(expression.slice(start).trim())
  return alternatives.filter(Boolean)
}

function getExactTableKey(pattern: string) {
  let expression = removeOuterGroups(pattern)
  if (expression.startsWith('^') && expression.endsWith('$')) {
    expression = expression.slice(1, -1)
  }
  expression = removeOuterGroups(expression)

  const literal = expression.replace(/\\(.)/g, '$1')
  return literal.includes('.') && escapeRegex(literal) === expression ? literal : undefined
}

function initializeSelection() {
  const exactKeys = new Set<string>()
  const custom: string[] = []
  splitAlternatives(props.modelValue).forEach((pattern) => {
    const key = getExactTableKey(pattern)
    if (key) {
      exactKeys.add(key)
    }
    else {
      custom.push(pattern)
    }
  })

  const inherited = new Set<string>()
  tables.value.forEach((table) => {
    if (!exactKeys.has(table.key) && matchedKeys.value.has(table.key)) {
      inherited.add(table.key)
    }
  })

  inherited.forEach(key => exactKeys.add(key))
  selectedKeys.value = exactKeys
  inheritedKeys.value = inherited
  customPatterns.value = custom
}

function selectInitialDatabase() {
  if (tables.value.some(table => table.database === activeDatabase.value)) {
    return
  }

  const selectedTable = tables.value.find(table => selectedKeys.value.has(table.key))
  activeDatabase.value = selectedTable?.database || tables.value[0]?.database || ''
}

async function loadTables() {
  const [databases, matched] = await Promise.all([
    getTableFilterDatabaseList(props.catalogName),
    getTableFilterMatchedTables(props.catalogName),
  ])
  const tableGroups = await Promise.all((databases || []).map(async (database) => {
    const response = await getTableFilterTableList({
      catalog: props.catalogName,
      db: database,
    })
    return (response || []).map(tableName => ({
      key: `${database}.${tableName}`,
      database,
      tableName,
    }))
  }))
  tables.value = tableGroups.flat().sort((left, right) => left.key.localeCompare(right.key))
  matchedKeys.value = new Set(matched || [])
}

async function openSelector() {
  if (props.disabled || !props.catalogName) {
    return
  }
  visible.value = true
  loading.value = true
  try {
    await loadTables()
    initializeSelection()
    selectInitialDatabase()
  }
  catch {
    message.error(t('tableFilterLoadFailed'))
  }
  finally {
    loading.value = false
  }
}

function toggleTable(key: string, checked: boolean) {
  if (inheritedKeys.value.has(key)) {
    return
  }

  const nextKeys = new Set(selectedKeys.value)
  if (checked) {
    nextKeys.add(key)
  }
  else {
    nextKeys.delete(key)
  }
  inheritedKeys.value.forEach(inheritedKey => nextKeys.add(inheritedKey))
  selectedKeys.value = nextKeys
}

function toggleVisibleTables(checked: boolean) {
  const nextKeys = new Set(selectedKeys.value)
  selectableActiveTableKeys.value.forEach((key) => {
    if (checked) {
      nextKeys.add(key)
    }
    else {
      nextKeys.delete(key)
    }
  })
  inheritedKeys.value.forEach(key => nextKeys.add(key))
  selectedKeys.value = nextKeys
}

function selectedCountForDatabase(database: string) {
  return tables.value.filter(table => table.database === database && selectedKeys.value.has(table.key)).length
}

function buildTableFilter() {
  const exactPatterns = [...selectedKeys.value]
    .filter(key => !inheritedKeys.value.has(key))
    .map(escapeRegex)
    .sort((left, right) => left.localeCompare(right))
  return [...customPatterns.value, ...exactPatterns].join('|')
}

function confirmSelection() {
  emit('update:modelValue', buildTableFilter())
  visible.value = false
}
</script>

<template>
  <a-button type="link" :disabled="disabled || !catalogName" class="table-filter-selector" @click="openSelector">
    {{ t('tableFilterSelectedCount', { count: selectedCount }) }}
  </a-button>
  <a-modal
    v-model:open="visible" :title="t('tableFilterSelectTitle')" :confirm-loading="loading"
    :ok-text="t('confirm')" :cancel-text="t('cancel')" width="900px" @ok="confirmSelection"
  >
    <div class="table-filter-picker">
      <section class="picker-panel database-panel">
        <div class="picker-title">
          {{ t('tableFilterDatabases') }}
        </div>
        <a-input
          v-model:value="databaseSearch" allow-clear :disabled="loading"
          :placeholder="t('tableFilterDatabaseSearchPlaceholder')"
        />
        <div v-if="!loading" class="picker-list">
          <button
            v-for="database in databases" :key="database" type="button"
            class="picker-list-item" :class="{ active: database === activeDatabase }"
            @click="activeDatabase = database"
          >
            <span class="item-name">{{ database }}</span>
            <span v-if="selectedCountForDatabase(database)" class="selected-count">
              {{ selectedCountForDatabase(database) }}
            </span>
          </button>
          <a-empty v-if="!databases.length" :description="t('tableFilterNoDatabases')" :image-style="{ height: '48px' }" />
        </div>
      </section>
      <section class="picker-panel table-panel">
        <div class="picker-title">
          {{ t('tableFilterTables') }}
        </div>
        <a-input
          v-model:value="tableSearch" allow-clear :disabled="loading || !activeDatabase"
          :placeholder="t('tableFilterTableSearchPlaceholder')"
        />
        <div v-if="!loading" class="picker-list table-list">
          <a-checkbox
            v-if="activeDatabaseTables.length" :checked="allVisibleTablesSelected"
            :indeterminate="someVisibleTablesSelected"
            class="select-all" @change="toggleVisibleTables(!allVisibleTablesSelected)"
          >
            {{ t('tableFilterSelectAllVisible') }}
          </a-checkbox>
          <a-checkbox
            v-for="table in activeDatabaseTables" :key="table.key" :checked="selectedKeys.has(table.key)"
            :disabled="inheritedKeys.has(table.key)" class="table-checkbox"
            @change="toggleTable(table.key, !selectedKeys.has(table.key))"
          >
            {{ table.tableName }}
          </a-checkbox>
          <a-empty
            v-if="!activeDatabaseTables.length" :description="t('tableFilterNoTables')"
            :image-style="{ height: '48px' }"
          />
        </div>
      </section>
    </div>
  </a-modal>
</template>

<style lang="less" scoped>
.table-filter-selector {
  padding: 0;
  height: 24px;
}

.table-filter-picker {
  display: flex;
  height: 420px;
  border: 1px solid #e8e8f0;

  .picker-panel {
    display: flex;
    min-width: 0;
    flex: 1;
    flex-direction: column;
    padding: 16px;

    & + .picker-panel {
      border-left: 1px solid #e8e8f0;
    }
  }

  .picker-title {
    margin-bottom: 12px;
    color: #102048;
    font-size: 16px;
    font-weight: 500;
  }

  .picker-list {
    min-height: 0;
    flex: 1;
    margin-top: 12px;
    overflow-y: auto;
  }

  .picker-list-item {
    display: flex;
    width: 100%;
    min-height: 36px;
    padding: 0 12px;
    color: #102048;
    text-align: left;
    background: transparent;
    border: 0;
    cursor: pointer;
    align-items: center;
    justify-content: space-between;

    &:hover,
    &.active {
      background: #f0f5ff;
    }

    &.active {
      color: #1677ff;
    }
  }

  .item-name {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .selected-count {
    min-width: 20px;
    height: 20px;
    margin-left: 8px;
    color: #1677ff;
    font-size: 12px;
    line-height: 20px;
    text-align: center;
    background: #e6f4ff;
    border-radius: 10px;
  }

  .table-list {
    display: flex;
    flex-direction: column;
  }

  .select-all,
  .table-checkbox {
    min-height: 36px;
    padding: 7px 8px;
    margin: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    &:hover {
      background: #fafafa;
    }
  }

  .select-all {
    margin-bottom: 6px;
    border-bottom: 1px solid #f0f0f0;
  }
}
</style>
