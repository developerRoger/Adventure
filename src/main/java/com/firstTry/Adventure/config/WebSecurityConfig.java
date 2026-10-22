package com.firstTry.Adventure.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.boot.autoconfigure.security.SecurityProperties;  
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.firstTry.Adventure.service.UserService;
/**
 *  权限控制拦截
 * @author Roger
 *
 */
@Configuration
@EnableWebSecurity
@Order(1)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private UserService userDetailsService; 	
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;  
	/**
	 * 跳过校验token验证请求的链接
	 * 目前项目只开放聊天室入口，仅放行聊天室相关页面、接口与所需静态资源，
	 * 其余页面（登录、后台、代码生成、七牛、Swagger等）一律要求登录鉴权，对匿名用户关闭
	 */
	private static final String[] PASS_URL = { 
		//聊天室入口（根路径与/index会重定向到/go）
		"/",
		"/index",
		"/go",
		//聊天室支撑接口：房间列表、访问记录、客户端IP
		"/rooms",
		"/visitLogs",
		"/getIp",
		//websocket连接
		"/feedback/*",
		//聊天页所需的静态资源
		"/js/**",
		"/css/**",
		"/img/**",
		"/fonts/**",
		"/libs/**",
		"/favicon.ico"
	};

    public WebSecurityConfig(UserService userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder) {  
        this.userDetailsService = userDetailsService;  
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;  
    }  
    

    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	//禁用 csrf
        http.cors().and().csrf().disable().authorizeRequests()
                //允许以下请求
                .antMatchers(PASS_URL).permitAll()
                //所有请求需要身份认证（未放行的路径对匿名用户返回403，登录页面入口已关闭）
                .anyRequest().authenticated()
                .and()
                //验证登陆（JWTLoginFilter仅拦截POST /login签发token，管理员API登录仍可用）
                .addFilter(new JWTLoginFilter(authenticationManager()))
                //验证token
                .addFilter(new JWTAuthenticationFilter(authenticationManager()));
    }
    
    @Override  
    public void configure(AuthenticationManagerBuilder auth) throws Exception {  
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);  
    } 
    
    
/*    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    	System.err.println("自定义校验---------------进来了");

        // auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
         // 使用自定义身份验证组件
         auth.authenticationProvider(new CustomAuthenticationProvider(userDetailsService,bCryptPasswordEncoder));
    }*/
}