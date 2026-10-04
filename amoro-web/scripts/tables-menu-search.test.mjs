/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
// eslint-disable-next-line test/no-import-node-test -- Use Node's built-in runner without adding a test framework.
import test from 'node:test'
import vm from 'node:vm'
import ts from 'typescript'
import * as vue from 'vue'
import { parse } from 'vue/compiler-sfc'

const utilsSource = readFileSync(new URL('../src/utils/index.ts', import.meta.url), 'utf8')
const menuSource = readFileSync(new URL('../src/components/tables-sub-menu/TablesMenu.vue', import.meta.url), 'utf8')
const { descriptor } = parse(menuSource)

// Compile the production modules, replacing only their external dependencies.
function loadModule(source, dependencies = {}) {
  const exports = {}
  const { outputText } = ts.transpileModule(source, {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 },
  })
  vm.runInNewContext(outputText, {
    exports,
    require(name) {
      assert.ok(name in dependencies, `Unexpected dependency: ${name}`)
      return dependencies[name]
    },
    setTimeout,
    clearTimeout,
    localStorage: { getItem: () => null },
  })
  return exports
}

const renderer = vue.createRenderer({
  createComment: () => ({}),
  insert() {},
  remove() {},
  parentNode: () => null,
  nextSibling: () => null,
})

async function mountMenu(t) {
  const calls = { tables: [], databases: [] }
  const component = loadModule(descriptor.script.content, {
    'vue': vue,
    'vue-router': { useRoute: () => ({ query: {} }), useRouter: () => ({}) },
    './CreateDB.vue': {},
    '@/store/index': { default: () => ({}) },
    '@/services/table.service': {
      getCatalogList: async () => [],
      getTableList: async (params) => {
        calls.tables.push({ ...params })
        return []
      },
      getDatabaseList: async (params) => {
        calls.databases.push({ ...params })
        return []
      },
    },
    '@/utils/index': loadModule(utilsSource),
    '@/hooks/usePlaceholder': { usePlaceholder: () => ({}) },
    '@/components/VirtualRecycleScroller.vue': {},
  }).default
  component.render = () => null
  const app = renderer.createApp(component)
  const menu = app.mount({})
  t.after(() => app.unmount())
  // Finish catalog initialization before simulating user input.
  await Promise.resolve()
  await Promise.resolve()
  menu.curCatalog = 'catalog'
  menu.database = 'database'
  return { app, menu, calls }
}

test('rapid table searches wait for the last input and send its keywords once', async (t) => {
  t.mock.timers.enable({ apis: ['setTimeout'] })
  const { menu, calls } = await mountMenu(t)
  for (const keywords of ['o', 'or', 'orders']) {
    menu.tableSearchInput = keywords
    menu.handleSearch('table')
    t.mock.timers.tick(100)
  }
  assert.equal(calls.tables.length, 0)
  t.mock.timers.tick(199)
  assert.equal(calls.tables.length, 0)
  t.mock.timers.tick(1)
  assert.deepEqual(calls.tables, [{ catalog: 'catalog', db: 'database', keywords: 'orders' }])
})

test('database and table searches debounce independently', async (t) => {
  t.mock.timers.enable({ apis: ['setTimeout'] })
  const { menu, calls } = await mountMenu(t)
  menu.DBSearchInput = 's'
  menu.handleSearch('db')
  t.mock.timers.tick(100)
  menu.tableSearchInput = 'orders'
  menu.handleSearch('table')
  t.mock.timers.tick(100)
  menu.DBSearchInput = 'sales'
  menu.handleSearch('db')
  t.mock.timers.tick(199)
  assert.equal(calls.tables.length, 0)
  assert.equal(calls.databases.length, 0)
  t.mock.timers.tick(1)
  assert.equal(calls.tables.length, 1)
  assert.equal(calls.databases.length, 0)
  t.mock.timers.tick(100)
  assert.deepEqual(calls.databases, [{ catalog: 'catalog', keywords: 'sales' }])
})

test('clearing either search replaces its pending request with empty keywords', async (t) => {
  t.mock.timers.enable({ apis: ['setTimeout'] })
  const { menu, calls } = await mountMenu(t)
  menu.tableSearchInput = 'orders'
  menu.DBSearchInput = 'sales'
  menu.handleSearch('table')
  menu.handleSearch('db')
  t.mock.timers.tick(100)
  menu.clearSearch('table')
  menu.clearSearch('db')
  t.mock.timers.tick(299)
  assert.equal(calls.tables.length, 0)
  assert.equal(calls.databases.length, 0)
  t.mock.timers.tick(1)
  assert.deepEqual(calls.tables, [{ catalog: 'catalog', db: 'database', keywords: '' }])
  assert.deepEqual(calls.databases, [{ catalog: 'catalog', keywords: '' }])
})

test('unmount cancels both searches without cancelling another menu instance', async (t) => {
  t.mock.timers.enable({ apis: ['setTimeout'] })
  const first = await mountMenu(t)
  const second = await mountMenu(t)
  for (const { menu } of [first, second]) {
    menu.handleSearch('table')
    menu.handleSearch('db')
  }
  first.app.unmount()
  t.mock.timers.tick(300)
  assert.equal(first.calls.tables.length, 0)
  assert.equal(first.calls.databases.length, 0)
  assert.equal(second.calls.tables.length, 1)
  assert.equal(second.calls.databases.length, 1)
})

test('debounce forwards the latest arguments and can be cancelled and reused', (t) => {
  t.mock.timers.enable({ apis: ['setTimeout'] })
  const { debounce } = loadModule(utilsSource)
  const calls = []
  const search = debounce((...args) => calls.push(args))
  search('old', 1)
  t.mock.timers.tick(100)
  search('latest', 2)
  t.mock.timers.tick(300)
  assert.deepEqual(calls, [['latest', 2]])
  search('cancelled', 3)
  search.cancel()
  search.cancel()
  t.mock.timers.tick(300)
  assert.equal(calls.length, 1)
  search('reused', 4)
  t.mock.timers.tick(300)
  assert.deepEqual(calls, [['latest', 2], ['reused', 4]])
})
