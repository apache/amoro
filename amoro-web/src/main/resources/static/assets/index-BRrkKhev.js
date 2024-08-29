
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

import{d as v,a7 as S,r as _,c as y,o as k,X as I,a0 as B,a3 as l,k as o,Y as a,G as F,a6 as P,ab as U,ac as L}from"./vue-KZ6HWXOH.js";import{u as $,l as N,_ as O}from"./index-C3lMfZSI.js";import{u as V}from"./usePlaceholder-DDdrf-Dx.js";import{a as f,F as q,U as R,I as C,c as Q,R as j,V as D,B as E}from"./antd-CPSoav-4.js";import"./pinia-BCc-Ghqu.js";import"./dayjs-DZyl58SH.js";import"./monaco-DM8nFf3H.js";import"./sql-BwF-LpWF.js";import"./axios-B4uVmeYG.js";const G=v({name:"Login",setup(){const e=S(),s=_({username:"",password:""}),c=_(V()),d=async i=>{try{const t=$(),n=await N.login({user:i.username,password:i.password});if(n.code!==200){f.error(n.message);return}const{path:r,query:p}=t.historyPathInfo;e.replace({path:r||"/",query:p})}catch(t){f.error(t.message)}},u=y(()=>!(s.username&&s.password));return k(()=>{}),{placeholder:c,formState:s,onFinish:d,disabled:u}}}),H=""+new URL("logo-all1-B0QkNmHQ.svg",import.meta.url).href,h=e=>(U("data-v-fce3b952"),e=e(),L(),e),M={class:"login-wrap g-flex-jc"},T={class:"login-content"},X=h(()=>l("div",{class:"img-logo"},[l("img",{src:H,class:"arctic-logo",alt:""})],-1)),Y=h(()=>l("div",{class:"content-title"}," Lakehouse management system ",-1));function z(e,s,c,d,u,i){const t=R,n=C,r=Q,p=j,g=D,w=E,b=q;return I(),B("div",M,[l("div",T,[X,Y,o(b,{model:e.formState,name:"normal_login",class:"login-form label-120",onFinish:e.onFinish},{default:a(()=>[o(r,{label:"",name:"username",rules:[{required:!0,message:e.placeholder.usernamePh}]},{default:a(()=>[o(n,{value:e.formState.username,"onUpdate:value":s[0]||(s[0]=m=>e.formState.username=m),placeholder:e.placeholder.usernamePh,style:{height:"48px",background:"#fff"}},{prefix:a(()=>[o(t,{class:"site-form-item-icon"})]),_:1},8,["value","placeholder"])]),_:1},8,["rules"]),o(r,{label:"",name:"password",rules:[{required:!0,message:e.placeholder.passwordPh}]},{default:a(()=>[o(g,{value:e.formState.password,"onUpdate:value":s[1]||(s[1]=m=>e.formState.password=m),placeholder:e.placeholder.passwordPh,style:{height:"48px"}},{prefix:a(()=>[o(p,{class:"site-form-item-icon"})]),_:1},8,["value","placeholder"])]),_:1},8,["rules"]),o(r,null,{default:a(()=>[o(w,{disabled:e.disabled,type:"primary","html-type":"submit",class:"login-form-button"},{default:a(()=>[F(P(e.$t("signin")),1)]),_:1},8,["disabled"])]),_:1})]),_:1},8,["model","onFinish"])])])}const ae=O(G,[["render",z],["__scopeId","data-v-fce3b952"]]);export{ae as default};
