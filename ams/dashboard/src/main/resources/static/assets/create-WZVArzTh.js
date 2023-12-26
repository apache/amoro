
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

import{v as k,D as r,C as d,o as P,e as D,z as s,u as a,ad as $,aC as I,y as p,h as n,E as S,G as T,I as q,H as w,x}from"./index-Sv1U-v8F.js";/* empty css              *//* empty css              *//* empty css              *//* empty css              *//* empty css              *//* empty css              */import{u as y}from"./usePlaceholder-SblOBb_X.js";const O={class:"create-table"},U={class:"nav-bar"},E={class:"title g-ml-8"},R={class:"content"},V={class:"basic"},z={class:"title"},F=k({__name:"create",emits:["goBack"],setup(G,{emit:m}){const _=m,b=r(),f=r([{value:"catalog1",label:"catalog1"},{value:"catalog2",label:"catalog2"}]),g=r([{value:"database1",label:"database1"},{value:"database2",label:"database2"}]),e=d({catalog:"catalog1",database:"",tableName:""}),t=d(y());function h(){}function v(){}function C(){_("goBack")}return(u,o)=>{const i=S,c=T,B=q,N=w;return P(),D("div",O,[s("div",U,[a($(I),{onClick:C}),s("span",E,p(u.$t("createTable")),1)]),s("div",R,[s("div",V,[s("p",z,p(u.$t("basicInformation")),1),a(N,{ref_key:"formRef",ref:b,model:e,class:"label-120"},{default:n(()=>[a(c,{name:"catalog",label:"Catalog",rules:[{required:!0,message:`${t.selectClPh}`}]},{default:n(()=>[a(i,{value:e.catalog,"onUpdate:value":o[0]||(o[0]=l=>e.catalog=l),options:f.value,showSearch:"",onChange:h,placeholder:t.selectClPh},null,8,["value","options","placeholder"])]),_:1},8,["rules"]),a(c,{name:"database",label:"Database",rules:[{required:!0,message:`${t.selectDBPh}`}]},{default:n(()=>[a(i,{value:e.database,"onUpdate:value":o[1]||(o[1]=l=>e.database=l),options:g.value,showSearch:"",onChange:v,placeholder:t.selectDBPh},null,8,["value","options","placeholder"])]),_:1},8,["rules"]),a(c,{name:"tableName",label:"Table",rules:[{required:!0,message:`${t.inputTNPh}`}]},{default:n(()=>[a(B,{value:e.tableName,"onUpdate:value":o[2]||(o[2]=l=>e.tableName=l),placeholder:t.inputTNPh},null,8,["value","placeholder"])]),_:1},8,["rules"])]),_:1},8,["model"])])])])}}}),W=x(F,[["__scopeId","data-v-4d1fcf10"]]);export{W as default};
