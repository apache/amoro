
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
 *-----------------------------------------------------------------------------*/var e={comments:{lineComment:"//"},brackets:[["{","}"],["[","]"],["(",")"]],autoClosingPairs:[{open:"{",close:"}"},{open:"[",close:"]"},{open:"(",close:")"},{open:'"',close:'"',notIn:["string","comment"]}],surroundingPairs:[{open:"{",close:"}"},{open:"[",close:"]"},{open:"(",close:")"},{open:'"',close:'"'}]},t={keywords:["namespace","open","as","operation","function","body","adjoint","newtype","controlled","if","elif","else","repeat","until","fixup","for","in","while","return","fail","within","apply","Adjoint","Controlled","Adj","Ctl","is","self","auto","distribute","invert","intrinsic","let","set","w/","new","not","and","or","use","borrow","using","borrowing","mutable","internal"],typeKeywords:["Unit","Int","BigInt","Double","Bool","String","Qubit","Result","Pauli","Range"],invalidKeywords:["abstract","base","bool","break","byte","case","catch","char","checked","class","const","continue","decimal","default","delegate","do","double","enum","event","explicit","extern","finally","fixed","float","foreach","goto","implicit","int","interface","lock","long","null","object","operator","out","override","params","private","protected","public","readonly","ref","sbyte","sealed","short","sizeof","stackalloc","static","string","struct","switch","this","throw","try","typeof","unit","ulong","unchecked","unsafe","ushort","virtual","void","volatile"],constants:["true","false","PauliI","PauliX","PauliY","PauliZ","One","Zero"],builtin:["X","Y","Z","H","HY","S","T","SWAP","CNOT","CCNOT","MultiX","R","RFrac","Rx","Ry","Rz","R1","R1Frac","Exp","ExpFrac","Measure","M","MultiM","Message","Length","Assert","AssertProb","AssertEqual"],operators:["and=","<-","->","*","*=","@","!","^","^=",":","::","..","==","...","=","=>",">",">=","<","<=","-","-=","!=","or=","%","%=","|","+","+=","?","/","/=","&&&","&&&=","^^^","^^^=",">>>",">>>=","<<<","<<<=","|||","|||=","~~~","_","w/","w/="],namespaceFollows:["namespace","open"],symbols:/[=><!~?:&|+\-*\/\^%@._]+/,escapes:/\\[\s\S]/,tokenizer:{root:[[/[a-zA-Z_$][\w$]*/,{cases:{"@namespaceFollows":{token:"keyword.$0",next:"@namespace"},"@typeKeywords":"type","@keywords":"keyword","@constants":"constant","@builtin":"keyword","@invalidKeywords":"invalid","@default":"identifier"}}],{include:"@whitespace"},[/[{}()\[\]]/,"@brackets"],[/@symbols/,{cases:{"@operators":"operator","@default":""}}],[/\d*\.\d+([eE][\-+]?\d+)?/,"number.float"],[/\d+/,"number"],[/[;,.]/,"delimiter"],[/"/,{token:"string.quote",bracket:"@open",next:"@string"}]],string:[[/[^\\"]+/,"string"],[/@escapes/,"string.escape"],[/"/,{token:"string.quote",bracket:"@close",next:"@pop"}]],namespace:[{include:"@whitespace"},[/[A-Za-z]\w*/,"namespace"],[/[\.=]/,"delimiter"],["","","@pop"]],whitespace:[[/[ \t\r\n]+/,"white"],[/(\/\/).*/,"comment"]]}};export{e as conf,t as language};
