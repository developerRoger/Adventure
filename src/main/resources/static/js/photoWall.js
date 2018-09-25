var vm = new Vue({
	el:'#upload',
	data:{
		showList: true,
		url: null,
		page: {},
		fileText:"",
		name:"",
		nowDate:false
	},
	methods: {
		queryList:function(){
			var _this=this;
		      $.ajax({
			        url: '/adventure/qiniu/list?nowDate='+_this.nowDate,
			        type: 'POST',
			        dataType: 'json',
			        cache: false,
			        data: JSON.stringify(null),
			        success: (res) => {        
			        	_this.page=res;
			        	console.log(res);
			        },	
			        error: function(err) {
			        	console.log("空空如也");
			        }
			      });
			
		},
		changeShow:function(i){
			var _this=this;
			_this.nowDate=i;
			_this.queryList();
		},
		// 上传图片
		onUpload:function(e){
		      var formData = new FormData(); 
		      var file=e.target.files[0];
		      // 判断是否为获取数据，直接返回。
		      if(!file||undefined==file||null==file){
		    	  return;
		      }if(!this.name||undefined==this.name||null==this.name){
		    	  $.xfm.alert("请填写标题哦");
		    	  return;
		      }
		      
		      this.fileText=file.name;
			    formData.append('image',file);
		      $.ajax({
		        url: '/adventure/qiniu/uploadImg?name='+this.name,// 这里是后台接口需要换掉
		        type: 'POST',
		        dataType: 'json',
		        cache: false,
		        data: formData,
		        processData: false,
		        contentType: false,
		        success: (res) => {        
		        	this.url=res.url;
		        	this.fileText="";
		        	this.name="";
		        	$.xfm.alert("上传成功！"+"图片路径:"+res.url);
		        	 this.queryList();
		        },	
		        error: function(err) {
		        	$.xfm.alert("网络错误");
		        }
		      });
		}
	},
	created:function(){
	 var _this=this;
	 _this.queryList();
	 console.log("welcome go  in");
	}
});
