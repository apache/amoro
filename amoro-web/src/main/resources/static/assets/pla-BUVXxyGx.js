
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

/*!-----------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Version: 0.49.0(383fdf3fc0e1e1a024068b8d0fd4f3dcbae74d04)
 * Released under the MIT license
 * https://github.com/microsoft/monaco-editor/blob/main/LICENSE.txt
 *-----------------------------------------------------------------------------*/var e={comments:{lineComment:"#"},brackets:[["[","]"],["<",">"],["(",")"]],autoClosingPairs:[{open:"[",close:"]"},{open:"<",close:">"},{open:"(",close:")"}],surroundingPairs:[{open:"[",close:"]"},{open:"<",close:">"},{open:"(",close:")"}]},o={defaultToken:"",tokenPostfix:".pla",brackets:[{open:"[",close:"]",token:"delimiter.square"},{open:"<",close:">",token:"delimiter.angle"},{open:"(",close:")",token:"delimiter.parenthesis"}],keywords:[".i",".o",".mv",".ilb",".ob",".label",".type",".phase",".pair",".symbolic",".symbolic-output",".kiss",".p",".e",".end"],comment:/#.*$/,identifier:/[a-zA-Z]+[a-zA-Z0-9_\-]*/,plaContent:/[01\-~\|]+/,tokenizer:{root:[{include:"@whitespace"},[/@comment/,"comment"],[/\.([a-zA-Z_\-]+)/,{cases:{"@eos":{token:"keyword.$1"},"@keywords":{cases:{".type":{token:"keyword.$1",next:"@type"},"@default":{token:"keyword.$1",next:"@keywordArg"}}},"@default":{token:"keyword.$1"}}}],[/@identifier/,"identifier"],[/@plaContent/,"string"]],whitespace:[[/[ \t\r\n]+/,""]],type:[{include:"@whitespace"},[/\w+/,{token:"type",next:"@pop"}]],keywordArg:[[/[ \t\r\n]+/,{cases:{"@eos":{token:"",next:"@pop"},"@default":""}}],[/@comment/,"comment","@pop"],[/[<>()\[\]]/,{cases:{"@eos":{token:"@brackets",next:"@pop"},"@default":"@brackets"}}],[/\-?\d+/,{cases:{"@eos":{token:"number",next:"@pop"},"@default":"number"}}],[/@identifier/,{cases:{"@eos":{token:"identifier",next:"@pop"},"@default":"identifier"}}],[/[;=]/,{cases:{"@eos":{token:"delimiter",next:"@pop"},"@default":"delimiter"}}]]}};export{e as conf,o as language};
