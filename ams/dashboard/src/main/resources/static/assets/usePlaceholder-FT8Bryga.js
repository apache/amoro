
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

import{U as d,V as l}from"./index-_J0gz4e7.js";function v(){const{t:e}=d(),t=l(()=>e("catalog")).value,a=l(()=>e("databaseName")).value,h=l(()=>e("tableName")).value,P=l(()=>e("optimzerGroup")).value,u=l(()=>e("resourceGroup")).value,o=l(()=>e("parallelism")).value,r=l(()=>e("username")).value,n=l(()=>e("password")).value,c=l(()=>e("database",2)).value,s=l(()=>e("table",2)).value,p=l(()=>e("name")).value,i=l(()=>e("container")).value;return{selectPh:e("selectPlaceholder"),inputPh:e("inputPlaceholder"),selectClPh:e("selectPlaceholder",{selectPh:t}),selectDBPh:e("selectPlaceholder",{selectPh:a}),inputDBPh:e("inputPlaceholder",{inputPh:a}),inputClPh:e("inputPlaceholder",{inputPh:t}),inputTNPh:e("inputPlaceholder",{inputPh:h}),selectOptGroupPh:e("inputPlaceholder",{inputPh:P}),resourceGroupPh:e("inputPlaceholder",{inputPh:u}),parallelismPh:e("inputPlaceholder",{inputPh:o}),usernamePh:e("inputPlaceholder",{inputPh:r}),passwordPh:e("inputPlaceholder",{inputPh:n}),filterDBPh:e("filterPlaceholder",{inputPh:c}),filterTablePh:e("filterPlaceholder",{inputPh:s}),groupNamePh:e("inputPlaceholder",{inputPh:p}),groupContainer:e("selectPlaceholder",{selectPh:i})}}export{v as u};
