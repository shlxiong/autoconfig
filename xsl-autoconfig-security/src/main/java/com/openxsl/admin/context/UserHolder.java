package com.openxsl.admin.context;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.openxsl.admin.security.session.SessionPrincipalDao;

@Component
public class UserHolder {
	private final String SESSION_NAME = "newtms_sessionId";
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    @Autowired
	private SessionPrincipalDao sessionDao;
    
    public UserSession getUser() {  //AccessDecisionFilter
    	String sessionId = this.getSessionId();
        return (UserSession)sessionDao.get(sessionId);
    }
    
    public void setUser(UserSession sessionInfo, boolean local) {
    	if (local) {  //SessionInterceptor
    		LocalUserHolder.setUser_(sessionInfo);
    		sessionDao.updateExpires(sessionInfo.getSessionId());
    	} else {  //securiy.SessionRegistry
	    	this.setCookie(sessionInfo.getSessionId());
    	}
    }

    public void clearOnlineUser() {
        sessionDao.delete(this.getSessionId());
    }
    
    private final String getSessionId() {
    	try {
    		return request.getSession(false).getId();
    	} catch (Exception e) {
    		String sessionId = request.getParameter("token");
    		if (sessionId != null && !"undefined".equals(sessionId)) {
    			logger.info("request token: {}", sessionId);
    			return sessionId;
    		}
	        Cookie[] cookies = this.request.getCookies();
	        if (null != cookies) {
	            for (Cookie cooke : cookies) {
	                if (cooke.getName().equals(SESSION_NAME)) {// && "/".equals(cooke.getPath())) {
	                	logger.info("A cookie sessionId is found: {}", cooke.getValue());
	                    return cooke.getValue();
	                }
	            }
	        }
	
	        sessionId = UUID.randomUUID().toString();
	        logger.info("create sessionId: {}", sessionId);
            this.setCookie(sessionId);
	        return sessionId;
    	}
    }
    
    private void setCookie(String sessionId) {
    	Cookie ck = new Cookie(SESSION_NAME, sessionId);
        ck.setPath("/");
        this.response.addCookie(ck);
    }

}
