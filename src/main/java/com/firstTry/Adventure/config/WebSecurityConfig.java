package com.firstTry.Adventure.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.boot.autoconfigure.security.SecurityProperties;  
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.firstTry.Adventure.service.UserService;
/**
 *  权限控制拦截
 * @author Roger
 *
 */
@Configuration
@EnableWebSecurity
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)  
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private UserService userDetailsService; 	
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;  
	/**
	 * 跳过校验token验证请求的链接
	 */
	private static final String[] PASS_URL = { 
		"/",
		"index"
	};

    public WebSecurityConfig(UserService userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder) {  
        this.userDetailsService = userDetailsService;  
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;  
    }  
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers(PASS_URL).permitAll()
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
                .addFilter(new JWTLoginFilter(authenticationManager()))  
                .addFilter(new JWTAuthenticationFilter(authenticationManager()))
            .logout()
            .logoutSuccessUrl("/login")
                .permitAll();
    }

/*    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .inMemoryAuthentication()
                .withUser("user").password("password").roles("USER");
    	 auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);  
    }*/
}