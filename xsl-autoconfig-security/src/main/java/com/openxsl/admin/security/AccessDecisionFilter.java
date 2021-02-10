package com.openxsl.admin.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.GenericFilterBean;

import com.openxsl.admin.api.IAccessDecision;
import com.openxsl.admin.api.IRestrictedSource;
import com.openxsl.admin.api.IUser;
import com.openxsl.admin.context.UserHolder;
import com.openxsl.admin.entity.OperationLog;
import com.openxsl.admin.service.OperationLogService;

/**
 * 动态资源权限验证
 */
@Service
public class AccessDecisionFilter extends GenericFilterBean {
//	public static final String USERID = "userId";
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private static final String FILTER_APPLIED = "__spring_security_AccessDecisionFilter_applied";
	
//	@Autowired
//	private AuthenticateService authenticate;
	@Autowired	
	private IAccessDecision accessService;
	@Autowired
	private UserHolder userHolder;
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
						FilterChain chain) throws IOException, ServletException {
		if (request.getAttribute(FILTER_APPLIED) != null) {
            chain.doFilter(request, response);
            return ;
        }
		
		request.setAttribute(FILTER_APPLIED,true);
		HttpServletRequest httpReq = (HttpServletRequest)request;
		String uri = httpReq.getRequestURI().substring(httpReq.getContextPath().length());
		uri = uri.replace("//", "/");
		IRestrictedSource resource = accessService.findResourceByUrl(uri);
		
		String userName = "anonymous";
		Object principal = this.getPrincipal();
		boolean flag = resource != null && resource.getAuthorities().size() > 0;
		if (flag || uri.startsWith("/admin/")) {
			logger.info("request resource: {}", uri);
			try {
				if (principal != null && principal instanceof IUser) {
					IUser user = (IUser)principal;
					userName = user.getUsername();
					if (uri.startsWith("/admin/")) {
						if (!user.isSysAdmin()) {
							throw new AccessDeniedException("没有操作权限");
						}
					} else if (!accessService.accessible(user, resource)) {
						throw new AccessDeniedException("没有操作权限");
					}
//					try{
//						HttpSession session = httpReq.getSession(false);
//						session.setAttribute(USERID, authenticate.getPasswordEncoder().encode(user.getUserId()));
//					}catch(Exception e){
//					}
				} else {
					throw new AccessDeniedException("没有操作权限");
				}
			} finally {
				OperationLog operLog = OperationLogService.getOperateInfo(httpReq, uri);
				operLog.setUserName(userName);
				accessService.accessLog(operLog);
			}
		}
		
		chain.doFilter(request, response);
	}
	private Object getPrincipal() {
		Object principal = null;
		Authentication authen = SecurityContextHolder.getContext().getAuthentication();
		boolean authenticated = authen!=null && authen.isAuthenticated();
		if (authenticated) {
			//AnoymouseUser: SecurityContextPersistenceFilter/HttpSessionSecurityContextRepository
			principal = authen.getPrincipal();
			authenticated = principal instanceof IUser;
		}
		if (!authenticated) {
			try {
				IUser user = userHolder.getUser().getUser();
				if (user != null) {
					SecurityContextHolder.getContext().setAuthentication(
							new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
				}
				principal = user;
			} catch (NullPointerException npe) {
			}
		}
		return principal;
	}

}
