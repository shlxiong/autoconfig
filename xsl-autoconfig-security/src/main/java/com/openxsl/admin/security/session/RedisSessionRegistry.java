package com.openxsl.admin.security.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.openxsl.admin.context.UserHolder;
import com.openxsl.admin.context.UserSession;
import com.openxsl.admin.dao.UserDao;
import com.openxsl.admin.entity.User;
import com.openxsl.admin.organ.entity.Staff;
import com.openxsl.admin.organ.service.StaffService;
import com.openxsl.admin.security.exception.AnoymousException;

/**
 * 用户登录的Session
 * @author 001327
 * SessionInformation(sessionId, Principal, lastDate, expires)
 */
@Component("sessionRegistry")
public class RedisSessionRegistry implements SessionRegistry,
								ApplicationListener<SessionDestroyedEvent> {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	// <Principal,SessionId> 每个用户有哪些sessionID
	private final ConcurrentMap<Object, Set<String>> principals = new ConcurrentHashMap<Object, Set<String>>();
	// <sessionId, SessionInformation> 每个session的登录信息
	private final Map<String, SessionInformation> sessionIds = new HashMap<String, SessionInformation>();
	
	@Autowired(required=false)  //由spring-session接管分布式存储
	private SessionPrincipalDao sessionDao;
	@Autowired(required=false)
	private UserDao userDao;
	@Autowired(required=false)
	private StaffService staffService;
	@Autowired
	private UserHolder userHolder;
	
	@Override
	public SessionInformation getSessionInformation(String sessionId){
		Assert.hasText(sessionId, "SessionId is mandatory");
		
		synchronized (sessionIds) {
			SessionInformation sessionInfo = sessionIds.get(sessionId);
			if (sessionInfo == null && sessionDao != null){
				sessionInfo = sessionDao.get(sessionId);
				if (sessionInfo != null){
					sessionIds.put(sessionId, sessionInfo);
				}
			}
			return sessionInfo;
		}
	}
	
	@Override
	public void registerNewSession(String sessionId, Object principal) {  //onAuthen
		Assert.hasText(sessionId, "SessionId is mandatory");
		Assert.notNull(principal, "Principal is mandatory");
//		if (getSessionInformation(sessionId) != null) {
//			this.removeSessionInformation(sessionId);
//		}

		logger.info("registerNewSession(id={}), for principal: {}",sessionId,principal);
		UserSession sessionInfo = new UserSession(principal, sessionId, new Date());
		if (principal instanceof User) {
			int userId = ((User)principal).getId();
			if (userDao != null){
				sessionInfo.setUserDetail(userDao.getUserDetail(userId));
			}
			if (staffService != null){
				Staff staff = staffService.getByUserId(userId);
				if (staff != null) {
					staff.setSubCorpCodes(staffService.getSubCorps(staff));
					sessionInfo.setStaff(staff);
				}
				if (!sessionInfo.getUser().isSysAdmin() &&
						sessionInfo.getStaff() == null) {
					throw new AnoymousException("该用户没有分配机构");
				}
			}
			userHolder.setUser(sessionInfo, false);
		}
		
		synchronized (sessionIds) {
			sessionIds.put(sessionId, sessionInfo);
			if (sessionDao != null){
				sessionDao.save(sessionId, sessionInfo);
			}
		}

		Set<String> sessions = principals.get(principal);
		if (sessions == null) {
			sessions = new CopyOnWriteArraySet<String>();
		}
		sessions.add(sessionId);
		principals.putIfAbsent(principal, sessions);
	}
	
	@Override
	public void removeSessionInformation(String sessionId) {
		Assert.hasText(sessionId, "SessionId is mandatory");

		sessionIds.remove(sessionId);
		SessionInformation info = this.getSessionInformation(sessionId);
		if (info == null)   return;

		if (sessionDao != null){
			sessionDao.delete(sessionId);
		}
		Set<String> sessions = principals.get(info.getPrincipal());
		if (sessions == null) {
			return;
		}
		sessions.remove(sessionId);
		if (sessions.isEmpty()) {
			principals.remove(info.getPrincipal());
		}
	}

	@Override
	public List<Object> getAllPrincipals() {
		return new ArrayList<Object>(principals.keySet());
	}

	@Override
	public List<SessionInformation> getAllSessions(Object principal,
			boolean includeExpiredSessions) {
		final Set<String> sessions = this.getSessionIds(principal);
		if (sessions == null) {
			return Collections.emptyList();
		}

		List<SessionInformation> list = new ArrayList<SessionInformation>(sessions.size());
		for (String sessionId : sessions) {
			SessionInformation sessionInformation = getSessionInformation(sessionId);
			if (sessionInformation == null) {
				continue;
			}

			if (includeExpiredSessions || !sessionInformation.isExpired()) {
				list.add(sessionInformation);
			}
		}

		return list;
	}
	public Set<String> getSessionIds(Object principal){
		if (principals.containsKey(principal)) {
			return principals.get(principal);
		}else{
			return Collections.emptySet();
		}
	}

	@Override
	public void refreshLastRequest(String sessionId) {
		Assert.hasText(sessionId, "SessionId required as per interface contract");

		SessionInformation info = getSessionInformation(sessionId);
		if (info != null) {
			info.refreshLastRequest();
		}
	}
	
	@Override
	public void onApplicationEvent(SessionDestroyedEvent event) {
		logger.info("SessionDestroyed: id={}", event.getId());
		removeSessionInformation(event.getId());
	}

}
