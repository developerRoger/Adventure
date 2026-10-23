
//将base64字符串转为Uint8Array（canvas压缩图片用）
function _base64ToBytes(dataUrl) {
	var arr = dataUrl.split(',');
	var bstr = atob(arr[1]);
	var n = bstr.length;
	var bytes = new Uint8Array(n);
	for (var i = 0; i < n; i++) {
		bytes[i] = bstr.charCodeAt(i);
	}
	return bytes;
}

// ===== 图片分片加密（ChaCha20 流密码，纯JS实现，无外部依赖） =====
// 目的：图片二进制分片在网络上不再是明文，防止截流后把分片直接拼接还原出图片。
// 方案：发送方每张图片随机生成32字节密钥，随 image_meta 元信息帧（key字段 base64）带给接收方；
//       各分片按固定64KB对齐，每片用独立位置的keystream异或加密，可乱序到达、逐片独立解密。
var IMG_CHUNK_SIZE = 64 * 1024; // 分片大小（与发送分片保持一致，接收端据此计算每片加密偏移）

// ChaCha20 quarter round：对状态x的a/b/c/d四个字做一轮运算（32位无符号环绕）
function _chachaQr(x, a, b, c, d) {
	x[a] = (x[a] + x[b]) | 0; x[d] ^= x[a]; x[d] = ((x[d] << 16) | (x[d] >>> 16)) >>> 0;
	x[c] = (x[c] + x[d]) | 0; x[b] ^= x[c]; x[b] = ((x[b] << 12) | (x[b] >>> 20)) >>> 0;
	x[a] = (x[a] + x[b]) | 0; x[d] ^= x[a]; x[d] = ((x[d] << 8) | (x[d] >>> 24)) >>> 0;
	x[c] = (x[c] + x[d]) | 0; x[b] ^= x[c]; x[b] = ((x[b] << 7) | (x[b] >>> 25)) >>> 0;
}

// 生成 ChaCha20 一个64字节keystream块（IETF变体：counter为32位，nonce为12字节）
function _chachaBlock(key, nonce, counter) {
	var CONST = [0x61707865, 0x3320646e, 0x79622d32, 0x6b206574]; // "expand 32-byte k"
	var state = new Array(16);
	state[0] = CONST[0]; state[1] = CONST[1]; state[2] = CONST[2]; state[3] = CONST[3];
	// key: 32字节 -> 8个字（小端）
	for (var i = 0; i < 8; i++) {
		state[4 + i] = ((key[i * 4] & 0xFF) | ((key[i * 4 + 1] & 0xFF) << 8) | ((key[i * 4 + 2] & 0xFF) << 16) | ((key[i * 4 + 3] & 0xFF) << 24)) >>> 0;
	}
	state[12] = counter >>> 0; // 计数器（低32位）
	// nonce: 12字节 -> 3个字（小端）
	state[13] = ((nonce[0] & 0xFF) | ((nonce[1] & 0xFF) << 8) | ((nonce[2] & 0xFF) << 16) | ((nonce[3] & 0xFF) << 24)) >>> 0;
	state[14] = ((nonce[4] & 0xFF) | ((nonce[5] & 0xFF) << 8) | ((nonce[6] & 0xFF) << 16) | ((nonce[7] & 0xFF) << 24)) >>> 0;
	state[15] = ((nonce[8] & 0xFF) | ((nonce[9] & 0xFF) << 8) | ((nonce[10] & 0xFF) << 16) | ((nonce[11] & 0xFF) << 24)) >>> 0;
	var x = state.slice();
	// 10轮（每轮两次quarter-round循环，共20轮）
	for (var round = 0; round < 10; round++) {
		_chachaQr(x, 0, 4, 8, 12);
		_chachaQr(x, 1, 5, 9, 13);
		_chachaQr(x, 2, 6, 10, 14);
		_chachaQr(x, 3, 7, 11, 15);
		_chachaQr(x, 0, 5, 10, 15);
		_chachaQr(x, 1, 6, 11, 12);
		_chachaQr(x, 2, 7, 8, 13);
		_chachaQr(x, 3, 4, 9, 14);
	}
	// 输出 = 最终状态 + 初始状态，按小端写出64字节
	var out = new Uint8Array(64);
	for (var i = 0; i < 16; i++) {
		var v = (x[i] + state[i]) >>> 0;
		out[i * 4] = v & 0xFF;
		out[i * 4 + 1] = (v >>> 8) & 0xFF;
		out[i * 4 + 2] = (v >>> 16) & 0xFF;
		out[i * 4 + 3] = (v >>> 24) & 0xFF;
	}
	return out;
}

// 生成从字节偏移start起、长度length的keystream（分片加解密按整图字节偏移对齐）
function _chachaKeystream(key, nonce, start, length) {
	var out = new Uint8Array(length);
	var blockIdx = Math.floor(start / 64);
	var inOffset = start % 64;
	var written = 0;
	while (written < length) {
		var block = _chachaBlock(key, nonce, blockIdx);
		var take = Math.min(64 - inOffset, length - written);
		out.set(block.subarray(inOffset, inOffset + take), written);
		written += take;
		blockIdx++;
		inOffset = 0;
	}
	return out;
}

// 生成32字节随机密钥（优先用 crypto.getRandomValues，普通http环境也可用）
function _randomKey32() {
	var key = new Uint8Array(32);
	if (typeof crypto !== 'undefined' && crypto.getRandomValues) {
		crypto.getRandomValues(key);
	} else {
		for (var i = 0; i < 32; i++) key[i] = Math.floor(Math.random() * 256);
	}
	return key;
}

// 字节数组转base64（仅用于传输32字节密钥）
function _bytesToB64(bytes) {
	var bin = '';
	for (var i = 0; i < bytes.length; i++) bin += String.fromCharCode(bytes[i]);
	return btoa(bin);
}

// base64转字节数组（还原32字节密钥）
function _b64ToBytes(str) {
	var bin = atob(str);
	var bytes = new Uint8Array(bin.length);
	for (var i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i);
	return bytes;
}

// 由msgId派生12字节nonce（双方由同一msgId算出，无需额外传输）
function _imgNonce(msgId) {
	var nonce = new Uint8Array(12);
	for (var i = 0; i < 12 && i < msgId.length; i++) {
		nonce[i] = msgId.charCodeAt(i) & 0xFF;
	}
	return nonce;
}

// 对一段分片数据做加密/解密（异或同一段keystream），offset为该分片在整图中的字节偏移
function _cryptChunk(data, key, nonce, offset) {
	var ks = _chachaKeystream(key, nonce, offset, data.length);
	var out = new Uint8Array(data.length);
	for (var i = 0; i < data.length; i++) out[i] = data[i] ^ ks[i];
	return out;
}

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
        // 房间列表浮窗是否已最小化（缩成右上角小角标）
        panelHidden: false,
        // 敏感词列表，与后端保持一致（匹配到的内容统一替换为 ***）
        sensitiveWords: ['傻逼','傻b','sb','尼玛','草泥马','你妈','妈的','操你','卧槽','fuck','cnm','贱人','婊子','白痴','脑残','去死','狗屎','滚蛋','垃圾','废物','蠢货','蠢猪','艹','cao','神经病','恶心'],
        websocket: null,
        // 图片分片接收缓存：msgId -> {chunks, totalChunk, filename, mime, from}
        imageIncoming: {},
        // 点击放大的图片地址
        previewSrc: '',
        // 自定义轻提示内容与确认弹框数据（替代浏览器原生 alert/confirm）
        toastMsg: '',
        toastTimer: null,
        confirmData: null,
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
                    // 指定二进制消息以ArrayBuffer接收，便于图片分片在浏览器端组装
                    if (_this.websocket) {
                        _this.websocket.binaryType = 'arraybuffer';
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
                //二进制帧（图片分片），直接走分片接收组装逻辑
                if (event.data instanceof ArrayBuffer || event.data instanceof Blob) {
                	_this.onBinary(event.data);
                	return;
                }
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
                } else if (date.type == 'image_meta') {
	                	//图片消息元信息：初始化接收缓存，等待后续二进制分片收齐后组装显示（含解密密钥key）
	                	if (date.msgId && date.totalChunk && date.totalChunk > 0) {
	                		var rec = {chunks: [], totalChunk: date.totalChunk, filename: date.filename, mime: date.mime, from: date.from, key: date.key ? _b64ToBytes(date.key) : null};
	                		rec.chunks.length = date.totalChunk;
	                		_this.imageIncoming[date.msgId] = rec;
                		//60秒未收齐自动丢弃，防止接收缓存内存泄漏
                		setTimeout(function () {
                			if (_this.imageIncoming[date.msgId]) {
                				delete _this.imageIncoming[date.msgId];
                			}
                		}, 60000);
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
                	_this.nice = false;
                	//错误提示改用自定义轻提示展示，不再显示到下方红色标题
                	_this.showToast(date.msg);
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
        	var doMatch = function(){
        		_this.nice=true;
        		_this.websocket.send('{"type":"match"}');
        		_this.listFeedback=[];
        		_this.text="正在匹配中，请稍候...";
        	};
        	//已在房间时，需确认后才能重新随机匹配
        	if(_this.currentRoom){
        		_this.confirmBox('你当前在房间 ' + _this.currentRoom + '，确定要离开并随机匹配新房间吗？', doMatch);
        		return;
        	}
        	doMatch();
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
        //切换房间确认：已在房间且目标不是当前房间时，弹自定义确认框，确认后执行onOk
        confirmSwitchRoom:function(targetRoom, onOk){
        	var _this=this;
        	if(_this.currentRoom && _this.currentRoom != targetRoom){
        		_this.confirmBox('你当前在房间 ' + _this.currentRoom + '，确定要离开并加入房间 ' + targetRoom + ' 吗？', onOk);
        		return;
        	}
        	onOk();
        },
        //输入房间名加入指定房间
        joinRoom:function(){
        	var _this=this;
        	if(_this.roomKey==null||_this.roomKey.trim()==''){
        		_this.showToast('请输入房间名');
        		return;
        	}
        	var room = _this.roomKey.trim().toUpperCase();
        	_this.confirmSwitchRoom(room, function(){ _this.doJoin(room); });
        },
        //点击浮窗房间列表，直接加入对应房间
        joinByRoom:function(room){
        	var _this=this;
        	if(!room)return;
        	_this.confirmSwitchRoom(room, function(){
        		_this.roomKey=room;
        		_this.doJoin(room);
        	});
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
        //自定义轻提示（替代原生alert）：居中浮层展示，2.5秒后自动消失，点击可立即关闭
        showToast:function(msg){
        	var _this=this;
        	_this.toastMsg = msg;
        	if(_this.toastTimer) clearTimeout(_this.toastTimer);
        	_this.toastTimer = setTimeout(function(){ _this.toastMsg=''; }, 2500);
        },
        //自定义确认弹框（替代原生confirm）：点击"确定"后执行回调，点击"取消"/遮罩则关闭
        confirmBox:function(message, callback){
        	this.confirmData = {message: message, callback: callback};
        },
        //点击"确定"：关闭弹框并执行确认回调
        confirmOk:function(){
        	var cb = this.confirmData ? this.confirmData.callback : null;
        	this.confirmData = null;
        	if(cb) cb();
        },
        //执行加入房间（切换确认通过后统一调用）
        doJoin:function(room){
        	var _this=this;
        	_this.nice=true;
        	_this.websocket.send('{"type":"join","room":"'+room+'"}');
        	_this.listFeedback=[];
        },
        //打开图片选择框
        openFilePicker:function(){
        	document.getElementById('imgFile').click();
        },
        //选择图片文件：1MB以内直接发送，超过1MB先canvas压缩再发送
        onFileChange:function(e){
        	var _this=this;
        	var file = e.target.files[0];
        	e.target.value=''; //清空，允许下次选择同一张图片
        	if(!file)return;
        	if(file.type.indexOf('image/') != 0){
        		_this.showToast('只能发送图片文件');
        		return;
        	}
        	var filename = _this.filterSensitive(file.name || 'image.jpg');
        	if(file.size <= 1048576){
        		//未超过1MB，原图直接发送
        		_this.sendImage(file, filename);
        	}else{
        		//超过1MB，canvas压缩到1MB以内
        		_this.compressImage(file, function(blob){
        			if(!blob){
        			_this.showToast('图片压缩失败，请换一张图片试试');
        			return;
        		}
        			_this.sendImage(blob, filename);
        		});
        	}
        },
        //canvas压缩图片到1MB以内（长边缩至1280px以内 + 逐步降低jpeg质量）
        compressImage:function(file, callback){
        	var reader = new FileReader();
        	reader.onload = function(e){
        		var img = new Image();
        		img.onload = function(){
        			var MAX_EDGE = 1280; //长边最大1280px，超过先等比缩放
        			var scale = Math.min(1, MAX_EDGE / Math.max(img.width, img.height));
        			var canvas = document.createElement('canvas');
        			canvas.width = Math.max(1, Math.round(img.width * scale));
        			canvas.height = Math.max(1, Math.round(img.height * scale));
        			var ctx = canvas.getContext('2d');
        			ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
        			//逐步降低jpeg质量，直到体积<=1MB（1048576字节）
        			var qualities = [0.8, 0.6, 0.4, 0.3, 0.2];
        			var idx = 0;
        			var tryEncode = function(){
        				if(idx >= qualities.length){ callback(null); return; }
        				var dataUrl = canvas.toDataURL('image/jpeg', qualities[idx]);
        				var bytes = _base64ToBytes(dataUrl);
        				if(bytes.byteLength <= 1048576){
        					callback(new Blob([bytes], {type: 'image/jpeg'}));
        				}else{
        					idx++;
        					tryEncode();
        				}
        			};
        			tryEncode();
        		};
        		img.onerror = function(){ callback(null); };
        		img.src = e.target.result;
        	};
        	reader.readAsDataURL(file);
        },
        //发送图片：先发文本帧(image_meta)，再按64KB/片发二进制帧（32字节msgId + 4字节大端片序号 + 加密数据）
        sendImage:function(blob, filename){
        	var _this=this;
        	if(!_this.websocket || _this.websocket.readyState != 1){
        		_this.showToast('连接已断开，请刷新页面重试');
        		return;
        	}
        	if(!_this.currentRoom){
        		_this.showToast('请先加入房间再发送图片');
        		return;
        	}
        	//msgId仅含ASCII字符，便于二进制帧头固定32字节解析（不足部分以0填充）
        	var msgId = 'IMG' + Date.now().toString(36) + Math.random().toString(36).substr(2, 6);
        	//图片分片加密：每张图随机32字节密钥（随元信息帧携带）+ msgId派生nonce，分片逐片ChaCha20加密
        	var imgKey = _randomKey32();
        	var nonce = _imgNonce(msgId);
        	var reader = new FileReader();
        	reader.onload = function(e){
        		var buf = e.target.result;
        		var total = buf.byteLength;
        		var CHUNK = IMG_CHUNK_SIZE; //64KB/片
        		var totalChunk = Math.max(1, Math.ceil(total / CHUNK));
        		var mime = blob.type || 'image/jpeg';
        		//1. 文本帧：图片元信息（含密钥key字段，接收方据此解密）
        		_this.websocket.send(JSON.stringify({msgId: msgId, type: 'image_meta', filename: filename, mime: mime, totalChunk: totalChunk, enc: true, key: _bytesToB64(imgKey)}));
        		//2. 二进制帧：逐片加密后发送
        		var headBytes = new Uint8Array(32);
        		for(var i=0; i<msgId.length && i<32; i++){
        			headBytes[i] = msgId.charCodeAt(i);
        		}
        		for(var i=0; i<totalChunk; i++){
        			var start = i * CHUNK;
        			var end = Math.min(start + CHUNK, total);
        			// 对整图字节偏移start处的本片数据加密（长度不变，帧头明文仅用于路由）
        			var enc = _cryptChunk(new Uint8Array(buf, start, end - start), imgKey, nonce, start);
        			var frame = new Uint8Array(36 + enc.length);
        			frame.set(headBytes, 0);
        			frame[32] = (i >> 24) & 0xFF;
        			frame[33] = (i >> 16) & 0xFF;
        			frame[34] = (i >> 8) & 0xFF;
        			frame[35] = i & 0xFF;
        			frame.set(enc, 36);
        			_this.websocket.send(frame.buffer);
        		}
        		//3. 本地回显压缩后的图片（后端广播不包含发送者自己）
        		_this.listFeedback.push({id: 3, name: '我', img: URL.createObjectURL(blob), mime: mime, filename: filename});
        	};
        	reader.readAsArrayBuffer(blob);
        },
        //接收二进制帧：解析msgId+片序号，全部收齐后由前端组装完整图片并显示
        onBinary:function(data){
        	var _this=this;
        	if(data instanceof Blob){
        		//个别浏览器binaryType失效时Blob回退，转成ArrayBuffer处理
        		var br = new FileReader();
        		br.onload = function(){ _this.onBinary(br.result); };
        		br.readAsArrayBuffer(data);
        		return;
        	}
        	var arr = new Uint8Array(data);
        	if(arr.length <= 36)return;
        	//解析前32字节msgId（0截断）
        	var msgId = '';
        	for(var i=0; i<32; i++){
        		if(arr[i] === 0)break;
        		msgId += String.fromCharCode(arr[i]);
        	}
        	var chunkIndex = ((arr[32] & 0xFF) << 24) | ((arr[33] & 0xFF) << 16) | ((arr[34] & 0xFF) << 8) | (arr[35] & 0xFF);
	        	var rec = _this.imageIncoming[msgId];
	        	if(!rec)return; //元信息未到或已超时
	        	if(chunkIndex < 0 || chunkIndex >= rec.totalChunk)return;
	        	var chunkData = arr.subarray(36);
	        	//分片加密：按64KB对齐偏移逐片ChaCha20解密（无key则为旧协议明文分片，直接使用）
	        	if(rec.key){
	        		chunkData = _cryptChunk(chunkData, rec.key, _imgNonce(msgId), chunkIndex * IMG_CHUNK_SIZE);
	        	}
	        	rec.chunks[chunkIndex] = chunkData;
        	//统计已收到的分片数，全部收齐后组装为Blob显示
        	var received = 0;
        	for(var k=0; k<rec.totalChunk; k++){
        		if(rec.chunks[k])received++;
        	}
        	if(received >= rec.totalChunk){
        		var full = new Blob(rec.chunks, {type: rec.mime});
        		var url = URL.createObjectURL(full);
        		_this.listFeedback.push({id: 4, name: rec.from, img: url, mime: rec.mime, filename: rec.filename});
        		delete _this.imageIncoming[msgId];
        	}
        },
        //滚动聊天区到底部（文本、图片消息通用）
        scrollToBottom:function(){
        	var el = document.querySelector('#feedbackSection section');
        	if (el) {
        		el.scrollTop = el.scrollHeight;
        	}
        },
        //图片加载完成回调：图片是异步加载的，加载后才会撑开布局，此时重新滚动到底部
        onImgLoad:function(){
        	this.scrollToBottom();
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
                _this.scrollToBottom();
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
