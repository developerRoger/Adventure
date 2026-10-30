
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

// ===== 媒体消息尺寸上限（字节） =====
var IMG_MAX = 1 * 1024 * 1024;          // 普通图片：≤1MB原样，>1MB canvas压缩
var GIF_MAX = 3 * 1024 * 1024;          // GIF动图：≤3MB原样展示，>3MB拦截
var VIDEO_RAW_MAX = 10 * 1024 * 1024;   // 视频：≤10MB原样
var VIDEO_COMPRESS_MAX = 20 * 1024 * 1024; // 视频：>20MB拦截，11~20MB压缩
// 聊天区最多保留的消息条数，超出丢弃最旧消息并释放其图片/视频的 Blob 对象URL，防止长时间聊天内存只增不减
var FEEDBACK_MAX = 300;

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
        // 媒体分片接收缓存：msgId -> {chunks, totalChunk, filename, mime, from, key, retried}
        imageIncoming: {},
        // 已发送媒体缓存：msgId -> {buf,key,nonce,filename,mime,totalChunk,CHUNK}，用于丢包重传
        pendingSend: {},
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
                } else if (date.type == 'media_end') {
                	//媒体分片发送完成标记：校验是否有缺失分片，缺失则发起丢包重传
                	_this.onMediaEnd(date);
                } else if (date.type == 'retrans') {
                	//收到丢包重传请求（自己作为发送者）：重发请求中指定的缺失分片
                	_this.onRetrans(date);
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
        //释放某条消息里图片/视频的 Blob 对象URL（防止长时间聊天导致内存只增不减）
        revokeMediaUrl:function(item){
            if(!item)return;
            if(item.img && item.img.indexOf('blob:')==0){ try{ URL.revokeObjectURL(item.img); }catch(e){} }
            if(item.video && item.video.indexOf('blob:')==0){ try{ URL.revokeObjectURL(item.video); }catch(e){} }
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
        //选择图片文件：GIF动图≤3MB原样发送(保留动画)，普通图≤1MB原样、>1MB先canvas压缩再发送
        onFileChange:function(e){
        	var _this=this;
        	var file = e.target.files[0];
        	e.target.value=''; //清空，允许下次选择同一张图片
        	if(!file)return;
        	if(file.type.indexOf('image/') != 0){
        		_this.showToast('只能发送图片文件');
        		return;
        	}
        	var filename = _this.filterSensitive(file.name || 'image.gif');
        	if(file.type === 'image/gif'){
        		//GIF动图：≤3MB原样发送展示动画，>3MB直接拦截
        		if(file.size > GIF_MAX){
        			_this.showToast('GIF动图不能超过3MB');
        			return;
        		}
        		_this.sendMedia(file, filename, file.type);
        		return;
        	}
        	if(file.size <= IMG_MAX){
        		//未超过1MB，原图直接发送
        		_this.sendMedia(file, filename, file.type);
        	}else{
        		//超过1MB，canvas压缩到1MB以内
        		_this.compressImage(file, function(blob){
        			if(!blob){
        				_this.showToast('图片压缩失败，请换一张图片试试');
        				return;
        			}
        			_this.sendMedia(blob, filename, blob.type || 'image/jpeg');
        		});
        	}
        },
        //打开视频选择框
        openVideoPicker:function(){
        	document.getElementById('videoFile').click();
        },
        //选择视频文件：≤10MB原样发送，11~20MB压缩后发送，>20MB直接拦截
        onVideoFileChange:function(e){
        	var _this=this;
        	var file = e.target.files[0];
        	e.target.value=''; //清空，允许下次选择同一视频
        	if(!file)return;
        	if(file.type.indexOf('video/') != 0){
        		_this.showToast('只能发送视频文件');
        		return;
        	}
        	var filename = _this.filterSensitive(file.name || 'video.mp4');
        	if(file.size > VIDEO_COMPRESS_MAX){
        		_this.showToast('视频不能超过20MB');
        		return;
        	}
        	if(file.size <= VIDEO_RAW_MAX){
        		//≤10MB原样发送
        		_this.sendMedia(file, filename, file.type);
        	}else{
        		//11~20MB：压缩到640×360@400~600kbps后发送
        		_this.showToast('视频较大，正在压缩，请稍候...');
        		_this.compressVideo(file, function(blob){
        			if(!blob){
        				_this.showToast('视频压缩失败，请更换视频试试');
        				return;
        			}
        			_this.sendMedia(blob, filename, blob.type || 'video/webm');
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
        //压缩视频：canvas强制640×360 + MediaRecorder目标码率500kbps(400~600范围)，合成原音频轨
        compressVideo:function(file, callback){
        	var url = URL.createObjectURL(file);
        	var video = document.createElement('video');
        	video.src = url;
        	video.muted = true; //静音播放以满足自动播放策略，音频轨仍从captureStream取得
        	video.playsInline = true;
        	var canvas = document.createElement('canvas');
        	canvas.width = 640; canvas.height = 360;
        	var ctx = canvas.getContext('2d');
        	var rafId = null, rec = null, chunks = [];
        	var onReady = function(){
        		var videoStream = video.captureStream ? video.captureStream() : null;
        		var canvasStream = canvas.captureStream ? canvas.captureStream(30) : null;
        		var tracks = [];
        		if(canvasStream && canvasStream.getVideoTracks().length) tracks.push(canvasStream.getVideoTracks()[0]);
        		if(videoStream && videoStream.getAudioTracks().length) tracks.push(videoStream.getAudioTracks()[0]);
        		var stream = new MediaStream(tracks);
        		var mime = 'video/webm;codecs=vp8,opus';
        		if(MediaRecorder.isTypeSupported && !MediaRecorder.isTypeSupported(mime)) mime = 'video/webm';
        		if(!MediaRecorder.isTypeSupported || MediaRecorder.isTypeSupported(mime)){
        			rec = new MediaRecorder(stream, {mimeType: mime, videoBitsPerSecond: 500000, audioBitsPerSecond: 64000});
        		}else{
        			rec = new MediaRecorder(stream);
        		}
        		rec.ondataavailable = function(ev){ if(ev.data && ev.data.size>0) chunks.push(ev.data); };
        		rec.onstop = function(){
        			cancelAnimationFrame(rafId);
        			if(stream) stream.getTracks().forEach(function(t){ t.stop(); });
        			var blob = new Blob(chunks, {type: rec.mimeType || 'video/webm'});
        			URL.revokeObjectURL(url);
        			callback(blob);
        		};
        		rec.start(200);
        		video.play();
        		//逐帧把视频画到640×360画布，让canvas流携带缩放后的画面
        		var draw = function(){
        			try{ ctx.drawImage(video, 0, 0, 640, 360); }catch(err){}
        			rafId = requestAnimationFrame(draw);
        		};
        		rafId = requestAnimationFrame(draw);
        	};
        	video.onloadedmetadata = function(){ video.currentTime = 0; onReady(); };
        	video.onended = function(){ if(rec && rec.state != 'inactive'){ try{ rec.stop(); }catch(err){} } };
        	video.onerror = function(){
        		cancelAnimationFrame(rafId);
        		URL.revokeObjectURL(url);
        		callback(null);
        	};
        },
        //发送媒体(图片/GIF/视频)：元信息帧 + 64KB加密分片二进制帧 + 结束标记，缓存用于丢包重传
        sendMedia:function(blob, filename, mime){
        	var _this=this;
        	if(!_this.websocket || _this.websocket.readyState != 1){
        		_this.showToast('连接已断开，请刷新页面重试');
        		return;
        	}
        	if(!_this.currentRoom){
        		_this.showToast('请先加入房间再发送');
        		return;
        	}
        	//msgId仅含ASCII字符，便于二进制帧头固定32字节解析（不足部分以0填充）
        	var prefix = (mime && mime.indexOf('video/')==0) ? 'VID' : ((mime=='image/gif') ? 'GIF' : 'IMG');
        	var msgId = prefix + Date.now().toString(36) + Math.random().toString(36).substr(2, 6);
        	//分片加密：每条媒体随机32字节密钥（随元信息帧携带）+ msgId派生nonce，分片逐片ChaCha20加密
        	var mediaKey = _randomKey32();
        	var nonce = _imgNonce(msgId);
        	var reader = new FileReader();
        	reader.onload = function(e){
        		var buf = e.target.result;
        		var total = buf.byteLength;
        		var CHUNK = IMG_CHUNK_SIZE; //64KB/片
        		var totalChunk = Math.max(1, Math.ceil(total / CHUNK));
        		var mmime = mime || blob.type || 'application/octet-stream';
        		//1. 缓存整份数据，用于后续丢包重传（60秒后释放）
        		var pending = {buf: buf, key: mediaKey, nonce: nonce, filename: filename, mime: mmime, totalChunk: totalChunk, CHUNK: CHUNK};
        		_this.pendingSend[msgId] = pending;
        		setTimeout(function(){ delete _this.pendingSend[msgId]; }, 60000);
        		//2. 文本帧：媒体元信息（含密钥key字段，接收方据此解密）
        		_this.websocket.send(JSON.stringify({msgId: msgId, type: 'image_meta', filename: filename, mime: mmime, totalChunk: totalChunk, enc: true, key: _bytesToB64(mediaKey)}));
        		//3. 二进制帧：逐片加密后全部发送
        		_this.sendChunkRange(msgId, pending, 0, totalChunk - 1);
        		//4. 结束标记：通知接收方校验缺失分片并发起丢包重传
        		_this.websocket.send(JSON.stringify({type: 'media_end', msgId: msgId}));
        		//5. 本地回显媒体（后端广播不包含发送者自己）
        		_this.echoMedia(mmime, filename, blob);
        	};
        	reader.readAsArrayBuffer(blob);
        },
        //发送某段片序号闭区间[from,to]的加密分片（初次全量发送与丢包重传共用）
        sendChunkRange:function(msgId, pending, from, to){
        	var _this=this;
        	if(!_this.websocket || _this.websocket.readyState != 1) return;
        	var buf = pending.buf;
        	var total = buf.byteLength;
        	var CHUNK = pending.CHUNK;
        	var headBytes = new Uint8Array(32);
        	for(var i=0; i<msgId.length && i<32; i++){ headBytes[i] = msgId.charCodeAt(i); }
        	for(var idx=from; idx<=to; idx++){
        		var start = idx * CHUNK;
        		var end = Math.min(start + CHUNK, total);
        		//对整媒体字节偏移start处的本片数据加密（长度不变，帧头明文仅用于路由）
        		var enc = _cryptChunk(new Uint8Array(buf, start, end - start), pending.key, pending.nonce, start);
        		var frame = new Uint8Array(36 + enc.length);
        		frame.set(headBytes, 0);
        		frame[32] = (idx >> 24) & 0xFF;
        		frame[33] = (idx >> 16) & 0xFF;
        		frame[34] = (idx >> 8) & 0xFF;
        		frame[35] = idx & 0xFF;
        		frame.set(enc, 36);
        		_this.websocket.send(frame.buffer);
        	}
        },
        //本地回显媒体：按mime区分视频/图片生成对应消息项
        echoMedia:function(mime, filename, blob){
        	var _this=this;
        	var url = URL.createObjectURL(blob);
        	if(mime && mime.indexOf('video/')==0){
        		_this.listFeedback.push({id: 5, name: '我', video: url, mime: mime, filename: filename});
        	}else{
        		_this.listFeedback.push({id: 3, name: '我', img: url, mime: mime, filename: filename});
        	}
        },
        //接收二进制帧：解析msgId+片序号，全部收齐后由前端组装完整媒体并显示
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
        		_this.assembleMedia(msgId, rec);
        	}
        },
        //组装完整媒体：按mime区分视频/图片生成Blob并显示
        assembleMedia:function(msgId, rec){
        	var _this=this;
        	var full = new Blob(rec.chunks, {type: rec.mime});
        	var url = URL.createObjectURL(full);
        	if(rec.mime && rec.mime.indexOf('video/')==0){
        		_this.listFeedback.push({id: 6, name: rec.from, video: url, mime: rec.mime, filename: rec.filename});
        	}else{
        		_this.listFeedback.push({id: 4, name: rec.from, img: url, mime: rec.mime, filename: rec.filename});
        	}
        	delete _this.imageIncoming[msgId];
        },
        //收到media_end：校验该msgId是否有缺失分片，缺失则发起丢包重传
        onMediaEnd:function(date){
        	var _this=this;
        	var rec = _this.imageIncoming[date.msgId];
        	if(!rec)return;
        	var missing = [];
        	for(var i=0;i<rec.totalChunk;i++){ if(!rec.chunks[i]) missing.push(i); }
        	if(missing.length>0){
        		_this.requestRetrans(date.msgId, rec, missing);
        	}else{
        		_this.assembleMedia(date.msgId, rec);
        	}
        },
        //请求重传缺失分片（最多重试3次，间隔1.5秒，仍缺失则放弃）
        requestRetrans:function(msgId, rec, missing){
        	var _this=this;
        	if(rec.retried == null) rec.retried = 0;
        	if(rec.retried >= 3) return;
        	rec.retried++;
        	if(_this.websocket && _this.websocket.readyState == 1){
        		_this.websocket.send(JSON.stringify({type:'retrans', msgId: msgId, chunks: missing}));
        	}
        	//定时再次校验（等待重传分片到达）
        	setTimeout(function(){
        		var r = _this.imageIncoming[msgId];
        		if(!r) return;
        		var miss2 = [];
        		for(var i=0;i<r.totalChunk;i++){ if(!r.chunks[i]) miss2.push(i); }
        		if(miss2.length>0){
        			_this.requestRetrans(msgId, r, miss2);
        		}else{
        			_this.assembleMedia(msgId, r);
        		}
        	}, 1500);
        },
        //收到retrans：作为发送者，重发请求中指定的缺失分片
        onRetrans:function(date){
        	var _this=this;
        	var pending = _this.pendingSend[date.msgId];
        	if(!pending) return;
        	var chunks = date.chunks;
        	if(!chunks || chunks.length==0) return;
        	for(var i=0;i<chunks.length;i++){
        		var idx = chunks[i];
        		if(idx < 0 || idx >= pending.totalChunk) continue;
        		_this.sendChunkRange(date.msgId, pending, idx, idx);
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
        listFeedback: function (newVal, oldVal) {
            var _this = this;
            _this.$nextTick(function () {
                _this.scrollToBottom();
            });
            // 清屏（整体替换数组）时，释放旧消息里图片/视频的 Blob 对象URL，避免内存只增不减
            if (oldVal && oldVal !== newVal) {
                for (var i = 0; i < oldVal.length; i++) {
                    _this.revokeMediaUrl(oldVal[i]);
                }
            }
            // 消息条数上限：超出丢弃最旧消息并释放其媒体对象URL
            while (_this.listFeedback.length > FEEDBACK_MAX) {
                _this.revokeMediaUrl(_this.listFeedback.shift());
            }
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
