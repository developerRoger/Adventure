package com.firstTry.Adventure.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * HTTP相关的工具类
 * 
 * @author billy.lin
 *
 */
public class HttpUtil {

	/**
	 * 允许 ajax 跨域访问
	 * 
	 * @param response
	 */
	public static void allowOrigin(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "*");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept,phoneType,appId,agent,UserName,UserToken,SeesionId,platform");
	}

	/**
	 * 从Request中获得URI
	 * 
	 * @param request
	 * @return request URI
	 */
	public static String getRequestURI(HttpServletRequest request) {
		return request.getRequestURI();
	}

	/**
	 * 从Request中获得URL
	 * 
	 * @param request
	 * @return request URL
	 */
	public static String getRequestURL(HttpServletRequest request) {
		return request.getRequestURL().toString();
	}

	/**
	 * 从Request中获得Method
	 * 
	 * @param request
	 * @return request method
	 */
	public static String getMethod(HttpServletRequest request) {
		return request.getMethod();
	}

	/**
	 * 根据Cookie Name从Request中获得Cookie Value
	 * 
	 * @param cookieName
	 * @return Cookie Value
	 */
	public static String getCookie(Cookie[] cookies, String cookieName) {
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equalsIgnoreCase(cookieName)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * 创建Cookie
	 * 
	 * @param cookieName
	 * @param cookieValue
	 * @param path
	 * @param domain
	 * @param maxAge
	 * @return Cookie
	 */
	public static Cookie makeCookie(String name, String value, String path, String domain, int age) {
		Cookie cookie = new Cookie(name, value);
		if (path != null && !"".equals(path)) {
			cookie.setPath(path);
		}
		if (domain != null && !"".equals(domain)) {
			cookie.setDomain(domain);
		}
		return cookie;
	}

	/**
	 * HttpClient GET
	 * 
	 * @param url
	 * @return String
	 * @throws IOException
	 */
	public static String getHttpGetResult(String url) throws IOException {
		CloseableHttpClient httpclient = HttpClients.createSystem();
		try {
			HttpGet httpget = new HttpGet(url);
			CloseableHttpResponse response = httpclient.execute(httpget);
			try {
				if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
					return EntityUtils.toString(response.getEntity(), HTTP.DEF_CONTENT_CHARSET);
				} else {
					return null;
				}
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
	}

	/**
	 * HttpClient POST
	 * 
	 * @param url
	 * @param nvps
	 * @return String
	 * @throws IOException
	 */
	public static String getHttpPostResult(String url, List<NameValuePair> nvps) throws IOException {
		CloseableHttpClient httpclient = HttpClients.createSystem();
		try {
			HttpPost httppost = new HttpPost(url);
			if (nvps != null && !nvps.isEmpty()) {
				httppost.setEntity(new UrlEncodedFormEntity(nvps));
			}
			CloseableHttpResponse response = httpclient.execute(httppost);
			try {
				if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
					return EntityUtils.toString(response.getEntity(), HTTP.DEF_CONTENT_CHARSET);
				} else {
					return null;
				}
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
	}

	/**
	 * HttpClient POST
	 * 
	 * @param url
	 * @param nvps
	 * @return OutputStream
	 * @throws IOException
	 */
	public static OutputStream getHttpPostStream(String url, List<NameValuePair> nvps) throws IOException {
		CloseableHttpClient httpclient = HttpClients.createSystem();
		try {
			HttpPost httppost = new HttpPost(url);
			if (nvps != null && !nvps.isEmpty()) {
				httppost.setEntity(new UrlEncodedFormEntity(nvps));
			}
			CloseableHttpResponse response = httpclient.execute(httppost);
			try {
				if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					response.getEntity().writeTo(byteArrayOutputStream);
					return byteArrayOutputStream;
				} else {
					return null;
				}
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
	}
}
