$(function () {
    $("#jqGrid").jqGrid({
        url: 'sys/generator/list',
        datatype: "json",
        colModel: [			
			{ label: '表名', name: 'tableName', index:'tableName', width: 100, key: true },
			{ label: 'Engine', name: 'engine', width: 70},
			{ label: '表备注', name: 'tableComment', width: 100 },
			{ label: '创建时间', name: 'createTime', width: 100 },
			{
				label: '详情', name: '', index: 'operate', width: 50, align: 'center',
				formatter: function (cellvalue, options, rowObject) {
                var detail="<a class='btn btn-default' onclick='vm.alertModel(\"" + rowObject.tableName +"\")'>查看字段详情</a>";
                return detail;
				}
			}
        ],
		viewrecords: true,
        height: 385,
        rowNum: 10,
		rowList : [10,30,50,100,200],
        rownumbers: true, 
        rownumWidth: 25, 
        autowidth:true,
        multiselect: true,
        pager: "#jqGridPager",
        jsonReader : {
            root: "page.list",
            page: "page.currPage",
            total: "page.totalPage",
            records: "page.totalCount"
        },
        prmNames : {
            page:"page", 
            rows:"limit", 
            order: "order"
        },
        gridComplete:function(){
        	//隐藏grid底部滚动条
        	$("#jqGrid").closest(".ui-jqgrid-bdiv").css({ "overflow-x" : "hidden" }); 
        }
    });
});

var vm = new Vue({
	el:'#rrapp',
	data:{
		q:{
			tableName: null
		},
		data:[],
		clomnusLists:[]
	},
	methods: {
		query: function () {
			$("#jqGrid").jqGrid('setGridParam',{ 
                postData:{'tableName': vm.q.tableName},
                page:1 
            }).trigger("reloadGrid");
		},
		generator: function() {
			var tableNames = getSelectedRows();
			if(tableNames == null){
				return ;
			}
			location.href = "sys/generator/code?tables=" + JSON.stringify(tableNames);
		},
		//获取表字段详情数据
		alertModel:function(a){
			var _this=this;
	      $.ajax({
		      //几个参数需要注意一下
		        type: "POST",//方法类型
		        dataType: "json",//服务端接收的数据类型
		        url: "sys/generator/queryColumns?tableName=" + a,//url
				async: false,
		        data: null,	
		        success: function (result) {
		          console.log(result);//打印服务端返回的数据(调试用)
		          if (result.code == 0) {
					_this.clomnusLists=[];
					_this.clomnusLists=result.list;
					//表单渲染
					if(_this.clomnusLists){	
						_this.loadModel();
					}
		          }
		          ;
		        },
		        error : function() {
		          layui.open("异常!");
		        }
		      });
		},
		//打开查看详情的弹出框
		loadModel:function(){
			var _this=this;
			 layer.open({
				 type: 1,
				 title: false,
				 moveType: 0,
                 area:['100%','100%'],
                 offset: '60px',
                 content: $('#showDis')
             })
		}
	}
});

