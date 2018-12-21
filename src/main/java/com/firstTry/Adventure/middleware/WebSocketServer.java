package com.firstTry.Adventure.middleware;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.firstTry.Adventure.exception.RRException;
import com.firstTry.Adventure.utils.R;

/**
 * 所有websocket的终类
 * 
 * @author Roger
 * @date 2018/11/9
 */
@ServerEndpoint(value = "/feedback/{sid}", configurator = SpringConfigurator.class)
@Component
public class WebSocketServer {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	static Log log = LogFactory.getLog(WebSocketServer.class);
	// 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
	private static int onlineCount = 0;
	// concurrent包的线程安全Map，用来存放每个客户端对应的MyWebSocket对象。
	private static Map<String, Session> webSocketSet = new ConcurrentHashMap<>();
	// 存储连接用户id数组(用作随机匹配母体)
	private static List<String> socketList = Collections.synchronizedList(new ArrayList<String>());

	private static final String timeKey = "im";

	/**
	 * 连接建立成功调用的方法
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam("sid") String sid) {
		if (!webSocketSet.containsKey(sid))
			webSocketSet.put(sid, session); // 加入put中
		if (sid != null) {
			// 存入redis
			redisTemplate.opsForValue().set(sid, null);
		}
		addOnlineCount(); // 在线数加1
		log.info("有新窗口开始监听:" + sid + ",当前在线人数为" + getOnlineCount());
		try {
			sendMessage(R.error(100, "连接成功").toString(), session);
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
		// 对方已离线，发送消息
		String sendId = null;
		if (null != redisTemplate.opsForValue().get(sid)) {
			sendId = redisTemplate.opsForValue().get(sid).toString();
			redisTemplate.delete((String) redisTemplate.opsForValue().get(sid));
			redisTemplate.delete(sid);
		}
		try {
			if (null != sendId)
				sendMessage(R.error(504, "对方已离线，你可以重新匹配").toString(), webSocketSet.get(sendId));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		socketList.remove(sid);
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

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					// TODO Auto-generated method stub
					if ("connet".equals(message)) {
						// 如果已经有值匹配了，不会进入随机匹配
						if (null == redisTemplate.opsForValue().get(sid)) {
							// 存到母体中，等待匹配
							if (!socketList.contains(sid))
								socketList.add(sid);
							// 5秒后过期
							redisTemplate.opsForValue().set(timeKey + "_" + sid, "connet", 5, TimeUnit.SECONDS);
							//全员通知
							pushAllMessage("当前有人正在等待匹配....ing");
							randomMatching(sid);
						} else {
							sendInfo(R.error(200, "你的操作太频繁").toString(), sid);
						}
					} else if ("heartbeat".equals(message)) {
						log.info("用户心跳:" + sid + ",当前在线人数为" + getOnlineCount());
					} else {
						// 无人匹配，返回相同内容
						if (null == redisTemplate.opsForValue().get(sid))
							sendInfo(R.error(404, message).toString(), sid);
						else
							sendInfo(R.error(200, message).toString(), (String) redisTemplate.opsForValue().get(sid));
					}
				} catch (Exception e) {
					log.error(e.getMessage());
					redisTemplate.delete(sid);
					// 异常后，直接关闭
				}
			}

		}).start();

		System.out.println(session.getId());

	}

	/**
	 * 群发信息
	 * 
	 * @param message
	 */
	public static void pushAllMessage(String message) {
		log.info("websocketServer pushAllMessage");
		// 群发消息
		for (String key : webSocketSet.keySet()) {
			try {
				if (webSocketSet.get(key).isOpen())
					sendMessage(R.error(666, message).toString(), webSocketSet.get(key));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param a
	 * @param sid
	 * @throws IOException
	 */
	private synchronized void randomMatching(String sid) throws IOException {
		// 随机匹配之前先删除自己
		List<String> list = new ArrayList<>();
		list = socketList;

		list.remove(sid);
		int num = list.size();
		// 当递归不再执行,5秒redis自动清空值
		if (null == redisTemplate.opsForValue().get(sid)
				&& null == redisTemplate.opsForValue().get(timeKey + "_" + sid)) {
			sendInfo(R.error(504, "暂时无人匹配，你稍后再来试试吧").toString(), sid);
		} else if (num == 1) {// 只有一人匹配的时候
			// 已经匹配成功
			if (null != redisTemplate.opsForValue().get(sid)) 
				return;
			if (null != redisTemplate.opsForValue().get(list.get(0))){
				sendInfo(R.error(504, "(ｷ｀ﾟДﾟ´)!!不巧，你的对象刚刚偷偷溜走了").toString(), sid);
				return;
			}
			redisTemplate.opsForValue().set(sid, list.get(0));
			redisTemplate.opsForValue().set(list.get(0), sid);
			sendInfo(R.error(200, "匹配成功").toString(), sid);
			sendInfo(R.error(200, "匹配成功").toString(), list.get(0));
			return;
		} else if (num > 1) {
			if (null != redisTemplate.opsForValue().get(timeKey + "_" + sid) && num == 0)
				randomMatching(sid);
			Random random = new Random();
			int key = random.nextInt(num);
			// 已经匹配成功
			if (null != redisTemplate.opsForValue().get(list.get(key)))
				return;
			redisTemplate.opsForValue().set(sid, list.get(key));
			redisTemplate.opsForValue().set(list.get(key), sid);
			sendInfo(R.error(200, "匹配成功").toString(), sid);
			sendInfo(R.error(200, "匹配成功").toString(), list.get(key));
			return;
		}
		// 递归持续执行6
		if (null != redisTemplate.opsForValue().get(timeKey + "_" + sid)
				&& null == redisTemplate.opsForValue().get(sid))
			randomMatching(sid);
	}

	/**
	 * 
	 * @param session
	 * @param error
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		log.error("发生错误");
		// error.printStackTrace();
		log.error(error.getMessage());
	}

	/**
	 * 实现服务器主动推送
	 */
	public static void sendMessage(String message, Session session) throws IOException {
		session.getBasicRemote().sendText(message);
	}

	/**
	 * 群发自定义消息
	 */
	public static void sendInfo(String message, @PathParam("sid") String sid) throws IOException {
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
