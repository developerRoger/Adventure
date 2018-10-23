package com.firstTry.Adventure.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

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
