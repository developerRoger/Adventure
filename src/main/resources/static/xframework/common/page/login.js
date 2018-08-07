$(function() {
	// 登录
	$("#login_btn").click(function() {
		var _this = this;
		$("#login-info").html("");
		var btn = $(_this).button('loading');
		$(_this).attr("disabled", true);
		
		var userName = $("#userName").val();
		var password = $("#password").val();
		if ($.trim(userName) == '' || $.trim(password) == '') {
			$("#login-info").html("帐号和密码不能为空！");
			btn.button('reset');
			$(_this).attr("disabled", false);
			return;
		}
		$(".x-login-loading").show();
		$.xfm.post({
			url : "/openapi/sysmanage/login",
			data : {userName : userName, password : password},
			success : function(result) {
				$(".x-login-loading").hide();
				if(result.state == 0) {
					btn.button('reset');
					$(_this).attr("disabled", false);
					$(".x-login-loading").hide();
					$.xfm.alert(result.message.info+'('+result.message.code+')');
					return;
				}
				// 存储角色，应用菜单数据到本地session缓存
				localStorage.ROLE = JSON.stringify(result.data.ROLE);
				localStorage.APPLICATION = JSON.stringify(result.data.ROLE.applicationList);
				// 系统信息
				var ROLEObj = {
						  id : 1,
						  name : "核心业务系统",
						  PARTNER_ID : "",
						  BELONG_PLATFORM : result.data.ROLE.belongPlatform,
						  PARTNER_NAME : "",
						  LEVEL : result.data.ROLE.level,
						  token : result.data.ACCESS_TOKEN
 
				}
				window.localStorage.ROLE = JSON.stringify(ROLEObj);
				var user = result.data.USER;
				// 登录者信息
				var obj = {
					  id : result.data.ROLE.id,
					  partnerId : "",
					  partnerName : "",
					  cityCode : "",
					  cityName : "",
					  token : user.rongToken,
					  rongToken : user.rongToken,
					  isLeader : "",
					  mobile : "",
					  teamId : "",
					  userName : user.userName,
					  userId : user.id,
					  issueAuthoFlag : ""		
				}
				window.localStorage.operation = JSON.stringify(obj);
				window.location.href = "./main.html";
			},
			error: function(result) {
				$("#login-info").html(result.message.info);
				btn.button('reset');
				$(_this).attr("disabled", false);
				$(".x-login-loading").hide();
			}  
		});
	});
	
	$("#userName").focus();
	// 回车
	$("body").keydown(function() {
        if (event.keyCode == "13") {
            $('#login_btn').click();
        }
    });
});