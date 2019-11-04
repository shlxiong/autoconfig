package com.openxsl.config.dal.freemarker.validator;

import java.util.List;
import java.util.regex.Pattern;

import com.openxsl.config.dal.freemarker.validator.Validators.Validator;

/**
 * 人行联行号，12位数字
 * @author 001327
 */
public class BranchNoValidator implements Validator {
	Pattern digit12 = Pattern.compile("\\d{12}");

	@Override
	public Object validate(String attr, Object value, List<Comparable<?>> compareValues)
					throws ValidateException {
		if (value != null){
			if (!digit12.matcher(value.toString()).matches()){
				throw new ValidateException(String.format("银行联行号(%s=%s)必须是12位数字", attr,value));
			}
		}
		
		return null;
	}

}
