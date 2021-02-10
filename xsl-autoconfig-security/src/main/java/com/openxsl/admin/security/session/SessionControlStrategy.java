package com.openxsl.admin.security.session;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.stereotype.Component;

import com.openxsl.admin.service.AuthenticateService;

/**
 * ConcurrentSessionControlStrategy
 *   1、 攻击者预先设定session id，让合法用户使用这个session id来访问；
 *   2、 目标用户携带攻击者设定的Session ID登录站点；
 *   3、 攻击者通过Session ID获得合法会话
 * 
 * @author 001327
 */
@Component("sessionStrategy")
public class SessionControlStrategy extends SessionFixationProtectionStrategy {
	@Autowired
	private SessionRegistry sessionRegistry;
	private int maximumSessions = -1;    //最大session个数
	private boolean exceptionIfMaximumExceeded = false;    //超过最大数是否抛异常
	
	@Autowired
	private AuthenticateService authenticate;
	
	@Override
	public void onAuthentication(Authentication authentication,
					HttpServletRequest request, HttpServletResponse response) {
		this.checkIfAllowedExceed(authentication.getPrincipal(), request);
		//验证密码复杂度
		authenticate.checkPassword(authentication);
		//会产生新的session
        super.onAuthentication(authentication, request, response);
        
        this.sessionRegistry.registerNewSession(request.getSession().getId(),
        			authentication.getPrincipal());
	}
	
	public void setMaximumSessions(int maximumSessions) {
		this.maximumSessions = maximumSessions;
	}

	public void setExceptionIfMaximumExceeded(boolean exceptionIfMaximumExceeded) {
		this.exceptionIfMaximumExceeded = exceptionIfMaximumExceeded;
	}
	
	private void checkIfAllowedExceed(Object principal, HttpServletRequest request){
		HttpSession session = request.getSession(false);
		List<SessionInformation> sessions = this.sessionRegistry.getAllSessions(principal, false);
        int sessionCount = sessions.size();
        if (session==null || sessionCount < maximumSessions || maximumSessions == -1) {
        	return;    //新用户或没达上限
        }
        
		for (SessionInformation si : sessions) {
			if (si.getSessionId().equals(session.getId())) {  //已经存在的
				return;
			}
		}

		if (exceptionIfMaximumExceeded) {  //不允许后面的登录
			throw new SessionAuthenticationException("您的账号已在其他地方登陆，请确认密码是否被盗");
		}else{
			SessionInformation leastRecentlyUsed = null;
			for (SessionInformation sess : sessions) {
				if ((leastRecentlyUsed == null)
						|| sess.getLastRequest().before(leastRecentlyUsed.getLastRequest())) {
					leastRecentlyUsed = sess;
				}
			}
			leastRecentlyUsed.expireNow();  //把最早的强制下线
		}
	}
	
}
