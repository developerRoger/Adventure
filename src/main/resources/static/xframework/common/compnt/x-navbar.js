/**
 * 远程加载生成简单的导航栏。
 * 
 * @author billy.lin
 * @version 1.0.0
 */

(function($) {
	$.fn.XNavbar = function(options) {
		var defaults = {
			url: '',						// 远程加载数据的URL
			params: '',						// 远程加载数据的参数
			method: 'GET',
			dataType: 'json',				// data type of load
			contentType: 'application/json',// content type of load
			resultRef: '',					// 影射到返回结果哪个key
			attrRef: {						// 属性影射
				id: 'id',
				name: 'name'
			},
			localData: [],					// 使用本地数据
			click: function(){}				// 点击事件
		}
		
		options = $.extend(defaults, options);
		var _self = $(this);
		_self.html("");
		loadData(_self, options);
		
		/*
		 * private function
		 * load data
		 */
		function loadData(target, options) {
			// load local data if it is set
			if(options.localData) {
				// make bar
				makeNavbar(options.localData, target, options);
				return;
			}
		}
		
		/*
		 * private function
		 * make bar
		 */
		function makeNavbar(navbarData, parent, options) {
			if ($.trim(options.resultRef) == '') {
				navbarData = navbarData;
			} else {
				navbarData = navbarData[options.resultRef];
			}
			var ul =  $("<ul></ul>");
			ul.addClass("x-navbar-container x-navbar-table");
			$.each(navbarData, function(index, node) {
				var li = $("<li></li>");
				li.addClass("x-navbar-node x-navbar-cell");
				// set the reference attribute
				var name = node[options.attrRef.name];
				var menuList = node[options.attrRef.menuList];
				li.html(name);
				var id = node[options.attrRef.id];
				li.attr("id", id);
				// bind on click event
				li.bind("click", function(node) {
					options.click({
						id: id,
						name: name,
						menuList:menuList
					});
					li.addClass("x-navbar-node-active");
					ul.children("li").not(li).removeClass("x-navbar-node-active");
				});
				// 默认第一个节点选中
				if (index == 0) {
					li.click();
					li.addClass("x-navbar-node-active");
				}
				ul.append(li);
			});
			parent.append(ul);
		}
	}
})(jQuery);