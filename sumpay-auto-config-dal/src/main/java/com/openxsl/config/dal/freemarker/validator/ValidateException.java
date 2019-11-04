package com.openxsl.config.dal.freemarker.validator;

import com.openxsl.config.exception.ErrorCodes;
import com.openxsl.config.exception.ServiceException;

@SuppressWarnings("serial")
public class ValidateException extends ServiceException{
	
	public ValidateException(String message) {
        super(ErrorCodes.VALIDATION, message);
    }
	public ValidateException(Exception cause){
		super(ErrorCodes.VALIDATION, cause);
	}

}
