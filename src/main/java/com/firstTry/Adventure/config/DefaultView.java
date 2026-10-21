package com.firstTry.Adventure.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.firstTry.Adventure.middleware.WebSocketServer;
import com.firstTry.Adventure.service.WebSocketVisitLogService;
import com.firstTry.Adventure.vo.IpVo;
import com.firstTry.Adventure.vo.SinaIpVo;
import com.google.gson.Gson;

/**
 * 默认首页,请求/
 * 
 * @author Roger
 *
 */
@Controller
public class DefaultView {
	
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private WebSocketVisitLogService webSocketVisitLogService;
    
	@RequestMapping("/go")
	public String index(HttpServletRequest request,HttpServletResponse response) {
//		System.out.println(getIpAddress(request));
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
	 * 获取登录用户远程主机ip地址
	 * 
	 * @param request
	 * @return
	 */
	public String getIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		
	        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	            ip = request.getHeader("Proxy-Client-IP");
	        }
	        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	            ip = request.getHeader("WL-Proxy-Client-IP");
	        }
	        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	            ip = request.getRemoteAddr();
	        }
	        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
	            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
	            if( ip.indexOf(",")!=-1 ){
	                ip = ip.split(",")[0];
	            }
	        }
	        //新浪查询失败查询阿里
/*	        String sina = restTemplate.getForObject("http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=json&ip={ip}", String.class,ip);
	        SinaIpVo sinaIpVo = new Gson().fromJson(sina, SinaIpVo.class);
	        if(sinaIpVo.getRet()!=-1){
	            System.out.println(sinaIpVo.getProvince());
	            System.out.println(sinaIpVo.getCity());
	        }else{*/
	            String object = restTemplate.getForObject("http://ip.taobao.com/service/getIpInfo.php?ip={ip}", String.class,ip);
	            IpVo ipVo = new Gson().fromJson(object, IpVo.class);
	            // XX表示内网 
	            if(ipVo.getCode()==0 && !ipVo.getAddress().getRegion().equals("XX")){
	                System.out.println(ipVo.getAddress().getRegion());
	                System.out.println(ipVo.getAddress().getCity());
//	            }
	        }
	            System.out.println(ip);
		return ip;
	}

	@RequestMapping("/login")
	public String login(HttpServletRequest request) {
		getIpAddress(request);
		return "html/login";
	}

	@RequestMapping("/hhh")
	public String hello() {
		return "html/index";
	}
	
	@RequestMapping("/preface")
	public String preface(){
		return "html/preface";
	}
}
