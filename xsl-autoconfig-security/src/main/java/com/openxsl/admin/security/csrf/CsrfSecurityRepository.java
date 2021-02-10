package com.openxsl.admin.security.csrf;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * HttpSessionCsrfTokenRepository不会清除Token
 * @author 001327
 */
@Service
public class CsrfSecurityRepository implements CsrfTokenRepository {
	private static final String DEFAULT_CSRF_PARAMETER_NAME = "_csrf";
	private static final String DEFAULT_CSRF_HEADER_NAME = "X-CSRF-TOKEN";
	private static final String DEFAULT_CSRF_TOKEN_ATTR_NAME = HttpSessionCsrfTokenRepository.class
			.getName().concat(".CSRF_TOKEN");

	private String parameterName = DEFAULT_CSRF_PARAMETER_NAME;
	private String headerName = DEFAULT_CSRF_HEADER_NAME;
	private String sessionAttributeName = DEFAULT_CSRF_TOKEN_ATTR_NAME;
	
	/**2016-10-10 将Token放到Redis里面*/
	@Autowired(required=false)
	private CsrfTokenDao distributeDao;
	//code injection
	public void setCsrfTokenDao(CsrfTokenDao distributeDao){
		this.distributeDao = distributeDao;
	}
	
	/**
	 * 新增加的方法
	 */
	public void removeToken(HttpServletRequest request){
		HttpSession session = request.getSession(false);
		if (session != null){
			session.removeAttribute(sessionAttributeName);
			if (distributeDao != null){
				distributeDao.delete(session.getId());
			}
		}
	}
	
	@Override
	public void saveToken(CsrfToken token, HttpServletRequest request,
						HttpServletResponse response) {
		if (token == null) {  //清空
			this.removeToken(request);
		} else {
			HttpSession session = request.getSession();
			session.setAttribute(sessionAttributeName, token);
			if (distributeDao != null){
				distributeDao.save(session.getId(), token);
			}
		}
	}

	@Override
	public CsrfToken loadToken(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		CsrfToken token = null;
		if (session != null){
			token = (CsrfToken) session.getAttribute(sessionAttributeName);
		}
		if (token == null && distributeDao != null){
			String sessionId = request.getParameter("tsid");
			if (sessionId != null && distributeDao != null){
				token = distributeDao.get(sessionId);
			}
		}
		return token;
	}
	
	@Override
	public CsrfToken generateToken(HttpServletRequest request) {
		return new XhhCsrfToken(headerName, parameterName, createNewToken());
	}

	/**
	 * Sets the {@link HttpServletRequest} parameter name that the {@link CsrfToken} is
	 * expected to appear on
	 * @param parameterName the new parameter name to use
	 */
	public void setParameterName(String parameterName) {
		Assert.hasLength(parameterName, "parameterName cannot be null or empty");
		this.parameterName = parameterName;
	}

	/**
	 * Sets the header name that the {@link CsrfToken} is expected to appear on and the
	 * header that the response will contain the {@link CsrfToken}.
	 *
	 * @param headerName the new header name to use
	 */
	public void setHeaderName(String headerName) {
		Assert.hasLength(headerName, "headerName cannot be null or empty");
		this.headerName = headerName;
	}

	/**
	 * Sets the {@link HttpSession} attribute name that the {@link CsrfToken} is stored in
	 * @param sessionAttributeName the new attribute name to use
	 */
	public void setSessionAttributeName(String sessionAttributeName) {
		Assert.hasLength(sessionAttributeName,
				"sessionAttributename cannot be null or empty");
		this.sessionAttributeName = sessionAttributeName;
	}

	private String createNewToken() {
		return UUID.randomUUID().toString();
	}
	
}
