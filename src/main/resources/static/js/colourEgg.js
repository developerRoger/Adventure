
var vm = new Vue({
    el: '#feedbackSection',
    data: {
    	nice:false,
        time: 20,
        speak:"",
        websocket: null,
        text:"One's Night",
        wx: null,
        connetId:null,
        arrColor: ['#5dd9ff','#fbe091','#ff0','#b5d8f4','#0f0','#0ff','#83dd57','#fff','#b4f4ff','#ccc','#fff'],
        listFeedback: [],
        listBarrage:[]
    },
    methods: {
        init: function () {
            var _this = this;
            //判断当前浏览器是否支持WebSocket
            if ('WebSocket' in window) {
                if (location.protocol == 'http:') {
                    _this.wx = "ws:"+ location.host+"/feedback/"+_this.connetId;
                }
                if (location.protocol == 'https:') {
                    _this.wx = "wss:"+ location.host+"/feedback/"+_this.connetId;
                }
                console.log(_this.wx);
                try {
                    if (!_this.websocket || _this.websocket.readyState == 3) {
                        _this.websocket = new WebSocket(_this.wx);
                    }
                } catch (e) {
                	console.log("提示您的浏览器不支持消息通知，建议安装谷歌或火狐浏览器");
                }
            }
            else {
            	console.log("提示您的浏览器不支持消息通知，建议安装谷歌或火狐浏览器");
            }


            //连接发生错误的回调方法
            _this.websocket.onerror = function () {
            	console.log('提示连接失效，请重新刷新页面');
            };

            //连接成功建立的回调方法
            _this.websocket.onopen = function (event) {
            	_this.heartbeat();
            	
            };

            //接收到消息的回调方法
            _this.websocket.onmessage = function (event) {
                console.log(event.data);
                var date=_this.param(event.data);
                _this.text=date.msg;
                if(date.code==100){
                	
                }else if(date.code==504){
                	_this.nice=false;
                	
                }else if(date.code==404){
                	 _this.text="小老弟，没人跟你匹配呢。加加油";
                	 _this.listFeedback=[];
                }else if(date.code==666){
                	//有值的情况不给推送
                	if(_this.listFeedback.length==0){
	                 	_this.text=null;
	                	_this.listBarrage.push(date.msg);
	                	_this.systemPush(); 
                	}
                }else{
                	_this.listFeedback.push({"id":2,"msg":_this.text});
                	//设置滚动条一直在最底部
                	$("section").scrollTop($('section')[0].scrollHeight );
                }
                
            };
            //连接关闭的回调方法
            _this.websocket.onclose = function () {
                console.log('websocket close');
                try {
                    //_this.init();
                } catch (e) {
                    console.log(e);
                    console.log('提示连接失效，请重新刷新页面');
                }

            };

            
        },
        //心跳
        heartbeat: function () {
        	var _this=this;
            setInterval(function () {
                if (_this.websocket) {
                	_this.websocket.send('heartbeat');
                }
            }, 100000);
        },
        //连接随机socket
        connet:function(){
        	var _this=this;
        	_this.nice=true;
        	_this.websocket.send('connet');
        	_this.listFeedback=[];
        },
         systemPush:function() {
        	 var _this=this;
        	 if(_this.listBarrage.length==0)return;
        	 var num=_this.listBarrage.length-2;
       	         $('.scroll li').eq(num).css('color',_this.arrColor[parseInt(10*Math.random())]);
       	         $('.scroll li').eq(num).animate({'left':-200},15000,function () {
    	         $('.scroll li').eq(num).css('left','100%');
       	         });
       	         //做个限制
       	         if(_this.listBarrage.length>=10)
       	        	_this.listBarrage=[];
       	     },
        //发送消息
        sendObject:function(){
        	var _this=this;
        	if(_this.speak==null||_this.speak=='')return;
        	_this.websocket.send(_this.speak);
        	_this.listFeedback.push({"id":1,"msg":_this.speak});
        	_this.speak="";
        	//设置滚动条一直在最底部
        	$("section").scrollTop($('section')[0].scrollHeight );
        },
        //解析后端返回传递参数
        param:function(str){
        	var r={};
        	str.replace(/([^={},\s]+)=([^={},\s]*)/g,(m,n,o)=>r[n]=o);
        	return r;
        },
        uuid: function () {
            var s = []; 
            var hexDigits = "0123456789abcdef";
            for (var i = 0; i < 12; i++) {
                s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
            }
            s[14] = "4";  // bits 12-15 of the time_hi_and_version field to 0010
            s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);  // bits 6-7 of the clock_seq_hi_and_reserved to 01
            s[8] = s[13] = s[18] = s[23];
         
            var uuid = s.join("");
            return uuid;
        }
    },
    created: function () {
        var _this = this;
        //生成随机uuid为连接id
        _this.connetId=this.uuid();
        //初始化连接的websocket
        _this.init();
        _this.systemPush();
    }
});