$(function() {
	// 加载数据
	$(".x-main-loading").show();
	$.when(loadCommonCode(), loadNavBar()).done(function() {
		$(".x-main-loading").fadeOut(500);
	});
	// 初始化XTabs组件
	var XTabs = initTabs();
	// 主页按钮
	$('#btnHome').click(function(){
		$.xfm.openTab('主页','/xframework/common/page/home.html');
	});
	// 修改密码按钮
	$('#btnModifyPassword').click(function(){
		$.xfm.openTab('修改密码','/xframework/common/page/modify-password.shtml');
	});
	// 退出系统按钮
	$('#btnLogout').click(function(){
		$.xfm.confirm('您确定要退出系统吗？',function(){
			$.xfm.post({
				url : "/openapi/sysmanage/logout",
				data :{},
				success : function(result) {
					if(result.state == 0) {
						$.xfm.alert(result.message.info+'('+result.message.code+')');
						return;
					}
					// 清除缓存 
					sessionStorage.clear();
					localStorage.clear();
					window.location.href = "/index.html";
				}
			});
		});
	});
});

// 加载并缓存基础数据
function loadCommonCode() {
	return $.xfm.get({
		url: "/openapi/sysmanage/commonCode/formatByGroupCode",
		success: function(result) {
			window.top.XFM_CACHES = result.data;
			var dictionary;
			$.getJSON("./dictionary_quote.json", function(data) {
				dictionary = data;
				var DICTIONARY = Object.assign(dictionary, result.data)
				if(window.localStorage) {
				  window.localStorage.DICTIONARY = JSON.stringify(DICTIONARY)
				}
			});
		}
	});
}

//加载导航栏数据(应用)
function loadNavBar() {
	// 从本地session缓存获取应用数据
	$("#applications").XNavbar({
		localData: JSON.parse(localStorage.APPLICATION),
		resultRef: "",
		attrRef: {
			id: "code",
			name: "name",
			menuList:'menuList'
		},
		// 点击加载菜单树
		click: function(navbarNode) {
			$("#menuTree").XTree({
				url: "/openapi/sysmanage/menu/treeQuery",
				params: {navbarNode : navbarNode},
				resultRef: "",
				attrRef: {
					id: "id",
					name: "name",
					url: "url",
					children: "childList"
				},
				disableTarget: true,
				click: function getNode(treeNode) {
					if (treeNode.url && $.trim(treeNode.url) != '' && $.trim(treeNode.url) != '#') {
						XTabs.addTab(treeNode.name, treeNode.url, true);
					}
				}
			});
		}
	});
}

// 初始化并缓存XTabs组件
function initTabs() {
	XTabs.init("#operatePages", $(window).height()-$("#navbarArea").height());
	XTabs.addTab("主页","home.html",true);
	window.top.XTabs = XTabs;
	return XTabs;
}
