package com.firstTry.Adventure.config;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.firstTry.Adventure.entity.ChatMessageEntity;
import com.firstTry.Adventure.middleware.WebSocketServer;
import com.firstTry.Adventure.service.ChatMessageService;
import com.firstTry.Adventure.service.WebSocketVisitLogService;

/**
 * 默认首页,请求/
 * 目前项目只保留聊天室入口
 * 
 * @author Roger
 *
 */
@Controller
public class DefaultView {
	
    @Autowired
    private WebSocketVisitLogService webSocketVisitLogService;
    
    @Autowired
    private ChatMessageService chatMessageService;
    
	/**
	 * 站点根路径与/index统一重定向到聊天室，保证访客只进入聊天室入口
	 * 
	 * @return
	 */
	@RequestMapping("/")
	public String root() {
		return "redirect:/go";
	}

	@RequestMapping("/index")
	public String indexPage() {
		return "redirect:/go";
	}

	/**
	 * 聊天室入口
	 * 
	 * @return
	 */
	@RequestMapping("/go")
	public String index() {
		return "html/colourEgg";
	}

	/**
	 * 查询websocket访问记录（IP、进入时间、房间名）
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/visitLogs")
	public Object visitLogs() {
		return webSocketVisitLogService.queryList();
	}

	/**
	 * 获取当前请求的真实IP（优先X-Forwarded-For代理头，其次远程地址），
	 * 供前端拼接到websocket连接地址，解决直连场景下无法记录IP的问题
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getIp")
	public String getIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		} else if (ip.indexOf(",") != -1) {
			// 多次代理时取第一个真实IP
			ip = ip.split(",")[0].trim();
		}
		return ip;
	}

	/**
	 * 获取当前所有聊天室（房间key + 人数），供前端浮窗展示
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/rooms")
	public Object rooms() {
		return WebSocketServer.getRoomList();
	}

	/**
	 * 获取当前在线总人数（websocket连接数），供前端右上角悬浮展示
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/online")
	public Object online() {
		return WebSocketServer.getOnlineCount();
	}

	/**
	 * 查询房间聊天历史（隐藏房间已落库消息），支持向上翻页
	 * 首次加载不传 beforeId，翻页时传当前已加载最早一条的id
	 * 
	 * @param room 房间key
	 * @param beforeId 翻页游标：取id更小的更早记录（可选）
	 * @param size 每页条数，默认20，最大50
	 * @return 按id正序（旧的在前）的消息列表
	 */
	@ResponseBody
	@RequestMapping("/chatHistory")
	public Object chatHistory(@RequestParam("room") String room,
			@RequestParam(value = "beforeId", required = false) Long beforeId,
			@RequestParam(value = "size", defaultValue = "20") int size) {
		int limit = (size <= 0 || size > 50) ? 20 : size;
		List<ChatMessageEntity> list = chatMessageService.queryHistory(room, beforeId, limit);
		// DB 按id倒序取回（新的在前），翻转为正序（旧的在前）便于前端直接顺序拼接
		Collections.reverse(list);
		return list;
	}
}
