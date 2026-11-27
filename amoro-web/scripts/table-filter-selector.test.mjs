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
import { createRequire } from 'node:module'
import vm from 'node:vm'
import ts from 'typescript'
import * as Vue from 'vue'

const require = createRequire(import.meta.url)
const requireVueDependency = createRequire(require.resolve('vue/package.json'))
const { parse, compileScript } = requireVueDependency('@vue/compiler-sfc')
const source = readFileSync(new URL('../src/views/catalogs/TableFilterSelector.vue', import.meta.url), 'utf8')
const { descriptor } = parse(source)
const script = compileScript(descriptor, { id: 'table-filter-selector-test' })
const { outputText } = ts.transpileModule(script.content, {
  compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 },
})

const groups = {
  iceberg_db: ['opt_minor_verify', 'opt_minor_verify_01', 'opt_minor_verify_02'],
  test_db: ['opt_minor_verify_01', 'opt_minor_verify_02'],
}
const allTables = Object.entries(groups).flatMap(([db, names]) => names.map(name => `${db}.${name}`))

function createSelector(value, matched = [], fail = false) {
  const events = []
  const errors = []
  const module = { exports: {} }
  const services = {
    getTableFilterDatabaseList: async () => {
      if (fail)
        throw new Error('catalog unavailable')
      return Object.keys(groups)
    },
    getTableFilterTableList: async ({ db }) => groups[db],
    getTableFilterMatchedTables: async () => matched,
  }
  vm.runInNewContext(outputText, {
    module,
    exports: module.exports,
    require: (id) => {
      if (id === 'vue')
        return Vue
      if (id === 'vue-i18n')
        return { useI18n: () => ({ t: key => key }) }
      if (id === 'ant-design-vue')
        return { message: { error: error => errors.push(error) } }
      if (id === '@/services/table.service')
        return services
      throw new Error(`Unexpected import: ${id}`)
    },
  })
  const props = Vue.reactive({ catalogName: 'test_catalog', modelValue: value, disabled: false })
  const state = module.exports.default.setup(props, {
    expose() {},
    emit: (name, payload) => events.push([name, payload]),
  })
  return { state, props, events, errors }
}

{
  const { state, events } = createSelector('')
  await state.openSelector()
  state.toggleTable('iceberg_db.opt_minor_verify_01', true)
  state.activeDatabase.value = 'test_db'
  state.toggleTable('test_db.opt_minor_verify_02', true)
  assert.equal(state.selectedKeys.value.size, 2, 'switching databases must preserve pending selections')
  assert.equal(events.length, 0, 'selecting tables must not persist before confirmation')
  state.confirmSelection()
  assert.equal(events[0][1], 'iceberg_db\\.opt_minor_verify_01|test_db\\.opt_minor_verify_02')
}

{
  const original = '(iceberg_db\\.opt_minor_verify)|(test_db\\.opt_minor_verify_01)|(iceberg_db\\.opt_minor_verify_02)'
  const { state, events } = createSelector(original, [
    'iceberg_db.opt_minor_verify',
    'test_db.opt_minor_verify_01',
    'iceberg_db.opt_minor_verify_02',
  ])
  await state.openSelector()
  assert.equal(state.selectedKeys.value.size, 3, 'legacy grouped exact rules must be recognized')
  assert.equal(state.inheritedKeys.value.size, 0, 'exact rules remain editable')
  state.toggleTable('iceberg_db.opt_minor_verify_02', false)
  state.toggleTable('iceberg_db.opt_minor_verify_01', true)
  state.confirmSelection()
  assert.equal(events[0][1], 'iceberg_db\\.opt_minor_verify|iceberg_db\\.opt_minor_verify_01|test_db\\.opt_minor_verify_01')
}

{
  const original = 'iceberg_db\\.opt_minor_verify_.*|test_db\\.opt_minor_verify_0[12]'
  const matched = allTables.filter(name => !name.endsWith('.opt_minor_verify'))
  const { state, events } = createSelector(original, matched)
  await state.openSelector()
  assert.equal(state.inheritedKeys.value.size, 4, 'all custom-regex matches must be locked')
  state.toggleTable('iceberg_db.opt_minor_verify_01', false)
  assert.ok(state.selectedKeys.value.has('iceberg_db.opt_minor_verify_01'))
  state.toggleTable('iceberg_db.opt_minor_verify', true)
  state.confirmSelection()
  assert.equal(events[0][1], `${original}|iceberg_db\\.opt_minor_verify`)
}

{
  const { state, props, events } = createSelector('iceberg_db\\.opt_minor_verify')
  await state.openSelector()
  state.toggleTable('test_db.opt_minor_verify_02', true)
  state.visible.value = false
  assert.equal(props.modelValue, 'iceberg_db\\.opt_minor_verify', 'cancel must leave the text unchanged')
  assert.equal(events.length, 0)
  await state.openSelector()
  assert.equal(state.selectedKeys.value.size, 1, 'reopening must discard cancelled selections')
}

{
  const { state } = createSelector('')
  await state.openSelector()
  state.activeDatabase.value = 'iceberg_db'
  state.tableSearch.value = '_01'
  state.toggleVisibleTables(true)
  assert.deepEqual([...state.selectedKeys.value], ['iceberg_db.opt_minor_verify_01'])
  state.activeDatabase.value = 'test_db'
  state.tableSearch.value = '_02'
  state.toggleVisibleTables(true)
  state.toggleVisibleTables(false)
  assert.deepEqual([...state.selectedKeys.value], ['iceberg_db.opt_minor_verify_01'], 'batch changes must not affect another database')
  state.databaseSearch.value = 'TEST'
  assert.deepEqual([...state.databases.value], ['test_db'], 'database search is case insensitive')
}

{
  const { state, events } = createSelector('(iceberg_db\\.opt_minor_verify)|(iceberg_db\\.opt_minor_verify)')
  await state.openSelector()
  state.toggleTable('iceberg_db.opt_minor_verify', true)
  state.confirmSelection()
  assert.equal(events[0][1], 'iceberg_db\\.opt_minor_verify', 'exact rules must be deduplicated without nested groups')
  const special = 'a$b.t.[x]+'
  assert.equal(state.getExactTableKey(state.escapeRegex(special)), special, 'regex metacharacters must round trip')
}

{
  const { state } = createSelector('(?i)iceberg_db\\.OPT_MINOR_VERIFY_.*|(?:test_db\\.opt_minor_verify_01)')
  assert.equal(state.customPatterns.value.length, 2, 'Java regex groups must not be stripped')
}

{
  const { state, errors, events } = createSelector('iceberg_db\\.opt_minor_verify', [], true)
  await state.openSelector()
  assert.equal(errors.length, 1)
  assert.equal(state.loading.value, false)
  assert.equal(events.length, 0, 'loading failures must not emit changes')
}

console.log('Table filter selector: 8 scenarios passed')
