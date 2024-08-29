
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
 *-----------------------------------------------------------------------------*/var e={comments:{lineComment:"//",blockComment:["/*","*/"]},brackets:[["{","}"],["[","]"],["(",")"]],autoClosingPairs:[{open:"{",close:"}"},{open:"[",close:"]"},{open:"(",close:")"},{open:'"',close:'"'},{open:"'",close:"'"},{open:"`",close:"`"}],surroundingPairs:[{open:"{",close:"}"},{open:"[",close:"]"},{open:"(",close:")"},{open:'"',close:'"'},{open:"'",close:"'"},{open:"`",close:"`"}]},i={defaultToken:"",tokenPostfix:".cypher",ignoreCase:!0,brackets:[{open:"{",close:"}",token:"delimiter.curly"},{open:"[",close:"]",token:"delimiter.bracket"},{open:"(",close:")",token:"delimiter.parenthesis"}],keywords:["ALL","AND","AS","ASC","ASCENDING","BY","CALL","CASE","CONTAINS","CREATE","DELETE","DESC","DESCENDING","DETACH","DISTINCT","ELSE","END","ENDS","EXISTS","IN","IS","LIMIT","MANDATORY","MATCH","MERGE","NOT","ON","ON","OPTIONAL","OR","ORDER","REMOVE","RETURN","SET","SKIP","STARTS","THEN","UNION","UNWIND","WHEN","WHERE","WITH","XOR","YIELD"],builtinLiterals:["true","TRUE","false","FALSE","null","NULL"],builtinFunctions:["abs","acos","asin","atan","atan2","avg","ceil","coalesce","collect","cos","cot","count","degrees","e","endNode","exists","exp","floor","head","id","keys","labels","last","left","length","log","log10","lTrim","max","min","nodes","percentileCont","percentileDisc","pi","properties","radians","rand","range","relationships","replace","reverse","right","round","rTrim","sign","sin","size","split","sqrt","startNode","stDev","stDevP","substring","sum","tail","tan","timestamp","toBoolean","toFloat","toInteger","toLower","toString","toUpper","trim","type"],operators:["+","-","*","/","%","^","=","<>","<",">","<=",">=","->","<-","-->","<--"],escapes:/\\(?:[tbnrf\\"'`]|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,digits:/\d+/,octaldigits:/[0-7]+/,hexdigits:/[0-9a-fA-F]+/,tokenizer:{root:[[/[{}[\]()]/,"@brackets"],{include:"common"}],common:[{include:"@whitespace"},{include:"@numbers"},{include:"@strings"},[/:[a-zA-Z_][\w]*/,"type.identifier"],[/[a-zA-Z_][\w]*(?=\()/,{cases:{"@builtinFunctions":"predefined.function"}}],[/[a-zA-Z_$][\w$]*/,{cases:{"@keywords":"keyword","@builtinLiterals":"predefined.literal","@default":"identifier"}}],[/`/,"identifier.escape","@identifierBacktick"],[/[;,.:|]/,"delimiter"],[/[<>=%+\-*/^]+/,{cases:{"@operators":"delimiter","@default":""}}]],numbers:[[/-?(@digits)[eE](-?(@digits))?/,"number.float"],[/-?(@digits)?\.(@digits)([eE]-?(@digits))?/,"number.float"],[/-?0x(@hexdigits)/,"number.hex"],[/-?0(@octaldigits)/,"number.octal"],[/-?(@digits)/,"number"]],strings:[[/"([^"\\]|\\.)*$/,"string.invalid"],[/'([^'\\]|\\.)*$/,"string.invalid"],[/"/,"string","@stringDouble"],[/'/,"string","@stringSingle"]],whitespace:[[/[ \t\r\n]+/,"white"],[/\/\*/,"comment","@comment"],[/\/\/.*$/,"comment"]],comment:[[/\/\/.*/,"comment"],[/[^/*]+/,"comment"],[/\*\//,"comment","@pop"],[/[/*]/,"comment"]],stringDouble:[[/[^\\"]+/,"string"],[/@escapes/,"string"],[/\\./,"string.invalid"],[/"/,"string","@pop"]],stringSingle:[[/[^\\']+/,"string"],[/@escapes/,"string"],[/\\./,"string.invalid"],[/'/,"string","@pop"]],identifierBacktick:[[/[^\\`]+/,"identifier.escape"],[/@escapes/,"identifier.escape"],[/\\./,"identifier.escape.invalid"],[/`/,"identifier.escape","@pop"]]}};export{e as conf,i as language};
