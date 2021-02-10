package com.openxsl.admin.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import com.openxsl.admin.service.SsoService;
import com.openxsl.config.verifycode.VerifyCodeService;

public class RandcodeFilter extends OncePerRequestFilter {
	@Autowired
	private VerifyCodeService verifyService;
	@Autowired
	private SsoService ssoService;
	@Autowired
    private AuthenticationFailureHandler loginFailureHandler;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String uri = request.getRequestURI().substring(request.getContextPath().length());
		if (uri.equals("/j_security_check")){
			String ssoCode = request.getParameter("sso");
			AuthenticationException ex = null;
            try {
            	if (ssoCode != null) {
            		String userName = request.getParameter("username");
            		if (!ssoService.validate(userName, ssoCode)) {
            			ex = new VerifyCodeException("验证码错误");
            		}
            	} else {
	            	if (!verifyService.validate(request)) {
	    				ex = new VerifyCodeException("验证码错误");
	    			}
            	}
            } catch (Exception e) {
                ex = new VerifyCodeException(e);
            }
            
            if (ex != null) {
            	loginFailureHandler.onAuthenticationFailure(request, response, ex);
                return;
            }
        }
        // 3. 校验通过，就放行
        filterChain.doFilter(request, response);
	}
	
	@SuppressWarnings("serial")
	public class VerifyCodeException extends AuthenticationException {
	    public VerifyCodeException(String msg) {
	        super(msg);
	    }
	    
	    public VerifyCodeException(Exception e) {
	    	super(e.getMessage(), e);
	    }
	}

}
