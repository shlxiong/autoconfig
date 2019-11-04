package com.openxsl.config.exception;

/**
 * AuthenticationException, AccessDeniedException
 * @author 001327
 */
@SuppressWarnings("serial")
public class SecurityException extends SystemException {

	public SecurityException(String message) {
		super(message);
	}
	
	public SecurityException(Throwable e){
		super(e);
	}
	
	public SecurityException(String message, Throwable e){
		super(message, e);
	}

}
