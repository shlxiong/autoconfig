package com.openxsl.admin.security.csrf;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Repository;

@Repository
public class CsrfTokenDaoRedisImpl //extends AbstractRedisDao<String, XhhCsrfToken>
				implements CsrfTokenDao{
//	final String redisKey = "Csrf_" + ContextLoaderListener.SYSTEM_ID + "_${sessionid}";

	@Override
	public XhhCsrfToken get(String tokenId){
		return null;
	}
	
	@Override
	public void save(String tokenId, CsrfToken token){
		
	}
	
	@Override
	public void delete(String sessionId){
		
	}
}
