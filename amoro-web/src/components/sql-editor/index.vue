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

<script lang="ts" setup>
import type * as Monaco from 'monaco-editor'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

import { EDITOR_OPTIONS } from './editor-config'
import { type MonacoApi, setupMonaco } from '@/utils/editor'

interface EditorCommand {
  [commandName: string]: string | null
}

// const value = ''
const props = defineProps<{ sqlValue: string, options: any, readOnly: boolean }>()
const emit = defineEmits<{
  (e: 'save'): void
  (e: 'update:value', val: any): void
  (e: 'change', val: any): void
  (e: 'ready'): void
  (e: 'loadError', error: Error): void
}>()
const editorElement = ref<HTMLElement>()
let editor: Monaco.editor.IStandaloneCodeEditor | undefined
// @Component
// export default class MSqlEditor extends Vue {
// @Model('change', { type: String })
// private value!: string;

// @Prop({ default: () => ({}) })
// private options!: object;

// @Prop({
//   default: false
// })
// private readOnly!: boolean;

let oldValue = ''
const commandMap: EditorCommand = {}

// @Watch('value')
// private onValueChanged(val = '') {
//   if (this.oldValue !== val && this.editor) {
//     this.editor.setValue(val)
//   }
// }
watch(
  () => props.sqlValue,
  (value) => {
    if (value) {
      if (oldValue !== value && editor) {
        editor.setValue(value)
      }
    }
  },
)

window.addEventListener('resize', resize)

function resize() {
  editor && editor.layout()
}
defineExpose({
  executeCommand(command: string) {
    const cmd = commandMap[command]
    const newEditor = editor as any
    cmd && newEditor && newEditor._commandService.executeCommand(cmd)
  },
  updateOptions(options: any = {}) {
    editor && editor.updateOptions(options)
  },
  getSelection() {
    if (!editor) {
      return ''
    }
    const selection = editor.getSelection()
    const model = editor.getModel()
    if (selection && model) {
      return model.getValueInRange(selection)
    }
    return ''
  },

})

// setupMonaco loads asynchronously: unmount may happen while waiting,
// used to abandon a stale editor creation
let disposed = false

onBeforeUnmount(() => {
  disposed = true
  window.removeEventListener('resize', resize)
  editor && editor.dispose()
})

onMounted(() => {
  nextTick(async () => {
    try {
      const monaco = await setupMonaco()
      if (disposed || !editorElement.value)
        return
      const newEditor = (editor = monaco.editor.create(editorElement.value, { ...EDITOR_OPTIONS, ...props.options }))
      addCommand(monaco)

      newEditor.setValue(props.sqlValue || '')

      newEditor.onDidChangeModelContent(() => {
        const val = newEditor.getValue()
        emit('update:value', val)
        emit('change', val)
        oldValue = val
      })
      emit('ready')
    }
    catch (error) {
      editor?.dispose()
      editor = undefined
      if (!disposed) {
        emit('loadError', error instanceof Error ? error : new Error(String(error)))
      }
    }
  })
})
/**
 * Monaco Editor
 * API： https://microsoft.github.io/monaco-editor/api/modules/monaco.editor.html
 * config： https://microsoft.github.io/monaco-editor/api/interfaces/monaco.editor.ieditoroptions.html
 */

function addCommand(monaco: MonacoApi) {
  if (editor) {
    const saveBinding = editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS, () => {
      emit('save')
    })
    commandMap.save = saveBinding
    const formatBinding = editor.addCommand(monaco.KeyMod.Alt | monaco.KeyMod.Shift | monaco.KeyCode.KeyF, () => {
      formatSql()
    })
    commandMap.format = formatBinding
  }
}

function formatSql() {
  const action = editor && editor.getAction('editor.action.formatDocument')
  action && action.run()
}
</script>

<template>
  <div ref="editorElement" class="m-sql-editor" :class="{ disabled: readOnly }" style="height: 100%; width: 100%;" />
</template>

<style lang="less" scoped>
.m-sql-editor {
  &.disabled {
    cursor: not-allowed !important;
    .monaco-editor .view-lines {
      cursor: not-allowed !important;
    }
  }
}
</style>
