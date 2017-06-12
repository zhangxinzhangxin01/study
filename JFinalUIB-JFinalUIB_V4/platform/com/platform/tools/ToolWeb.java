package com.platform.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.platform.constant.ConstantInit;

/**
 * WEB工具类
 * @author 董华健 2012-9-3 下午7:39:43
 */
public abstract class ToolWeb {

	private static final Log log = Log.getLog(ToolWeb.class);

	/**
	 * 获取客户端IP地址
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * 获取上下文URL全路径
	 * 
	 * @param request
	 * @return
	 */
	public static String getContextPath(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		sb.append(request.getScheme()).append("://").append(request.getServerName());
		int port = request.getServerPort();
		if(port != 80 ){
			sb.append(":").append(port);
		}
		sb.append(request.getContextPath());
		String path = sb.toString();
		sb = null;
		return path;
	}

	/**
	 * 获取完整请求路径(含内容路径及请求参数)
	 * 
	 * @param request
	 * @return
	 */
	public static String getRequestURIWithParam(HttpServletRequest request) {
		return request.getRequestURI() + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
	}

	/**
	 * 获取请求参数
	 * 
	 * @param request
	 * @param name
	 * @return
	 */
	public static String getParam(HttpServletRequest request, String name) {
		String value = request.getParameter(name);
		if (StrKit.notBlank(value)) {
			try {
				return URLDecoder.decode(value, ToolString.encoding).trim();
			} catch (UnsupportedEncodingException e) {
				if(log.isErrorEnabled()) log.error("decode异常：" + value);
				return value;
			}
		}
		return value;
	}

	/**
	 * 获取ParameterMap
	 * @param request
	 * @return
	 */
	public static Map<String, String> getParamMap(HttpServletRequest request){
		Map<String, String> map = new HashMap<String, String>();
		Enumeration<String> enume = request.getParameterNames();
		while (enume.hasMoreElements()) {
			String name = (String) enume.nextElement();
			map.put(name, request.getParameter(name));
		}
		return map;
	}

	/**
	 * 输出servlet文本内容
	 * 
	 * @author 董华健 2012-9-14 下午8:04:01
	 * @param response
	 * @param content
	 * @param contentType
	 */
	public static void outPage(HttpServletResponse response, String content, String contentType) {
		try {
			outPage(response, content.getBytes(ToolString.encoding), contentType); // char to byte 性能提升
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 输出servlet文本内容
	 * 
	 * @author 董华健 2012-9-14 下午8:04:01
	 * @param response
	 * @param content
	 * @param contentType
	 */
	public static void outPage(HttpServletResponse response, byte[] content, String contentType) {
		if(StrKit.isBlank(contentType)){
			contentType = "text/html; charset=UTF-8";
		}
		response.setContentType(contentType);
		response.setCharacterEncoding(ToolString.encoding);
		// PrintWriter out = response.getWriter();
		// out.print(content);
		try {
			response.getOutputStream().write(content);// char to byte 性能提升
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 输出CSV文件下载
	 * 
	 * @author 董华健 2012-9-14 下午8:02:33
	 * @param response
	 * @param content CSV内容
	 */
	public static void outDownCsv(HttpServletResponse response, String content) {
		response.setContentType("application/download; charset=gb18030");
		try {
			response.setHeader("Content-Disposition", "attachment; filename=" + java.net.URLEncoder.encode(ToolDateTime.format(ToolDateTime.getDate(), ToolDateTime.pattern_ymd_hms_s) + ".csv", ToolString.encoding));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		// PrintWriter out = response.getWriter();
		// out.write(content);
		try {
			response.getOutputStream().write(content.getBytes(ToolString.encoding));
		} catch (IOException e) {
			e.printStackTrace();
		}// char to byte 性能提升
			// out.flush();
			// out.close();
	}

	/**
	 * 请求流转字符串
	 * 
	 * @param request
	 * @return
	 */
	public static String requestStream(HttpServletRequest request) {
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		try {
			request.setCharacterEncoding(ToolString.encoding);
			inputStream = (ServletInputStream) request.getInputStream();
			inputStreamReader = new InputStreamReader(inputStream, ToolString.encoding);
			bufferedReader = new BufferedReader(inputStreamReader);
			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		} catch (IOException e) {
			if(log.isErrorEnabled()) log.error("request.getInputStream() to String 异常", e);
			return null;
		} finally { // 释放资源
			if(null != bufferedReader){
				try {
					bufferedReader.close();
				} catch (IOException e) {
					if(log.isErrorEnabled()) log.error("bufferedReader.close()异常", e);
				}
				bufferedReader = null;
			}
			
			if(null != inputStreamReader){
				try {
					inputStreamReader.close();
				} catch (IOException e) {
					if(log.isErrorEnabled()) log.error("inputStreamReader.close()异常", e);
				}
				inputStreamReader = null;
			}
			
			if(null != inputStream){
				try {
					inputStream.close();
				} catch (IOException e) {
					if(log.isErrorEnabled()) log.error("inputStream.close()异常", e);
				}
				inputStream = null;
			}
		}
	}
	
	/**
	 * 
	 * @param response
	 * @param domain		设置cookie所在域
	 * @param path			设置cookie所在路径
	 * @param isHttpOnly	是否只读
	 * @param name			cookie的名称
	 * @param value			cookie的值
	 * @param maxAge		cookie存放的时间(以秒为单位,假如存放三天,即3*24*60*60; 如果值为0,cookie将随浏览器关闭而清除)
	 */
	public static void addCookie(HttpServletResponse response, 
			String domain, String path, boolean isHttpOnly, 
			String name, String value, int maxAge) {
		Cookie cookie = new Cookie(name, value);

		// 所在域：比如a1.4bu4.com 和 a2.4bu4.com 共享cookie
		if(StrKit.notBlank(domain)){
			cookie.setDomain(domain);			
		}
		
		// 设置cookie所在路径
		cookie.setPath("/");
		if(StrKit.notBlank(path)){
			cookie.setPath(path);				
		}
		
		// 是否只读
		try {
			cookie.setHttpOnly(isHttpOnly);
		} catch (Exception e) {
			if(log.isErrorEnabled()) log.error("servlet容器版本太低，servlet3.0以前不支持设置cookie只读" + e.getMessage());
		}
		
		// 设置cookie的过期时间
		if (maxAge > 0){
			cookie.setMaxAge(maxAge);
		}
		
		// 添加cookie
		response.addCookie(cookie);
	}

	/**
	 * 获取cookie的值
	 * 
	 * @param request
	 * @param name
	 *            cookie的名称
	 * @return
	 */
	public static String getCookieValueByName(HttpServletRequest request, String name) {
		Map<String, Cookie> cookieMap = ToolWeb.readCookieMap(request);
		// 判断cookie集合中是否有我们像要的cookie对象 如果有返回它的值
		if (cookieMap.containsKey(name)) {
			Cookie cookie = (Cookie) cookieMap.get(name);
			return cookie.getValue();
		} else {
			return null;
		}
	}

	/**
	 * 获得cookie
	 * 
	 * @param request
	 * @param name
	 * @return
	 */
	public static Cookie getCookieByName(HttpServletRequest request, String name) {
		Map<String, Cookie> cookieMap = ToolWeb.readCookieMap(request);
		// 判断cookie集合中是否有我们像要的cookie对象 如果有返回它的值
		if (cookieMap.containsKey(name)) {
			Cookie cookie = (Cookie) cookieMap.get(name);
			return cookie;
		} else {
			return null;
		}
	}

	/**
	 * 获得所有cookie
	 * 
	 * @param request
	 * @return
	 */
	public static Map<String, Cookie> readCookieMap(HttpServletRequest request) {
		Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();
		// 从request范围中得到cookie数组 然后遍历放入map集合中
		Cookie[] cookies = request.getCookies();
		if (null != cookies) {
			for (int i = 0; i < cookies.length; i++) {
				cookieMap.put(cookies[i].getName(), cookies[i]);
			}
		}
		return cookieMap;
	}

	/**
	 * 效验Referer有效性
	 * 
	 * @author 董华健 2012-10-30 上午10:26:04
	 * @return
	 */
	public static boolean authReferer(HttpServletRequest request) {
		String referer = request.getHeader("Referer");
		if (null != referer && !referer.trim().equals("")) {
			referer = referer.toLowerCase();
			String domainStr = PropKit.get(ConstantInit.config_domain_key);
			String[] domainArr = domainStr.split(",");
			for (String domain : domainArr) {
				if (referer.startsWith(domain.trim())) {
					return true;
				}
			}
		}
		return false;
	}
	
}
