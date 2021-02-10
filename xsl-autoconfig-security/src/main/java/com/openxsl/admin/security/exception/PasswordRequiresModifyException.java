package com.openxsl.admin.security.exception;

import org.springframework.security.core.AuthenticationException;

@SuppressWarnings("serial")
public class PasswordRequiresModifyException extends AuthenticationException{

	public PasswordRequiresModifyException(String msg) {
		super(msg);
	}
	
	public PasswordRequiresModifyException(String msg, Throwable t) {
		super(msg, t);
	}

}
