package com.openxsl.admin.security.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.openxsl.admin.context.UserSession;
import com.openxsl.config.redis.GenericRedisHelper;

@Repository
public class SessionPrincipalDao {
	@Value("${security.session-timeout}")
	private long liveTime = 1800L;  //30min
	private GenericRedisHelper<UserSession> redisHelper;
	
	@Autowired
	public void setRedisHelper(GenericRedisHelper<UserSession> redisHelper) {
		this.redisHelper = redisHelper;
		this.redisHelper.setEntityClass(UserSession.class);
	}

	public UserSession get(String sessionId) {
		String key = this.getCacheKey(sessionId);
		return redisHelper.get(key);
	}

	public void save(String sessionId, UserSession sessionInfo) {
		String key = this.getCacheKey(sessionId);
		redisHelper.save(key, sessionInfo, liveTime);
	}

	public void delete(String sessionId) {
		String key = this.getCacheKey(sessionId);
		redisHelper.delete(key);
	}
	
	public void updateExpires(String sessionId) {
		redisHelper.updateExpires(sessionId, liveTime);
	}
	
	private final String getCacheKey(String sessionId) {
		return "SESSION:" + sessionId;
	}
	
}
