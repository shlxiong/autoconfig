package com.openxsl.admin.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.openxsl.config.webmvc.Response;

/**
 * 当验证失败时，输出提示信息（前后端分离）
 * 
 * AuthenticationEntryPoint
 *    |--LoginUrlAuthenticationEntryPoint: 跳转登录页面
 *    |--Http401AuthenticationEntryPoint: 弹出输入框
 *    |--Http403ForbiddenEntryPoint: Error
 *    
 * @author shuilin.xiong
 */
@Component("outputAuthEntryPoint")
public class OutputAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public OutputAuthenticationEntryPoint(
				@Value("${security.authen-login-url}")String loginFormUrl) {
		super(loginFormUrl);
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
						AuthenticationException authException) 
				throws IOException, ServletException {
		logger.error("AuthenticationException: ", authException);
		
		if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
			Response json = new Response();
			json.setStatus(403);
			json.setMessage(authException.getMessage());
			response.setContentType("application/json;charset=UTF-8");
			response.getOutputStream().println(JSON.toJSONString(json));
		} else {
			super.commence(request, response, authException);
		}
	}

}
