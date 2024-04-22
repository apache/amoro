
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

var e={comments:{lineComment:"#"},brackets:[["{","}"],["[","]"],["(",")"]],autoClosingPairs:[{open:"{",close:"}"},{open:"[",close:"]"},{open:"(",close:")"},{open:'"',close:'"'}],surroundingPairs:[{open:"{",close:"}"},{open:"[",close:"]"},{open:"(",close:")"},{open:'"',close:'"'}]},o={defaultToken:"",tokenPostfix:".r",roxygen:["@param","@return","@name","@rdname","@examples","@include","@docType","@S3method","@TODO","@aliases","@alias","@assignee","@author","@callGraphDepth","@callGraph","@callGraphPrimitives","@concept","@exportClass","@exportMethod","@exportPattern","@export","@formals","@format","@importClassesFrom","@importFrom","@importMethodsFrom","@import","@keywords","@method","@nord","@note","@references","@seealso","@setClass","@slot","@source","@title","@usage"],constants:["NULL","FALSE","TRUE","NA","Inf","NaN ","NA_integer_","NA_real_","NA_complex_","NA_character_ ","T","F","LETTERS","letters","month.abb","month.name","pi","R.version.string"],keywords:["break","next","return","if","else","for","in","repeat","while","array","category","character","complex","double","function","integer","list","logical","matrix","numeric","vector","data.frame","factor","library","require","attach","detach","source"],special:["\\n","\\r","\\t","\\b","\\a","\\f","\\v","\\'",'\\"',"\\\\"],brackets:[{open:"{",close:"}",token:"delimiter.curly"},{open:"[",close:"]",token:"delimiter.bracket"},{open:"(",close:")",token:"delimiter.parenthesis"}],tokenizer:{root:[{include:"@numbers"},{include:"@strings"},[/[{}\[\]()]/,"@brackets"],{include:"@operators"},[/#'/,"comment.doc","@roxygen"],[/(^#.*$)/,"comment"],[/\s+/,"white"],[/[,:;]/,"delimiter"],[/@[a-zA-Z]\w*/,"tag"],[/[a-zA-Z]\w*/,{cases:{"@keywords":"keyword","@constants":"constant","@default":"identifier"}}]],roxygen:[[/@\w+/,{cases:{"@roxygen":"tag","@eos":{token:"comment.doc",next:"@pop"},"@default":"comment.doc"}}],[/\s+/,{cases:{"@eos":{token:"comment.doc",next:"@pop"},"@default":"comment.doc"}}],[/.*/,{token:"comment.doc",next:"@pop"}]],numbers:[[/0[xX][0-9a-fA-F]+/,"number.hex"],[/-?(\d*\.)?\d+([eE][+\-]?\d+)?/,"number"]],operators:[[/<{1,2}-/,"operator"],[/->{1,2}/,"operator"],[/%[^%\s]+%/,"operator"],[/\*\*/,"operator"],[/%%/,"operator"],[/&&/,"operator"],[/\|\|/,"operator"],[/<</,"operator"],[/>>/,"operator"],[/[-+=&|!<>^~*/:$]/,"operator"]],strings:[[/'/,"string.escape","@stringBody"],[/"/,"string.escape","@dblStringBody"]],stringBody:[[/\\./,{cases:{"@special":"string","@default":"error-token"}}],[/'/,"string.escape","@popall"],[/./,"string"]],dblStringBody:[[/\\./,{cases:{"@special":"string","@default":"error-token"}}],[/"/,"string.escape","@popall"],[/./,"string"]]}};export{e as conf,o as language};
