webpackJsonp([1],{BTtN:function(t,a){},NHnr:function(t,a,e){"use strict";Object.defineProperty(a,"__esModule",{value:!0});var r=e("7+uW"),o={render:function(){var t=this.$createElement,a=this._self._c||t;return a("div",{attrs:{id:"app"}},[a("router-view")],1)},staticRenderFns:[]};var i=e("VU/8")({name:"App"},o,!1,function(t){e("gsu9")},null,null).exports,s=e("/ocq"),l=e("mtWM"),d=e.n(l);d.a.defaults.baseURL="plat-gateway";e("mw3O");var n={name:"home",data:function(){return{routerInfoList:[],isUpdate:!0,dialogVisible:!1,isView:!0,addFormData:{id:"",protocolType:"HTTP",hostUrl:"",path:"",isOn:"true",RPCInterface:"",RPCMethod:"",RPCParams:"",descr:"",example:""},submitData:{id:"",hostUrl:"",predicates:[{name:"Path",args:{_genkey_0:""}}],filters:[{name:"SofaAdapt",args:{_genkey_0:!0,_genkey_1:"",_genkey_2:"",_genkey_3:""}}]},addRule:{id:[{required:!0,message:"该字段不能为空",trigger:"blur"}],protocolType:[{required:!0,message:"该字段不能为空",trigger:"blur"}],RPCInterface:[{required:!0,message:"该字段不能为空",trigger:"blur"}],hostUrl:[{required:!0,message:"该字段不能为空",trigger:"blur"}],path:[{required:!0,message:"该字段不能为空",trigger:"blur"}],isOn:[{required:!0,message:"该字段不能为空",trigger:"blur"}],RPCMethod:[{required:!0,message:"该字段不能为空",trigger:"blur"}],RPCParams:[{required:!0,message:"该字段不能为空",trigger:"blur"}]},filters:{id:""}}},mounted:function(){this.loadData()},methods:{loadData:function(){var t=this;d.a.get("/routes").then(function(a){var e=a.data;t.routerInfoList=e})},getRouter:function(){this.loadData()},addRouter:function(){this.isView=!0,this.dialogVisible=!0,this.isUpdate=!1,this.setFormDataNull()},checkDetail:function(t){this.setFormData(t),this.isView=!1,this.dialogVisible=!0,this.isUpdate=!1},modifyRouter:function(t){this.setFormData(t),this.isView=!0,this.dialogVisible=!0,this.isUpdate=!0},deleteRouter:function(t){var a=this;this.$alert("是否删除这条记录","信息删除",{confirmButtonText:"确定",callback:function(e){d.a.delete("/routes/"+t.route_id).then(function(t){console.info(t),200==t.data.code?a.$message({type:"info",message:"已删除"}):a.$message({type:"info",message:"删除失败"}),a.loadData()})}})},setSubmitData:function(){this.submitData.id=this.addFormData.id,this.submitData.predicates[0].args._genkey_0=this.addFormData.path,this.submitData.filters[0].args._genkey_1=this.addFormData.protocolType,this.submitData.filters[0].args._genkey_2=this.addFormData.descr,this.submitData.filters[0].args._genkey_3=this.addFormData.example,"HTTP"===this.addFormData.protocolType?(this.submitData.hostUrl="http://"+this.addFormData.hostUrl,this.submitData.filters[0].name="HttpAdapt"):"SOFA"===this.addFormData.protocolType?(this.submitData.hostUrl="bolt://"+this.addFormData.hostUrl,this.submitData.filters[0].name="SofaAdapt",this.submitData.filters[0].args._genkey_4=this.addFormData.RPCInterface,this.submitData.filters[0].args._genkey_5=this.addFormData.RPCMethod,this.submitData.filters[0].args._genkey_6=this.addFormData.RPCParams):"FSP"===this.addFormData.protocolType&&(this.submitData.hostUrl="fsp://"+this.addFormData.hostUrl,this.submitData.filters[0].name="FspAdapt")},setFormData:function(t){this.addFormData.id=t.route_id,this.addFormData.hostUrl=t.route_definition.uri.slice(7),this.addFormData.path=t.route_definition.predicates[0].args._genkey_0,this.addFormData.isOn=t.route_definition.filters[0].args._genkey_0,this.addFormData.protocolType=t.route_definition.filters[0].args._genkey_1,this.addFormData.descr=t.route_definition.filters[0].args._genkey_2,this.addFormData.example=t.route_definition.filters[0].args._genkey_3,this.addFormData.RPCInterface=t.route_definition.filters[0].args._genkey_4,this.addFormData.RPCMethod=t.route_definition.filters[0].args._genkey_5,this.addFormData.RPCParams=t.route_definition.filters[0].args._genkey_6},setFormDataNull:function(){this.addFormData.id="",this.addFormData.protocolType="HTTP",this.addFormData.hostUrl="",this.addFormData.path="",this.addFormData.isOn="true",this.addFormData.RPCInterface="",this.addFormData.RPCMethod="",this.addFormData.RPCParams="",this.addFormData.descr="",this.addFormData.example=""},addSubmit:function(){var t=this;this.$refs.addFormData.validate(function(a){a&&(t.setSubmitData(),d.a.post("/routes",t.submitData,{headers:{"Content-Type":"application/json;charset=UTF-8"}}).then(function(a){200==a.data.code?(t.$message({type:"info",message:a.data.message}),t.loadData()):t.$message({type:"info",message:"添加失败"}),t.dialogVisible=!1}))})},updateSubmit:function(){var t=this;this.$refs.addFormData.validate(function(a){a&&(t.setSubmitData(),d.a.post("/routes/"+t.submitData.id,t.submitData,{headers:{"Content-Type":"application/json;charset=UTF-8"}}).then(function(a){200==a.data.code?(t.$message({type:"info",message:a.data.message}),t.loadData()):t.$message({type:"info",message:"添加失败"}),t.dialogVisible=!1}))})},protocolTypeChange:function(t){}}},m={render:function(){var t=this,a=t.$createElement,e=t._self._c||a;return e("div",[e("el-col",{staticClass:"toolbar",staticStyle:{"padding-bottom":"0px"},attrs:{span:24}},[e("el-form",{attrs:{inline:!0,model:t.filters}},[e("el-form-item",[e("el-input",{attrs:{placeholder:"唯一标识"},model:{value:t.filters.id,callback:function(a){t.$set(t.filters,"id",a)},expression:"filters.id"}})],1),t._v(" "),e("el-form-item",[e("el-button",{attrs:{type:"primary"},on:{click:t.getRouter}},[t._v("查询")])],1),t._v(" "),e("el-form-item",[e("el-button",{attrs:{type:"primary",plain:""},on:{click:t.addRouter}},[t._v("新增")])],1)],1)],1),t._v(" "),e("el-table",{staticStyle:{width:"100%"},attrs:{data:t.routerInfoList}},[e("el-table-column",{attrs:{"show-overflow-tooltip":!0,prop:"route_definition.id",label:"唯一标识",width:"400"}}),t._v(" "),e("el-table-column",{attrs:{prop:"route_definition.uri",label:"转发地址",width:"200"}}),t._v(" "),e("el-table-column",{attrs:{"show-overflow-tooltip":!0,prop:"route_definition.predicates[0].args._genkey_0",label:"拦截路径",width:"200"}}),t._v(" "),e("el-table-column",{attrs:{"show-overflow-tooltip":!0,prop:"route_definition.filters[0].args._genkey_2",label:"描述",width:"180"}}),t._v(" "),e("el-table-column",{attrs:{label:"操作",align:"center","min-width":"100"},scopedSlots:t._u([{key:"default",fn:function(a){return[e("el-button",{attrs:{type:"text"},on:{click:function(e){return t.checkDetail(a.row)}}},[t._v("查看详情")]),t._v(" "),e("el-button",{attrs:{type:"primary",plain:""},on:{click:function(e){return t.modifyRouter(a.row)}}},[t._v("修改")]),t._v(" "),e("el-button",{attrs:{type:"primary",plain:""},on:{click:function(e){return t.deleteRouter(a.row)}}},[t._v("删除")])]}}])})],1),t._v(" "),e("el-dialog",{attrs:{title:"新增/修改",visible:t.dialogVisible,width:"70%","close-on-click-modal":!1},on:{"update:visible":function(a){t.dialogVisible=a}}},[e("el-form",{ref:"addFormData",staticClass:"demo-ruleForm login-container",attrs:{model:t.addFormData,rules:t.addRule,"label-width":"100px"}},[t.isUpdate?t._e():e("el-form-item",{attrs:{prop:"id",label:"唯一标识"}},[e("el-input",{attrs:{type:"text",placeholder:"如：wish.plat-base.HelloService.sayHello，分别表示组别、工程名、类名、方法名"},model:{value:t.addFormData.id,callback:function(a){t.$set(t.addFormData,"id",a)},expression:"addFormData.id"}})],1),t._v(" "),e("div",{staticClass:"element-switch-patch"},[e("el-form-item",{attrs:{prop:"protocolType",label:"协议类型"}},[e("el-radio-group",{on:{change:t.protocolTypeChange},model:{value:t.addFormData.protocolType,callback:function(a){t.$set(t.addFormData,"protocolType",a)},expression:"addFormData.protocolType"}},[e("el-radio",{attrs:{label:"HTTP"}},[t._v("HTTP")]),t._v(" "),e("el-radio",{attrs:{label:"SOFA"}},[t._v("SOFA")]),t._v(" "),e("el-radio",{attrs:{label:"FSP"}},[t._v("FSP")]),t._v(" "),e("el-radio",{attrs:{label:"OTHER",disabled:""}},[t._v("OTHER")])],1)],1)],1),t._v(" "),e("el-form-item",{attrs:{prop:"hostUrl",label:"转发地址"}},[e("el-input",{attrs:{type:"text",placeholder:"如：172.29.12.100:12201，表示重定向的服务器位置、请注意端口"},model:{value:t.addFormData.hostUrl,callback:function(a){t.$set(t.addFormData,"hostUrl",a)},expression:"addFormData.hostUrl"}},["SOFA"===t.addFormData.protocolType?e("template",{slot:"prepend"},[t._v("bolt://")]):t._e(),t._v(" "),"HTTP"===t.addFormData.protocolType?e("template",{slot:"prepend"},[t._v("http://")]):t._e(),t._v(" "),"FSP"===t.addFormData.protocolType?e("template",{slot:"prepend"},[t._v("fsp://")]):t._e()],2)],1),t._v(" "),e("el-form-item",{attrs:{prop:"path",label:"拦截路径"}},[e("el-input",{attrs:{type:"text",placeholder:"如：/base/api/oper/getOperListByOrg/**，与接口@Path对应、支持POST"},model:{value:t.addFormData.path,callback:function(a){t.$set(t.addFormData,"path",a)},expression:"addFormData.path"}})],1),t._v(" "),e("div",{staticClass:"element-switch-patch"},[e("el-form-item",{attrs:{prop:"isOn",label:"是否启用"}},[e("el-switch",{attrs:{"active-value":"true","inactive-value":"false"},model:{value:t.addFormData.isOn,callback:function(a){t.$set(t.addFormData,"isOn",a)},expression:"addFormData.isOn"}})],1)],1),t._v(" "),"SOFA"===t.addFormData.protocolType?e("el-form-item",{attrs:{prop:"RPCInterface",label:"类名"}},[e("el-input",{attrs:{type:"text",placeholder:"如：com.wish.plat.base.api.OperService，表示RPC对应接口"},model:{value:t.addFormData.RPCInterface,callback:function(a){t.$set(t.addFormData,"RPCInterface",a)},expression:"addFormData.RPCInterface"}})],1):t._e(),t._v(" "),"SOFA"===t.addFormData.protocolType?e("el-form-item",{attrs:{prop:"RPCMethod",label:"方法名"}},[e("el-input",{attrs:{type:"text",placeholder:"如：getOperListByOrg，表示接口对应的方法"},model:{value:t.addFormData.RPCMethod,callback:function(a){t.$set(t.addFormData,"RPCMethod",a)},expression:"addFormData.RPCMethod"}})],1):t._e(),t._v(" "),"SOFA"===t.addFormData.protocolType?e("el-form-item",{attrs:{prop:"RPCParams",label:"参数"}},[e("el-input",{attrs:{type:"text",placeholder:"如：{'city_id': 'java.lang.String'}，表示参数名称和类型、可为空。尤其注意'java.lang.String'的格式、使用标准JSON"},model:{value:t.addFormData.RPCParams,callback:function(a){t.$set(t.addFormData,"RPCParams",a)},expression:"addFormData.RPCParams"}})],1):t._e(),t._v(" "),e("el-form-item",{attrs:{prop:"descr",label:"描述"}},[e("el-input",{attrs:{type:"text",placeholder:"请添加描述"},model:{value:t.addFormData.descr,callback:function(a){t.$set(t.addFormData,"descr",a)},expression:"addFormData.descr"}})],1),t._v(" "),e("el-form-item",{attrs:{prop:"example",label:"调用样例"}},[e("el-input",{attrs:{type:"textarea",rows:4,placeholder:"请添加调用样例"},model:{value:t.addFormData.example,callback:function(a){t.$set(t.addFormData,"example",a)},expression:"addFormData.example"}})],1)],1),t._v(" "),e("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[e("el-button",{nativeOn:{click:function(a){t.dialogVisible=!1,t.addFormData={}}}},[t._v("取 消")]),t._v(" "),t.isView&t.isUpdate?e("el-button",{attrs:{type:"primary"},nativeOn:{click:function(a){return t.updateSubmit(a)}}},[t._v("保 存")]):t._e(),t._v(" "),t.isView&!t.isUpdate?e("el-button",{attrs:{type:"primary"},nativeOn:{click:function(a){return t.addSubmit(a)}}},[t._v("确 定")]):t._e()],1)],1)],1)},staticRenderFns:[]};var p=e("VU/8")(n,m,!1,function(t){e("BTtN")},null,null).exports;r.default.use(s.a);var u=new s.a({routes:[{path:"/",name:"home",component:p}]}),c=e("zL8q"),h=e.n(c);e("tvR6");r.default.config.productionTip=!1,r.default.use(h.a),new r.default({el:"#app",router:u,components:{App:i},template:"<App/>"})},gsu9:function(t,a){},tvR6:function(t,a){}},["NHnr"]);
//# sourceMappingURL=app.672d48a849e92c27b3bf.js.map