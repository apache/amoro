
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
 *-----------------------------------------------------------------------------*/var e={wordPattern:/(-?\d*\.\d\w*)|([^\`\~\!\@\#%\^\&\*\(\)\=\+\[\{\]\}\\\|\;\:\'\"\,\.\<\>\/\?\s]+)/g,comments:{lineComment:"#",blockComment:["<#","#>"]},brackets:[["{","}"],["[","]"],["(",")"]],autoClosingPairs:[{open:"{",close:"}"},{open:"[",close:"]"},{open:"(",close:")"},{open:'"',close:'"',notIn:["string"]},{open:"'",close:"'",notIn:["string","comment"]}],surroundingPairs:[{open:"{",close:"}"},{open:"[",close:"]"},{open:"(",close:")"},{open:'"',close:'"'},{open:"'",close:"'"}],folding:{markers:{start:new RegExp("^\\s*#region\\b"),end:new RegExp("^\\s*#endregion\\b")}}},n={defaultToken:"",ignoreCase:!0,tokenPostfix:".ps1",brackets:[{token:"delimiter.curly",open:"{",close:"}"},{token:"delimiter.square",open:"[",close:"]"},{token:"delimiter.parenthesis",open:"(",close:")"}],keywords:["begin","break","catch","class","continue","data","define","do","dynamicparam","else","elseif","end","exit","filter","finally","for","foreach","from","function","if","in","param","process","return","switch","throw","trap","try","until","using","var","while","workflow","parallel","sequence","inlinescript","configuration"],helpKeywords:/SYNOPSIS|DESCRIPTION|PARAMETER|EXAMPLE|INPUTS|OUTPUTS|NOTES|LINK|COMPONENT|ROLE|FUNCTIONALITY|FORWARDHELPTARGETNAME|FORWARDHELPCATEGORY|REMOTEHELPRUNSPACE|EXTERNALHELP/,symbols:/[=><!~?&%|+\-*\/\^;\.,]+/,escapes:/`(?:[abfnrtv\\"'$]|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,tokenizer:{root:[[/[a-zA-Z_][\w-]*/,{cases:{"@keywords":{token:"keyword.$0"},"@default":""}}],[/[ \t\r\n]+/,""],[/^:\w*/,"metatag"],[/\$(\{((global|local|private|script|using):)?[\w]+\}|((global|local|private|script|using):)?[\w]+)/,"variable"],[/<#/,"comment","@comment"],[/#.*$/,"comment"],[/[{}()\[\]]/,"@brackets"],[/@symbols/,"delimiter"],[/\d*\.\d+([eE][\-+]?\d+)?/,"number.float"],[/0[xX][0-9a-fA-F_]*[0-9a-fA-F]/,"number.hex"],[/\d+?/,"number"],[/[;,.]/,"delimiter"],[/\@"/,"string",'@herestring."'],[/\@'/,"string","@herestring.'"],[/"/,{cases:{"@eos":"string","@default":{token:"string",next:'@string."'}}}],[/'/,{cases:{"@eos":"string","@default":{token:"string",next:"@string.'"}}}]],string:[[/[^"'\$`]+/,{cases:{"@eos":{token:"string",next:"@popall"},"@default":"string"}}],[/@escapes/,{cases:{"@eos":{token:"string.escape",next:"@popall"},"@default":"string.escape"}}],[/`./,{cases:{"@eos":{token:"string.escape.invalid",next:"@popall"},"@default":"string.escape.invalid"}}],[/\$[\w]+$/,{cases:{'$S2=="':{token:"variable",next:"@popall"},"@default":{token:"string",next:"@popall"}}}],[/\$[\w]+/,{cases:{'$S2=="':"variable","@default":"string"}}],[/["']/,{cases:{"$#==$S2":{token:"string",next:"@pop"},"@default":{cases:{"@eos":{token:"string",next:"@popall"},"@default":"string"}}}}]],herestring:[[/^\s*(["'])@/,{cases:{"$1==$S2":{token:"string",next:"@pop"},"@default":"string"}}],[/[^\$`]+/,"string"],[/@escapes/,"string.escape"],[/`./,"string.escape.invalid"],[/\$[\w]+/,{cases:{'$S2=="':"variable","@default":"string"}}]],comment:[[/[^#\.]+/,"comment"],[/#>/,"comment","@pop"],[/(\.)(@helpKeywords)(?!\w)/,{token:"comment.keyword.$2"}],[/[\.#]/,"comment"]]}};export{e as conf,n as language};
