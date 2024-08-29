
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

import{u as P}from"./usePlaceholder-DDdrf-Dx.js";import{H as D,F as $,b as I,c as T,I as q}from"./antd-CPSoav-4.js";import{d as w,f as r,r as d,X as O,a0 as S,a3 as s,k as e,a6 as m,Y as n}from"./vue-KZ6HWXOH.js";import{_ as U}from"./index-C3lMfZSI.js";import"./dayjs-DZyl58SH.js";import"./pinia-BCc-Ghqu.js";import"./monaco-DM8nFf3H.js";import"./sql-BwF-LpWF.js";import"./axios-B4uVmeYG.js";const x={class:"create-table"},y={class:"nav-bar"},F={class:"title g-ml-8"},R={class:"content"},V={class:"basic"},E={class:"title"},H=w({__name:"create",emits:["goBack"],setup(L,{emit:p}){const _=p,b=r(),f=r([{value:"catalog1",label:"catalog1"},{value:"catalog2",label:"catalog2"}]),g=r([{value:"database1",label:"database1"},{value:"database2",label:"database2"}]),a=d({catalog:"catalog1",database:"",tableName:""}),t=d(P());function h(){}function v(){}function C(){_("goBack")}return(i,o)=>{const k=D,u=I,c=T,B=q,N=$;return O(),S("div",x,[s("div",y,[e(k,{onClick:C}),s("span",F,m(i.$t("createTable")),1)]),s("div",R,[s("div",V,[s("p",E,m(i.$t("basicInformation")),1),e(N,{ref_key:"formRef",ref:b,model:a,class:"label-120"},{default:n(()=>[e(c,{name:"catalog",label:"Catalog",rules:[{required:!0,message:`${t.selectClPh}`}]},{default:n(()=>[e(u,{value:a.catalog,"onUpdate:value":o[0]||(o[0]=l=>a.catalog=l),options:f.value,"show-search":"",placeholder:t.selectClPh,onChange:h},null,8,["value","options","placeholder"])]),_:1},8,["rules"]),e(c,{name:"database",label:"Database",rules:[{required:!0,message:`${t.selectDBPh}`}]},{default:n(()=>[e(u,{value:a.database,"onUpdate:value":o[1]||(o[1]=l=>a.database=l),options:g.value,"show-search":"",placeholder:t.selectDBPh,onChange:v},null,8,["value","options","placeholder"])]),_:1},8,["rules"]),e(c,{name:"tableName",label:"Table",rules:[{required:!0,message:`${t.inputTNPh}`}]},{default:n(()=>[e(B,{value:a.tableName,"onUpdate:value":o[2]||(o[2]=l=>a.tableName=l),placeholder:t.inputTNPh},null,8,["value","placeholder"])]),_:1},8,["rules"])]),_:1},8,["model"])])])])}}}),Q=U(H,[["__scopeId","data-v-0be40d41"]]);export{Q as default};
