package com.openxsl.admin.security.csrf;

import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

/**
 * spring-security csrf（DefaultRequiresCsrfMatcher）会拦截post请求
 * @author 001327
 */
@Component
public class CsrfSecurityRequestMatcher implements RequestMatcher {
	private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");
	private List<String> execludeUrls;
	
	@Override
	public boolean matches(HttpServletRequest request) {
		if (allowedMethods.matcher(request.getMethod()).matches()){
			return false;
		}
		if (execludeUrls != null && execludeUrls.size() > 0) {
             final String servletPath = request.getServletPath();
             for (String url : execludeUrls) {
                 if (servletPath.contains(url)) {
                     return false;
                 }
	        }
		}
		return true;
	}
	
	public void setExcludeUrls(List<String> execludeUrls) {
        this.execludeUrls = execludeUrls;
	}

}
