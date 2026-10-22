package com.firstTry.Adventure.middleware;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.firstTry.Adventure.entity.WebSocketVisitLogEntity;
import com.firstTry.Adventure.service.WebSocketVisitLogService;

/**
 * 所有websocket的终类（多人房间制聊天室）
 * 
 * 支持两种加入方式：
 * 1. 指定房间key加入：发送 {"type":"join","room":"房间key"}
 * 2. 随机匹配：发送 {"type":"match"}，服务端自动将其加入一个未满的房间
 * 
 * @author Roger
 * @date 2018/11/9
 */
@ServerEndpoint(value = "/feedback/{sid}", configurator = WebSocketConfigurator.class)
@Component
public class WebSocketServer {

	static Log log = LogFactory.getLog(WebSocketServer.class);

	// websocket访问记录服务（记录IP、进入时间、房间名）
	@Autowired
	private WebSocketVisitLogService webSocketVisitLogService;
	// 单个房间最大人数
	private static final int MAX_ROOM_SIZE = 50;

	// 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
	private static int onlineCount = 0;
	// concurrent包的线程安全Map，用来存放每个客户端对应的Session对象。
	private static Map<String, Session> webSocketSet = new ConcurrentHashMap<>();
	// 用户sid -> 所在房间key
	private static Map<String, String> userRoomMap = new ConcurrentHashMap<>();
	// 房间key -> 房间内成员sid集合
	private static Map<String, Set<String>> roomMap = new ConcurrentHashMap<>();
	// 等待随机匹配的用户sid队列
	private static List<String> waitingQueue = Collections.synchronizedList(new ArrayList<String>());

	// ===== 图片消息分片转发（二进制帧） =====
	// 二进制帧头：前32字节msgId + 后4字节片序号(大端)，帧总开销36字节
	private static final int MSG_ID_LEN = 32;
	private static final int BIN_HEADER_LEN = 36;
	// 单图上限1MB、分片64KB约16片，此处留余量
	private static final int MAX_IMAGE_CHUNK = 20;
	// 图片传输上下文：msgId -> 传输信息（发送者、总片数、转发目标），仅存元数据，不缓存图片数据
	private static Map<String, ImageTransfer> imageTransferMap = new ConcurrentHashMap<>();

	/**
	 * 一次图片传输的上下文信息
	 */
	private static class ImageTransfer {
		String sid; // 发送者sid
		int totalChunk; // 总片数
		Set<String> targets; // 转发目标（房间内除发送者外的人）
		ImageTransfer(String sid, int totalChunk, Set<String> targets) {
			this.sid = sid;
			this.totalChunk = totalChunk;
			this.targets = targets;
		}
	}

	// 随机房间key使用的字符集（去掉易混淆的 0/O、1/I/L）
	private static final String ROOM_KEY_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";

	// 敏感词/辱骂词列表，匹配到的内容统一替换为 ***
	private static final String[] SENSITIVE_WORDS = {
		"傻逼", "傻b", "sb", "尼玛", "草泥马", "你妈", "妈的", "操你", "卧槽",
		"fuck", "cnm", "贱人", "婊子", "白痴", "脑残", "去死", "狗屎", "滚蛋",
		"垃圾", "废物", "蠢货", "蠢猪", "艹", "cao", "神经病", "恶心"
	};

	/**
	 * 敏感词脱敏：将文本中出现的敏感词统一替换为 ***
	 * 
	 * @param text
	 * @return
	 */
	private static String filterSensitive(String text) {
		if (text == null || text.length() == 0) {
			return text;
		}
		String result = text;
		for (String word : SENSITIVE_WORDS) {
			// Pattern.quote 避免正则特殊字符，(?i) 忽略大小写（针对英文敏感词）
			result = result.replaceAll("(?i)" + Pattern.quote(word), Matcher.quoteReplacement(mask(word)));
		}
		return result;
	}

	/**
	 * 敏感词统一替换为固定三个星号
	 * 
	 * @param word
	 * @return
	 */
	private static String mask(String word) {
		return "***";
	}

	/**
	 * 生成6位短房间key，方便用户口头/手动输入
	 * 
	 * @return
	 */
	private static String generateRoomKey() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			sb.append(ROOM_KEY_CHARS.charAt((int) (Math.random() * ROOM_KEY_CHARS.length())));
		}
		return sb.toString();
	}

	/**
	 * 查询当前所有聊天室（房间key + 人数），供前端浮窗展示
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> getRoomList() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (Map.Entry<String, Set<String>> entry : roomMap.entrySet()) {
			Map<String, Object> room = new java.util.HashMap<String, Object>();
			room.put("room", entry.getKey());
			room.put("members", entry.getValue().size());
			list.add(room);
		}
		return list;
	}

	/**
	 * 连接建立成功调用的方法
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam("sid") String sid) {
		webSocketSet.put(sid, session); // 加入put中
		// 调大二进制消息缓冲，保证64KB图片分片（含36字节帧头）能整帧收到，无需服务端拼接
		session.setMaxBinaryMessageBufferSize(128 * 1024);
		addOnlineCount(); // 在线数加1
		// 记录访问日志：IP、进入时间、sid
		recordVisit(sid, session);
		log.info("有新窗口开始监听:" + sid + ",当前在线人数为" + getOnlineCount());
		try {
			JSONObject obj = new JSONObject();
			obj.put("code", 100);
			obj.put("type", "system");
			obj.put("msg", "连接成功");
			obj.put("sid", sid);
			sendMessage(obj.toJSONString(), session);
		} catch (IOException e) {
			log.error("websocket IO异常");
		}
	}

	/**
	 * 记录websocket访问日志（IP、进入时间、连接标识）
	 * IP从握手阶段由WebSocketConfigurator解析后存放在session的userProperties中
	 * 
	 * @param sid
	 * @param session
	 */
	private void recordVisit(String sid, Session session) {
		try {
			WebSocketVisitLogEntity entity = new WebSocketVisitLogEntity();
			entity.setSid(sid);
			// 从握手阶段存入的userProperties中取客户端IP
			String clientIp = session.getUserProperties().get("clientIp") == null ? null
					: session.getUserProperties().get("clientIp").toString();
			entity.setIp(clientIp);
			entity.setEnterTime(new Date());
			webSocketVisitLogService.save(entity);
			log.info("websocket访问记录已保存, sid=" + sid + ", ip=" + clientIp);
		} catch (Exception e) {
			log.error("保存websocket访问记录失败, sid=" + sid, e);
		}
	}

	/**
	 * 用户加入房间后，回填访问记录中的房间名
	 * 
	 * @param sid
	 * @param roomKey
	 */
	private void updateVisitRoom(String sid, String roomKey) {
		try {
			if (roomKey != null) {
				webSocketVisitLogService.updateRoomName(sid, roomKey);
			}
		} catch (Exception e) {
			log.error("回填websocket访问记录房间名失败, sid=" + sid, e);
		}
	}

	/**
	 * 连接关闭调用的方法
	 */
	@OnClose
	public void onClose(@PathParam("sid") String sid) {
		webSocketSet.remove(sid); // 从set中删除
		leaveRoom(sid, true);
		subOnlineCount(); // 在线数减1
		log.info("有一连接关闭！当前在线人数为" + getOnlineCount());
	}

	/**
	 * 收到客户端消息后调用的方法
	 *
	 * @param message
	 *            客户端发送过来的消息
	 */
	@OnMessage
	public void onMessage(String message, Session session, @PathParam("sid") String sid) {
		log.info("收到来自窗口" + sid + "的信息:" + message);
		try {
			if (message == null || message.trim().length() == 0) {
				return;
			}
			String type = null;
			String roomKey = null;
			String content = null;
			String msgId = null;
			int totalChunk = 0;
			JSONObject json = null;
			// 兼容旧协议：纯文本 "connet" 视为随机匹配，"heartbeat" 为心跳，其余视为聊天内容
			if (message.startsWith("{")) {
				json = JSON.parseObject(message);
				type = json.getString("type");
				roomKey = json.getString("room");
				content = json.getString("msg");
				msgId = json.getString("msgId");
				totalChunk = json.getIntValue("totalChunk");
			} else {
				if ("connet".equals(message)) {
					type = "match";
				} else if ("heartbeat".equals(message)) {
					type = "heartbeat";
				} else {
					type = "chat";
					content = message;
				}
			}

			if ("match".equals(type)) {
				// 随机匹配
				randomMatch(sid);
			} else if ("join".equals(type)) {
				// 指定房间名加入房间
				if (roomKey == null || roomKey.trim().length() == 0) {
					sendError(sid, "房间名不能为空");
				} else {
					joinRoom(sid, roomKey.trim().toUpperCase());
				}
			} else if ("chat".equals(type)) {
				// 聊天消息，广播给房间内其他人
				sendChat(sid, content == null ? "" : content);
			} else if ("image_meta".equals(type)) {
				// 图片消息元信息（文本帧）：登记分片转发上下文，后续二进制分片逐片实时转发
				forwardImageMeta(sid, msgId, json, totalChunk);
			} else if ("heartbeat".equals(type)) {
				log.info("用户心跳:" + sid + ",当前在线人数为" + getOnlineCount());
			} else {
				sendError(sid, "无法识别的消息类型");
			}
		} catch (Exception e) {
			log.error("处理消息异常：" + e.getMessage(), e);
			try {
				sendError(sid, "处理消息异常");
			} catch (Exception ex) {
				log.error("推送错误消息失败");
			}
		}
	}

	/**
	 * 指定key加入房间，房间不存在则创建
	 * 
	 * @param sid
	 * @param roomKey
	 */
	private synchronized void joinRoom(String sid, String roomKey) {
		// 房间名长度限制（6位短码）
		if (roomKey.length() > 6) {
			sendError(sid, "房间名最多 6 位，请重新输入");
			return;
		}
		// 房间名敏感词脱敏，防止恶意房间名
		roomKey = filterSensitive(roomKey);
		// 已在目标房间，直接返回
		String oldRoom = userRoomMap.get(sid);
		if (roomKey.equals(oldRoom)) {
			sendSystem(sid, "你已在房间 " + roomKey + " 中", roomKey);
			return;
		}
		// 先离开旧房间
		leaveRoom(sid, true);

		Set<String> members = roomMap.get(roomKey);
		if (members != null && members.size() >= MAX_ROOM_SIZE) {
			sendError(sid, "房间 " + roomKey + " 已满，最多 " + MAX_ROOM_SIZE + " 人");
			return;
		}
		addToRoom(sid, roomKey);
	}

	/**
	 * 随机匹配：优先加入未满房间，否则创建新房间等待
	 * 
	 * @param sid
	 */
	private synchronized void randomMatch(String sid) {
		// 先离开旧房间
		leaveRoom(sid, true);
		waitingQueue.remove(sid);
		waitingQueue.add(sid);

		// 找一个未满的房间加入（为避免所有人都挤进同一个房间，优先选择等待队列所在房间）
		String targetRoom = null;
		// 先看等待队列其他人所在的房间
		for (String w : waitingQueue) {
			if (w.equals(sid)) {
				continue;
			}
			String wRoom = userRoomMap.get(w);
			if (wRoom != null && roomMap.get(wRoom) != null && roomMap.get(wRoom).size() < MAX_ROOM_SIZE) {
				targetRoom = wRoom;
				break;
			}
		}
		// 再找任意未满房间
		if (targetRoom == null) {
			for (Map.Entry<String, Set<String>> entry : roomMap.entrySet()) {
				if (entry.getValue().size() < MAX_ROOM_SIZE) {
					targetRoom = entry.getKey();
					break;
				}
			}
		}
		// 都满了或没有房间，创建新房间（6位短key）
		if (targetRoom == null) {
			targetRoom = generateRoomKey();
		}
		waitingQueue.remove(sid);
		addToRoom(sid, targetRoom);
	}

	/**
	 * 将用户加入房间并广播
	 * 
	 * @param sid
	 * @param roomKey
	 */
	private void addToRoom(String sid, String roomKey) {
		Set<String> members = roomMap.computeIfAbsent(roomKey, k -> new CopyOnWriteArraySet<String>());
		members.add(sid);
		userRoomMap.put(sid, roomKey);
		// 回填访问记录的房间名
		updateVisitRoom(sid, roomKey);
		// 通知房间内其他人
		JSONObject sysMsg = new JSONObject();
		sysMsg.put("code", 200);
		sysMsg.put("type", "system");
		sysMsg.put("msg", "用户 " + sid + " 加入了房间 " + roomKey + "，当前房间人数：" + members.size());
		sysMsg.put("room", roomKey);
		sysMsg.put("members", members.size());
		broadcast(roomKey, sid, sysMsg.toJSONString());
		// 通知自己加入成功
		JSONObject selfMsg = new JSONObject();
		selfMsg.put("code", 200);
		selfMsg.put("type", "joined");
		selfMsg.put("msg", "已加入房间 " + roomKey + "，当前房间人数：" + members.size());
		selfMsg.put("room", roomKey);
		selfMsg.put("members", members.size());
		sendInfo(selfMsg.toJSONString(), sid);
	}

	/**
	 * 离开房间
	 * 
	 * @param sid
	 * @param notifyOthers 是否通知房间内其他人
	 */
	private void leaveRoom(String sid, boolean notifyOthers) {
		// 清理该用户发起的进行中的图片传输（接收者则从转发目标中移除）
		cleanupImageTransfers(sid);
		String roomKey = userRoomMap.remove(sid);
		if (roomKey == null) {
			return;
		}
		Set<String> members = roomMap.get(roomKey);
		if (members != null) {
			members.remove(sid);
			if (members.isEmpty()) {
				roomMap.remove(roomKey);
			} else if (notifyOthers) {
				JSONObject sysMsg = new JSONObject();
				sysMsg.put("code", 200);
				sysMsg.put("type", "system");
				sysMsg.put("msg", "用户 " + sid + " 离开了房间 " + roomKey + "，当前人数：" + members.size());
				sysMsg.put("room", roomKey);
				sysMsg.put("members", members.size());
				broadcast(roomKey, sid, sysMsg.toJSONString());
			}
		}
	}

	/**
	 * 发送聊天消息给房间内其他人
	 * 
	 * @param sid
	 * @param content
	 */
	private void sendChat(String sid, String content) {
		String roomKey = userRoomMap.get(sid);
		if (roomKey == null) {
			sendError(sid, "你还没有加入房间，请先随机匹配或输入房间名加入");
			return;
		}
		// 聊天内容敏感词脱敏
		String filtered = filterSensitive(content);
		// 限制聊天内容长度不超过500字符，防止超长消息（前端已限制，此处兜底）
		if (filtered.length() > 500) {
			filtered = filtered.substring(0, 500);
		}
		JSONObject chatMsg = new JSONObject();
		chatMsg.put("code", 200);
		chatMsg.put("type", "chat");
		chatMsg.put("msg", filtered);
		chatMsg.put("from", sid);
		chatMsg.put("room", roomKey);
		broadcast(roomKey, sid, chatMsg.toJSONString());
	}

	/**
	 * 处理图片元信息（文本帧）：校验并登记分片转发上下文，立即把元信息转发给房间内其他人
	 * 重点：不在服务端缓存/组装图片数据，二进制分片到达后逐片实时转发
	 * 
	 * @param sid        发送者sid
	 * @param msgId      图片消息唯一标识
	 * @param meta       元信息JSON（含filename、mime、totalChunk等）
	 * @param totalChunk 总片数
	 */
	private void forwardImageMeta(String sid, String msgId, JSONObject meta, int totalChunk) {
		if (msgId == null || msgId.trim().length() == 0) {
			sendError(sid, "图片消息标识为空");
			return;
		}
		if (totalChunk < 1 || totalChunk > MAX_IMAGE_CHUNK) {
			sendError(sid, "图片分片数量不合法（最多 " + MAX_IMAGE_CHUNK + " 片）");
			return;
		}
		String roomKey = userRoomMap.get(sid);
		if (roomKey == null) {
			sendError(sid, "请先加入房间再发送图片");
			return;
		}
		Set<String> members = roomMap.get(roomKey);
		if (members == null || members.isEmpty()) {
			return;
		}
		// 记录转发目标：房间内除发送者外的所有人（快照，随分片实时转发）
		Set<String> targets = new CopyOnWriteArraySet<String>();
		targets.addAll(members);
		targets.remove(sid);
		imageTransferMap.put(msgId, new ImageTransfer(sid, totalChunk, targets));
		// 文件名敏感词脱敏 + 长度限制，随元信息转发
		String filename = meta.getString("filename");
		if (filename != null) {
			filename = filterSensitive(filename);
			if (filename.length() > 100) {
				filename = filename.substring(0, 100);
			}
			meta.put("filename", filename);
		}
		meta.put("from", sid);
		meta.put("room", roomKey);
		broadcast(roomKey, sid, meta.toJSONString());
	}

	/**
	 * 收到二进制帧（图片分片）：解析 msgId + 片序号，立即原样转发给目标用户
	 * 重点：不在服务端拼接/组装完整图片，分片组装由前端完成，极大降低JVM内存压力
	 * 
	 * 帧格式：前32字节msgId + 4字节片序号(大端) + 图片分片数据
	 */
	@OnMessage
	public void onMessage(byte[] bytes, Session session, @PathParam("sid") String sid) {
		try {
			if (bytes == null || bytes.length <= BIN_HEADER_LEN) {
				return;
			}
			// 解析帧头：前32字节msgId（不足部分以0填充，trim可去除），后4字节大端片序号
			String msgId = new String(bytes, 0, MSG_ID_LEN, StandardCharsets.UTF_8).trim();
			int chunkIndex = ((bytes[32] & 0xFF) << 24) | ((bytes[33] & 0xFF) << 16)
					| ((bytes[34] & 0xFF) << 8) | (bytes[35] & 0xFF);
			ImageTransfer transfer = imageTransferMap.get(msgId);
			if (transfer == null) {
				return; // 未登记的图片传输（元信息未到或已结束），直接忽略
			}
			if (chunkIndex < 0 || chunkIndex >= transfer.totalChunk) {
				return; // 片序号越界，忽略
			}
			// 立即原样转发给房间内目标用户（不缓存、不组装）
			for (String target : transfer.targets) {
				Session ts = webSocketSet.get(target);
				if (ts != null && ts.isOpen()) {
					try {
						ts.getBasicRemote().sendBinary(ByteBuffer.wrap(bytes));
					} catch (IOException e) {
						log.error("转发图片分片失败, 目标sid=" + target, e);
					}
				}
			}
			// 最后一片转发完成后清理传输上下文
			if (chunkIndex == transfer.totalChunk - 1) {
				imageTransferMap.remove(msgId);
			}
		} catch (Exception e) {
			log.error("处理图片分片异常, sid=" + sid, e);
		}
	}

	/**
	 * 清理图片传输上下文：发送者断开/离开则删除其全部传输记录；接收者离开则从转发目标中移除
	 * 
	 * @param sid
	 */
	private void cleanupImageTransfers(String sid) {
		Iterator<Map.Entry<String, ImageTransfer>> it = imageTransferMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, ImageTransfer> entry = it.next();
			if (sid.equals(entry.getValue().sid)) {
				it.remove();
			} else {
				entry.getValue().targets.remove(sid);
			}
		}
	}

	/**
	 * 广播消息给房间内除自己以外的所有人
	 * 
	 * @param roomKey
	 * @param excludeSid
	 * @param message
	 */
	private void broadcast(String roomKey, String excludeSid, String message) {
		Set<String> members = roomMap.get(roomKey);
		if (members == null) {
			return;
		}
		for (String member : members) {
			if (member.equals(excludeSid)) {
				continue;
			}
			Session session = webSocketSet.get(member);
			if (session != null && session.isOpen()) {
				try {
					sendMessage(message, session);
				} catch (IOException e) {
					log.error("广播消息失败, sid=" + member, e);
				}
			}
		}
	}

	/**
	 * 群发信息（给所有在线用户）
	 * 
	 * @param message
	 */
	public static void pushAllMessage(String message) {
		log.info("websocketServer pushAllMessage");
		JSONObject obj = new JSONObject();
		obj.put("code", 666);
		obj.put("type", "system");
		obj.put("msg", message);
		// 群发消息
		for (String key : webSocketSet.keySet()) {
			try {
				if (webSocketSet.get(key).isOpen())
					sendMessage(obj.toJSONString(), webSocketSet.get(key));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 给指定用户发送错误提示
	 * 
	 * @param sid
	 * @param msg
	 */
	private void sendError(String sid, String msg) {
		JSONObject obj = new JSONObject();
		obj.put("code", 504);
		obj.put("type", "error");
		obj.put("msg", msg);
		sendInfo(obj.toJSONString(), sid);
	}

	/**
	 * 给指定用户发送系统消息
	 * 
	 * @param sid
	 * @param msg
	 * @param roomKey
	 */
	private void sendSystem(String sid, String msg, String roomKey) {
		JSONObject obj = new JSONObject();
		obj.put("code", 200);
		obj.put("type", "system");
		obj.put("msg", msg);
		obj.put("room", roomKey);
		sendInfo(obj.toJSONString(), sid);
	}

	/**
	 * 
	 * @param session
	 * @param error
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		log.error("发生错误");
		log.error(error.getMessage());
	}

	/**
	 * 实现服务器主动推送
	 */
	public static void sendMessage(String message, Session session) throws IOException {
		if (session != null && session.isOpen()) {
			session.getBasicRemote().sendText(message);
		}
	}

	/**
	 * 群发自定义消息
	 */
	public static void sendInfo(String message, @PathParam("sid") String sid) {
		log.info("推送消息到窗口" + sid + "，推送内容:" + message);
		try {
			if (!webSocketSet.containsKey(sid)) {
				log.error("sendInfo----对方已离线！");
			} else {
				sendMessage(message, webSocketSet.get(sid));
			}
		} catch (IOException e) {
			log.error("sendInfo----发送异常！");
		}
	}

	public static synchronized int getOnlineCount() {
		return onlineCount;
	}

	public static synchronized void addOnlineCount() {
		WebSocketServer.onlineCount++;
	}

	public static synchronized void subOnlineCount() {
		WebSocketServer.onlineCount--;
	}
}
