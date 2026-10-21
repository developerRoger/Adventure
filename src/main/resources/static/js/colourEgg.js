
var vm = new Vue({
    el: '#feedbackSection',
    data: {
    	nice:false,
        time: 20,
        speak:"",
        roomKey:"",
        currentRoom:"",
        memberCount:0,
        rooms: [],
        roomOpen: false,
        // 敏感词列表，与后端保持一致（匹配到的内容统一替换为 ***）
        sensitiveWords: ['傻逼','傻b','sb','尼玛','草泥马','你妈','妈的','操你','卧槽','fuck','cnm','贱人','婊子','白痴','脑残','去死','狗屎','滚蛋','垃圾','废物','蠢货','蠢猪','艹','cao','神经病','恶心'],
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
                //先获取客户端真实IP并拼接到连接地址，供后端记录日志（解决直连场景IP取不到的问题）
                var ip = '';
                $.ajax({
                    url: '/getIp',
                    type: 'get',
                    async: false,
                    dataType: 'text',
                    success: function (data) { ip = data; }
                });
                var ipParam = ip ? '?ip=' + encodeURIComponent(ip) : '';
                if (location.protocol == 'http:') {
                    _this.wx = "ws:"+ location.host+"/feedback/"+_this.connetId + ipParam;
                }
                if (location.protocol == 'https:') {
                    _this.wx = "wss:"+ location.host+"/feedback/"+_this.connetId + ipParam;
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
                	//收到的聊天消息（再过滤一次兜底，确保任何情况下都不显示敏感词）
                	_this.listFeedback.push({"id":2,"name":date.from,"msg":_this.filterSensitive(date.msg)});
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
        	//已在房间时，需确认后才能重新随机匹配
        	if(_this.currentRoom && !confirm('你当前在房间 ' + _this.currentRoom + '，确定要离开并随机匹配新房间吗？')){
        		return;
        	}
        	_this.nice=true;
        	_this.websocket.send('{"type":"match"}');
        	_this.listFeedback=[];
        	_this.text="正在匹配中，请稍候...";
        },
        //敏感词脱敏：把聊天文字中的敏感词统一替换为 ***
        filterSensitive:function(text){
        	var t = text == null ? '' : text;
        	for(var i=0;i<this.sensitiveWords.length;i++){
        		var w = this.sensitiveWords[i];
        		var re = new RegExp(w, 'gi');
        		t = t.replace(re, '***');
        	}
        	return t;
        },
        //切换房间确认：已在房间且目标不是当前房间时，弹确认框
        confirmSwitchRoom:function(targetRoom){
        	var _this=this;
        	if(_this.currentRoom && _this.currentRoom != targetRoom){
        		return confirm('你当前在房间 ' + _this.currentRoom + '，确定要离开并加入房间 ' + targetRoom + ' 吗？');
        	}
        	return true;
        },
        //输入房间名加入指定房间
        joinRoom:function(){
        	var _this=this;
        	if(_this.roomKey==null||_this.roomKey.trim()==''){
        		alert('请输入房间名');
        		return;
        	}
        	var room = _this.roomKey.trim().toUpperCase();
        	if(!_this.confirmSwitchRoom(room)){
        		return;
        	}
        	_this.nice=true;
        	_this.websocket.send('{"type":"join","room":"'+room+'"}');
        	_this.listFeedback=[];
        },
        //点击浮窗房间列表，直接加入对应房间
        joinByRoom:function(room){
        	var _this=this;
        	if(!room)return;
        	if(!_this.confirmSwitchRoom(room)){
        		return;
        	}
        	_this.roomKey=room;
        	_this.nice=true;
        	_this.websocket.send('{"type":"join","room":"'+room+'"}');
        	_this.listFeedback=[];
        	_this.roomOpen=false;
        },
        //轮询获取当前所有聊天室列表
        loadRooms:function(){
        	var _this=this;
        	$.ajax({
        		url:'/rooms',
        		type:'get',
        		dataType:'json',
        		success:function(data){
        			_this.rooms=data;
        		},
        		error:function(){
        			//静默失败，不打扰用户
        		}
        	});
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
        	//发送前先做敏感词脱敏，自己这边也显示脱敏后的内容
        	var msg = _this.filterSensitive(_this.speak);
        	_this.websocket.send('{"type":"chat","msg":"'+msg+'"}');
        	_this.listFeedback.push({"id":1,"name":"我","msg":msg});
        	_this.speak="";
        	//自动滚动到底部由watch统一处理
        },
        //一键清屏：只清空自己本地显示的聊天记录，不影响房间其他人
        clearScreen:function(){
        	this.listFeedback = [];
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
    watch: {
        //监听聊天记录变化，等Vue渲染完DOM后自动滚动到底部，无需手动下滑
        listFeedback: function () {
            var _this = this;
            _this.$nextTick(function () {
                var el = document.querySelector('#feedbackSection section');
                if (el) {
                    el.scrollTop = el.scrollHeight;
                }
            });
        }
    },
    created: function () {
        var _this = this;
        //生成随机uuid为连接id
        _this.connetId=this.uuid();
        //初始化连接的websocket
        _this.init();
        _this.systemPush();
        //加载聊天室列表并定时刷新
        _this.loadRooms();
        setInterval(function(){ _this.loadRooms(); }, 5000);
    }
});
