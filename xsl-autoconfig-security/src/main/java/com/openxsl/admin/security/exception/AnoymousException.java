package com.openxsl.admin.security.exception;

import org.springframework.security.core.AuthenticationException;

@SuppressWarnings("serial")
public class AnoymousException extends AuthenticationException{

	public AnoymousException(String msg) {
		super(msg);
	}
	public AnoymousException(String msg, Throwable t) {
		super(msg, t);
	}

}

