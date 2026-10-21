package com.firstTry.Adventure.middleware;

import java.util.List;
import java.util.Map;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * WebSocket配置器：握手时解析客户端IP存入userProperties，供@OnOpen读取记录日志
 * 继承SpringConfigurator保留Spring bean注入能力
 * 
 * @author Roger
 */
public class WebSocketConfigurator extends SpringConfigurator {

	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		super.modifyHandshake(sec, request, response);
		// 解析客户端IP：优先X-Forwarded-For，其次X-Real-IP，最后握手URL上的ip参数
		String ip = resolveClientIp(request.getHeaders());
		if ("unknown".equals(ip)) {
			ip = resolveIpFromQuery(request.getQueryString());
		}
		sec.getUserProperties().put("clientIp", ip);
	}

	/**
	 * 从握手URL的query参数中解析ip（前端调用/getIp后拼接到连接地址，解决直连场景取不到IP的问题）
	 * 
	 * @param query
	 * @return
	 */
	private String resolveIpFromQuery(String query) {
		if (query == null || query.length() == 0) {
			return "unknown";
		}
		for (String pair : query.split("&")) {
			if (pair.startsWith("ip=")) {
				String value = pair.substring(3).trim();
				return value.length() == 0 ? "unknown" : value;
			}
		}
		return "unknown";
	}

	/**
	 * 从握手请求头中解析客户端IP
	 * 
	 * @param headers
	 * @return
	 */
	private String resolveClientIp(Map<String, List<String>> headers) {
		String xff = getHeader(headers, "X-Forwarded-For");
		if (xff != null && xff.trim().length() > 0) {
			// X-Forwarded-For 可能形如 "ip1, ip2, ..."，取第一个（真实客户端IP）
			int idx = xff.indexOf(',');
			return idx > 0 ? xff.substring(0, idx).trim() : xff.trim();
		}
		String xri = getHeader(headers, "X-Real-IP");
		if (xri != null && xri.trim().length() > 0) {
			return xri.trim();
		}
		return "unknown";
	}

	/**
	 * 忽略大小写查找请求头
	 * 
	 * @param headers
	 * @param name
	 * @return
	 */
	private String getHeader(Map<String, List<String>> headers, String name) {
		if (headers == null) {
			return null;
		}
		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			if (name.equalsIgnoreCase(entry.getKey())
					&& entry.getValue() != null && !entry.getValue().isEmpty()) {
				return entry.getValue().get(0);
			}
		}
		return null;
	}
}
