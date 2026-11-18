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

const assert = require('node:assert/strict')
const { readFileSync } = require('node:fs')
const { join } = require('node:path')
const { test } = require('node:test')
const { compileFunction } = require('node:vm')
const vue = require('vue')
const { compileScript, parse } = require('vue/compiler-sfc')
const ts = require('typescript')

// Mount the actual Cleanup component without a browser or the Ant Design layout.
function loadComponent(name, imports) {
  const filename = join(__dirname, '../src/views/tables/components', name)
  const { descriptor } = parse(readFileSync(filename, 'utf8'), { filename })
  const { content } = compileScript(descriptor, { id: name, inlineTemplate: true })
  const { outputText } = ts.transpileModule(content, {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 },
  })
  const exports = {}
  const load = compileFunction(outputText, ['require', 'exports'], { filename })
  load((id) => {
    assert.ok(Object.hasOwn(imports, id), `Missing test dependency ${id}`)
    return imports[id]
  }, exports)
  return exports.default
}

function element(tag) {
  return { tag, text: '', props: {}, children: [] }
}

function remove(node) {
  const siblings = node.parent?.children
  if (siblings)
    siblings.splice(siblings.indexOf(node), 1)
  node.parent = null
}

const renderer = vue.createRenderer({
  createElement: element,
  createComment: () => element('#comment'),
  createText: text => ({ ...element('#text'), text }),
  setText: (node, text) => { node.text = text },
  setElementText(node, text) {
    node.text = text
    node.children = []
  },
  patchProp: (node, key, prev, value) => { node.props[key] = value },
  parentNode: node => node.parent,
  nextSibling: node => node.parent?.children[node.parent.children.indexOf(node) + 1],
  remove,
  insert(node, parent, anchor) {
    remove(node)
    const index = anchor ? parent.children.indexOf(anchor) : parent.children.length
    parent.children.splice(index, 0, node)
    node.parent = parent
  },
})

function descendants(node) {
  return [node, ...node.children.flatMap(descendants)]
}

for (const { action, format, managed = false, writable = true, status = 'RUNNING', expected } of [
  { action: 'EXPIRE-SNAPSHOTS', format: 'PAIMON', managed: true, expected: 'cancel' },
  { action: 'EXPIRE-SNAPSHOTS', format: 'PAIMON', managed: true, writable: false, expected: 'hidden' },
  { action: 'EXPIRE-SNAPSHOTS', format: 'PAIMON', managed: true, status: 'SUCCESS', expected: 'disabled' },
  { action: 'EXPIRE-SNAPSHOTS', format: 'PAIMON', expected: 'details' },
  { action: 'EXPIRE-SNAPSHOTS', format: 'ICEBERG', expected: 'details' },
  { action: 'CLEAN-ORPHAN-FILES', format: '', expected: 'details' },
]) {
  test(`Cleanup ${action}, ${format || 'unknown format'}, ${status}, ${writable ? 'write' : 'read only'}: ${expected}`, async () => {
    // A native descriptor may use a numeric ID and the same action name as AMS.
    const managedRecord = { processId: '98765432101234567', optimizingType: action, status, amsCancellationSupported: true }
    const nativeRecord = { processId: '12345', optimizingType: action, status }
    const record = managed ? managedRecord : nativeRecord
    const list = format === 'PAIMON' ? [nativeRecord, managedRecord] : [record]
    const rows = Promise.withResolvers()
    const details = []
    const canceled = []
    const t = key => key
    const imports = {
      vue,
      'vue-i18n': { useI18n: () => ({ t }) },
      'vue-router': { useRoute: () => ({ query: { catalog: 'c', db: 'db', table: 't' } }) },
      'ant-design-vue': { Modal: { confirm: ({ onOk }) => onOk() } },
      '@/hooks/usePagination': { usePagination: () => ({ current: 1, pageSize: 25 }) },
      '@/utils/permission': { canManageTable: () => writable },
      '@/utils/index': { bytesToSize: String },
      '@/services/table.service': {
        getTableProcessTypes: async () => ({}),
        getOptimizingProcesses: () => rows.promise,
        getTasksByOptimizingProcessId: async ({ processId }) => {
          details.push(processId)
          return { list: [], total: 0 }
        },
        cancelOptimizingProcess: async ({ processId }) => { canceled.push(processId) },
      },
    }
    imports['./Optimizing.vue'] = { default: loadComponent('Optimizing.vue', imports) }
    const Cleanup = loadComponent('Cleanup.vue', imports)
    const formatReady = format === 'PAIMON' && expected === 'details'
    const tableType = vue.ref(formatReady ? format : '')
    const app = renderer.createApp(() => vue.h(Cleanup, { tableType: tableType.value }))
    app.config.globalProperties.$t = t
    for (const name of ['ASpace', 'ASelect', 'AButton', 'ARow', 'ACol', 'ATooltip', 'ABreadcrumb', 'ABreadcrumbItem', 'QuestionCircleOutlined']) {
      app.component(name, (props, { slots }) => vue.h(name, props, slots.default?.()))
    }
    app.component('ATable', (props, { slots }) => vue.h('table', (props['data-source'] || []).map(record => vue.h('tr', { id: record.processId }, [
      slots.bodyCell({ column: { dataIndex: 'processId' }, record }),
      slots.expandedRowRender?.({ record }),
    ]))))
    const root = element('root')
    const buttons = () => descendants(root).filter(node => node.tag === 'AButton')
    try {
      app.mount(root)
      rows.resolve({ list, total: list.length })
      await rows.promise
      await vue.nextTick()
      const rowButtons = () => descendants(descendants(root).find(node => node.tag === 'tr' && node.props.id === record.processId))
        .filter(node => node.tag === 'AButton')
      assert.equal(rowButtons().length, expected === 'hidden' ? 0 : 1)
      if (expected !== 'hidden') {
        const button = rowButtons()[0]
        const label = descendants(button).map(node => node.text).join('').trim()
        assert.equal(label, expected === 'details' ? record.processId : 'cancelProcess')
        assert.equal(Boolean(button.props.disabled), expected === 'disabled')
        if (expected !== 'disabled') {
          button.props.onClick()
          await vue.nextTick()
        }
      }
      if (expected === 'details') {
        assert.equal(buttons().length, 0, 'Native Paimon and unknown formats have no AMS cancellation in details')
        tableType.value = format
        await vue.nextTick()
        assert.equal(buttons().length, format === 'ICEBERG' ? 1 : 0)
      }
      assert.deepEqual(details, expected === 'details' ? [record.processId] : [])
      assert.deepEqual(canceled, expected === 'cancel' ? [record.processId] : [])
    }
    finally {
      app.unmount()
    }
  })
}
