/**
 * Validate
 * 会有一定局限性：如果页面打开一个子模态窗口，多次打开validateElements会重复加入数据，因此要使用用iframe嵌套的模态窗口。
 */

var ValidatePlugin = {};
ValidatePlugin.config = {
	notNullMsg: "不能为空",
	minLenMsg: "长度不能小于",
	maxLenMsg: "长度不能大于",
	numericMsg: "只能是整数",
	decimalMsg: "只能是最大精度为4位的小数",
	mailMsg: "不是有效的邮箱地址",
	phoneMsg: "不是有效固定电话：xxx-xxx...",
	mobileMsg: "不是有效手机号",
	ipMsg: "不是有效IP地址",
	URLMsg: "不是有效网址",
	idCardMsg: "身份证号码无效",
	passwordMsg: "格式不正确：6-18位，由数字，字母或下划组成",
	certifyMsg: "格式不正确",
	carNoMsg: "车牌号无效",
	dateMsg: "不是有效的日期格式：yyyy-MM-dd"
	
};

ValidatePlugin.install = function(Vue, options) {
	var validateElements = [];
	
	/**
	 * 提供正则表达式校验方法，用于特殊校验需求。
	 */
	Vue.pattern = function(toCheck, regex) {
		return regex.test(toCheck)
	}
	
	/**
	 * 提供手动调用方法，例如在表单提交前进行调用。
	 */
	Vue.validate = function() {
		if(!validateElements) return true;
		var errors = 0;
		for(var i=0; i<validateElements.length; i++) {
			var ele = validateElements[i].ele;
			var vType = validateElements[i].vType;
			var errorLabel = validateElements[i].errorLabel;
			if(!validateGroup(ele, vType, errorLabel)) {
				errors = errors + 1 ;
			}
		}
		return errors > 0 ? false : true;
	};
	
	/**
	 * v-validate指令，指定校验规则，例如{notNull:true, maxLen:20}
	 */
	Vue.directive('validate', {
		bind: function(el, binding) {
			/* 每个指令绑定时均会执行 */
			var ele = el;
			var vType = binding.value;
			var errorTpl = Vue.extend({
	            template: '<div class="validate-msg" style="color:red;clear:both;"></div>'
	        });
			var errorLabel = (new errorTpl()).$mount().$el;
			// 收集要校验的elements
			var validateEle = {
				ele : ele,
				vType : vType,
				/* 
				 * 生成的errorLabel在手工校验时也要使用。
				 * 如果在手工校验时，重新生成errorLable，在事件监听中无法识别。
				 */
				errorLabel : errorLabel
			};
			validateElements.push(validateEle);
			addEventListener(ele, vType, errorLabel);
		},
		update: function(el) {
			// 绑定的VM update时，清空errorLabel提示信息
		},
		componentUpdated: function(el) {
			//alert("componentUpdated")
		},
		unbind: function() {
			validateElements = [];
		}
	});
	
	/**
	 * 添加监听事件，即时校验
	 */
	function addEventListener(ele, vType, errorLabel) {
		ele.addEventListener("change", function(e) {
			validateGroup(ele, vType, errorLabel);
		});
	}
	
	/**
	 * 一系列校验的组合，每个el都要校验该组合
	 */
	function validateGroup(ele, vType, errorLabel) {
		// 1.非空校验
		if (vType.notNull) {
			// 如果域不能为空，则进行非空校验，再进行格式校验
			if(!validateNotNull(ele, vType, errorLabel)) {
				return false;
			}
			if(!matcheGroup(ele, vType, errorLabel)) {
				return false;
			}
		} else {
			// 如果域可以为空，并且域有值，则进行格式校验
			if(ele.value != '' && ele.value != null) {
				if(!matcheGroup(ele, vType, errorLabel)) {
					return false;
				}
			} else {
				// 值为空时清标签
				removeErrorLabel(ele, errorLabel);
			}
		}
		
		return true;
	};
	
	/**
	 * 格式校验组合
	 */
	function matcheGroup(ele, vType, errorLabel) {
		// 2.最小长度校验
		if (vType.minLen > 0) {
			if(!validateMinLen(ele, vType, errorLabel)) {
				return false;
			}
		}
		// 3.最大长度校验
		if (vType.maxLen > 0) {
			if(!validateMaxLen(ele, vType, errorLabel)) {
				return false;
			}
		}
		// 4.整数校验
		if (vType.numeric) {
			if(!validateNumeric(ele, vType, errorLabel)) {
				return false;
			}
		}
		// 5.小数校验
		if (vType.decimal) {
			if(!validateDecimal(ele, vType, errorLabel)) {
				return false;
			}
		}
		// 6.电子邮箱校验
		if (vType.mail) {
			if(!validateMail(ele, vType, errorLabel)) {
				return false;
			}
		}
		// 7.固定电话校验
		if (vType.phone) {
			if(!validatePhone(ele, vType, errorLabel)) {
				return false;
			}
		}
		// 8.移动电话校验
		if (vType.mobile) {
			if(!validateMobile(ele, vType, errorLabel)) {
				return false;
			}
		}
		// 9.IP地址校验
		if (vType.ip) {
			if(!validateIP(ele, vType, errorLabel)) {
				return false;
			}
		}
		// 10.URL校验
		if (vType.URL) {
			if(!validateURL(ele, vType, errorLabel)) {
				return false;
			}
		}
		// 11.身份证校验
		if (vType.idCard) {
			if(!validateIdCard(ele, vType, errorLabel)) {
				return false;
			}
		}
		// 12.密码校验
		if (vType.password) {
			if(!validatePassword(ele, vType, errorLabel)) {
				return false;
			}
		}
		// 13.证件校验
		if (vType.certify) {
			if(!validateCertify(ele, vType, errorLabel)) {
				return false;
			}
		}
		// 14.证件校验
		if (vType.carNo) {
			if(!validateCarNo(ele, vType, errorLabel)) {
				return false;
			}
		}
		// 15.日期校验
		if (vType.date) {
			if(!valiateDate(ele, vType, errorLabel)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 1.非空校验
	 */
	function validateNotNull(ele, vType, errorLabel) {
		errorLabel.innerHTML = options.notNullMsg;
		if(ele.value == '') {
			appendErrorLabel(ele, errorLabel);
			return false;
		} else {
			removeErrorLabel(ele, errorLabel);
			return true;
		}
	}
	
	/**
	 * 2.最小长度校验
	 */
	function validateMinLen(ele, vType, errorLabel) {
		errorLabel.innerHTML = options.minLenMsg + vType.minLen;
		if(ele.value.length < vType.minLen) {
			appendErrorLabel(ele, errorLabel);
			return false;
		} else {
			removeErrorLabel(ele, errorLabel);
			return true;
		}
	}
	
	/**
	 * 3.最大长度校验
	 */
	function validateMaxLen(ele, vType, errorLabel) {
		errorLabel.innerHTML = options.maxLenMsg + vType.maxLen;
		if(ele.value.length > vType.maxLen) {
			appendErrorLabel(ele, errorLabel);
			return false;
		} else {
			removeErrorLabel(ele, errorLabel);
			return true;
		}
	}
	
	/**
	 * 4.整数校验
	 */
	function validateNumeric(ele, vType, errorLabel) {
		errorLabel.innerHTML = options.numericMsg;
		if(!/^([0])|(\-?[1-9][0-9]*)$/.test(ele.value)) {
			appendErrorLabel(ele, errorLabel);
			return false;
		} else {
			removeErrorLabel(ele, errorLabel);
			return true;
		}
	}
	
	/**
	 * 5.小数校验
	 */
	function validateDecimal(ele, vType, errorLabel) {
		errorLabel.innerHTML = options.decimalMsg;
		if(!/^(\-?[0-9]*(\.\d{1,4})?)$/.test(ele.value)) {
			appendErrorLabel(ele, errorLabel);
			return false;
		} else {
			removeErrorLabel(ele, errorLabel);
			return true;
		}
	}
	
	/**
	 * 6.邮箱校验
	 */
	function validateMail(ele, vType, errorLabel) {
		errorLabel.innerHTML = options.mailMsg;
		if(!/^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/.test(ele.value)) {
			appendErrorLabel(ele, errorLabel);
			return false;
		} else {
			removeErrorLabel(ele, errorLabel);
			return true;
		}
	}
	
	/**
	 * 7.固定电话校验
	 */
	function validatePhone(ele, vType, errorLabel) {
		errorLabel.innerHTML = options.phoneMsg;
		if(!/^(^0\d{2}-?\d{8}$)|(^0\d{3}-?\d{7}$)|(^\(0\d{2}\)-?\d{8}$)|(^\(0\d{3}\)-?\d{7}$)$/.test(ele.value)) {
			appendErrorLabel(ele, errorLabel);
			return false;
		} else {
			removeErrorLabel(ele, errorLabel);
			return true;
		}
	}
	
	/**
	 * 8.移动电话校验
	 */
	function validateMobile(ele, vType, errorLabel) {
		errorLabel.innerHTML = options.mobileMsg;
		if(!/^([+]\d{1,4})?1[3,4,5,7,8]\d{9}$/.test(ele.value)) {
			appendErrorLabel(ele, errorLabel);
			return false;
		} else {
			removeErrorLabel(ele, errorLabel);
			return true;
		}
	}
	
	/**
	 * 9.IP地址校验
	 */
	function validateIP(ele, vType, errorLabel) {
		errorLabel.innerHTML = options.ipMsg;
		if(!/^((([1-9]\d?)|(1\d{2})|(2[0-4]\d)|(25[0-5]))\.){3}(([1-9]\d?)|(1\d{2})|(2[0-4]\d)|(25[0-5]))$/.test(ele.value)) {
			appendErrorLabel(ele, errorLabel);
			return false;
		} else {
			removeErrorLabel(ele, errorLabel);
			return true;
		}
	}
	
	/**
	 * 10.URL校验
	 */
	function validateURL(ele, vType, errorLabel) {
		errorLabel.innerHTML = options.urlMsg;
		if(!/^(([hH][tT]{2}[pP][sS]?)|([fF][tT][pP]))\:\/\/[wW]{3}\.[\w-]+\.\w{2,4}(\/.*)?$/.test(ele.value)) {
			appendErrorLabel(ele, errorLabel);
			return false;
		} else {
			removeErrorLabel(ele, errorLabel);
			return true;
		}
	}
	
	/**
	 * 11.身份证号码校验
	 	根据〖中华人民共和国国家标准 GB 11643-1999〗中有关公民身份号码的规定，公民身份号码是特征组合码，由十七位数字本体码和一位数字校验码组成。排列顺序从左至右依次为：六位数字地址码，八位数字出生日期码，三位数字顺序码和一位数字校验码。
		    地址码表示编码对象常住户口所在县(市、旗、区)的行政区划代码。
		    出生日期码表示编码对象出生的年、月、日，其中年份用四位数字表示，年、月、日之间不用分隔符。
		    顺序码表示同一地址码所标识的区域范围内，对同年、月、日出生的人员编定的顺序号。顺序码的奇数分给男性，偶数分给女性。
		    校验码是根据前面十七位数字码，按照ISO 7064:1983.MOD 11-2校验码计算出来的检验码。
	
		出生日期计算方法。
		    15位的身份证编码首先把出生年扩展为4位，简单的就是增加一个19或18,这样就包含了所有1800-1999年出生的人;
		    2000年后出生的肯定都是18位的了没有这个烦恼，至于1800年前出生的,那啥那时应该还没身份证号这个东东，⊙﹏⊙b汗...
		下面是正则表达式:
		 出生日期1800-2099  (18|19|20)?\d{2}(0[1-9]|1[12])(0[1-9]|[12]\d|3[01])
		 身份证正则表达式 /^\d{6}(18|19|20)?\d{2}(0[1-9]|1[12])(0[1-9]|[12]\d|3[01])\d{3}(\d|X)$/i            
		 15位校验规则 6位地址编码+6位出生日期+3位顺序号
		 18位校验规则 6位地址编码+8位出生日期+3位顺序号+1位校验位
		 
		 校验位规则     公式:∑(ai×Wi)(mod 11)……………………………………(1)
	                公式(1)中： 
	                i----表示号码字符从由至左包括校验码在内的位置序号； 
	                ai----表示第i位置上的号码字符值； 
	                Wi----示第i位置上的加权因子，其数值依据公式Wi=2^(n-1）(mod 11)计算得出。
	                i 18 17 16 15 14 13 12 11 10 9 8 7 6 5 4 3 2 1
	                Wi 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2 1
	  	身份证号合法性验证 
		支持15位和18位身份证号
		支持地址编码、出生日期、校验位验证
	 */
	function validateIdCard(ele, vType, errorLabel) {
		errorLabel.innerHTML = options.idCardMsg;
		var city={11:"北京",12:"天津",13:"河北",14:"山西",15:"内蒙古",21:"辽宁",22:"吉林",23:"黑龙江 ",31:"上海",32:"江苏",33:"浙江",34:"安徽",35:"福建",36:"江西",37:"山东",41:"河南",42:"湖北 ",43:"湖南",44:"广东",45:"广西",46:"海南",50:"重庆",51:"四川",52:"贵州",53:"云南",54:"西藏 ",61:"陕西",62:"甘肃",63:"青海",64:"宁夏",65:"新疆",71:"台湾",81:"香港",82:"澳门",91:"国外 "};
		var pass= true;
        var code = ele.value;
        if(!code || !/^\d{6}(18|19|20)?\d{2}(0[1-9]|1[12])(0[1-9]|[12]\d|3[01])\d{3}(\d|X)$/i.test(code)){
        	errorLabel.innerHTML = "身份证号格式错误";
            pass = false;
        }else if(!city[code.substr(0,2)]){
        	errorLabel.innerHTML = "地址编码错误";
            pass = false;
        }else{
            //18位身份证需要验证最后一位校验位
            if(code.length == 18){
                code = code.split('');
                //∑(ai×Wi)(mod 11)
                //加权因子
                var factor = [ 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 ];
                //校验位
                var parity = [ 1, 0, 'X', 9, 8, 7, 6, 5, 4, 3, 2 ];
                var sum = 0;
                var ai = 0;
                var wi = 0;
                for (var i = 0; i < 17; i++)
                {
                    ai = code[i];
                    wi = factor[i];
                    sum += ai * wi;
                }
                var last = parity[sum % 11];
                if(parity[sum % 11] != code[17]){
                	errorLabel.innerHTML = "校验位错误";
                    pass =false;
                }
            }
        }
		if(!pass) {
			appendErrorLabel(ele, errorLabel);
			return false;
		} else {
			removeErrorLabel(ele, errorLabel);
			return true;
		}
	}
	
	/**
	 * 12.password校验
	 */
	function validatePassword(ele, vType, errorLabel) {
		errorLabel.innerHTML = options.passwordMsg;
		if(!/^[\w\_]{6,18}$/.test(ele.value)) {
			appendErrorLabel(ele, errorLabel);
			return false;
		} else {
			removeErrorLabel(ele, errorLabel);
			return true;
		}
	}
	
	/**
	 * 13.证件号码校验
	 */
	function validateCertify(ele, vType, errorLabel) {
		errorLabel.innerHTML = options.certifyMsg;
		if(!/^\w*[-_.(]*\w*[-_.)]*$/.test(ele.value)) {
			appendErrorLabel(ele, errorLabel);
			return false;
		} else {
			removeErrorLabel(ele, errorLabel);
			return true;
		}
	}
	
	/**
	 * 14.车牌号校验
	 */
	function validateCarNo(ele, vType, errorLabel) {
		errorLabel.innerHTML = options.carNoMsg;
		if(!/^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{4}[A-Z0-9挂学警港澳]{1}$/.test(ele.value)) {
			appendErrorLabel(ele, errorLabel);
			return false;
		} else {
			removeErrorLabel(ele, errorLabel);
			return true;
		}
	}
	
	/**
	 * 15.日期格式校验yyyy-MM-dd
	 */
	function valiateDate(ele, vType, errorLabel) {
		errorLabel.innerHTML = options.dateMsg;
		if(!/^([0-9]\d{3})(\-)(([0][1-9])|([1][0,1,2]))(\-)(([0][1-9])|([1,2][0-9])|([3][0,1]))$/.test(ele.value)) {
			appendErrorLabel(ele, errorLabel);
			return false;
		} else {
			removeErrorLabel(ele, errorLabel);
			return true;
		}
	}
	
	/**
	 * 显示错误信息
	 */
	function appendErrorLabel(ele, errorLabel) {
		// 避免重复生成提示信息
		if(ele.parentNode.innerHTML.indexOf("validate-msg") < 0) {
			ele.focus();
			ele.parentNode.appendChild(errorLabel);
		}
	}
	
	/**
	 * 移除错误信息
	 */
	function removeErrorLabel(ele, errorLabel) {
		if(ele.parentNode.innerHTML.indexOf("validate-msg") >= 0) {
			ele.parentNode.removeChild(errorLabel);
		}
	}
}

// 注册插件
Vue.use(ValidatePlugin, ValidatePlugin.config);