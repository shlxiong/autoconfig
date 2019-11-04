package com.openxsl.config.exception;

@SuppressWarnings("serial")
public class SystemException extends RuntimeException{
	
	public SystemException(String message){
		super(message);
	}
	
	public SystemException(Throwable e){
		super(e);
	}
	
	public SystemException(String message, Throwable e){
		super(message, e);
	}
}
