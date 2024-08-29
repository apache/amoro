
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

import{N as F,f as H,O as V,P as Y,e as Z,w as G,r as $,H as C,Q as J,J as A,R as T,S as tt,y as et,I as st,c as nt}from"./vue-KZ6HWXOH.js";var ot=!1;/*!
 * pinia v2.1.7
 * (c) 2023 Eduardo San Martin Morote
 * @license MIT
 */let N;const E=t=>N=t,B=Symbol();function R(t){return t&&typeof t=="object"&&Object.prototype.toString.call(t)==="[object Object]"&&typeof t.toJSON!="function"}var x;(function(t){t.direct="direct",t.patchObject="patch object",t.patchFunction="patch function"})(x||(x={}));function ht(){const t=F(!0),o=t.run(()=>H({}));let s=[],e=[];const r=V({install(u){E(r),r._a=u,u.provide(B,r),u.config.globalProperties.$pinia=r,e.forEach(f=>s.push(f)),e=[]},use(u){return!this._a&&!ot?e.push(u):s.push(u),this},_p:s,_a:null,_e:t,_s:new Map,state:o});return r}const D=()=>{};function W(t,o,s,e=D){t.push(o);const r=()=>{const u=t.indexOf(o);u>-1&&(t.splice(u,1),e())};return!s&&T()&&tt(r),r}function P(t,...o){t.slice().forEach(s=>{s(...o)})}const ct=t=>t();function k(t,o){t instanceof Map&&o instanceof Map&&o.forEach((s,e)=>t.set(e,s)),t instanceof Set&&o instanceof Set&&o.forEach(t.add,t);for(const s in o){if(!o.hasOwnProperty(s))continue;const e=o[s],r=t[s];R(r)&&R(e)&&t.hasOwnProperty(s)&&!C(e)&&!J(e)?t[s]=k(r,e):t[s]=e}return t}const rt=Symbol();function ut(t){return!R(t)||!t.hasOwnProperty(rt)}const{assign:v}=Object;function at(t){return!!(C(t)&&t.effect)}function ft(t,o,s,e){const{state:r,actions:u,getters:f}=o,a=s.state.value[t];let w;function b(){a||(s.state.value[t]=r?r():{});const y=st(s.state.value[t]);return v(y,u,Object.keys(f||{}).reduce((d,_)=>(d[_]=V(nt(()=>{E(s);const m=s._s.get(t);return f[_].call(m,m)})),d),{}))}return w=Q(t,b,o,s,e,!0),w}function Q(t,o,s={},e,r,u){let f;const a=v({actions:{}},s),w={deep:!0};let b,y,d=[],_=[],m;const j=e.state.value[t];!u&&!j&&(e.state.value[t]={}),H({});let I;function L(c){let n;b=y=!1,typeof c=="function"?(c(e.state.value[t]),n={type:x.patchFunction,storeId:t,events:m}):(k(e.state.value[t],c),n={type:x.patchObject,payload:c,storeId:t,events:m});const h=I=Symbol();et().then(()=>{I===h&&(b=!0)}),y=!0,P(d,n,e.state.value[t])}const q=u?function(){const{state:n}=s,h=n?n():{};this.$patch(S=>{v(S,h)})}:D;function z(){f.stop(),d=[],_=[],e._s.delete(t)}function K(c,n){return function(){E(e);const h=Array.from(arguments),S=[],g=[];function U(i){S.push(i)}function X(i){g.push(i)}P(_,{args:h,name:c,store:l,after:U,onError:X});let p;try{p=n.apply(this&&this.$id===t?this:l,h)}catch(i){throw P(g,i),i}return p instanceof Promise?p.then(i=>(P(S,i),i)).catch(i=>(P(g,i),Promise.reject(i))):(P(S,p),p)}}const M={_p:e,$id:t,$onAction:W.bind(null,_),$patch:L,$reset:q,$subscribe(c,n={}){const h=W(d,c,n.detached,()=>S()),S=f.run(()=>G(()=>e.state.value[t],g=>{(n.flush==="sync"?y:b)&&c({storeId:t,type:x.direct,events:m},g)},v({},w,n)));return h},$dispose:z},l=$(M);e._s.set(t,l);const O=(e._a&&e._a.runWithContext||ct)(()=>e._e.run(()=>(f=F()).run(o)));for(const c in O){const n=O[c];if(C(n)&&!at(n)||J(n))u||(j&&ut(n)&&(C(n)?n.value=j[c]:k(n,j[c])),e.state.value[t][c]=n);else if(typeof n=="function"){const h=K(c,n);O[c]=h,a.actions[c]=n}}return v(l,O),v(A(l),O),Object.defineProperty(l,"$state",{get:()=>e.state.value[t],set:c=>{L(n=>{v(n,c)})}}),e._p.forEach(c=>{v(l,f.run(()=>c({store:l,app:e._a,pinia:e,options:a})))}),j&&u&&s.hydrate&&s.hydrate(l.$state,j),b=!0,y=!0,l}function bt(t,o,s){let e,r;const u=typeof o=="function";e=t,r=u?s:o;function f(a,w){const b=Y();return a=a||(b?Z(B,null):null),a&&E(a),a=N,a._s.has(e)||(u?Q(e,o,r,a):ft(e,r,a)),a._s.get(e)}return f.$id=e,f}export{ht as c,bt as d};
