//iframe自适应
$(window).on('resize', function() {
	var $content = $('.content');
	$content.height($(this).height() - 120);
	$content.find('iframe').each(function() {
		$(this).height($content.height());
	});
}).resize();

$(function () {
	//请求登陆     
	function getAccse(){
		var Authorization="<%=session.getAttribute('Authorization')%>"; 
		console.log(Authorization+"heihei");
		debugger	
	}
});
function getCookie(name) {                  //获取一个cookie  
    var strCookie = document.cookie;  
    var arr = strCookie.split(';');  
    for (var i = 0; i < arr.length; i++) {  
       var t = arr[i].split("=");  
        if(t[0] == name) {  
            return t[1];  
        }  
    };  
    return null;  
}  
var vm = new Vue({
	el:'#rrapp',
	data:{
		main:"main.html",
        navTitle:"欢迎页"
	},
    methods: {
        donate: function () {
            layer.open({
                type: 2,
                title: false,
                area: ['806px', '467px'],
                closeBtn: 1,
                shadeClose: false,
                content: ['img/login/demo-1-bg.jpg', 'no']
            });
        }
    }
});

//路由
var router = new Router();
var menus = ["main.html","generator.html"];
routerList(router, menus);
router.start();

function routerList(router, menus){
	for(var index in menus){
		router.add('#'+menus[index], function() {
			var url = window.location.hash;

			//替换iframe的url
			vm.main = url.replace('#', '');

			//导航菜单展开
			$(".treeview-menu li").removeClass("active");
			$("a[href='"+url+"']").parents("li").addClass("active");

			vm.navTitle = $("a[href='"+url+"']").text();
		});
	}
}
