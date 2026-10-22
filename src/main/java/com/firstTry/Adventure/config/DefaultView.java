package com.firstTry.Adventure.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.firstTry.Adventure.middleware.WebSocketServer;
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
}
