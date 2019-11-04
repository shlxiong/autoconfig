package com.openxsl.config.dal.freemarker.validator;

import java.util.List;

import com.openxsl.config.dal.freemarker.validator.Validators.Validator;

/**
 * 非空校验
 * @author 001327
 */
public class RequiredValidator implements Validator {

	@Override
	public Object validate(String attr, Object value,
					List<Comparable<?>> compareValues) throws ValidateException {
		if (value==null || value.toString().trim().length()<1){
			throw new ValidateException(String.format("参数(%s=%s)不能为空", attr,value));
		}
		
		return null;
	}

}
