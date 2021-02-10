package com.openxsl.admin.security.csrf;

import org.springframework.security.web.csrf.CsrfToken;

public interface CsrfTokenDao  {
	
	public CsrfToken get(String tokenId);
	
	public void save(String tokenId, CsrfToken token);
	
	public void delete(String sessionId);

}
