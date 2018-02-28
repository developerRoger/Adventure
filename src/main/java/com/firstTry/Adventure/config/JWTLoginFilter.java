package com.firstTry.Adventure.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firstTry.Adventure.entity.UserTest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JWTLoginFilter extends UsernamePasswordAuthenticationFilter {

	private AuthenticationManager authenticationManager;

	public JWTLoginFilter(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	// 接收并解析用户凭证
	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
			throws AuthenticationException {
		// 得到用户登陆信息,并封装到 Authentication 中,供自定义用户组件使用.
		String username = req.getParameter("username");
		String password = req.getParameter("password");

		if (username == null) {
			username = "";
		}

		if (password == null) {
			password = "";
		}

		username = username.trim();

		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
				password, new ArrayList<>());

		// authenticate()接受一个token参数,返回一个完全经过身份验证的对象，包括证书.
		// 这里并没有对用户名密码进行验证,而是使用 AuthenticationProvider 提供的 authenticate
		// 方法返回一个完全经过身份验证的对象，包括证书.
		// Authentication authenticate =
		// authenticationManager.authenticate(authenticationToken);

		// UsernamePasswordAuthenticationToken 是 Authentication 的实现类
		return authenticationToken;
	}

	// 用户成功登录后，这个方法会被调用，我们在这个方法里生成token
	@Override
	protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
			Authentication auth) throws IOException, ServletException {

		String token = Jwts.builder()
				.setSubject(auth.getName())
				.setExpiration(new Date(System.currentTimeMillis() + 60 * 60 * 24 * 1000))
				.signWith(SignatureAlgorithm.HS512, "MyJwtSecret").compact();
		res.addHeader("Authorization", "Bearer " + token);
		System.out.println("token====" + token);
	}

}
