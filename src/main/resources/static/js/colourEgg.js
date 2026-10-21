
var vm = new Vue({
    el: '#feedbackSection',
    data: {
    	nice:false,
        time: 20,
        speak:"",
        roomKey:"",
        currentRoom:"",
        memberCount:0,
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
                var date = JSON.parse(event.data);
                if (date.type == 'system') {
                	//弹幕消息
                	if (date.code == 666) {
                		_this.listBarrage.push(date.msg);
                		_this.systemPush();
                	} else {
                		//加入/离开等系统通知，显示到聊天区
                		_this.listFeedback.push({"id":0,"name":"系统","msg":date.msg});
                		$("section").scrollTop($('section')[0].scrollHeight );
                		//人数变化时同步更新顶部显示
                		if (date.members != null) {
                			_this.memberCount = date.members;
                			_this.text = "已加入房间 " + _this.currentRoom + "，当前房间人数：" + _this.memberCount;
                		}
                	}
                } else if (date.type == 'joined') {
                	//加入房间成功
                	_this.nice = true;
                	_this.currentRoom = date.room;
                	_this.memberCount = date.members;
                	_this.text = date.msg;
                	_this.listFeedback = [];
                } else if (date.type == 'chat') {
                	//收到的聊天消息
                	_this.listFeedback.push({"id":2,"name":date.from,"msg":date.msg});
                	$("section").scrollTop($('section')[0].scrollHeight );
                } else if (date.type == 'error') {
                	_this.text = date.msg;
                	_this.nice = false;
                } else {
                	_this.text = date.msg;
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
                	_this.websocket.send('{"type":"heartbeat"}');
                }
            }, 100000);
        },
        //随机匹配
        connet:function(){
        	var _this=this;
        	_this.nice=true;
        	_this.websocket.send('{"type":"match"}');
        	_this.listFeedback=[];
        	_this.text="正在匹配中，请稍候...";
        },
        //输入key加入指定房间
        joinRoom:function(){
        	var _this=this;
        	if(_this.roomKey==null||_this.roomKey==''){
        		alert('请输入聊天室key');
        		return;
        	}
        	_this.nice=true;
        	_this.websocket.send('{"type":"join","room":"'+_this.roomKey+'"}');
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
        	_this.websocket.send('{"type":"chat","msg":"'+_this.speak+'"}');
        	_this.listFeedback.push({"id":1,"name":"我","msg":_this.speak});
        	_this.speak="";
        	//设置滚动条一直在最底部
        	$("section").scrollTop($('section')[0].scrollHeight );
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
