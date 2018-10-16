$(function () {
    $("#jqGrid").jqGrid({
        url:'/adventure/menu/queryList',
        datatype: "json",
        colModel: [			
			{ label: 'id', name: 'id', index: 'id', width: 50, key: true },
			{ label: '应用编码', name: 'appCode', index: 'app_code', width: 80 }, 			
			{ label: '菜单层级', name: 'level', index: 'level', width: 80 }, 			
			{ label: '父id', name: 'parentId', index: 'parent_id', width: 80 }, 			
			{ label: '菜单名称', name: 'name', index: 'name', width: 80 }, 			
			{ label: '菜单Url', name: 'url', index: 'url', width: 80 }, 			
			{ label: '', name: 'httpUrl', index: 'http_url', width: 80 }, 			
			{ label: '展示名称', name: 'showName', index: 'show_name', width: 80 }, 			
			{ label: '展示顺序', name: 'showOrder', index: 'show_order', width: 80 }, 			
			{ label: '创建人', name: 'createBy', index: 'create_by', width: 80 }, 			
			{ label: '创建时间', name: 'createTime', index: 'create_time', width: 80 }, 			
			{ label: '更新人', name: 'updateBy', index: 'update_by', width: 80 }, 			
			{ label: '更新时间', name: 'updateTime', index: 'update_time', width: 80 }			
        ],
		viewrecords: true,
        height: 385,
        rowNum: 10,
		rowList : [10,30,50],
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
	el:'#adventure',
	data:{
		showList: true,
		title: null,
		menu: {},
		btn:true
	},
	methods: {
		query: function () {
			vm.reload();
		},
		add: function(){
//			vm.showList = false;
//			vm.title = "新增";
			$('.modal-title').html("新增");
			vm.menu = {};
//			$('#myModal').modal('show');
			$("#myModal").modal("toggle");
			
			
		},
		update: function (event) {
			var id = getSelectedRow();
			if(id == null){
				return ;
			}
			vm.btn = false;
//			vm.showList = false;
//            vm.title = "修改";
			$('.modal-title').html("修改");
			$("#myModal").modal("toggle");
            vm.getInfo(id)
		},
		saveOrUpdate: function (event) {
			var url = vm.menu.id == null ? "/adventure/menu/save" : "/adventure/menu/update";
			$.ajax({
				type: "POST",
			    url:url,
                contentType: "application/json",
			    data: JSON.stringify(vm.menu),
			    success: function(r){
			    	vm.menu = {};
			    	if(r.code === 0){
						alert('操作成功', function(index){
							vm.reload();
							$('#myModal').modal('hide');
						});
					}else{
						$.xfm.alert(r.msg);
					}
				}
			});
		},
		del: function (event) {
			var ids = getSelectedRows();
			if(ids == null){
				return ;
			}
			
			confirm('确定要删除选中的记录？', function(){
				$.ajax({
					type: "POST",
				    url:"/adventure/menu/delete",
                    contentType: "application/json",
				    data: JSON.stringify(ids),
				    success: function(r){
						if(r.code == 0){
							alert('操作成功', function(index){
								$("#jqGrid").trigger("reloadGrid");
							});
						}else{
							$.xfm.alert(r.msg);
						}
					}
				});
			});
		},
		getInfo: function(id){
			$.get("/adventure/menu/info?id="+id, function(r){
				console.log(r);
                vm.menu = r.menu;
                console.log(vm.menu);
            });
		},
		reload: function (event) {
			vm.showList = true;
			var page = $("#jqGrid").jqGrid('getGridParam','page');
			$("#jqGrid").jqGrid('setGridParam',{ 
				postData:vm.menu,
                page:page
            }).trigger("reloadGrid");
		}
	},
	created:function(){
	 var _this=this;
	 console.log("welcome go  in");
	}
});