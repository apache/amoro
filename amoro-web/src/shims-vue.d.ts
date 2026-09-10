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

/* eslint-disable */
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}
declare module "*.png" {
  const content: any;
  export default content;
}

declare module 'sql-formatter'

// monaco-editor 0.56 on-demand side-effect imports: the submodules below
// only have runtime side effects (feature/style registration) and ship no
// .d.ts in the package; declare them as empty modules for vue-tsc
// (their values are unused, so no types are needed)
declare module 'monaco-editor/languages/definitions/sql/register.js'
declare module 'monaco-editor/editor/browser/coreCommands.js'
declare module 'monaco-editor/editor/contrib/suggest/browser/suggestController.js'
declare module 'monaco-editor/editor/contrib/format/browser/formatActions.js'
declare module 'monaco-editor/features/find/register.js'
declare module 'monaco-editor/editor/contrib/contextmenu/browser/contextmenu.js'
declare module 'monaco-editor/editor/contrib/clipboard/browser/clipboard.js'
declare module 'monaco-editor/editor/contrib/folding/browser/folding.js'
declare module 'monaco-editor/editor/contrib/bracketMatching/browser/bracketMatching.js'
declare module 'monaco-editor/editor/contrib/linesOperations/browser/linesOperations.js'
declare module 'monaco-editor/editor/contrib/wordOperations/browser/wordOperations.js'
declare module 'monaco-editor/editor/contrib/comment/browser/comment.js'
declare module 'monaco-editor/features/codicon/register.js'
