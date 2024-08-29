
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

import{r as o}from"./index-C3lMfZSI.js";function i(){return o.get("ams/v1/catalogs")}function p(a){const{catalog:t,keywords:s}=a;return o.get(`ams/v1/catalogs/${t}/databases`,{params:{keywords:s}})}function $(a){const{catalog:t,db:s,keywords:e}=a;return o.get(`ams/v1/catalogs/${t}/databases/${s}/tables`,{params:{keywords:e}})}function u({catalog:a="",db:t="",table:s="",token:e=""}){return o.get(`ams/v1/tables/catalogs/${a}/dbs/${t}/tables/${s}/details`,{params:{token:e}})}function d({catalog:a="",db:t="",table:s=""}){return o.get(`ams/v1/tables/catalogs/${a}/dbs/${t}/tables/${s}/hive/details`)}function m({catalog:a="",db:t="",table:s=""}){return o.get(`ams/v1/tables/catalogs/${a}/dbs/${t}/tables/${s}/upgrade/status`)}function f(a){const{catalog:t,db:s,table:e,filter:n,page:g,pageSize:l,token:r}=a;return o.get(`ams/v1/tables/catalogs/${t}/dbs/${s}/tables/${e}/partitions`,{params:{filter:n,page:g,pageSize:l,token:r}})}function v(a){const{catalog:t,db:s,table:e,partition:n,specId:g,page:l,pageSize:r,token:b}=a;return o.get(`ams/v1/tables/catalogs/${t}/dbs/${s}/tables/${e}/partitions/${n}/files`,{params:{specId:g,page:l,pageSize:r,token:b}})}function z(a){const{catalog:t,db:s,table:e,page:n,pageSize:g,token:l,ref:r,operation:b}=a;return o.get(`ams/v1/tables/catalogs/${t}/dbs/${s}/tables/${e}/snapshots`,{params:{page:n,pageSize:g,token:l,ref:r,operation:b}})}function k(a){const{catalog:t,db:s,table:e,snapshotId:n,page:g,pageSize:l,token:r}=a;return o.get(`ams/v1/tables/catalogs/${t}/dbs/${s}/tables/${e}/snapshots/${n}/detail`,{params:{page:g,pageSize:l,token:r}})}function S(a){const{catalog:t,db:s,table:e,page:n,pageSize:g,token:l}=a;return o.get(`ams/v1/tables/catalogs/${t}/dbs/${s}/tables/${e}/operations`,{params:{page:n,pageSize:g,token:l}})}function h(a){const{catalog:t,db:s,table:e,page:n,pageSize:g,token:l}=a;return o.get(`ams/v1/tables/catalogs/${t}/dbs/${s}/tables/${e}/optimizing-processes`,{params:{page:n,pageSize:g,token:l}})}function T(a){const{catalog:t,db:s,table:e,processId:n,page:g,pageSize:l,token:r}=a;return o.get(`ams/v1/tables/catalogs/${t}/dbs/${s}/tables/${e}/optimizing-processes/${n}/tasks`,{params:{page:g,pageSize:l,token:r}})}function P({catalog:a="",db:t="",table:s="",properties:e={},pkList:n=[]}){return o.post(`ams/v1/tables/catalogs/${a}/dbs/${t}/tables/${s}/upgrade`,{properties:e,pkList:n})}function I(){return o.get("ams/v1/upgrade/properties")}function y({catalog:a="",db:t="",table:s="",processId:e=""}){return o.post(`ams/v1/tables/catalogs/${a}/dbs/${t}/tables/${s}/optimizing-processes/${e}/cancel`)}function D(a){const{catalog:t,db:s,table:e}=a;return o.get(`/ams/v1/tables/catalogs/${t}/dbs/${s}/tables/${e}/branches`)}function O(a){const{catalog:t,db:s,table:e}=a;return o.get(`/ams/v1/tables/catalogs/${t}/dbs/${s}/tables/${e}/tags`)}function B(a){const{catalog:t,db:s,table:e}=a;return o.get(`/ams/v1/tables/catalogs/${t}/dbs/${s}/tables/${e}/consumers`)}export{p as a,$ as b,u as c,f as d,v as e,S as f,i as g,D as h,O as i,B as j,z as k,k as l,T as m,h as n,y as o,m as p,d as q,I as r,P as u};
