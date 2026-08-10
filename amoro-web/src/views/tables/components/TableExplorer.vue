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

<script setup lang="ts">
import { computed, onBeforeMount, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCatalogList, getDatabaseList, getNamespaceList, getTableList } from '@/services/table.service'
import type { ICatalogItem } from '@/types/common.type'

// Node types: Catalog / Database / Table
type NodeType = 'catalog' | 'database' | 'table'

interface TableItem {
  name: string
  type: string
}

interface TreeNode {
  key: string
  title: string
  isLeaf?: boolean
  children?: TreeNode[]
  // Custom fields
  nodeType: NodeType
  catalog: string
  db?: string
  table?: string
  tableType?: string
  catalogNamespace?: string
}

const router = useRouter()
const route = useRoute()

const storageTableKey = 'easylake-menu-catalog-db-table'
const expandedKeysSessionKeyPrefix = 'tables_expanded_keys'
const allNamespaceValue = '__all__'

const state = reactive({
  loading: false,
  searchKey: '',
  filterKey: '',
  treeData: [] as TreeNode[],
  expandedKeys: [] as string[],
  selectedKeys: [] as string[],
  namespaces: [] as string[],
  selectedNamespace: '',
  selectedFormat: '',
  namespaceMode: false,
  // Cache
  catalogList: [] as string[],
  dbListByCatalog: {} as Record<string, string[]>,
  tablesByCatalogDb: {} as Record<string, TableItem[]>,
})

const formatOptions = [
  { label: 'Iceberg', value: 'ICEBERG' },
  { label: 'Paimon', value: 'PAIMON' },
  { label: 'Hive', value: 'HIVE' },
  { label: 'Hudi', value: 'HUDI' },
]

let namespaceGeneration = 0
let initialized = false

function expandedKeysSessionKey() {
  return `${expandedKeysSessionKeyPrefix}:${state.selectedNamespace || 'default'}`
}

function catalogPresentation(catalog: string) {
  if (state.namespaceMode && state.selectedNamespace === allNamespaceValue) {
    const separatorIndex = catalog.indexOf('@')
    if (separatorIndex > 0 && separatorIndex < catalog.length - 1) {
      return {
        title: catalog.slice(separatorIndex + 1),
        namespace: catalog.slice(0, separatorIndex),
      }
    }
  }

  if (!state.namespaceMode || !state.selectedNamespace) {
    return { title: catalog }
  }
  const prefix = `${state.selectedNamespace}@`
  return {
    title: catalog.startsWith(prefix) ? catalog.slice(prefix.length) : catalog,
  }
}

function buildCatalogNode(catalog: string): TreeNode {
  const presentation = catalogPresentation(catalog)
  return {
    key: `catalog:${catalog}`,
    title: presentation.title,
    isLeaf: false,
    nodeType: 'catalog',
    catalog,
    catalogNamespace: presentation.namespace,
  }
}

function buildDatabaseNode(catalog: string, db: string): TreeNode {
  return {
    key: `catalog:${catalog}/db:${db}`,
    title: db,
    isLeaf: false,
    nodeType: 'database',
    catalog,
    db,
  }
}

function buildTableNode(catalog: string, db: string, table: TableItem): TreeNode {
  return {
    key: `catalog:${catalog}/db:${db}/table:${table.name}`,
    title: table.name,
    isLeaf: true,
    nodeType: 'table',
    catalog,
    db,
    table: table.name,
    tableType: table.type,
  }
}

function updateTreeNodeChildren(targetKey: string, children: TreeNode[]) {
  const loop = (nodes: TreeNode[]): boolean => {
    for (const node of nodes) {
      if (node.key === targetKey) {
        node.children = children
        return true
      }
      if (node.children && node.children.length && loop(node.children)) {
        return true
      }
    }
    return false
  }

  loop(state.treeData)
}

async function initRootCatalogs() {
  if (state.namespaceMode && !state.selectedNamespace) {
    state.catalogList = []
    state.treeData = []
    return
  }
  const generation = namespaceGeneration
  state.loading = true
  try {
    const namespace = state.namespaceMode && state.selectedNamespace !== allNamespaceValue
      ? state.selectedNamespace
      : undefined
    const res = await getCatalogList(namespace)
    if (generation !== namespaceGeneration) {
      return
    }
    const catalogs = (res || []).map((item: ICatalogItem) => item.catalogName)
    state.catalogList = catalogs
    state.treeData = catalogs.map(catalog => buildCatalogNode(catalog))
  }
  finally {
    if (generation === namespaceGeneration) {
      state.loading = false
    }
  }
}

function clearExplorerState() {
  namespaceGeneration += 1
  state.loading = false
  state.searchKey = ''
  state.filterKey = ''
  state.selectedFormat = ''
  state.treeData = []
  state.expandedKeys = []
  state.selectedKeys = []
  state.catalogList = []
  state.dbListByCatalog = {}
  state.tablesByCatalogDb = {}
}

function handleFormatChange(format?: string) {
  state.selectedFormat = format || ''
}

async function loadChildren(node: any) {
  const data = node?.dataRef || node
  if (!data) {
    return
  }

  const nodeType = data.nodeType as NodeType
  const generation = namespaceGeneration
  if (nodeType === 'catalog') {
    const catalog = data.catalog as string
    if (!catalog || state.dbListByCatalog[catalog]) {
      return
    }

    state.loading = true
    try {
      const res = await getDatabaseList({ catalog, keywords: '' })
      if (generation !== namespaceGeneration) {
        return
      }
      const dbs = (res || []) as string[]
      state.dbListByCatalog[catalog] = dbs
      if (!dbs.length) {
        data.isLeaf = true
        updateTreeNodeChildren(data.key as string, [])
        return
      }
      data.isLeaf = false
      const children = dbs.map(db => buildDatabaseNode(catalog, db))
      updateTreeNodeChildren(data.key as string, children)
    }
    finally {
      state.loading = false
    }
  }
  else if (nodeType === 'database') {
    const catalog = data.catalog as string
    const db = data.db as string
    if (!catalog || !db) {
      return
    }
    const cacheKey = `${catalog}/${db}`
    if (state.tablesByCatalogDb[cacheKey]) {
      return
    }

    state.loading = true
    try {
      const res = await getTableList({ catalog, db, keywords: '' })
      if (generation !== namespaceGeneration) {
        return
      }
      const tables = (res || []) as TableItem[]
      state.tablesByCatalogDb[cacheKey] = tables
      if (!tables.length) {
        data.isLeaf = false
        updateTreeNodeChildren(data.key as string, [])
        return
      }
      data.isLeaf = false
      const children = tables.map(table => buildTableNode(catalog, db, table))
      updateTreeNodeChildren(data.key as string, children)
    }
    finally {
      state.loading = false
    }
  }
}

async function expandPathBySelected(catalog: string, db: string) {
  const safeCatalog = (catalog || '').trim()
  const safeDb = (db || '').trim()

  if (!safeCatalog) {
    return
  }

  const nextExpandedKeys = new Set(state.expandedKeys)

  const catalogKey = `catalog:${safeCatalog}`
  const catalogNode = state.treeData.find(node => node.key === catalogKey)
  if (catalogNode) {
    await loadChildren({ dataRef: catalogNode })
    nextExpandedKeys.add(catalogKey)
  }

  if (safeDb) {
    const dbKey = `catalog:${safeCatalog}/db:${safeDb}`
    const latestCatalogNode = state.treeData.find(node => node.key === catalogKey)
    const dbNode = latestCatalogNode?.children?.find(child => child.key === dbKey)
    if (dbNode) {
      await loadChildren({ dataRef: dbNode })
      nextExpandedKeys.add(dbKey)
    }
  }

  state.expandedKeys = Array.from(nextExpandedKeys)
  try {
    sessionStorage.setItem(expandedKeysSessionKey(), JSON.stringify(state.expandedKeys))
  }
  catch (e) {
    // ignore sessionStorage write errors
  }
}

function handleSelectTable(catalog: string, db: string, tableName: string, tableType: string) {
  if (!catalog || !db || !tableName) {
    return
  }

  const type = tableType || 'MIXED_ICEBERG'

  const namespace = state.namespaceMode ? state.selectedNamespace : 'default'
  const storedSelection = JSON.stringify({
    namespace,
    catalog,
    database: db,
    tableName,
    type,
  })
  localStorage.setItem(`${storageTableKey}:${namespace}`, storedSelection)
  localStorage.setItem(storageTableKey, storedSelection)

  const pathQuery = {
    path: '/tables',
    query: {
      ...(state.namespaceMode ? { namespace: state.selectedNamespace } : {}),
      catalog,
      db,
      table: tableName,
      type,
    },
  }

  if (route.path.includes('tables')) {
    router.replace(pathQuery)
  }
  else {
    router.push(pathQuery)
  }
}

function handleTreeSelect(selectedKeys: (string | number)[], info: any) {
  const node = info?.node
  if (!node) {
    return
  }

  const dataRef = (node.dataRef || node) as TreeNode | undefined
  if (!dataRef) {
    return
  }

  if (dataRef.nodeType !== 'table') {
    if (!dataRef.isLeaf) {
      toggleNodeExpand(dataRef)
    }
    return
  }

  state.selectedKeys = selectedKeys.map(key => String(key))

  const catalog = (dataRef.catalog || '') as string
  const db = (dataRef.db || '') as string
  const tableName = (dataRef.table || dataRef.title || '') as string
  const tableType = (dataRef.tableType || '') as string

  if (catalog && db && tableName) {
    handleSelectTable(catalog, db, tableName, tableType)
  }
}

async function handleTreeExpand(expandedKeys: (string | number)[], info: any) {
  state.expandedKeys = expandedKeys.map(key => String(key))
  try {
    sessionStorage.setItem(expandedKeysSessionKey(), JSON.stringify(state.expandedKeys))
  }
  catch (e) {
    // ignore sessionStorage write errors
  }
  const node = info?.node
  if (!node || node.isLeaf || !info.expanded) {
    return
  }
  await loadChildren(node)
}

async function toggleNodeExpand(dataRef: TreeNode) {
  if (!dataRef || dataRef.isLeaf) {
    return
  }

  const key = String(dataRef.key)
  const hasExpanded = state.expandedKeys.includes(key)

  let nextExpandedKeys: string[]
  if (hasExpanded) {
    nextExpandedKeys = state.expandedKeys.filter(item => item !== key)
  }
  else {
    nextExpandedKeys = [...state.expandedKeys, key]
    await loadChildren({ dataRef })
  }

  state.expandedKeys = nextExpandedKeys
  try {
    sessionStorage.setItem(expandedKeysSessionKey(), JSON.stringify(state.expandedKeys))
  }
  catch (e) {
    // ignore sessionStorage write errors
  }
}

function getNodeIcon(node: TreeNode) {
  if (node.nodeType === 'catalog') {
    return 'catalogs'
  }
  if (node.nodeType === 'database') {
    return 'database'
  }
  return 'tables'
}

function normalizeKeyword(raw: string) {
  return raw.trim().toLowerCase()
}

function filterBySingleKeyword(nodes: TreeNode[], keyword: string, expandedSet: Set<string>): TreeNode[] {
  const result: TreeNode[] = []

  nodes.forEach((node) => {
    const searchText = node.catalogNamespace
      ? `${node.title} @${node.catalogNamespace}`
      : node.title
    const titleMatch = searchText.toLowerCase().includes(keyword)
    let childrenMatches: TreeNode[] = []

    if (node.children && node.children.length) {
      childrenMatches = filterBySingleKeyword(node.children, keyword, expandedSet)
    }

    if (titleMatch || childrenMatches.length) {
      const cloned: TreeNode = { ...node }
      if (childrenMatches.length) {
        cloned.children = childrenMatches
        expandedSet.add(node.key)
      }
      result.push(cloned)
    }
  })

  return result
}

function filterByHierarchical(nodes: TreeNode[], parts: string[], expandedSet: Set<string>): TreeNode[] {
  const [catalogPart, dbPart, tablePart] = parts
  const result: TreeNode[] = []

  nodes.forEach((catalogNode) => {
    if (catalogNode.nodeType !== 'catalog') {
      return
    }
    const catalogSearchText = catalogNode.catalogNamespace
      ? `${catalogNode.title} @${catalogNode.catalogNamespace}`
      : catalogNode.title
    if (!catalogSearchText.toLowerCase().includes(catalogPart)) {
      return
    }

    const dbChildren = (catalogNode.children || []).filter(child => child.nodeType === 'database')
    const matchedDbNodes: TreeNode[] = []

    dbChildren.forEach((dbNode) => {
      if (dbPart && !dbNode.title.toLowerCase().includes(dbPart)) {
        return
      }

      if (!tablePart) {
        const clonedDb: TreeNode = { ...dbNode }
        clonedDb.children = dbNode.children
        matchedDbNodes.push(clonedDb)
        expandedSet.add(catalogNode.key)
        expandedSet.add(dbNode.key)
        return
      }

      const tableChildren = (dbNode.children || []).filter(child => child.title.toLowerCase().includes(tablePart))
      if (tableChildren.length) {
        const clonedDb: TreeNode = { ...dbNode, children: tableChildren }
        matchedDbNodes.push(clonedDb)
        expandedSet.add(catalogNode.key)
        expandedSet.add(dbNode.key)
      }
    })

    if (matchedDbNodes.length) {
      const clonedCatalog: TreeNode = { ...catalogNode, children: matchedDbNodes }
      result.push(clonedCatalog)
    }
  })

  return result
}

function filterTree(source: TreeNode[], rawKeyword: string): { tree: TreeNode[], expandedKeys: string[] } {
  const keyword = normalizeKeyword(rawKeyword)
  if (!keyword) {
    return {
      tree: source,
      expandedKeys: state.expandedKeys,
    }
  }

  const expandedSet = new Set<string>()
  const parts = keyword.split('.').map(p => p.trim()).filter(Boolean)

  let filteredTree: TreeNode[] = []
  if (parts.length > 1) {
    filteredTree = filterByHierarchical(source, parts, expandedSet)
  }
  else {
    filteredTree = filterBySingleKeyword(source, keyword, expandedSet)
  }

  return {
    tree: filteredTree,
    expandedKeys: Array.from(expandedSet),
  }
}

function filterTreeByFormat(source: TreeNode[], format: string): TreeNode[] {
  return source.reduce<TreeNode[]>((result, node) => {
    if (node.nodeType === 'table') {
      const tableType = (node.tableType || '').toUpperCase()
      if (tableType === format || tableType.endsWith(`_${format}`)) {
        result.push(node)
      }
      return result
    }

    result.push({
      ...node,
      children: node.children ? filterTreeByFormat(node.children, format) : node.children,
    })
    return result
  }, [])
}

let searchTimer: any = null

watch(
  () => state.searchKey,
  (val) => {
    if (searchTimer) {
      clearTimeout(searchTimer)
    }
    searchTimer = setTimeout(() => {
      state.filterKey = val || ''
    }, 500)
  },
)

watch(
  () => route.query,
  async (value, oldValue) => {
    if (!initialized) {
      return
    }

    const { namespace, catalog, db, table } = value as any
    const {
      namespace: oldNamespace,
      catalog: oldCatalog,
      db: oldDb,
      table: oldTable,
    } = (oldValue || {}) as any

    if (state.namespaceMode && namespace !== oldNamespace) {
      const nextNamespace = namespace === allNamespaceValue || state.namespaces.includes(namespace as string)
        ? namespace as string
        : ''
      if (nextNamespace !== state.selectedNamespace) {
        clearExplorerState()
        state.selectedNamespace = nextNamespace
        if (nextNamespace) {
          await initRootCatalogs()
          await restoreExpandedState()
        }
      }
    }

    if (
      namespace === oldNamespace
      && `${catalog || ''}${db || ''}${table || ''}` === `${oldCatalog || ''}${oldDb || ''}${oldTable || ''}`
    ) {
      return
    }

    const catalogStr = (catalog as string) || ''
    const dbStr = (db as string) || ''
    const tableStr = (table as string) || ''

    if (catalogStr && dbStr) {
      await expandPathBySelected(catalogStr, dbStr)
      if (tableStr) {
        const tableKey = `catalog:${catalogStr}/db:${dbStr}/table:${tableStr}`
        state.selectedKeys = [tableKey]
      }
      else {
        state.selectedKeys = []
      }
    }
    else {
      state.selectedKeys = []
    }
  },
)

const searchResult = computed(() => {
  const source = state.selectedFormat
    ? filterTreeByFormat(state.treeData, state.selectedFormat)
    : state.treeData
  const keyword = normalizeKeyword(state.filterKey)
  if (!keyword) {
    return {
      tree: source,
      expandedKeys: state.expandedKeys,
    }
  }
  return filterTree(source, keyword)
})

const displayTreeData = computed(() => searchResult.value.tree)
const displayExpandedKeys = computed(() => searchResult.value.expandedKeys)

async function restoreExpandedState() {
  let restoredExpandedKeys: string[] = []

  try {
    const stored = sessionStorage.getItem(expandedKeysSessionKey())
    if (stored) {
      const parsed = JSON.parse(stored)
      if (Array.isArray(parsed)) {
        restoredExpandedKeys = parsed.map((key: string | number) => String(key))
      }
    }
  }
  catch (e) {
    // ignore sessionStorage read/parse errors
  }

  if (restoredExpandedKeys.length) {
    const catalogKeys = restoredExpandedKeys.filter(key => key.startsWith('catalog:') && !key.includes('/db:'))
    for (const catalogKey of catalogKeys) {
      const catalogNode = state.treeData.find(node => node.key === catalogKey)
      if (catalogNode) {
        await loadChildren({ dataRef: catalogNode })
      }
    }

    const dbKeys = restoredExpandedKeys.filter(key => key.includes('/db:') && !key.includes('/table:'))
    for (const dbKey of dbKeys) {
      const [catalogPart] = dbKey.split('/db:')
      const catalogName = catalogPart.replace('catalog:', '')
      const catalogNode = state.treeData.find(node => node.key === `catalog:${catalogName}`)
      if (catalogNode && catalogNode.children && catalogNode.children.length) {
        const dbNode = catalogNode.children.find((child: TreeNode) => child.key === dbKey)
        if (dbNode) {
          await loadChildren({ dataRef: dbNode })
        }
      }
    }

    state.expandedKeys = restoredExpandedKeys
  }
}

async function restoreRouteSelection() {
  const query = route.query || {}
  const queryCatalog = (query.catalog as string) || ''
  const queryDb = (query.db as string) || ''
  const queryTable = (query.table as string) || ''

  if (queryCatalog && queryDb) {
    await expandPathBySelected(queryCatalog, queryDb)
    if (queryTable) {
      const tableKey = `catalog:${queryCatalog}/db:${queryDb}/table:${queryTable}`
      state.selectedKeys = [tableKey]
    }
  }
  else {
    state.selectedKeys = []
  }
}

async function initializeNamespaces() {
  let namespaces: string[]
  try {
    namespaces = (await getNamespaceList()) || []
  }
  catch (e) {
    // Fail closed: falling back to the legacy catalog endpoint could expose every namespace.
    namespaces = []
  }

  state.namespaces = namespaces
  state.namespaceMode = !(namespaces.length === 1 && namespaces[0] === 'default')
  if (!state.namespaceMode) {
    state.selectedNamespace = 'default'
    return
  }

  const routeNamespace = (route.query.namespace as string) || ''
  state.selectedNamespace = routeNamespace === allNamespaceValue || namespaces.includes(routeNamespace)
    ? routeNamespace
    : ''
}

async function handleNamespaceChange(namespace?: string) {
  clearExplorerState()
  state.selectedNamespace = namespace || ''

  await router.replace({
    path: route.path,
    query: state.selectedNamespace ? { namespace: state.selectedNamespace } : {},
  })

  if (state.selectedNamespace) {
    await initRootCatalogs()
    await restoreExpandedState()
  }
}

onBeforeMount(async () => {
  await initializeNamespaces()
  await initRootCatalogs()
  await restoreExpandedState()
  await restoreRouteSelection()
  initialized = true
})
</script>

<template>
  <div class="table-explorer">
    <div class="table-explorer-header">
      <div class="explorer-filter-row">
        <a-select
          :value="state.namespaceMode ? (state.selectedNamespace || undefined) : 'default'"
          :options="state.namespaceMode
            ? [
              { label: 'All', value: allNamespaceValue },
              ...state.namespaces.map(namespace => ({ label: namespace, value: namespace })),
            ]
            : [{ label: 'default', value: 'default' }]"
          :disabled="!state.namespaceMode"
          :allow-clear="state.namespaceMode"
          placeholder="Namespace"
          class="explorer-filter-select namespace-filter-select"
          @change="handleNamespaceChange"
        />
        <a-select
          :value="state.selectedFormat || undefined"
          :options="formatOptions"
          placeholder="Format"
          class="explorer-filter-select format-filter-select"
          allow-clear
          @change="handleFormatChange"
        />
      </div>
      <a-input
        v-model:value="state.searchKey"
        placeholder="Search catalog.database.table"
        allow-clear
        class="search-input"
      >
        <template #prefix>
          <SearchOutlined class="search-input-prefix-icon" />
        </template>
      </a-input>
    </div>

    <div class="table-explorer-body">
      <a-tree
        v-if="displayTreeData.length"
        :tree-data="displayTreeData"
        :expanded-keys="displayExpandedKeys"
        :selected-keys="state.selectedKeys"
        :show-line="{ showLeafIcon: false }"
        :show-icon="false"
        :auto-expand-parent="false"
        block-node
        @expand="handleTreeExpand"
        @select="handleTreeSelect"
      >
        <template #title="{ dataRef }">
          <span
            class="tree-node-title"
            :class="`node-${dataRef.nodeType}`"
          >
            <svg-icon
              :icon-class="getNodeIcon(dataRef)"
              class="tree-node-icon"
            />
            <span class="tree-node-text">
              {{ dataRef.title }}
            </span>
            <span v-if="dataRef.catalogNamespace" class="catalog-namespace-label">
              @{{ dataRef.catalogNamespace }}
            </span>
          </span>
        </template>
      </a-tree>
      <div v-else class="empty-placeholder">
        <span v-if="state.namespaceMode && !state.selectedNamespace">Select a namespace to browse tables.</span>
        <span v-else>No results.</span>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.table-explorer {
  position: relative;
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  padding: 0 0;
  background-color: #fff;
  font-size: 13px;
  display: flex;
  flex-direction: column;

  .table-explorer-header {
    padding: 0 7px 0 8px;
    margin-bottom: 8px;

    .explorer-filter-row {
      display: flex;
      gap: 8px;
      margin-bottom: 8px;

      .explorer-filter-select {
        min-width: 0;

        :deep(.ant-select-selector) {
          height: 32px;
          display: flex;
          align-items: center;
        }

        :deep(.ant-select-selection-item),
        :deep(.ant-select-selection-placeholder) {
          line-height: 30px;
          font-size: 14px;
        }

        :deep(.ant-select-selection-placeholder) {
          color: #ccc;
        }
      }

      .namespace-filter-select {
        flex: 3;
      }

      .format-filter-select {
        flex: 2;
      }
    }

    .search-input {
      width: 100%;
      height: 32px;

      :deep(.ant-input-prefix) {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 24px;
      }

      :deep(.search-input-prefix-icon) {
        font-size: 16px;
        line-height: 30px;
      }

      :deep(.ant-input) {
        line-height: 30px;
        font-size: 14px;

        &::placeholder {
          font-size: 14px;
          color: #ccc;
        }
      }
    }
  }

  .table-explorer-body {
    flex: 1;
    min-height: 0;
    overflow: auto;
    padding: 0 7px 0 8px;

    :deep(.ant-tree) {
      background-color: #fff;
    }

    :deep(.ant-tree-indent-unit) {
      width: 24px;
    }

    :deep(.ant-tree-switcher-line-icon) {
      color: #d9d9e3;
      font-size: 12px;
    }

    .tree-node-title {
      display: inline-flex;
      align-items: center;

      .tree-node-icon {
        margin-right: 6px;
      }

      .catalog-namespace-label {
        margin-left: 4px;
        color: #999;
        font-size: 11px;
        font-weight: normal;
      }
    }

    .empty-placeholder {
      padding: 8px 4px;
      color: #999;
    }
  }
}

.table-explorer-body .tree-node-title.node-catalog .tree-node-icon,
.table-explorer-body .tree-node-title.node-database .tree-node-icon { transform: translateY(1px); }
.table-explorer-body .tree-node-title.node-table .tree-node-icon { transform: none; }

/* Shift database and table rows left by 2px: indent + switcher + content together */
:deep(.ant-tree-treenode:has(.tree-node-title.node-database)),
:deep(.ant-tree-treenode:has(.tree-node-title.node-table)) {
  transform: translateX(-1px); // Adjust to -3 or -4 for a larger shift if needed
}
</style>
