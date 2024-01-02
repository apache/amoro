
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

var e={brackets:[["{","}"],["[","]"],["(",")"]],autoClosingPairs:[{open:"{",close:"}"},{open:"[",close:"]"},{open:"(",close:")"},{open:'"',close:'"'},{open:"'",close:"'"}],surroundingPairs:[{open:"{",close:"}"},{open:"[",close:"]"},{open:"(",close:")"},{open:'"',close:'"'},{open:"'",close:"'"}]},o={defaultToken:"",tokenPostfix:".dockerfile",variable:/\${?[\w]+}?/,tokenizer:{root:[{include:"@whitespace"},{include:"@comment"},[/(ONBUILD)(\s+)/,["keyword",""]],[/(ENV)(\s+)([\w]+)/,["keyword","",{token:"variable",next:"@arguments"}]],[/(FROM|MAINTAINER|RUN|EXPOSE|ENV|ADD|ARG|VOLUME|LABEL|USER|WORKDIR|COPY|CMD|STOPSIGNAL|SHELL|HEALTHCHECK|ENTRYPOINT)/,{token:"keyword",next:"@arguments"}]],arguments:[{include:"@whitespace"},{include:"@strings"},[/(@variable)/,{cases:{"@eos":{token:"variable",next:"@popall"},"@default":"variable"}}],[/\\/,{cases:{"@eos":"","@default":""}}],[/./,{cases:{"@eos":{token:"",next:"@popall"},"@default":""}}]],whitespace:[[/\s+/,{cases:{"@eos":{token:"",next:"@popall"},"@default":""}}]],comment:[[/(^#.*$)/,"comment","@popall"]],strings:[[/'$/,"string","@popall"],[/'/,"string","@stringBody"],[/"$/,"string","@popall"],[/"/,"string","@dblStringBody"]],stringBody:[[/[^\\\$']/,{cases:{"@eos":{token:"string",next:"@popall"},"@default":"string"}}],[/\\./,"string.escape"],[/'$/,"string","@popall"],[/'/,"string","@pop"],[/(@variable)/,"variable"],[/\\$/,"string"],[/$/,"string","@popall"]],dblStringBody:[[/[^\\\$"]/,{cases:{"@eos":{token:"string",next:"@popall"},"@default":"string"}}],[/\\./,"string.escape"],[/"$/,"string","@popall"],[/"/,"string","@pop"],[/(@variable)/,"variable"],[/\\$/,"string"],[/$/,"string","@popall"]]}};export{e as conf,o as language};
