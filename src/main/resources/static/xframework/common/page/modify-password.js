var modifyPasswordVM = new Vue({
	el : "#modifyPassword",
	data:{
		modifyObj:{
			newPassword:''
		},
		btnFlag:false
	},
	methods:{
		submit: function() {
			var _this = this;
			if(_this.btnFlag || !Vue.validate())
				return;
			if(_this.modifyObj.password !== _this.modifyObj.passwordConfirm){
				$.xfm.alert('两次密码输入不一致，请重新输入');
				return;
			}
			
			_this.btnFlag = true;
			var user = JSON.parse(localStorage.operation);
			_this.modifyObj.userName = user.userName;
			
			$.xfm.post({
				url : "/openapi/sysmanage/user/updatePassword",
				data : _this.modifyObj,
				success : function(result) {
					_this.btnFlag = false;
					if(result.state == 0){
						$.xfm.alert(result.message.info+'('+result.message.code+')');
						return;
					}
					$.xfm.alert('修改密码成功');
					_this.modifyObj = {};
				}
			});
		}
	},
	created: function(){
		console.log(window.document.cookie)
	}	
})
