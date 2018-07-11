package com.firstTry.Adventure.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 默认首页,请求/
 * 
 * @author Roger
 *
 */
@Controller
public class DefaultView {
	@RequestMapping("/go")
	public String index(HttpServletRequest request,HttpServletResponse response) {
		System.out.println(getIpAddress(request));
		return "index";
	}

	/**
	 * 获取登录用户远程主机ip地址
	 * 
	 * @param request
	 * @return
	 */
	public String getIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	@RequestMapping("/login")
	public String login() {
		return "html/login";
	}

	@RequestMapping("/xinxinniannian")
	public String hello() {
		return "html/index";
	}
	
	@RequestMapping("/narcotics")
	public String narcotics(){
		return "html/Narcotics";
	}
}
