
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

import{g as Fe}from"./table.service-t8OmEx3P.js";import{g as De,c as Ke,s as He,d as qe,a as ze}from"./setting.services-SdCB81x-.js";import{d as he,M as ve,G as ye,r as T,a as A,N as B,Z as Xe,V as Ce,b as S,o as l,j as d,B as U,c as m,w as i,z as c,e as p,l as b,F as J,v as Q,Y as F,W as le,$ as H,y as se,_ as _e,D as je,E as xe,a0 as Ve,a1 as We}from"./index-7UdTOOsH.js";import{_ as me,g as Ze}from"./optimize.service-Y_sPfOrx.js";import{u as Ye}from"./usePlaceholder-oV8njzjL.js";const Je={class:"detail-wrap"},Qe={class:"detail-content-wrap"},ea={class:"content-wrap"},aa={class:"header"},ta={key:1,class:"config-value"},oa={key:1},la={key:1},sa={key:1},na={class:"header"},ia={key:1,class:"config-value"},ra={key:1,class:"config-value"},ua={key:1,class:"config-value"},ca={key:3},pa=["onClick"],da={class:"header"},ga={key:1,class:"config-value"},fa={key:1,class:"config-value"},ma={key:1,class:"config-value"},ha={key:6},va=["onClick","title"],ya={key:1,class:"config-value"},Ca={key:1,class:"config-value"},_a={class:"header"},ba={class:"header"},ka={key:0,class:"footer-btn"},Ea={key:1,class:"footer-btn"},Ia=he({__name:"Detail",props:{isEdit:{type:Boolean}},emits:["updateEdit","updateCatalogs"],setup(ne,{emit:q}){const N={"Internal Catalog":"Internal Catalog","External Catalog":"External Catalog"},ee=ne,y=q,{t:g}=ve(),$=ye(),R=T(Ye()),x=A(""),r=B(()=>ee.isEdit),V=B(()=>"/ams/v1/files"),I=B(()=>{var t;const a=(((t=$.query)==null?void 0:t.catalogname)||"").toString();return decodeURIComponent(a)==="new catalog"}),G=B(()=>e.catalog.type==="hive");B(()=>e.catalog.type==="ams");const O=A(!1),D=A(),K=A(),z=A(),h={MIXED_HIVE:"MIXED_HIVE",ICEBERG:"ICEBERG",MIXED_ICEBERG:"MIXED_ICEBERG",PAIMON:"PAIMON"},f={[h.ICEBERG]:"Iceberg",[h.MIXED_HIVE]:"Mixed Hive",[h.MIXED_ICEBERG]:"Mixed Iceberg",[h.PAIMON]:"Paimon"},C={ams:[h.MIXED_ICEBERG,h.ICEBERG],hive:[h.MIXED_HIVE,h.MIXED_ICEBERG,h.ICEBERG,h.PAIMON],hadoop:[h.MIXED_ICEBERG,h.ICEBERG,h.PAIMON],glue:[h.MIXED_ICEBERG,h.ICEBERG],custom:[h.MIXED_ICEBERG,h.ICEBERG]},_={"hadoop.core.site":"core-site.xml","hadoop.hdfs.site":"hdfs-site.xml","hive.site":"hive-site.xml"},P={storageConfig:{"hadoop.core.site":"","hadoop.hdfs.site":""},authConfig:{"auth.kerberos.keytab":"","auth.kerberos.krb5":""}},w=A([{label:N["Internal Catalog"],value:N["Internal Catalog"]},{label:N["External Catalog"],value:N["External Catalog"]}]),e=T({catalog:{name:"",type:"ams",typeshow:N["Internal Catalog"],optimizerGroup:void 0},tableFormat:"",storageConfig:{},authConfig:{},properties:{},tableProperties:{},storageConfigArray:[],authConfigArray:[]}),W=T([{label:"SIMPLE",value:"SIMPLE"},{label:"KERBEROS",value:"KERBEROS"}]),X=T([{label:"AK/SK",value:"AK/SK"},{label:"CUSTOM",value:"CUSTOM"}]),ie={"hadoop.core.site":"Hadoop core-site","hadoop.hdfs.site":"Hadoop hdfs-site","hive.site":"Hadoop hive-site"},be={"auth.kerberos.keytab":"Kerberos Keytab","auth.kerberos.krb5":"Kerberos Krb5"},ae={ams:["warehouse"],hadoop:["warehouse"],custom:["catalog-impl"],glue:["warehouse","lock-impl","lock.table"],PAIMON:["warehouse"]};Xe(()=>$.query,a=>{a&&ke()},{immediate:!0,deep:!0});const Z=T([]),re=A([]);function ke(){te()}const Ee=async()=>{const t=(await Ze()||[]).map(s=>({lable:s.resourceGroup.name,value:s.resourceGroup.name}));re.value=t};async function Ie(){(await De()||[]).forEach(t=>{t.value!=="ams"&&Z.push({label:t.display,value:t.value})}),ue()}function ue(){x.value=(Z.find(a=>a.value===e.catalog.type)||{}).label}async function te(){try{O.value=!0;const{catalogname:a,type:t}=$.query;if(!a)return;if(I.value){e.catalog.name="",e.catalog.type=t||"ams",e.catalog.optimizerGroup=void 0,e.tableFormat=h.MIXED_ICEBERG,e.authConfig={...P.authConfig},e.storageConfig={...P.storageConfig};const n=ae[e.catalog.type]||[];e.properties={},n.forEach(v=>{e.properties[v]=""}),e.tableProperties={},e.storageConfigArray.length=0,e.authConfigArray.length=0}else{const n=await ze(a);if(!n)return;const{name:v,type:E,tableFormatList:k,storageConfig:L,authConfig:j,properties:oe,tableProperties:o,optimizerGroup:M}=n;e.catalog.name=v,e.catalog.type=E,e.catalog.optimizerGroup=M,e.tableFormat=k.join(""),e.authConfig=j,e.storageConfig=L,e.properties=oe||{},e.tableProperties=o||{},e.storageConfigArray.length=0,e.authConfigArray.length=0,ue()}e.catalog.typeshow=e.catalog.type==="ams"?N["Internal Catalog"]:N["External Catalog"];const{storageConfig:s,authConfig:u}=e;Object.keys(s).forEach(n=>{var E,k,L;const v=["hadoop.core.site","hadoop.hdfs.site"];if(G.value&&v.push("hive.site"),v.includes(n)){const j={key:n,label:ie[n],value:(E=s[n])==null?void 0:E.fileName,fileName:(k=s[n])==null?void 0:k.fileName,fileUrl:(L=s[n])==null?void 0:L.fileUrl,fileId:"",fileList:[],uploadLoading:!1,isSuccess:!1};e.storageConfigArray.push(j)}}),Object.keys(u).forEach(n=>{var v,E,k;if(["auth.kerberos.keytab","auth.kerberos.krb5"].includes(n)){const L={key:n,label:be[n],value:(v=u[n])==null?void 0:v.fileName,fileName:(E=u[n])==null?void 0:E.fileName,fileUrl:(k=u[n])==null?void 0:k.fileUrl,fileId:"",fileList:[],uploadLoading:!1,isSuccess:!1};e.authConfigArray.push(L)}})}catch{}finally{O.value=!1}}const we=a=>{a===N["Internal Catalog"]?e.catalog.type="ams":e.catalog.type=Z[0].value,de()},ce=B(()=>{const a=e.catalog.type;return C[a]||[]});async function pe(){const a=await K.value.getPropertiesWithoputValidation(),t=ae[e.catalog.type]||[];t.forEach(u=>{u&&!a[u]&&(a[u]="")});const s=ae[e.tableFormat]||[];s.forEach(u=>{u&&!a[u]&&(a[u]="")});for(const u in a)!a[u]&&!t.includes(u)&&!s.includes(u)&&delete a[u];e.properties=a}const Ne=T([{label:"S3",value:"S3"}]),$e=T([{label:"Hadoop",value:"Hadoop"}]),Me=T([{label:"Hadoop",value:"Hadoop"},{label:"S3",value:"S3"}]),Se=B(()=>{const a=e.catalog.type;return a==="ams"||a==="custom"?Me:a==="glue"?Ne:a==="hive"||a==="hadoop"?$e:null}),Re=B(()=>{const a=e.storageConfig["storage.type"];return a==="Hadoop"?W:a==="S3"?X:null});async function de(){if(e.tableFormat=ce.value[0],!I.value)return;const a=e.storageConfigArray.findIndex(t=>t.key==="hive.site");if(G.value){if(a>-1)return;e.storageConfigArray.push({key:"hive.site",label:ie["hive.site"],value:"",fileName:"",fileUrl:"",fileId:"",fileList:[],uploadLoading:!1,isSuccess:!1}),e.storageConfig["hive.site"]=""}else a>-1&&(e.storageConfigArray.splice(a,1),delete e.storageConfig["hive.site"]);await pe()}async function Pe(){await pe()}function Te(){y("updateEdit",!0)}async function Ue(){if(await Ke(e.catalog.name)){Be();return}le.confirm({title:g("cannotDeleteModalTitle"),content:g("cannotDeleteModalContent"),wrapClassName:"not-delete-modal"})}async function Ge(a,t){return t?/^[a-zA-Z][\w-]*$/.test(t)?Promise.resolve():Promise.reject(new Error(g("invalidInput"))):Promise.reject(new Error(g("inputPlaceholder")))}function Le(){const{storageConfig:a,authConfig:t,storageConfigArray:s,authConfigArray:u}=e;Object.keys(t).forEach(n=>{if(["auth.kerberos.keytab","auth.kerberos.krb5"].includes(n)){const v=(u.find(E=>E.key===n)||{}).fileId;t[n]=v}}),Object.keys(a).forEach(n=>{if(["hadoop.core.site","hadoop.hdfs.site","hive.site"].includes(n)){const v=(s.find(E=>E.key===n)||{}).fileId;a[n]=v}})}function Ae(){D.value.validateFields().then(async()=>{const{catalog:a,tableFormat:t,storageConfig:s,authConfig:u}=e,n=await K.value.getProperties(),v=await z.value.getProperties();if(!n||!v)return;O.value=!0;const{typeshow:E,...k}=a;Le(),await He({isCreate:I.value,...k,tableFormatList:[t],storageConfig:s,authConfig:u,properties:n,tableProperties:v}).then(()=>{H.success(`${g("save")} ${g("success")}`),y("updateEdit",!1,{catalogName:a.name,catalogType:a.type}),te(),D.value.resetFields()}).catch(()=>{H.error(`${g("save")} ${g("failed")}`)}).finally(()=>{O.value=!1})}).catch(()=>{})}function Oe(){D.value.resetFields(),y("updateEdit",!1),te()}async function Be(){le.confirm({title:g("deleteCatalogModalTitle"),onOk:async()=>{await qe(e.catalog.name),H.success(`${g("remove")} ${g("success")}`),y("updateEdit",!1,{})}})}function ge(a,t,s){try{if(a.file.status==="uploading"?t.uploadLoading=!0:t.uploadLoading=!1,a.file.status==="done"){const{code:u}=a.file.response;if(u!==200)throw new Error("failed");const{url:n,id:v}=a.file.response.result;t.isSuccess=!0,t.fileName=s==="STORAGE"?_[t.key]:a.file.name,t.fileUrl=n,t.fileId=v,H.success(`${a.file.name} ${g("uploaded")} ${g("success")}`)}else a.file.status==="error"&&(t.isSuccess=!1,H.error(`${a.file.name} ${g("uploaded")} ${g("failed")}`))}catch{H.error(`${g("uploaded")} ${g("failed")}`)}}function fe(a){a&&window.open(a)}return Ce(()=>{Ie(),Ee()}),(a,t)=>{const s=S("a-form-item"),u=S("a-input"),n=S("a-select"),v=S("a-radio"),E=S("a-radio-group"),k=S("a-button"),L=S("a-upload"),j=S("a-form"),oe=S("u-loading");return l(),d("div",Je,[U("div",Qe,[U("div",ea,[m(j,{ref_key:"formRef",ref:D,model:e,class:"catalog-form"},{default:i(()=>[m(s,null,{default:i(()=>[U("p",aa,c(a.$t("basic")),1)]),_:1}),m(s,{label:a.$t("name"),name:["catalog","name"],rules:[{required:r.value&&I.value,validator:Ge}]},{default:i(()=>[r.value&&I.value?(l(),p(u,{key:0,value:e.catalog.name,"onUpdate:value":t[0]||(t[0]=o=>e.catalog.name=o)},null,8,["value"])):(l(),d("span",ta,c(e.catalog.name),1))]),_:1},8,["label","rules"]),m(s,{label:a.$t("type"),name:["catalog","typeshow"]},{default:i(()=>[r.value&&I.value?(l(),p(n,{key:0,value:e.catalog.typeshow,"onUpdate:value":t[1]||(t[1]=o=>e.catalog.typeshow=o),options:w.value,placeholder:R.selectPh,onChange:we},null,8,["value","options","placeholder"])):(l(),d("span",oa,c(e.catalog.typeshow),1))]),_:1},8,["label"]),e.catalog.typeshow===N["External Catalog"]?(l(),p(s,{key:0,label:a.$t("metastore"),name:["catalog","type"],rules:[{required:r.value&&I.value}]},{default:i(()=>[r.value&&I.value?(l(),p(n,{key:0,value:e.catalog.type,"onUpdate:value":t[2]||(t[2]=o=>e.catalog.type=o),options:Z,placeholder:R.selectPh,onChange:de},null,8,["value","options","placeholder"])):(l(),d("span",la,c(x.value),1))]),_:1},8,["label","rules"])):b("",!0),m(s,{label:a.$t("tableFormat"),name:["tableFormat"],rules:[{required:r.value&&I.value}]},{default:i(()=>[m(E,{disabled:!r.value||!I.value,value:e.tableFormat,"onUpdate:value":t[3]||(t[3]=o=>e.tableFormat=o),name:"radioGroup",onChange:Pe},{default:i(()=>[(l(!0),d(J,null,Q(ce.value,o=>(l(),p(v,{key:o,value:o},{default:i(()=>[F(c(f[o]),1)]),_:2},1032,["value"]))),128))]),_:1},8,["disabled","value"])]),_:1},8,["label","rules"]),m(s,{label:a.$t("optimizerGroup"),name:["catalog","optimizerGroup"],rules:[{required:r.value}]},{default:i(()=>[r.value?(l(),p(n,{key:0,value:e.catalog.optimizerGroup,"onUpdate:value":t[4]||(t[4]=o=>e.catalog.optimizerGroup=o),options:re.value,placeholder:R.selectPh},null,8,["value","options","placeholder"])):(l(),d("span",sa,c(e.catalog.optimizerGroup),1))]),_:1},8,["label","rules"]),m(s,null,{default:i(()=>[U("p",na,c(a.$t("storageConfigName")),1)]),_:1}),m(s,{label:"Type",name:["storageConfig","storage.type"],rules:[{required:r.value}]},{default:i(()=>[r.value?(l(),p(n,{key:0,value:e.storageConfig["storage.type"],"onUpdate:value":t[5]||(t[5]=o=>e.storageConfig["storage.type"]=o),placeholder:R.selectPh,options:Se.value},null,8,["value","placeholder","options"])):(l(),d("span",ia,c(e.storageConfig["storage.type"]),1))]),_:1},8,["name","rules"]),e.storageConfig["storage.type"]==="S3"?(l(),p(s,{key:1,label:"Endpoint",name:["storageConfig","storage.s3.endpoint"],rules:[{required:!1}]},{default:i(()=>[r.value?(l(),p(u,{key:0,value:e.storageConfig["storage.s3.endpoint"],"onUpdate:value":t[6]||(t[6]=o=>e.storageConfig["storage.s3.endpoint"]=o)},null,8,["value"])):(l(),d("span",ra,c(e.storageConfig["storage.s3.endpoint"]),1))]),_:1},8,["name"])):b("",!0),e.storageConfig["storage.type"]==="S3"?(l(),p(s,{key:2,label:"Region",name:["storageConfig","storage.s3.region"],rules:[{required:!1}]},{default:i(()=>[r.value?(l(),p(u,{key:0,value:e.storageConfig["storage.s3.region"],"onUpdate:value":t[7]||(t[7]=o=>e.storageConfig["storage.s3.region"]=o)},null,8,["value"])):(l(),d("span",ua,c(e.storageConfig["storage.s3.region"]),1))]),_:1},8,["name"])):b("",!0),e.storageConfig["storage.type"]==="Hadoop"?(l(),d("div",ca,[(l(!0),d(J,null,Q(e.storageConfigArray,o=>(l(),p(s,{key:o.label,label:o.label,class:"g-flex-ac"},{default:i(()=>[r.value?(l(),p(L,{key:0,"file-list":o.fileList,"onUpdate:fileList":M=>o.fileList=M,name:"file",accept:".xml",showUploadList:!1,action:V.value,disabled:o.uploadLoading,onChange:M=>ge(M,o,"STORAGE")},{default:i(()=>[m(k,{type:"primary",ghost:"",loading:o.uploadLoading,class:"g-mr-12"},{default:i(()=>[F(c(a.$t("upload")),1)]),_:2},1032,["loading"])]),_:2},1032,["file-list","onUpdate:fileList","action","disabled","onChange"])):b("",!0),o.isSuccess||o.fileName?(l(),d("span",{key:1,class:se(["config-value",{"view-active":!!o.fileUrl}]),onClick:M=>fe(o.fileUrl)},c(o.fileName),11,pa)):b("",!0)]),_:2},1032,["label"]))),128))])):b("",!0),m(s,null,{default:i(()=>[U("p",da,c(a.$t("authenticationConfig")),1)]),_:1}),m(s,{label:"Type",name:["authConfig","auth.type"],rules:[{required:r.value}]},{default:i(()=>[r.value?(l(),p(n,{key:0,value:e.authConfig["auth.type"],"onUpdate:value":t[8]||(t[8]=o=>e.authConfig["auth.type"]=o),placeholder:R.selectPh,options:Re.value},null,8,["value","placeholder","options"])):(l(),d("span",ga,c(e.authConfig["auth.type"]),1))]),_:1},8,["name","rules"]),e.authConfig["auth.type"]==="SIMPLE"?(l(),p(s,{key:4,label:"Hadoop Username",name:["authConfig","auth.simple.hadoop_username"],rules:[{required:r.value}]},{default:i(()=>[r.value?(l(),p(u,{key:0,value:e.authConfig["auth.simple.hadoop_username"],"onUpdate:value":t[9]||(t[9]=o=>e.authConfig["auth.simple.hadoop_username"]=o)},null,8,["value"])):(l(),d("span",fa,c(e.authConfig["auth.simple.hadoop_username"]),1))]),_:1},8,["name","rules"])):b("",!0),e.authConfig["auth.type"]==="KERBEROS"?(l(),p(s,{key:5,label:"Kerberos Principal",name:["authConfig","auth.kerberos.principal"],rules:[{required:r.value}]},{default:i(()=>[r.value?(l(),p(u,{key:0,value:e.authConfig["auth.kerberos.principal"],"onUpdate:value":t[10]||(t[10]=o=>e.authConfig["auth.kerberos.principal"]=o)},null,8,["value"])):(l(),d("span",ma,c(e.authConfig["auth.kerberos.principal"]),1))]),_:1},8,["name","rules"])):b("",!0),e.authConfig["auth.type"]==="KERBEROS"?(l(),d("div",ha,[(l(!0),d(J,null,Q(e.authConfigArray,o=>(l(),p(s,{key:o.label,label:o.label,class:"g-flex-ac"},{default:i(()=>[r.value?(l(),p(L,{key:0,"file-list":o.fileList,"onUpdate:fileList":M=>o.fileList=M,name:"file",accept:o.key==="auth.kerberos.keytab"?".keytab":".conf",showUploadList:!1,action:V.value,disabled:o.uploadLoading,onChange:M=>ge(M,o)},{default:i(()=>[m(k,{type:"primary",ghost:"",loading:o.uploadLoading,class:"g-mr-12"},{default:i(()=>[F(c(a.$t("upload")),1)]),_:2},1032,["loading"])]),_:2},1032,["file-list","onUpdate:fileList","accept","action","disabled","onChange"])):b("",!0),o.isSuccess||o.fileName?(l(),d("span",{key:1,class:se(["config-value auth-filename",{"view-active":!!o.fileUrl}]),onClick:M=>fe(o.fileUrl),title:o.fileName},c(o.fileName),11,va)):b("",!0)]),_:2},1032,["label"]))),128))])):b("",!0),e.authConfig["auth.type"]==="AK/SK"?(l(),p(s,{key:7,label:"Access Key",name:["authConfig","auth.ak_sk.access_key"],rules:[{required:r.value}]},{default:i(()=>[r.value?(l(),p(u,{key:0,value:e.authConfig["auth.ak_sk.access_key"],"onUpdate:value":t[11]||(t[11]=o=>e.authConfig["auth.ak_sk.access_key"]=o)},null,8,["value"])):(l(),d("span",ya,c(e.authConfig["auth.ak_sk.access_key"]),1))]),_:1},8,["name","rules"])):b("",!0),e.authConfig["auth.type"]==="AK/SK"?(l(),p(s,{key:8,label:"Secret Key",name:["authConfig","auth.ak_sk.secret_key"],rules:[{required:r.value}]},{default:i(()=>[r.value?(l(),p(u,{key:0,value:e.authConfig["auth.ak_sk.secret_key"],"onUpdate:value":t[12]||(t[12]=o=>e.authConfig["auth.ak_sk.secret_key"]=o)},null,8,["value"])):(l(),d("span",Ca,c(e.authConfig["auth.ak_sk.secret_key"]),1))]),_:1},8,["name","rules"])):b("",!0),m(s,null,{default:i(()=>[U("p",_a,c(a.$t("properties")),1)]),_:1}),m(s,null,{default:i(()=>[m(me,{propertiesObj:e.properties,isEdit:r.value,ref_key:"propertiesRef",ref:K},null,8,["propertiesObj","isEdit"])]),_:1}),m(s,null,{default:i(()=>[U("p",ba,c(a.$t("tableProperties")),1)]),_:1}),m(s,null,{default:i(()=>[m(me,{propertiesObj:e.tableProperties,isEdit:r.value,ref_key:"tablePropertiesRef",ref:z},null,8,["propertiesObj","isEdit"])]),_:1})]),_:1},8,["model"])])]),r.value?(l(),d("div",ka,[m(k,{type:"primary",onClick:Ae,class:"save-btn g-mr-12"},{default:i(()=>[F(c(a.$t("save")),1)]),_:1}),m(k,{onClick:Oe},{default:i(()=>[F(c(a.$t("cancel")),1)]),_:1})])):b("",!0),r.value?b("",!0):(l(),d("div",Ea,[m(k,{type:"primary",onClick:Te,class:"edit-btn g-mr-12"},{default:i(()=>[F(c(a.$t("edit")),1)]),_:1}),m(k,{onClick:Ue,class:"remove-btn"},{default:i(()=>[F(c(a.$t("remove")),1)]),_:1})])),O.value?(l(),p(oe,{key:2})):b("",!0)])}}}),wa=_e(Ia,[["__scopeId","data-v-f24d6861"]]),Na={class:"catalogs-wrap g-flex"},$a={class:"catalog-list-left"},Ma={class:"catalog-header"},Sa={key:0,class:"catalog-list"},Ra=["onClick"],Pa={class:"catalog-detail"},Y="new catalog",Ta=he({__name:"index",setup(ne){const{t:q}=ve(),N=je(),ee=ye(),y=T([]),g=T({}),$=A(!1),R=A(!1),x=xe.PRESENTED_IMAGE_SIMPLE;async function r(){try{R.value=!0;const f=await Fe();y.length=0,(f||[]).forEach(C=>{y.push({catalogName:C.catalogName,catalogType:C.catalogType})})}finally{R.value=!1}}function V(){var P,w;const{catalogname:f="",type:C}=ee.query,_={};if(decodeURIComponent(f)===Y){K();return}f?(_.catalogName=f,_.catalogType=C):(_.catalogName=(P=y[0])==null?void 0:P.catalogName,_.catalogType=(w=y[0])==null?void 0:w.catalogType),G(_)}function I(f){$.value?h(()=>{G(f),$.value=!1,O(!1)}):G(f)}async function G(f){const{catalogName:C,catalogType:_}=f;g.catalogName=C||"",g.catalogType=_||"",await N.replace({path:"/catalogs",query:{catalogname:encodeURIComponent(g.catalogName),type:g.catalogType}})}async function O(f,C){var P,w,e,W;$.value=f,C&&(await D(),C!=null&&C.catalogName||(C.catalogName=(P=y[0])==null?void 0:P.catalogName,C.catalogType=(w=y[0])==null?void 0:w.catalogType));const _=y.findIndex(X=>X.catalogName===Y);if(_>-1){y.splice(_);const X={catalogName:(e=y[0])==null?void 0:e.catalogName,catalogType:(W=y[0])==null?void 0:W.catalogType};G(X);return}C&&G(C)}async function D(){await r()}function K(){$.value?h(()=>{z()}):z()}async function z(){const f={catalogName:Y,catalogType:""};await G(f),y.push(f),$.value=!0}Ce(async()=>{await r(),V()});function h(f){le.confirm({title:q("leavePageModalTitle"),content:q("leavePageModalContent"),okText:q("leave"),onOk:async()=>{f&&await f()}})}return Ve((f,C,_)=>{$.value?h(()=>{_()}):_()}),(f,C)=>{const _=S("a-button"),P=S("a-empty");return l(),d("div",Na,[U("div",$a,[U("div",Ma,c(`${f.$t("catalog")} ${f.$t("list")}`),1),y.length&&!R.value?(l(),d("ul",Sa,[(l(!0),d(J,null,Q(y,w=>(l(),d("li",{key:w.catalogName,class:se(["catalog-item g-text-nowrap",{active:w.catalogName===g.catalogName}]),onClick:e=>I(w)},c(w.catalogName),11,Ra))),128))])):b("",!0),m(_,{onClick:K,disabled:g.catalogName===Y,class:"add-btn"},{default:i(()=>[F("+")]),_:1},8,["disabled"])]),U("div",Pa,[!y.length&&!R.value?(l(),p(P,{key:0,image:We(x),class:"detail-empty"},null,8,["image"])):(l(),p(wa,{key:1,isEdit:$.value,onUpdateEdit:O,onUpdateCatalogs:D},null,8,["isEdit"]))])])}}}),Ba=_e(Ta,[["__scopeId","data-v-ff66562c"]]);export{Ba as default};
