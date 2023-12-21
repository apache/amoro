
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

import{u as P}from"./usePlaceholder-oV8njzjL.js";import{d as D,a as d,r as m,b as n,o as $,j as w,B as s,c as e,a1 as S,ad as T,z as p,w as c,_ as q}from"./index-7UdTOOsH.js";const O={class:"create-table"},U={class:"nav-bar"},x={class:"title g-ml-8"},y={class:"content"},I={class:"basic"},R={class:"title"},V=D({__name:"create",emits:["goBack"],setup(j,{emit:_}){const b=_,f=d(),g=d([{value:"catalog1",label:"catalog1"},{value:"catalog2",label:"catalog2"}]),h=d([{value:"database1",label:"database1"},{value:"database2",label:"database2"}]),a=m({catalog:"catalog1",database:"",tableName:""}),t=m(P());function v(){}function C(){}function B(){b("goBack")}return(u,l)=>{const i=n("a-select"),r=n("a-form-item"),N=n("a-input"),k=n("a-form");return $(),w("div",O,[s("div",U,[e(S(T),{onClick:B}),s("span",x,p(u.$t("createTable")),1)]),s("div",y,[s("div",I,[s("p",R,p(u.$t("basicInformation")),1),e(k,{ref_key:"formRef",ref:f,model:a,class:"label-120"},{default:c(()=>[e(r,{name:"catalog",label:"Catalog",rules:[{required:!0,message:`${t.selectClPh}`}]},{default:c(()=>[e(i,{value:a.catalog,"onUpdate:value":l[0]||(l[0]=o=>a.catalog=o),options:g.value,showSearch:"",onChange:v,placeholder:t.selectClPh},null,8,["value","options","placeholder"])]),_:1},8,["rules"]),e(r,{name:"database",label:"Database",rules:[{required:!0,message:`${t.selectDBPh}`}]},{default:c(()=>[e(i,{value:a.database,"onUpdate:value":l[1]||(l[1]=o=>a.database=o),options:h.value,showSearch:"",onChange:C,placeholder:t.selectDBPh},null,8,["value","options","placeholder"])]),_:1},8,["rules"]),e(r,{name:"tableName",label:"Table",rules:[{required:!0,message:`${t.inputTNPh}`}]},{default:c(()=>[e(N,{value:a.tableName,"onUpdate:value":l[2]||(l[2]=o=>a.tableName=o),placeholder:t.inputTNPh},null,8,["value","placeholder"])]),_:1},8,["rules"])]),_:1},8,["model"])])])])}}}),L=q(V,[["__scopeId","data-v-f6c88dc2"]]);export{L as default};
