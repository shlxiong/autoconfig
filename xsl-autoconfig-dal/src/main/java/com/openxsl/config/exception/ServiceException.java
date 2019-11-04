package com.openxsl.config.exception;

/**
 * 业务系统抛出的异常，带有errorCode
 * @author 001327
 */
@SuppressWarnings("serial")
public class ServiceException extends Exception{
	private int code;
	
	public ServiceException(int code){
		this.code = code;
	}
	public ServiceException(int code, Throwable cause){
		super(cause);
		this.code = code;
	}
	public ServiceException(int code, String message){
		super(message);
		this.code = code;
	}
	
	public ServiceException(ErrorCodes errorCode){
		this.code = errorCode.code();
	}
	public ServiceException(ErrorCodes errorCode, Throwable cause){
		this(errorCode.code(), cause);
	}
	public ServiceException(ErrorCodes errorCode, String message){
		this(errorCode.code(), message);
	}
	
	public int getCode(){
		return this.code;
	}
}
