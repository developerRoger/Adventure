package com.firstTry.Adventure.middleware;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

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
@ServerEndpoint(value = "/feedback/{sid}", configurator = SpringConfigurator.class)
@Component
public class WebSocketServer {

	static Log log = LogFactory.getLog(WebSocketServer.class);
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

	/**
	 * 连接建立成功调用的方法
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam("sid") String sid) {
		webSocketSet.put(sid, session); // 加入put中
		addOnlineCount(); // 在线数加1
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
			// 兼容旧协议：纯文本 "connet" 视为随机匹配，"heartbeat" 为心跳，其余视为聊天内容
			if (message.startsWith("{")) {
				JSONObject json = JSON.parseObject(message);
				type = json.getString("type");
				roomKey = json.getString("room");
				content = json.getString("msg");
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
				// 指定key加入房间
				if (roomKey == null || roomKey.trim().length() == 0) {
					sendError(sid, "房间key不能为空");
				} else {
					joinRoom(sid, roomKey.trim());
				}
			} else if ("chat".equals(type)) {
				// 聊天消息，广播给房间内其他人
				sendChat(sid, content == null ? "" : content);
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
		// 都满了或没有房间，创建新房间
		if (targetRoom == null) {
			targetRoom = "room-" + UUID.randomUUID().toString().substring(0, 8);
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
			sendError(sid, "你还没有加入房间，请先随机匹配或输入房间key加入");
			return;
		}
		JSONObject chatMsg = new JSONObject();
		chatMsg.put("code", 200);
		chatMsg.put("type", "chat");
		chatMsg.put("msg", content);
		chatMsg.put("from", sid);
		chatMsg.put("room", roomKey);
		broadcast(roomKey, sid, chatMsg.toJSONString());
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
