
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

import{d as U,M as T,s as q,b as p,o as C,j as P,c as a,w as d,e as H,l as A,a1 as B,_ as D,r as k,a as I,Z as M,V as K,B as v,z as j,F as z,v as G,Y as V,ab as S,ac as Y,D as Z,G as J,N as Q,ad as W}from"./index-7UdTOOsH.js";import{q as X,p as ee,u as te}from"./table.service-t8OmEx3P.js";import{u as ae}from"./usePlaceholder-oV8njzjL.js";const oe={class:"field-wrap"},se=U({__name:"Field",props:{fields:{},loading:{type:Boolean}},setup(F,{expose:_}){const{t:o}=T(),f=F,u=q([{dataIndex:"field",title:o("field"),ellipsis:!0},{dataIndex:"type",title:o("type"),ellipsis:!0},{dataIndex:"comment",title:o("description"),ellipsis:!0},{dataIndex:"primaryKey",title:o("primaryKey"),scopedSlots:{customRender:"primaryKey"}}]);return _({getPkname(){return f.fields.filter(n=>n.checked).map(n=>({fieldName:n.field||""}))}}),(n,h)=>{const l=p("a-checkbox"),x=p("a-table");return C(),P("div",oe,[a(x,{loading:n.loading,class:"ant-table-common",columns:B(u),"data-source":f.fields,pagination:!1},{bodyCell:d(({column:b,record:$})=>[b.dataIndex==="primaryKey"?(C(),H(l,{key:0,checked:$.checked,"onUpdate:checked":O=>$.checked=O},null,8,["checked","onUpdate:checked"])):A("",!0)]),_:1},8,["loading","columns","data-source"])])}}}),ne=D(se,[["__scopeId","data-v-09ced4ae"]]),le={class:"partition-field-wrap"},ie=U({__name:"Partition",props:{partitionFields:{},loading:{type:Boolean}},setup(F){const{t:_}=T(),o=F,f=q([{dataIndex:"field",title:_("field"),ellipsis:!0},{dataIndex:"type",title:_("type"),ellipsis:!0},{dataIndex:"comment",title:_("description"),ellipsis:!0}]);return(u,n)=>{const h=p("a-table");return C(),P("div",le,[a(h,{loading:u.loading,class:"ant-table-common",columns:B(f),"data-source":o.partitionFields,pagination:!1},null,8,["loading","columns","data-source"])])}}}),re={class:"config-properties"},ce={class:"config-header g-flex"},de={class:"td g-flex-ac"},ue={class:"td g-flex-ac bd-left"},pe=U({__name:"Properties",props:{propertiesObj:{}},setup(F,{expose:_}){const o=F,f=k([]),u=I(),n=k([]),h=I(),l=k({data:[]}),x=k(ae());M(()=>o.propertiesObj,()=>{b()},{immediate:!0,deep:!0});function b(){f.length=0,l.data.length=0,Object.keys(o.propertiesObj).forEach(e=>{l.data.push({key:e,value:o.propertiesObj[e],uuid:S()})})}async function $(){n.length=0,u.value=[];const e=await X();Object.keys(e).forEach(t=>{const y={key:t,label:t,value:t,text:e[t]||""};n.push(y),u.value.push(y)})}function O(e,t){return t.key.toUpperCase().indexOf(e.toUpperCase())>=0}function L(e,t,y){const s=t.key,i=n.find(g=>g.key===s),r=l.data.find(g=>g.uuid===y.uuid);r&&(r.value=i.text||"",r.key=i.key||"")}function E(e){const t=l.data.indexOf(e);t!==-1&&l.data.splice(t,1)}function N(){l.data.push({key:"",value:"",uuid:S()})}return _({getProperties(){return h.value.validateFields().then(()=>{const e={};return l.data.forEach(t=>{e[t.key]=t.value}),Promise.resolve(e)}).catch(()=>!1)}}),K(()=>{$()}),(e,t)=>{const y=p("a-auto-complete"),s=p("a-form-item"),i=p("a-input"),r=p("a-form"),g=p("a-button");return C(),P("div",re,[v("div",ce,[v("div",de,j(e.$t("key")),1),v("div",ue,j(e.$t("value")),1)]),a(r,{ref_key:"propertiesFormRef",ref:h,model:l,class:"g-mt-12"},{default:d(()=>[(C(!0),P(z,null,G(l.data,(c,R)=>(C(),P("div",{class:"config-row",key:c.uuid},[a(s,{name:["data",R,"key"],rules:[{required:!0,message:`${e.$t(x.selectPh)}`}],class:"g-mr-8"},{default:d(()=>[a(y,{value:c.key,"onUpdate:value":m=>c.key=m,options:u.value,onSelect:(m,w)=>L(m,w,c),"filter-option":O,style:{width:"100%"},class:"g-mr-12"},{option:d(({key:m})=>[v("span",null,j(m),1)]),_:2},1032,["value","onUpdate:value","options","onSelect"])]),_:2},1032,["name","rules"]),a(s,{name:["data",R,"value"],rules:[{required:!0,message:`${e.$t(x.inputPh)}`}]},{default:d(()=>[a(i,{value:c.value,"onUpdate:value":m=>c.value=m,maxlength:64,style:{width:"100%"}},null,8,["value","onUpdate:value"])]),_:2},1032,["name","rules"]),a(B(Y),{class:"icon-close",onClick:m=>E(c)},null,8,["onClick"])]))),128))]),_:1},8,["model"]),a(g,{class:"config-btn",onClick:N},{default:d(()=>[V("+")]),_:1})])}}}),fe={class:"upgrade-table"},me={class:"nav-bar"},_e={class:"title g-ml-8"},he={class:"content"},ge={class:"table-attrs"},ve={class:"footer-btn"},be=U({__name:"upgrade",emits:["goBack","refresh"],setup(F,{emit:_}){const o=I(!1),f=k([]),u=k([]),n=k({}),h=k([]),l=_;Z();const x=J(),b=Q(()=>({...x.query})),$=I(),O=I();async function L(){try{const{catalog:s,db:i,table:r}=b.value;if(!s||!i||!r)return;o.value=!0,u.length=0,f.length=0;const g=await ee({...b.value}),{partitionColumnList:c=[],schema:R,properties:m}=g;(c||[]).forEach(w=>{u.push(w)}),(R||[]).forEach(w=>{f.push(w)}),Object.assign(n,m)}catch{}finally{o.value=!1}}function E(){N()}async function N(){h.length=0,$.value.getPkname().forEach(i=>{h.push(i)}),O.value.getProperties().then(i=>{i&&(Object.assign(n,i),e())})}async function e(){try{const{catalog:s,db:i,table:r}=b.value;if(!s||!i||!r)return;o.value=!0,await te({...b.value,pkList:h,properties:n}),t(),l("refresh")}catch{t()}finally{o.value=!1}}function t(){l("goBack")}function y(){t()}return K(()=>{L()}),(s,i)=>{const r=p("a-form-item"),g=p("a-form"),c=p("a-button");return C(),P("div",fe,[v("div",me,[a(B(W),{onClick:t}),v("span",_e,j(s.$t("upgradeHiveTable")),1)]),v("div",he,[v("div",ge,[a(g,{name:"fields",class:"label-120"},{default:d(()=>[a(r,{label:s.$t("field"),name:"field"},{default:d(()=>[a(ne,{loading:o.value,fields:f,ref_key:"schemaFieldRef",ref:$},null,8,["loading","fields"])]),_:1},8,["label"]),a(r,{label:s.$t("partitonField"),name:"partitonField"},{default:d(()=>[a(ie,{loading:o.value,partitionFields:u},null,8,["loading","partitionFields"])]),_:1},8,["label"]),a(r,{label:s.$t("otherProperties"),name:"otherProperties"},{default:d(()=>[a(pe,{propertiesObj:n,ref_key:"propertiesRef",ref:O},null,8,["propertiesObj"])]),_:1},8,["label"])]),_:1})]),v("div",ve,[a(c,{type:"primary",onClick:E,loading:o.value,class:"btn g-mr-12"},{default:d(()=>[V(j(s.$t("ok")),1)]),_:1},8,["loading"]),a(c,{type:"ghost",onClick:y,class:"btn"},{default:d(()=>[V(j(s.$t("cancel")),1)]),_:1})])])])}}}),Ce=D(be,[["__scopeId","data-v-86c4eeeb"]]);export{Ce as default};
