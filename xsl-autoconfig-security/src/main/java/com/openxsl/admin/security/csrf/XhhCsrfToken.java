package com.openxsl.admin.security.csrf;

import org.springframework.security.web.csrf.CsrfToken;

@SuppressWarnings("serial")
public class XhhCsrfToken implements CsrfToken {
	private String headerName;
	private String parameterName;
	private String token;
	
	public XhhCsrfToken(){}
	public XhhCsrfToken(String headerName, String parameterName, String token){
		this.setHeaderName(headerName);
		this.setParameterName(parameterName);
		this.setToken(token);
	}

	@Override
	public String getHeaderName() {
		return headerName;
	}

	@Override
	public String getParameterName() {
		return parameterName;
	}

	@Override
	public String getToken() {
		return token;
	}

	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
