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

/**
 * Keywords and formatting configuration in sql editor
 *
 * On-demand monaco entry: loads only editor.api, the SQL language
 * definition and the editor features used by this page, instead of the
 * full 'monaco-editor' entry (which bundles ~90 languages and all
 * contribs). The feature subset is trimmed against the monaco-editor
 * 0.56 full entry (esm/vs/index.js): completion (suggestController),
 * formatting (formatActions), find, context menu, clipboard, folding,
 * bracket matching, line/word operations, comment, core commands and
 * codicons.
 *
 * Loading: setupMonaco() is called (dynamic import) when the editor
 * component first mounts; the app startup path (main.ts) no longer
 * imports monaco statically.
 */
import type * as Monaco from 'monaco-editor'
import * as sqlFormatter from 'sql-formatter'
import { language as sqlLanguage } from './sql'

/** Module type of the editor.api entry (runtime on-demand subset, compatible with the full 'monaco-editor' types) */
export type MonacoApi = typeof import('monaco-editor/editor/editor.api.js')

let setupPromise: Promise<MonacoApi> | null = null

export function setupMonaco(): Promise<MonacoApi> {
  // clear the cache on failure so the next mount can retry
  // (a transient network error must not break the editor for the whole session)
  setupPromise ??= doSetup().catch((err) => {
    setupPromise = null
    throw err
  })
  return setupPromise
}

async function doSetup(): Promise<MonacoApi> {
  const [monaco] = await Promise.all([
    import('monaco-editor/editor/editor.api.js'),
    // SQL language registration (the Monarch lexer is lazy-loaded by the loader)
    import('monaco-editor/languages/definitions/sql/register.js'),
    import('monaco-editor/editor/browser/coreCommands.js'),
    import('monaco-editor/editor/contrib/suggest/browser/suggestController.js'),
    import('monaco-editor/editor/contrib/format/browser/formatActions.js'),
    import('monaco-editor/features/find/register.js'),
    import('monaco-editor/editor/contrib/contextmenu/browser/contextmenu.js'),
    import('monaco-editor/editor/contrib/clipboard/browser/clipboard.js'),
    import('monaco-editor/editor/contrib/folding/browser/folding.js'),
    import('monaco-editor/editor/contrib/bracketMatching/browser/bracketMatching.js'),
    import('monaco-editor/editor/contrib/linesOperations/browser/linesOperations.js'),
    import('monaco-editor/editor/contrib/wordOperations/browser/wordOperations.js'),
    import('monaco-editor/editor/contrib/comment/browser/comment.js'),
    // codicon icon styles (this register module relatively imports codicon.css,
    // working around the package exports map which only exposes .js subpaths)
    import('monaco-editor/features/codicon/register.js'),
  ])
  registerSql(monaco)
  registerLogLanguage(monaco)
  return monaco
}

function registerSql(monaco: MonacoApi) {
  // SQL keyword hints
  monaco.languages.registerCompletionItemProvider('sql', {
    provideCompletionItems: (model, position) => {
      const textUntilPosition = model.getValueInRange({
        startLineNumber: position.lineNumber,
        startColumn: 1,
        endLineNumber: position.lineNumber,
        endColumn: position.column,
      })
      const match = textUntilPosition.match(/(\S+)$/)
      const suggestions: Monaco.languages.CompletionItem[] = []
      if (match) {
        const matchStr = match[0].toUpperCase()
        sqlLanguage.keywords.forEach((item: string) => {
          if (item.startsWith(matchStr)) {
            suggestions.push({
              label: item,
              kind: monaco.languages.CompletionItemKind.Keyword,
              insertText: item,
            } as Monaco.languages.CompletionItem)
          }
        })
        sqlLanguage.operators.forEach((item: string) => {
          if (item.startsWith(matchStr)) {
            suggestions.push({
              label: item,
              kind: monaco.languages.CompletionItemKind.Operator,
              insertText: item,
            } as Monaco.languages.CompletionItem)
          }
        })
        sqlLanguage.builtinFunctions.forEach((item: string) => {
          if (item.startsWith(matchStr)) {
            suggestions.push({
              label: item,
              kind: monaco.languages.CompletionItemKind.Function,
              insertText: item,
            } as Monaco.languages.CompletionItem)
          }
        })
      }
      return {
        suggestions: Array.from(new Set(suggestions)),
      }
    },
  })

  // format SQL
  monaco.languages.registerDocumentFormattingEditProvider('sql', {
    provideDocumentFormattingEdits(model) {
      const formatted = sqlFormatter.format(model.getValue())
      return [{
        range: model.getFullModelRange(),
        text: formatted.replace(/\s-\s/g, '-'),
      }]
    },
  })

  const themeData: any = {
    base: 'vs',
    inherit: !1,
    colors: {
      'editorHoverWidget.background': '#FAFAFA',
      'editorHoverWidget.border': '#DEDEDE',
      'editor.lineHighlightBackground': '#EFF8FF',
      'editor.selectionBackground': '#D5D5EF',
      'editorLineNumber.foreground': '#999999',
      'editorSuggestWidget.background': '#FFFFFF',
      'editorSuggestWidget.selectedBackground': '#EFF8FF',
    },
    rules: [{
      token: 'comment',
      foreground: '8E908C',
    }, {
      token: 'comments',
      foreground: '8E908C',
    }, {
      token: 'keyword',
      foreground: '8959A8',
    }, {
      token: 'predefined',
      foreground: '11B7BE',
    }, {
      token: 'doubleString',
      foreground: 'AB1010',
    }, {
      token: 'singleString',
      foreground: 'AB1010',
    }, {
      token: 'number',
      foreground: 'AB1010',
    }, {
      token: 'string.sql',
      foreground: '718C00',
    }],
  }
  monaco.editor.defineTheme('arcticSql', themeData)
}

function registerLogLanguage(monaco: MonacoApi) {
  monaco.languages.register({ id: 'logLanguage' })

  monaco.languages.setMonarchTokensProvider('logLanguage', {
    tokenizer: {
      root: [
        [/INFO.*/, 'custom-info'],
        [/ERROR.*/, 'custom-error'],
        [/WARN.*/, 'custom-warn'],
        [/DEBUG.*/, 'custom-debug'],
        [/\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}:\d{2},\d{3}/, 'custom-date'],
      ],
    },
  })

  const themeData: Monaco.editor.IStandaloneThemeData = {
    base: 'vs',
    inherit: false,
    colors: {
      'editor.background': '#f6f7f8',
    },
    rules: [
      { token: 'custom-info', foreground: '808080' },
      { token: 'custom-error', foreground: 'ff0000', fontStyle: 'bold' },
      { token: 'custom-warn', foreground: 'ffa500' },
      { token: 'custom-debug', foreground: 'ffa500' },
      { token: 'custom-date', foreground: '008800' },
      { token: '', background: '#f6f7f8' },
    ],
  }
  monaco.editor.defineTheme('logTheme', themeData)
}
