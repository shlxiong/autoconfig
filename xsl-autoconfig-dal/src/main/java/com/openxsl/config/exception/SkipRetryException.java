package com.openxsl.config.exception;

@SuppressWarnings("serial")
public class SkipRetryException extends SystemException {

	public SkipRetryException(String message) {
		super(message);
	}
	public SkipRetryException(Throwable e) {
		super(e);
	}
	public SkipRetryException(String message, Throwable e) {
		super(message, e);
	}

}
