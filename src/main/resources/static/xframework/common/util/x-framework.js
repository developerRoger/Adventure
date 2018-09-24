/*********************************************************
 * 通用方法封装。 命名空间：xfm； 使用方式：$.xfm.xxx();
 *********************************************************/
$.extend({
	xfm : {}
});
$.extend($.xfm, {
	/**
	 * POST封装，固定部份参数。
	 * 返回promise。jQuery1.5后的版本，$.Deferred()对象，项目中主要用于异步请求流程控制。
	 * 例如，select组件，异步情况下，如果select组件中的option没有加载完成，form表单的数据就已经加载，
	 * 此时，select对象对应的v-modal会变为undefined，select的选项就无法选中。
	 * 通过$.when(select组件option数据请求).done(function(){});达到流程控制的目的。
	 */
	post : function(params) {
		var promise = $.ajax({
			url : params.url,
			// POST请求参数stringify
			data : JSON.stringify(params.data),
			type : "POST",
			dataType : "json",
			contentType : "application/json"
		});
		promise.done( function(result) {
			if (params.success) {
				params.success(result);
				return;
			}
			if(result.state == 0){
				$.xfm.alert(result.message.code + " : " +result.message.info);
			}
		}).fail(function(result) {
			// 服务未能处理的异常会进入error方法
			// 对请求异常的结果进行处理，取responseText部份
			var respText = JSON.parse(result.responseText);
			if (params.error) {
				params.error(respText);
			} else {
				// session time out
				if (respText.message.code === "SYS0-9999") {
					// 清除缓存 
					sessionStorage.clear();
					window.top.location.reload();
					return;
				}
				$.xfm.alert(respText.message.code + " : " +respText.message.info);
			}
		});
		return promise;
	},

	/**
	 * GET封装，固定部份参数。
	 */
	get : function(params) {

		var promise = $.ajax({
			url : params.url,
			data : params.data,
			type : "GET",
			dataType : "json",
			contentType : "application/json"
		});
		promise.done( function(result) {
			if (params.success) {
				params.success(result);
				return;
			}
			if(result.state == 0){
				$.xfm.alert(result.message.code + " : " +result.message.info);
			}
		}).fail(function(result) {
			// 服务未能处理的异常会进入error方法
			// 对请求异常的结果进行处理，取responseText部份
			var respText = JSON.parse(result.responseText);
			if (params.error) {
				params.error(respText);
			} else {
				// session time out
				if (respText.message.code === "SYS0-9999") {
					// 清除缓存 
					sessionStorage.clear();
					window.top.location.reload();
					return;
				}
				$.xfm.alert(respText.message.code + " : " +respText.message.info);
			}
		});
		return promise;
	},

	/**
	 * Bootstrap modal插件封装。
	 * 嵌套iframe，隔离页面，解决校验等问题。
	 */
	openModal : function(title, url, _callback) {
		var modal = $("<div class='modal' tabindex='-1' role='dialog' id='test'><div>");
		var modalDialog = $("<div class='modal-dialog' role='document' style='width: 98% !important;'><div>");
		var modalContent = $("<div class='modal-content'><div>");
		var modalHeader = $("<div class='modal-header'></div>");
		var modalTitle = "<h5 class='modal-title'><span class='glyphicon glyphicon-list-alt text-info'></span>&nbsp;"+ title +"</h5>"
		var modalClose = "<button type='button' class='close' id='modal_close' data-dismiss='modal' aria-hidden='true'>&times;</button>"
		var fmHeight = $(window).height() - 60;
		var frm = $("<iframe width='99.88%' height='"+ fmHeight +"px' src='"+ url +"' frameborder='0' style=''></iframe>")
		modal.append(modalDialog);
		modalDialog.append(modalContent);
		modalContent.append(modalHeader);
		modalContent.append(frm);
		modalHeader.append(modalClose);
		modalHeader.append(modalTitle);
		
		modal.modal({
			show : true,
			backdrop : 'static'
		});
		
		modal.on('hide.bs.modal', function() {
			modal.remove();
			if (_callback) _callback();
		});
	},
	
	/**
	 * 自定义alert窗口，可传入function在确认时调用。
	 */
	alert: function(content, _callback) {
		var modal = $("<div class='modal' tabindex='-1' role='dialog' id='test'><div>");
		var modalDialog = $("<div class='modal-dialog modal-sm' role='document'><div>");
		var modalContent = $("<div class='modal-content'><div>");
		var header = $("<div class='modal-header'><h4 class='modal-title'><span class='glyphicon glyphicon-info-sign text-info'></span>&nbsp;系统提示</h4></div>");
		var body = $("<div class='modal-body'><span class='glyphicon glyphicon-bell' style='color: #cc0000;'></span>&nbsp;</div>");
		body.append(content);
		var footer = $("<div class='modal-footer'></div>");
		var button = $("<input type='button' class='btn btn-primary' data-dismiss='modal' value='确 认'>");
		footer.append(button);
		button.click(function() {
			if(_callback) _callback();
		});
		modal.append(modalDialog);
		modalDialog.append(modalContent);
		modalContent.append(header);
		modalContent.append(body);
		modalContent.append(footer);
		modal.modal({
			show : true,
			backdrop : false
		});
		modal.on('hide.bs.modal', function() {
			modal.remove();
		});
	},
	
	/**
	 * 自定义confirm窗口，可传入function在确认或取消时调用。
	 */
	confirm: function(content, ok_callback, cancel_callback) {
		var modal = $("<div class='modal' tabindex='-1' role='dialog'><div>");
		var modalDialog = $("<div class='modal-dialog modal-sm' role='document'><div>");
		var modalContent = $("<div class='modal-content'><div>");
		var header = $("<div class='modal-header'><h4 class='modal-title'><span class='glyphicon glyphicon-info-sign text-info'></span>&nbsp;系统提示</h4></div>");
		var body = $("<div class='modal-body'><span class='glyphicon glyphicon-bell' style='color: #cc0000;'></span>&nbsp;</div>");
		body.append(content);
		var footer = $("<div class='modal-footer'></div>");
		var button_ok = $("<input type='button' class='btn btn-primary' data-dismiss='modal' value='确 认'>");
		var button_cancel = $("<input type='button' class='btn btn-default' data-dismiss='modal' value='取 消'>");
		footer.append(button_ok);
		footer.append(button_cancel);
		button_ok.click(function() {
			if(ok_callback) ok_callback();
		});
		button_cancel.click(function() {
			if(cancel_callback) cancel_callback();
		});
		modal.append(modalDialog);
		modalDialog.append(modalContent);
		modalContent.append(header);
		modalContent.append(body);
		modalContent.append(footer);
		modal.modal({
			show : true,
			backdrop : false
		});
		modal.on('hide.bs.modal', function() {
			modal.remove();
		});
	},
	
	/**
	 * 自定义数据提交窗口，可传入function在确认时调用。
	 */
	submitting: function() {
		var submit = function(state, message, _callback) {
			var template = `
				<div class="modal" id="modal" tabindex='-1' role='dialog'>
					<div class='modal-dialog modal-md' role='document'>
						<div class='modal-content'>
							<div class='modal-header'><h5 class='modal-title'>系统提示</h5></div>
							<div class='modal-body' id='modalBody' style='height: 80px; line-height: 40px;'>
								<img src='/xframework/common/img/loading.gif'/>&nbsp;&nbsp;&nbsp;&nbsp;提交中，请稍后 ...
							</div>
							<div class='modal-footer'>
								<input type='button' id="modalButton" class='btn btn-primary' data-dismiss='modal' value='确 认'>
							</div>
						<div>
					<div>
				<div>
			`
			// 展示modal
			var modal = $(template);
			modal.modal({
				show : true,
				backdrop : false
			});
			var body = $("#modalBody");
			var button = $("#modalButton");
			// 显示提交结果
			if(state) {
				var icon = (state == '1')? "<img src='/xframework/common/img/ok.png'/>" : "<img src='/xframework/common/img/error.png'/>";
				if(message) body.html(icon + "&nbsp;&nbsp;&nbsp;&nbsp;" + message);
			}
			// 确认按钮事件
			button.click(function() {
				if(_callback) _callback();
			});
			// 关闭窗口
			modal.on('hide.bs.modal', function() {
				modal.remove();
			});
		}
		return submit;
	},
	/**
	 * 获得URL参数
	 */
	getParameter : function(key) {
		var vars = {}, hash;
		var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
		for (var i = 0; i < hashes.length; i++) {
			hash = hashes[i].split('=');
			vars[hash[0]] = decodeURIComponent(hash[1]);
		}
		return vars[key];
	},
	
	/**
	 * 打开与主界面平级的Tab窗
	 */
	openTab: function(tabName, url) {
		window.top.XTabs.addTab(tabName, url, true);
	},
	
	/**
	 * 重置表单时，清除Validate信息标签
	 */
	resetValidate: function() {
		$(".validate-msg").remove();
	},
	
	/**
	 * 日期控件初始化，使用bootstrap datetimepicker，无法与v-model指令同时使用
	 */
	initDate: function() {
		var datetimes = $(".datetimepicker");
		$.each(datetimes, function(index, datetime) {
			var icon = $("<span class='glyphicon glyphicon-calendar form-control-feedback'></span>");
			var datetimeObj = $(datetime);
			datetimeObj.after(icon);
			datetimeObj.datepicker({
				language : "zh-CN",
				forceParse: false,
				autoclose: true,
				clearBtn: true
			});
			datetimeObj.datepicker().on('changeDate', function(ev) {
			}).on('hide', function(event) {
				event.preventDefault();
				event.stopPropagation();
			});
			icon.click(function() {
				datetimeObj.focus();
			});
		});
	},
	
	/**
	 * 简单日期初始化，人工录入，后面补充日期控件
	 */
	initSimpleDate : function() {
		var datetimes = $(".simpledate");
		$.each(datetimes, function(index, datetime) {
			var icon = $("<span class='glyphicon glyphicon-calendar form-control-feedback'></span>");
			var datetimeObj = $(datetime);
			datetimeObj.after(icon);
			datetimeObj.keyup(function(e) {
				if(e.which == 8 || e.which == 46) {
					return;
				}
				var val = $(this).val();
				if (val.length == 4) {
					$(this).val(val + "-");
				} else if (val.length == 7) {
					$(this).val(val + "-");
				}
			});
		});
	},
	
	/**
	 * 获取cookie的值
	 */
	getCookie : function(name) {
		var arr, reg = new RegExp("(^| )"+name+"=([^;]*)(;|$)");
		if(arr = document.cookie.match(reg)) {
			return unescape(arr[2]);
		} else {
			return null;
		}
	},
	compare:function(property){
	  return function(obj1,obj2){
	    var value1 = obj1[property];
	    var value2 = obj2[property];
	    return value2 - value1;
	  }
	}
});

/*********************************************************
 * 初始化
 *********************************************************/
$(function() {
	// 简单日期录入框初始化
	$.xfm.initSimpleDate();
});