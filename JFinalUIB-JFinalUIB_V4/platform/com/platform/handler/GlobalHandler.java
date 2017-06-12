package com.platform.handler;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jfinal.handler.Handler;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.platform.constant.ConstantInit;
import com.platform.constant.ConstantWebContext;
import com.platform.mvc.syslog.Syslog;
import com.platform.plugin.I18NPlugin;
import com.platform.thread.ThreadSysLog;
import com.platform.tools.ToolDateTime;
import com.platform.tools.ToolRandoms;
import com.platform.tools.ToolWeb;

/**
 * 全局Handler，设置一些通用功能
 * @author 董华健
 * 描述：主要是一些全局变量的设置，再就是日志记录开始和结束操作
 */
public class GlobalHandler extends Handler {
	
	private static final Log log = Log.getLog(GlobalHandler.class);

	@Override
	public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
		if(log.isDebugEnabled()) log.debug("初始化访问系统功能日志");
		Syslog reqSysLog = getSysLog(request);
		long starttime = ToolDateTime.getDateByTime();
		reqSysLog.set(Syslog.column_startdate, ToolDateTime.getSqlTimestamp(starttime)); // 开始时间
		request.setAttribute(ConstantWebContext.reqSysLogKey, reqSysLog);
		
		if(log.isDebugEnabled()) log.debug("设置 web 路径");
		String cxt = ToolWeb.getContextPath(request);
		request.setAttribute(ConstantWebContext.request_cxt, cxt);

		if(log.isDebugEnabled()) log.debug("request 随机分配一个请求id");
		request.setAttribute(ConstantWebContext.request_id, ToolRandoms.getUuid(true));
		
		if(log.isDebugEnabled()) log.debug("request cookie 处理");
		Map<String, Cookie> cookieMap = ToolWeb.readCookieMap(request);
		request.setAttribute(ConstantWebContext.request_cookieMap, cookieMap);

		if(log.isDebugEnabled()) log.debug("request param 请求参数处理");
		request.setAttribute(ConstantWebContext.request_paramMap, ToolWeb.getParamMap(request));

		if(log.isDebugEnabled()) log.debug("request 国际化");
		String localePram = request.getParameter(ConstantWebContext.request_localePram);
		if(StrKit.isBlank(localePram)){
			localePram = request.getHeader("localePram");
		}
		if(StrKit.notBlank(localePram)){
			String cxtPath = request.getContextPath();
			if(StrKit.isBlank(cxtPath)){
				cxtPath = "/";
			}
			
			int maxAge = PropKit.getInt(ConstantInit.config_maxAge_key);
			localePram = I18NPlugin.localParse(localePram);
			ToolWeb.addCookie(response,  "", cxtPath, true, ConstantWebContext.cookie_language, localePram, maxAge);
		}else {
			localePram = ToolWeb.getCookieValueByName(request, ConstantWebContext.cookie_language);
			if(StrKit.isBlank(localePram)){
				Locale locale = request.getLocale();
				String language = locale.getLanguage();
				localePram = language;
				String country = locale.getCountry();
				if(StrKit.notBlank(country)){
					localePram += "_" + country;
				}
			}
			localePram = I18NPlugin.localParse(localePram);
		}
		Map<String, String> i18nMap = I18NPlugin.get(localePram);
		request.setAttribute(ConstantWebContext.request_localePram, localePram);
		request.setAttribute(ConstantWebContext.request_i18nMap, i18nMap);
		response.setHeader(ConstantWebContext.request_localePram, localePram);
		
		if(log.isDebugEnabled()) log.debug("设置Header");
		request.setAttribute("decorator", "none");
		response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
		response.setHeader("Pragma","no-cache"); // HTTP 1.0
		response.setDateHeader ("Expires", 0); // prevents caching at the proxy server
		
		next.handle(target, request, response, isHandled);
		
		if(log.isDebugEnabled()) log.debug("请求处理完毕，计算耗时");
		
		// 结束时间
		long endtime = ToolDateTime.getDateByTime();
		reqSysLog.set(Syslog.column_enddate, ToolDateTime.getSqlTimestamp(endtime));
		
		// 总耗时
		Long haoshi = endtime - starttime;
		reqSysLog.set(Syslog.column_haoshi, haoshi);
		
		// 视图耗时
		long renderTime = 0;
		if(null != request.getAttribute(ConstantWebContext.renderTimeKey)){
			renderTime = (Long) request.getAttribute(ConstantWebContext.renderTimeKey);
		}
		reqSysLog.set(Syslog.column_viewhaoshi, renderTime);
		
		// action耗时
		reqSysLog.set(Syslog.column_actionhaoshi, haoshi - renderTime);
		
		if(log.isDebugEnabled()) log.debug("日志添加到入库队列");
		if(reqSysLog.getSyslog().equals("1")){
			ThreadSysLog.add(reqSysLog);
		}
	}
	
	/**
	 * 创建日志对象,并初始化一些属性值
	 * @param request
	 * @return
	 */
	public Syslog getSysLog(HttpServletRequest request){
		String requestPath = ToolWeb.getRequestURIWithParam(request); 
		String ip = ToolWeb.getIpAddr(request);
		String referer = request.getHeader("Referer"); 
		String userAgent = request.getHeader("User-Agent");
		String cookie = request.getHeader("Cookie");
		String method = request.getMethod();
		String xRequestedWith = request.getHeader("X-Requested-With");
		String host = request.getHeader("Host");
		String acceptLanguage = request.getHeader("Accept-Language");
		String acceptEncoding = request.getHeader("Accept-Encoding");
		String accept = request.getHeader("Accept");
		String connection = request.getHeader("Connection");

		Syslog reqSysLog = new Syslog();
		
		reqSysLog.set(Syslog.column_ips, ip);
		reqSysLog.set(Syslog.column_requestpath, requestPath);
		reqSysLog.set(Syslog.column_referer, referer);
		reqSysLog.set(Syslog.column_useragent, userAgent);
		reqSysLog.set(Syslog.column_cookie, cookie);
		reqSysLog.set(Syslog.column_method, method);
		reqSysLog.set(Syslog.column_xrequestedwith, xRequestedWith);
		reqSysLog.set(Syslog.column_host, host);
		reqSysLog.set(Syslog.column_acceptlanguage, acceptLanguage);
		reqSysLog.set(Syslog.column_acceptencoding, acceptEncoding);
		reqSysLog.set(Syslog.column_accept, accept);
		reqSysLog.set(Syslog.column_connection, connection);

		return reqSysLog;
	}
	
}
