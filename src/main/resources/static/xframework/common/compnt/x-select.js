/**
 * 基于Vue.js的select选项填充插件。<br>
 * 使用方式：<br>
 * 1.参数方式，使用页面缓存中的标准代码：x-select:[groupCode]="{parent:'parentCode或#元素的id'}"<br>
 * 2.使用VM中的数据:x-select="{data:VM中的数据,codeCol:'对应列',nameCol:'对应列',parentCol:'对应列',parent:'parentCode或#元素的id'}"<br>
 * 
 * @author billy.lin
 * @version v1.0.0
 */
var SelectPlugin = {};
SelectPlugin.config = {};

SelectPlugin.install = function(Vue, options) {
	/**
	 * 将CommonCode数据设置为全局变量。
	 * 为了方便操作，但缓存了多份数据，即每个iFrame都缓存了数据，数据量大时，会影响使用！
	 */
	Vue.prototype.XFM_CommonCode = window.top.XFM_CACHES;
	
	/**
	 * 提供根据groupCode获取CommonCode方法
	 */
	Vue.getCommonCode = function(groupCode) {
		return window.top.XFM_CACHES[groupCode];
	}
	
	/**
	 * 提供filter功能
	 */
	Vue.select = function(code, select, codeMapping, nameMapping) {
		if(select instanceof Array) {
			// 传入数据
			for(var i=0; i<select.length; i++) {
				if(code == (select[i])[codeMapping]) {
					return (select[i])[nameMapping];
				}
			}
		} else if(typeof select === "string") {
			// 从缓存中取数
			var datas = window.top.XFM_CACHES[select];
			for(var i=0; i<datas.length; i++) {
				if(code == (datas[i])["code"]) {
					return (datas[i])["name"];
				}
			}
		}
	}
	
	/**
	 * v-select指令
	 */
	Vue.directive('select', {
		bind : function(el, binding) {
			/* 每个指令绑定时均会执行 */
			var ele = el;
			// 创建ID和对象的映射关系，用于级联选择操作
			createIdObjMapper(ele);
			// 创建select的选项
			var parentObj = createOptions(ele, binding);
			// 注册级联父对象的事件
			if (parentObj != null) {
				parentObj.addEventListener("change", function(e) {
					createOptions(ele, binding);
				});
			}
		},
		update : function(el, binding, vnode, oldVnode) {
			var ele = el;
			// 非参数形式的情况下，绑定模板的数据发生变化时，更新下拉列表
			if (!binding.arg) {
				if (binding.value.data != binding.oldValue.data) {
					createOptions(ele, binding);
				}
			}
		}
	});

	//ID和对象的映射关系
	var _idObjMapper = {};
	function createIdObjMapper(ele) {
		if (ele.id) {
			_idObjMapper["#" + ele.id] = ele;
		} else {
			console.log("级联操作的父节点需要指定ID");
		}
	}

	/**
	 * 创建选项
	 * 
	 * @param ele
	 * @param binding
	 */
	function createOptions(ele, binding) {
		var param = binding.arg;
		var direValue = binding.value;
		var datas;
		var parent;
		if (direValue) {
			parent = direValue.parent;
			datas = direValue.data;
		}
		// 清空option
		var template = "<option value=''> </option>";
		var options;
		
		if(param) {
			// 参数形式，直接从Cache中取值
			datas = window.top.XFM_CACHES[param];
			options = {
				codeKey: "code",
				nameKey: "name",
				groupCodeKey: "groupCode",
				groupCodeVal: param,
				parentCodeKey: "parentCode",
				parentCodeVal: (parent? parent : "")
			};
		} else {
			// 非参数形式，从VM数据中取值
			datas = direValue.data;
			options = {
				codeKey: direValue.codeCol,
				nameKey: direValue.nameCol,
				groupCodeKey: "",
				groupCodeVal: "",
				parentCodeKey: direValue.parentCol,
				parentCodeVal: (parent? parent : "")
			};
		}
		return createTemplate(ele, datas, options, template);
	}

	/**
	 * 创建HTML文档
	 * 
	 * @param ele
	 * @param datas
	 * @param options
	 * @param template
	 */
	function createTemplate(ele, datas, options, template) {
		if (!datas) {
			return;
		}
		var dataLen = datas.length;
		var codeKey = options.codeKey;
		var nameKey = options.nameKey;
		var groupCodeKey = options.groupCodeKey;
		var groupCodeVal = options.groupCodeVal;
		var parentCodeKey = options.parentCodeKey;
		var parentCodeVal = options.parentCodeVal;
		var parentObj = null; // 级联时的父对象
		
		if (parentCodeVal && parentCodeVal.indexOf("#") < 0) {
			// 有parent并且parent不是id，则用parentCode取数
			for (var i = 0; i < dataLen; i++) {
				var data = datas[i];
				var dataGroupVal = (data[groupCodeKey])? data[groupCodeKey] : "";
				if (dataGroupVal == groupCodeVal && data[parentCodeKey] == parentCodeVal) {
					template += "<option value=" + data[codeKey] + ">" + data[nameKey] + "</option>"
				}
			}
		} else if (parentCodeVal && parentCodeVal.indexOf("#") >= 0) {
			// 有parent并且parent是某个元素的id，则parentCode为该元素的值
			parentObj = _idObjMapper[parentCodeVal];
			for (var i = 0; i < dataLen; i++) {
				var data = datas[i];
				var dataGroupVal = (data[groupCodeKey])? data[groupCodeKey] : "";
				if (dataGroupVal == groupCodeVal && data[parentCodeKey] == parentObj.value) {
					template += "<option value=" + data[codeKey] + ">" + data[nameKey] + "</option>"
				}
			}
		} else {
			// 无parent的情况
			for (var i = 0; i < dataLen; i++) {
				var data = datas[i];
				var dataGroupVal = (data[groupCodeKey])? data[groupCodeKey] : "";
				if (dataGroupVal == groupCodeVal) {
					template += "<option value=" + data[codeKey] + ">" + data[nameKey] + "</option>"
				}
			}
		}
		ele.innerHTML = template;
		return parentObj;
	}
}

//注册插件
Vue.use(SelectPlugin, SelectPlugin.config);