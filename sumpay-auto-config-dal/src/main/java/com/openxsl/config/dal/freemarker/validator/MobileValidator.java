package com.openxsl.config.dal.freemarker.validator;

import java.util.List;

import com.openxsl.config.dal.freemarker.validator.Validators.Validator;
import com.openxsl.config.util.Patterns;

public class MobileValidator implements Validator {

	@Override
	public Object validate(String attr, Object value, List<Comparable<?>> compareValue) 
				throws ValidateException {
		if (value == null){
			throw new ValidateException(String.format("参数[%s]不能为空", attr));
		}
		if (!Patterns.MOBILE.matcher((String)value).matches()){
			String format = "参数(%s=%s)不符合手机号码格式";
			throw new ValidateException(String.format(format, attr,value));
		}
		
		return null;
	}

}
