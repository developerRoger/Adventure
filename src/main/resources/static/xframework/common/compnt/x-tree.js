/**
 * 树状插件，以JSON格式数据生成，每个节点包含的属性有：id,name,url,children。
 * 依赖第三方：Bootstrap 3.3.7(字体图标)，jquery 2.2.4
 * 
 * 1-属性支持:
 * 	url:''   		 					// 远程加载的URL
 * 	params : ''        	 				// 远程加载的参数
 * 	method : 'GET',		 				// POST or GET
 * 	dataType : 'json',	 				// return data type
 * 	contentType : 'application/json', 	// request contetnType
 * 	selectAble : false, 				// 是否可选
 * 
 * 2-事件支持:
 *	click : function(node){}, 			// click event
 * 	dblClick : function(node){}, 		// dblClick event
 * 
 * 3-方法支持:
 * 	getTreeData(),						// 返回树的JSON数据
 * 	getSelected()						// 返回选中的节点(父节点自动选择)
 * 
 * @author billy.lin
 * @version 1.0
 */

(function($) {
	 $.fn.XTree = function (options) {
		 var defaults = {
				 url:'', // 远程加载的URL
				 params : '', // 远程加载的参数
				 method : 'GET', // POST or GEG
				 dataType : 'json', // return data type
				 contentType : 'application/json', // request contetnType
				 resultRef: '',					// 影射到返回结果哪个key
				 attrRef: {						// 属性影射
					 id: 'id',
					 name: 'name',
					 url: 'url',
					 children: 'children'
				 },
				 selectAble : false, // 是否可选
				 disableTarget : false, // 是否禁用打开窗口
				 target : '_blank', // 链接打开的目标
				 click : function(node){}, // click event
				 dblClick : function(node){} // dblClick event
		 }
		 options = $.extend(defaults, options);
		 var _self = $(this);
		 _self.html("");
		 makeTree(_self, options);
		 
		 /*
		  * private function
		  * loadData from remote
		  */
		 function makeTree(target, options) {
			 // var menuList = options.params.navbarNode.menuList;
			 var treeData = options.params.navbarNode.menuList;
			 if(treeData){
				 plainTree(treeData, target, options);
			 }else{
				 $.ajax({
					 url : options.url,
					 data : options.params,
					 type : options.method,
					 dataType : options.dataType,
					 contentType : options.contentType,
					 success : function(result) {
						 var treeData;
						 treeData = result[options.resultRef];
						 window.top[cacheKey] = treeData;
						 plainTree(treeData, target, options);
					 },
					 error : function(result) {
						 alert("load tree data error.");
					 }
				 });
			 }
			 
		 }
		 
		 /*
		  * private function
		  * plain tree, to make elements
		  * using x-tree.css and bootstrap.css
		  */
		 function plainTree(treeData, parent, options) {
			 if (treeData && treeData.length <= 0) {
				 return;
			 } else {
				 var ul = $("<ul></ul>");
				 ul.addClass("x-tree-group");
				 $.each(treeData, function(index, node) {
					 var children, hasChildren;
					 var children = node[options.attrRef.children];
					 hasChildren = (children && children.length > 0) ? true : false;
					 li = makeNode(node, hasChildren, options);
					 ul.append(li);
					 parent.append(ul);
					 plainTree(children, li, options);
				 });
			 }
		 }
		 
		 /*
		  * private function
		  * make a node
		  */
		 function makeNode(node, hasChildren, options) {
			 var li = $("<li></li>");
			 li.addClass("x-tree-node x-tree-middle");
			 // make icon
			 var isShow = true;
			 var icon = makeIcon(hasChildren, isShow);
			 li.append(icon);
			 // make check box
			 if (options.selectAble) {
				 var box = makeCheckBox(node, options);
				 li.append(box);
				 li.append(" ");
			 }
			 // node content
			 var a = $("<a></a>");
			 a.html(node[options.attrRef.name]);
			 a.attr("id", node[options.attrRef.id]);
			 var url = node[options.attrRef.url];
			 if (options.disableTarget) {
				 a.attr("href", "javascript:void(0)");
			 } else {
				 a.attr("href", (url && url != '') ? url : "#");
				 a.attr("target", options.target);
			 }
			 a.addClass("x-tree-node x-tree-middle");
			 // bind events
			 a.click(function() {
				 options.click(node);
				 // toggle and change icon
				 li.find("ul").toggle();
				 li.find("span.x-tree-icon:eq(0)").remove();
				 isShow = !isShow;
				 li.prepend(makeIcon(hasChildren, isShow));
				 a.addClass("x-tree-node-active");
				 _self.find("a").not(a).removeClass("x-tree-node-active");
			 });
			 a.dblclick(function() {
				 options.dblClick(node);
			 });
			 li.append(a);
			 return li;
		 }
		 
		 /*
		  * private function
		  * make icon of the node
		  */
		 function makeIcon(hasChildren, isShow) {
			 // is folder or node ?
			 var icon = $("<span></span>");
			 // icon.addClass("x-tree-middle");
			 if (hasChildren) {
				 if (isShow) {
					 icon.addClass("glyphicon glyphicon-folder-open x-tree-icon x-tree-icon-folder");
				 } else {
					 icon.addClass("glyphicon glyphicon-folder-close x-tree-icon x-tree-icon-folder");
				 }
			 } else {
				 icon.addClass("glyphicon glyphicon-stop x-tree-icon x-tree-icon-node");
			 }
			 return icon;
		 }
		 
		 /*
		  * private function
		  * make checkbox or radio to the node
		  */
		 function makeCheckBox(node, options) {
			 var box, box_id
			 box_id = "node_checkbox_" + node.id;
			 box = $("<input type='checkbox' id='" + box_id + "'>");
			 box.addClass("x-tree-middle");
			 // bind click event
			 box.bind('click', function() {
				 var childrenBox = box.parent("li").find("input[type='checkbox']");
				 var parentsBox = box.parents("li").find("input[type='checkbox']:eq(0)");
				 if (box.is(":checked")) {
					 // all children checked
					 // after jquery1.6+ use function prop to operate check box *
					 childrenBox.prop("checked", true);
					 // all parents checked
					 parentsBox.prop("checked", true);
				 } else {
					// all children unchecked
					childrenBox.prop("checked", false);
					// all parent unchecked if there is no children checked
					box.parents("li").each(function() {
						if ($(this).find("input[type='checkbox']:checked").length-1 <= 0) {
							$(this).find("input[type='checkbox']:eq(0)").prop("checked", false);
						}
					});
				 }
			 });
			 return box;
		 }
		 
		 /*
		  * private function
		  * return selected nodes
		  */
		 function selectedNodes() {
			 var nodeIds = [];
			 var selecteds =  _self.find("input[type='checkbox']:checked");
			 selecteds.each(function() {
				 nodeIds.push($(this).parent("li").children("a").attr("id"));
			 });
			 return nodeIds;
		 }
		 
		 /*
		  * XTree returns 
		  */
		 return {
			 getSelected : function() {
				 return selectedNodes();
			 }
		 }
	 }
})(jQuery);
