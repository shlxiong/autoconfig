package com.openxsl.admin.context;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.context.support.SecurityWebApplicationContextUtils;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * 清空线程上下文的Session
 * 
 * @author shuilin.xiong
 */
@WebListener
public class SessionContextListener extends HttpSessionEventPublisher {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		logger.info("session detroyed: {}", se.getSession().getId());
		LocalUserHolder.clear();
		this.getContext(se).getBean(UserHolder.class).clearOnlineUser();
		super.sessionDestroyed(se);
	}
	
	private ApplicationContext getContext(HttpSessionEvent se) {
		return SecurityWebApplicationContextUtils.findRequiredWebApplicationContext(
						se.getSession().getServletContext());
	}

}