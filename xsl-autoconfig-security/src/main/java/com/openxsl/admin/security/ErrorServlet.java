package com.openxsl.admin.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.web.util.WebUtils;

import com.alibaba.fastjson.JSON;
import com.openxsl.config.util.StringUtils;

@WebServlet(urlPatterns={"/errorPage","/error"}, loadOnStartup=100)
@SuppressWarnings("serial")
public class ErrorServlet extends HttpServlet {
private String loginUrl = "/login.jsp";
	
	@Override
	public void init(ServletConfig config){
		InputStream is;
		try {
			Properties props = new Properties();
			is = new DefaultResourceLoader().getResource("classpath:/application.properties")
							.getInputStream();
			props.load(is);
			loginUrl = props.getProperty("security.authen-login-url");
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		loginUrl = config.getServletContext().getContextPath() + loginUrl;
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
	        	throws ServletException, IOException{
		ErrorInfo errorInfo = new ErrorInfo(request);
		int statusCode = response.getStatus();
		if (statusCode == 403){
			AccessDeniedException exception = (AccessDeniedException) 
						request.getAttribute(WebAttributes.ACCESS_DENIED_403);
			if (exception != null){ //AccessDeniedHandlerImpl.forward过来的
				if (exception instanceof CsrfException) { //MissingCsrfException or InvalidCsrfException
					errorInfo.setMessage("由于网络原因或服务器重启，需要重新登录");
					errorInfo.setRedirectUrl(loginUrl);
				} else { //AcessDeniedException, AuthenticationException
					//return "redirect:/403.jsp";
				}
				errorInfo.setDetail(StringUtils.getStackTrace(exception));
			}
		}else if (statusCode == 404){
			errorInfo.setMessage("主人：服务器找不到请求的页面，小的已尽力了！");
			errorInfo.setDetail("Not found: "+request.getRequestURI());
		}else if (statusCode == 500){
			errorInfo.setMessage("服务器内部错误");
			Exception expt = null;
			expt = (Exception)request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE);
			if (expt != null){
				errorInfo.setDetail(StringUtils.getStackTrace(expt));
			}
		}
		//TODO DelegatingAccessDeniedHandler
		response.setContentType("text/html; charset=UTF-8");
		this.render(response.getWriter(), errorInfo);
	}
	
	private void render(PrintWriter out, ErrorInfo errorInfo){
		String jsFunc = (errorInfo.getRedirectUrl()==null) ? "history.back(-1)"
				: ("location.href='"+errorInfo.getRedirectUrl()+"'");
		
		StringBuilder html = new StringBuilder(512);
		html.append("<style>\n")
			.append("span{ text-align:center; cursor:pointer; text-decoration:underline; }")
			.append("\n</style>\n");
		html.append("<script language='javaScript'>\n")
			.append("function toggleDetails(){\n")
			.append("   var details = document.getElementById('detail');")
			.append("details.style.display = (details.style.display=='none')?'block':'none';")
			.append("\n}\n</script>\n");
		
		html.append("<pre>　　").append(errorInfo.getMessage())
			.append("   <span onclick=\"toggleDetails()\">【详情】<span></pre>");
		html.append("<div id='detail' style='display:none'><BR><pre>")
			.append(errorInfo.getDetail()).append("<pre></div>");
		html.append("<P><center>");
		html.append("	<span onclick=\""+jsFunc+";\">回　退</span>")
			.append("	<span onclick=\"location.href='"+loginUrl+"'\">重新登录</span>");
		html.append("</center></P>");
		out.println(html.toString());
	}

	private final String getRequestParameter(HttpServletRequest request, String name,
							String defValue){
		String value = request.getParameter(name);
		if (value == null){
			value = (String)request.getAttribute(name);
		}
		if (value == null){
			value = defValue;
		}
		return value;
	}

	public class ErrorInfo{
		String message;
		String detail;
		transient String redirectUrl;
		
		public ErrorInfo(HttpServletRequest request){
			message = getRequestParameter(request, "errorMsg", "会话超时或无权限访问！");
			detail = getRequestParameter(request, "errorDetail", "｛^v^｝, 没有更多信息...");
			redirectUrl = getRequestParameter(request, "location", request.getHeader("Referer"));
		}
		
		public String toString(String format){
			if ("xml".equalsIgnoreCase(format)){
				StringBuilder buffer = new StringBuilder(128);
				buffer.append("<message>").append(message).append("</message>")
					.append("<detail>").append(detail).append("</detail>");
				return buffer.toString();
			}else{
				return JSON.toJSONString(this);
			}
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getDetail() {
			return detail;
		}

		public void setDetail(String detail) {
			this.detail = detail;
		}

		public String getRedirectUrl() {
			return redirectUrl;
		}

		public void setRedirectUrl(String redirectUrl) {
			this.redirectUrl = redirectUrl;
		}
	}
}
