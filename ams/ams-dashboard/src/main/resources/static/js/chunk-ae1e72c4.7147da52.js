(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-ae1e72c4"],{"096a":function(e,t,r){},5899:function(e,t){e.exports="\t\n\v\f\r                　\u2028\u2029\ufeff"},"58a8":function(e,t,r){var n=r("e330"),a=r("1d80"),c=r("577e"),o=r("5899"),i=n("".replace),u="["+o+"]",l=RegExp("^"+u+u+"*"),s=RegExp(u+u+"*$"),p=function(e){return function(t){var r=c(a(t));return 1&e&&(r=i(r,l,"")),2&e&&(r=i(r,s,"")),r}};e.exports={start:p(1),end:p(2),trim:p(3)}},"5ab7":function(e,t,r){},8552:function(e,t,r){"use strict";r.d(t,"a",(function(){return c}));var n=r("7a23"),a=r("47e2");function c(){var e=Object(a["b"])(),t=e.t,r=Object(n["computed"])((function(){return t("catalog")})).value,c=Object(n["computed"])((function(){return t("databaseName")})).value,o=Object(n["computed"])((function(){return t("tableName")})).value,i=Object(n["computed"])((function(){return t("optimzerGroup")})).value,u=Object(n["computed"])((function(){return t("resourceGroup")})).value,l=Object(n["computed"])((function(){return t("parallelism")})).value,s=Object(n["computed"])((function(){return t("username")})).value,p=Object(n["computed"])((function(){return t("password")})).value,b=Object(n["computed"])((function(){return t("database",2)})).value,d=Object(n["computed"])((function(){return t("table",2)})).value;return{selectPh:t("selectPlaceholder"),inputPh:t("inputPlaceholder"),selectClPh:t("selectPlaceholder",{selectPh:r}),selectDBPh:t("selectPlaceholder",{selectPh:c}),inputDBPh:t("inputPlaceholder",{inputPh:c}),inputClPh:t("inputPlaceholder",{inputPh:r}),inputTNPh:t("inputPlaceholder",{inputPh:o}),selectOptGroupPh:t("inputPlaceholder",{inputPh:i}),resourceGroupPh:t("inputPlaceholder",{inputPh:u}),parallelismPh:t("inputPlaceholder",{inputPh:l}),usernamePh:t("inputPlaceholder",{inputPh:s}),passwordPh:t("inputPlaceholder",{inputPh:p}),filterDBPh:t("filterPlaceholder",{inputPh:b}),filterTablePh:t("filterPlaceholder",{inputPh:d})}}},a8eb:function(e,t,r){"use strict";r("5ab7")},a9e3:function(e,t,r){"use strict";var n=r("83ab"),a=r("da84"),c=r("e330"),o=r("94ca"),i=r("6eeb"),u=r("1a2d"),l=r("7156"),s=r("3a9b"),p=r("d9b5"),b=r("c04e"),d=r("d039"),m=r("241c").f,f=r("06cf").f,O=r("9bf2").f,j=r("408a"),h=r("58a8").trim,v="Number",g=a[v],N=g.prototype,z=a.TypeError,w=c("".slice),y=c("".charCodeAt),x=function(e){var t=b(e,"number");return"bigint"==typeof t?t:C(t)},C=function(e){var t,r,n,a,c,o,i,u,l=b(e,"number");if(p(l))throw z("Cannot convert a Symbol value to a number");if("string"==typeof l&&l.length>2)if(l=h(l),t=y(l,0),43===t||45===t){if(r=y(l,2),88===r||120===r)return NaN}else if(48===t){switch(y(l,1)){case 66:case 98:n=2,a=49;break;case 79:case 111:n=8,a=55;break;default:return+l}for(c=w(l,2),o=c.length,i=0;i<o;i++)if(u=y(c,i),u<48||u>a)return NaN;return parseInt(c,n)}return+l};if(o(v,!g(" 0o1")||!g("0b1")||g("+0x1"))){for(var G,k=function(e){var t=arguments.length<1?0:g(x(e)),r=this;return s(N,r)&&d((function(){j(r)}))?l(Object(t),r,k):t},S=n?m(g):"MAX_VALUE,MIN_VALUE,NaN,NEGATIVE_INFINITY,POSITIVE_INFINITY,EPSILON,MAX_SAFE_INTEGER,MIN_SAFE_INTEGER,isFinite,isInteger,isNaN,isSafeInteger,parseFloat,parseInt,fromString,range".split(","),P=0;S.length>P;P++)u(g,G=S[P])&&!u(k,G)&&O(k,G,f(g,G));k.prototype=N,N.constructor=k,i(a,v,k)}},b356:function(e,t,r){"use strict";function n(){var e=0,t=1,r=["25","50","100"],n=25;return{total:e,current:t,pageSize:n,pageSizeOptions:r,showQuickJumper:!0,showSizeChanger:!0,hideOnSinglePage:!1}}r.d(t,"a",(function(){return n}))},cac2:function(e,t,r){"use strict";r.r(t);var n=r("7a23"),a={class:"border-wrap"},c={class:"optimize-wrap"},o={class:"optimize-group g-flex-ac"},i={class:"left-group"},u={class:"g-mr-16"},l={class:"btn-wrap"},s={class:"g-ml-16 f-shink-0"},p={class:"text-color"},b={class:"text-color"},d={class:"content"};function m(e,t,r,m,f,O){var j=Object(n["resolveComponent"])("a-select"),h=Object(n["resolveComponent"])("a-button"),v=Object(n["resolveComponent"])("List"),g=Object(n["resolveComponent"])("a-tab-pane"),N=Object(n["resolveComponent"])("a-tabs"),z=Object(n["resolveComponent"])("scale-out-modal");return Object(n["openBlock"])(),Object(n["createElementBlock"])("div",a,[Object(n["createElementVNode"])("div",c,[Object(n["createElementVNode"])("div",o,[Object(n["createElementVNode"])("div",i,[Object(n["createElementVNode"])("span",u,Object(n["toDisplayString"])(e.$t("optimzerGroup")),1),Object(n["createVNode"])(j,{value:e.curGroupName,"onUpdate:value":t[0]||(t[0]=function(t){return e.curGroupName=t}),showSearch:!0,options:e.groupList,placeholder:e.placeholder.selectOptGroupPh,onChange:e.onChangeGroup,style:{width:"240px"}},null,8,["value","options","placeholder","onChange"])]),Object(n["createElementVNode"])("div",l,[Object(n["createElementVNode"])("span",s,[Object(n["createTextVNode"])(Object(n["toDisplayString"])(e.$t("resourceOccupation"))+" ",1),Object(n["createElementVNode"])("span",p,Object(n["toDisplayString"])(e.groupInfo.occupationCore),1),Object(n["createTextVNode"])(" "+Object(n["toDisplayString"])(e.$t("core"))+" ",1),Object(n["createElementVNode"])("span",b,Object(n["toDisplayString"])(e.groupInfo.occupationMemory),1),Object(n["createTextVNode"])(" "+Object(n["toDisplayString"])(e.groupInfo.unit),1)]),Object(n["createVNode"])(h,{type:"primary",onClick:e.expansionJob,class:"g-ml-8"},{default:Object(n["withCtx"])((function(){return[Object(n["createTextVNode"])(Object(n["toDisplayString"])(e.$t("scaleOut")),1)]})),_:1},8,["onClick"])])]),Object(n["createElementVNode"])("div",d,[Object(n["createVNode"])(N,{activeKey:e.activeTab,"onUpdate:activeKey":t[1]||(t[1]=function(t){return e.activeTab=t}),destroyInactiveTabPane:""},{default:Object(n["withCtx"])((function(){return[(Object(n["openBlock"])(!0),Object(n["createElementBlock"])(n["Fragment"],null,Object(n["renderList"])(e.tabConfig,(function(t){return Object(n["openBlock"])(),Object(n["createBlock"])(g,{key:t.value,tab:t.label,class:Object(n["normalizeClass"])([e.activeTab===t.value?"active":""])},{default:Object(n["withCtx"])((function(){return[Object(n["createVNode"])(v,{curGroupName:e.curGroupName,type:t.value,needFresh:e.needFresh},null,8,["curGroupName","type","needFresh"])]})),_:2},1032,["tab","class"])})),128))]})),_:1},8,["activeKey"])])]),e.showScaleOutModal?(Object(n["openBlock"])(),Object(n["createBlock"])(z,{key:0,visible:e.showScaleOutModal,resourceGroup:"all"===e.curGroupName?"":e.curGroupName,onCancel:t[2]||(t[2]=function(t){return e.showScaleOutModal=!1}),onRefreshOptimizersTab:e.refreshOptimizersTab},null,8,["visible","resourceGroup","onRefreshOptimizersTab"])):Object(n["createCommentVNode"])("",!0)])}var f=r("5530"),O=r("1da1"),j=(r("96cf"),r("d3b7"),r("159b"),r("ac1f"),r("1276"),r("47e2")),h=r("8552"),v=r("b356"),g=r("e723"),N=(r("3b18"),r("f64c")),z=(r("a9e3"),Object(n["defineComponent"])({props:{visible:{type:Boolean},resourceGroup:null},emits:["cancel","refreshOptimizersTab"],setup:function(e,t){var r=t.emit,a=e,c=Object(n["reactive"])(Object(h["a"])()),o=Object(n["ref"])(),i=Object(n["reactive"])({resourceGroup:a.resourceGroup||void 0,parallelism:1}),u=Object(n["reactive"])([]);function l(){return s.apply(this,arguments)}function s(){return s=Object(O["a"])(regeneratorRuntime.mark((function e(){var t;return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:return e.next=2,Object(g["a"])();case 2:t=e.sent,u.length=0,(t||[]).forEach((function(e){u.push(Object(f["a"])(Object(f["a"])({},e),{},{label:e.optimizerGroupName,value:e.optimizerGroupName}))}));case 5:case"end":return e.stop()}}),e)}))),s.apply(this,arguments)}function p(){o.value.validateFields().then(Object(O["a"])(regeneratorRuntime.mark((function e(){return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:return e.next=2,Object(g["f"])({optimizerGroup:i.resourceGroup||"",parallelism:Number(i.parallelism)});case 2:o.value.resetFields(),r("cancel"),r("refreshOptimizersTab");case 5:case"end":return e.stop()}}),e)})))).catch((function(e){N["a"].error(e.message)}))}function b(){o.value.resetFields(),r("cancel")}return Object(n["onMounted"])((function(){l()})),function(e,t){var r=Object(n["resolveComponent"])("a-select"),l=Object(n["resolveComponent"])("a-form-item"),s=Object(n["resolveComponent"])("a-input"),d=Object(n["resolveComponent"])("a-form"),m=Object(n["resolveComponent"])("a-modal");return Object(n["openBlock"])(),Object(n["createBlock"])(m,{visible:a.visible,title:e.$t("scaleOut"),onOk:p,onCancel:b},{default:Object(n["withCtx"])((function(){return[Object(n["createVNode"])(d,{ref_key:"formRef",ref:o,model:Object(n["unref"])(i),class:"label-120"},{default:Object(n["withCtx"])((function(){return[Object(n["createVNode"])(l,{name:"resourceGroup",label:e.$t("resourceGroup"),rules:[{required:!0,message:"".concat(Object(n["unref"])(c).resourceGroupPh)}]},{default:Object(n["withCtx"])((function(){return[Object(n["createVNode"])(r,{value:Object(n["unref"])(i).resourceGroup,"onUpdate:value":t[0]||(t[0]=function(e){return Object(n["unref"])(i).resourceGroup=e}),showSearch:!0,options:Object(n["unref"])(u),placeholder:Object(n["unref"])(c).resourceGroupPh},null,8,["value","options","placeholder"])]})),_:1},8,["label","rules"]),Object(n["createVNode"])(l,{name:"parallelism",label:e.$t("parallelism"),rules:[{required:!0,message:"".concat(Object(n["unref"])(c).parallelismPh)}]},{default:Object(n["withCtx"])((function(){return[Object(n["createVNode"])(s,{value:Object(n["unref"])(i).parallelism,"onUpdate:value":t[1]||(t[1]=function(e){return Object(n["unref"])(i).parallelism=e}),type:"number",placeholder:Object(n["unref"])(c).parallelismPh},null,8,["value","placeholder"])]})),_:1},8,["label","rules"])]})),_:1},8,["model"])]})),_:1},8,["visible","title"])}}}));const w=z;var y=w,x=(r("cd17"),r("ed3b")),C=(r("99af"),r("b680"),r("d257")),G=r("6c02"),k={class:"list-wrap"},S=["onClick"],P=["title"],I=["onClick"],E=Object(n["defineComponent"])({props:{curGroupName:null,type:null,needFresh:{type:Boolean}},setup:function(e){var t=e,r=Object(j["b"])(),a=r.t,c=Object(G["e"])(),o=Object(n["ref"])(!1),i=Object(n["shallowReactive"])([{dataIndex:"tableName",title:a("table"),ellipsis:!0,scopedSlots:{customRender:"tableName"}},{dataIndex:"optimizeStatus",title:a("status"),width:"10%",ellipsis:!0},{dataIndex:"durationDisplay",title:a("duration"),width:"10%",ellipsis:!0},{dataIndex:"fileCount",title:a("fileCount"),width:"10%",ellipsis:!0},{dataIndex:"fileSizeDesc",title:a("fileSize"),width:"10%",ellipsis:!0},{dataIndex:"quota",title:a("quota"),width:"10%",ellipsis:!0},{dataIndex:"quotaOccupationDesc",title:a("quotaOccupation"),width:160,ellipsis:!0}]),u=Object(n["shallowReactive"])([{dataIndex:"index",title:a("order"),width:80,ellipsis:!0},{dataIndex:"groupName",title:a("optimizerGroup"),ellipsis:!0},{dataIndex:"container",title:a("container"),ellipsis:!0},{dataIndex:"jobStatus",title:a("status"),ellipsis:!0},{dataIndex:"resourceAllocation",title:a("resourceAllocation"),width:"20%",ellipsis:!0},{dataIndex:"operation",title:a("operation"),key:"operation",ellipsis:!0,width:160,scopedSlots:{customRender:"operation"}}]),l=Object(n["reactive"])(Object(v["a"])()),s=Object(n["reactive"])([]),p=Object(n["reactive"])([]),b=Object(n["computed"])((function(){return"optimizers"===t.type?u:i})),d=Object(n["computed"])((function(){return"optimizers"===t.type?s:p}));function m(e){e&&(l.current=1),"optimizers"===t.type?f():N()}function f(){return h.apply(this,arguments)}function h(){return h=Object(O["a"])(regeneratorRuntime.mark((function e(){var r,n,c,i;return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:return e.prev=0,s.length=0,o.value=!0,r={optimizerGroup:t.curGroupName,page:l.current,pageSize:l.pageSize},e.next=6,Object(g["b"])(r);case 6:n=e.sent,c=n.list,i=n.total,l.total=i,(c||[]).forEach((function(e,t){e.resourceAllocation="".concat(e.coreNumber," ").concat(a("core")," ").concat(Object(C["h"])(e.memory)),e.index=(l.current-1)*l.pageSize+t+1,s.push(e)})),e.next=14;break;case 12:e.prev=12,e.t0=e["catch"](0);case 14:return e.prev=14,o.value=!1,e.finish(14);case 17:case"end":return e.stop()}}),e,null,[[0,12,14,17]])}))),h.apply(this,arguments)}function N(){return z.apply(this,arguments)}function z(){return z=Object(O["a"])(regeneratorRuntime.mark((function e(){var r,n,a,c;return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:return e.prev=0,p.length=0,o.value=!0,r={optimizerGroup:t.curGroupName||"",page:l.current,pageSize:l.pageSize},e.next=6,Object(g["c"])(r);case 6:n=e.sent,a=n.list,c=n.total,l.total=c,(a||[]).forEach((function(e){e.quotaOccupationDesc=e.quotaOccupation-5e-4>0?"".concat((100*e.quotaOccupation).toFixed(1),"%"):"0",e.durationDesc=Object(C["e"])(e.duration||0),e.durationDisplay=Object(C["d"])(e.duration||0),e.fileSizeDesc=Object(C["a"])(e.fileSize),p.push(e)})),e.next=14;break;case 12:e.prev=12,e.t0=e["catch"](0);case 14:return e.prev=14,o.value=!1,e.finish(14);case 17:case"end":return e.stop()}}),e,null,[[0,12,14,17]])}))),z.apply(this,arguments)}function w(e){"external"!==e.containerType&&x["a"].confirm({title:a("releaseOptModalTitle"),content:"",okText:"",cancelText:"",onOk:function(){y(e)}})}function y(e){return E.apply(this,arguments)}function E(){return E=Object(O["a"])(regeneratorRuntime.mark((function e(t){return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:return e.next=2,Object(g["e"])({optimizerGroup:t.groupName,jobId:t.jobId});case 2:m(!0);case 3:case"end":return e.stop()}}),e)}))),E.apply(this,arguments)}function T(e){var t=e.current,r=void 0===t?l.current:t,n=e.pageSize,a=void 0===n?l.pageSize:n;l.current=r;var c=a!==l.pageSize;l.pageSize=a,m(c)}function V(e){var t=e.tableIdentifier,r=t.catalog,n=t.database,a=t.tableName;c.push({path:"/tables",query:{catalog:r,db:n,table:a}})}return Object(n["watch"])((function(){return t.curGroupName}),(function(e){e&&m()})),Object(n["watch"])((function(){return t.needFresh}),(function(e){e&&m(!0)})),Object(n["onMounted"])((function(){m()})),function(e,t){var r=Object(n["resolveComponent"])("a-table");return Object(n["openBlock"])(),Object(n["createElementBlock"])("div",k,[Object(n["createVNode"])(r,{class:"ant-table-common",columns:Object(n["unref"])(b),"data-source":Object(n["unref"])(d),pagination:Object(n["unref"])(l),loading:o.value,onChange:T},{bodyCell:Object(n["withCtx"])((function(e){var t=e.column,r=e.record;return["tableName"===t.dataIndex?(Object(n["openBlock"])(),Object(n["createElementBlock"])("span",{key:0,class:"primary-link",onClick:function(e){return V(r)}},Object(n["toDisplayString"])(r.tableName),9,S)):Object(n["createCommentVNode"])("",!0),"durationDisplay"===t.dataIndex?(Object(n["openBlock"])(),Object(n["createElementBlock"])("span",{key:1,title:r.durationDesc},Object(n["toDisplayString"])(r.durationDisplay),9,P)):Object(n["createCommentVNode"])("",!0),"operation"===t.dataIndex?(Object(n["openBlock"])(),Object(n["createElementBlock"])("span",{key:2,class:Object(n["normalizeClass"])(["primary-link",{disabled:"external"===r.containerType}]),onClick:function(e){return w(r)}},Object(n["toDisplayString"])(Object(n["unref"])(a)("release")),11,I)):Object(n["createCommentVNode"])("",!0)]})),_:1},8,["columns","data-source","pagination","loading"])])}}}),T=(r("a8eb"),r("6b0d")),V=r.n(T);const R=V()(E,[["__scopeId","data-v-4b788e4c"]]);var B=R,D=Object(n["defineComponent"])({name:"Optimize",components:{List:B,ScaleOutModal:y},setup:function(){var e=Object(j["b"])(),t=e.t,r=Object(n["shallowReactive"])([{label:t("optimizers"),value:"optimizers"},{label:t("tables"),value:"tables"}]),a=Object(n["reactive"])(Object(h["a"])()),c=Object(n["reactive"])(Object(v["a"])()),o=Object(n["reactive"])({curGroupName:"all",groupList:[{label:t("allGroups"),value:"all"}],groupInfo:{occupationCore:0,occupationMemory:0,unit:""},activeTab:"tables",showScaleOutModal:!1,needFresh:!1}),i=Object(n["computed"])((function(){return"tables"===o.activeTab})),u=function(){s()},l=function(){var e=Object(O["a"])(regeneratorRuntime.mark((function e(){var t;return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:return e.next=2,Object(g["a"])();case 2:t=e.sent,(t||[]).forEach((function(e){o.groupList.push({label:e.optimizerGroupName,value:e.optimizerGroupName})}));case 4:case"end":return e.stop()}}),e)})));return function(){return e.apply(this,arguments)}}(),s=function(){var e=Object(O["a"])(regeneratorRuntime.mark((function e(){var t,r,n;return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:return e.next=2,Object(g["d"])(o.curGroupName||"");case 2:t=e.sent,r=Object(C["h"])(t.occupationMemory||0),n=r.split(" "),o.groupInfo={occupationCore:t.occupationCore,occupationMemory:n[0],unit:n[1]||""};case 6:case"end":return e.stop()}}),e)})));return function(){return e.apply(this,arguments)}}(),p=function(){o.showScaleOutModal=!0},b=function(){o.activeTab="optimizers",o.needFresh=!0};return Object(n["onMounted"])((function(){l(),s()})),Object(f["a"])(Object(f["a"])({isTableTab:i,placeholder:a,pagination:c},Object(n["toRefs"])(o)),{},{tabConfig:r,onChangeGroup:u,expansionJob:p,refreshOptimizersTab:b})}});r("e076");const _=V()(D,[["render",m],["__scopeId","data-v-6ef3387e"]]);t["default"]=_},e076:function(e,t,r){"use strict";r("096a")},e723:function(e,t,r){"use strict";r.d(t,"a",(function(){return a})),r.d(t,"c",(function(){return c})),r.d(t,"b",(function(){return o})),r.d(t,"d",(function(){return i})),r.d(t,"f",(function(){return u})),r.d(t,"e",(function(){return l}));r("99af");var n=r("b32d");function a(){return n["a"].get("ams/v1/optimize/optimizerGroups")}function c(e){var t=e.optimizerGroup,r=e.page,a=e.pageSize;return n["a"].get("ams/v1/optimize/optimizerGroups/".concat(t,"/tables"),{params:{page:r,pageSize:a}})}function o(e){var t=e.optimizerGroup,r=e.page,a=e.pageSize;return n["a"].get("ams/v1/optimize/optimizerGroups/".concat(t,"/optimizers"),{params:{page:r,pageSize:a}})}function i(e){return n["a"].get("ams/v1/optimize/optimizerGroups/".concat(e,"/info"))}function u(e){var t=e.optimizerGroup,r=e.parallelism;return n["a"].post("ams/v1/optimize/optimizerGroups/".concat(t,"/optimizers"),{parallelism:r})}function l(e){var t=e.optimizerGroup,r=e.jobId;return n["a"].delete("ams/v1/optimize/optimizerGroups/".concat(t,"/optimizers/").concat(r))}}}]);