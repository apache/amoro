
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

import{T as e}from"./index-7UdTOOsH.js";function g(){return e.get("ams/v1/catalog/metastore/types")}function o(t){return e.get(`ams/v1/catalogs/${t}`)}function r(t){return e.delete(`ams/v1/catalogs/${t}`)}function c(t){return e.get(`ams/v1/catalogs/${t}/delete/check`)}function i(t){const{isCreate:a,name:s}=t;return delete t.isCreate,a?e.post("ams/v1/catalogs",{...t}):e.put(`ams/v1/catalogs/${s}`,{...t})}function u(){return e.get("ams/v1/settings/system")}function l(){return e.get("ams/v1/settings/containers")}export{o as a,u as b,c,r as d,l as e,g,i as s};
