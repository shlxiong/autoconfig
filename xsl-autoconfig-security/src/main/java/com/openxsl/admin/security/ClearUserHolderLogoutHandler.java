package com.openxsl.admin.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.openxsl.admin.context.UserHolder;

@Component
public class ClearUserHolderLogoutHandler extends SimpleUrlLogoutSuccessHandler {
					//extends SecurityContextLogoutHandler {
	@Value("${security.authen-login-url}")
	private String targetUrl;
	@Autowired
	private UserHolder userHolder;
	
//	public void logout(HttpServletRequest request, HttpServletResponse response,
//			Authentication authentication) {
//		super.logout(request, response, authentication);
//	}
	
	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
								Authentication authentication) throws IOException, ServletException {
		userHolder.clearOnlineUser();
		super.onLogoutSuccess(request, response, authentication);
	}
}
