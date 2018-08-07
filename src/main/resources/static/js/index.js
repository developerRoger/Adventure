//iframe自适应
$(window).on('resize', function() {
	var $content = $('.content');
	$content.height($(this).height() - 120);
	$content.find('iframe').each(function() {
		$(this).height($content.height());
	});
}).resize();

$(function() {
	//请求登陆     
	function getAccse() {
		var Authorization = "<%=session.getAttribute('Authorization')%>";
		console.log(Authorization + "heihei");
		debugger
	}
});
function getCookie(name) { //获取一个cookie  
	var strCookie = document.cookie;
	var arr = strCookie.split(';');
	for (var i = 0; i < arr.length; i++) {
		var t = arr[i].split("=");
		if (t[0] == name) {
			return t[1];
		}
	}
	;
	return null;
}
var vm = new Vue({
	el : '#rrapp',
	data : {
		main : "main.html",
		navTitle : "欢迎页",
		applicationLists : [],
		listUrl:[]
	},
	methods : {
		donate : function() {
			layer.open({
				type : 2,
				title : false,
				area : [ '806px', '467px' ],
				closeBtn : 1,
				shadeClose : false,
				content : [ 'img/login/demo-1-bg.jpg', 'no' ]
			});
		},
		//简易版接口文档
		swagger : function() {
			var _this=this;
			_this.navTitle = "swagger";
			_this.main = "swagger-ui.html";
		},
		//获取服务端数据
		getMenu : function() {
			var _this = this;
			$.ajax({
				//几个参数需要注意一下
				type : "POST",//方法类型
				dataType : "json",//服务端接收的数据类型
				url : "/adventure/application/list",
				contentType : "application/json",
				async:false, 
				data : null,
				success : function(result) {
					_this.applicationLists = result;//打印服务端返回的数据(调试用)
					if(_this.applicationLists.length>0){
						for(var a in _this.applicationLists){
							for(var i in _this.applicationLists[a].listMenu){
								//将url存储起来，已便放入动态路由
								_this.listUrl.push(_this.applicationLists[a].listMenu[i].url);
							}
						}
					}
				},
				error : function() {
					console.log("异常!");
				}
			});
		},
		routerList:function(router, menus){
			var _this=this;
			for ( var index in menus) {
				router.add('#' + menus[index], function() {
					var url = window.location.hash;
					//替换iframe的url
					_this.main = url.replace('#', '');
					//导航菜单展开
					$(".treeview-menu li").removeClass("active");
					$("a[href='" + url + "']").parents("li").addClass("active");
					_this.navTitle = $("a[href='" + url + "']").text();
				});
			}
		}
	},
	created:function(){
		var _this=this;
		_this.getMenu();
		//路由
		var router = new Router();
		var menus = _this.listUrl;
		_this.routerList(router, menus);
		router.start();
	}
});

