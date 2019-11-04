package com.openxsl.config.dal.freemarker.validator;

import java.util.List;

import com.openxsl.config.dal.freemarker.validator.Validators.Validator;

/**
 * 当值为空时，设置一个默认值
 * @author 001327
 */
public class NullSetValidator implements Validator {

	@Override
	public Object validate(String attr, Object value, List<Comparable<?>> parameters) throws ValidateException {
		if (value != null) {
			return null;   //无需替换
		}
		if (parameters.size()<1 || parameters.get(0)==null){
			throw new ValidateException(String.format("参数(%s)不能为空", attr));
		}
		
		return parameters.get(0);
	}

}
