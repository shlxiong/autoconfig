package com.openxsl.config.dal.freemarker.validator;

import java.util.List;
import java.util.regex.Pattern;

public class RegexpValidator extends RequiredValidator {

	@Override
	public Object validate(String attrName, Object value, List<Comparable<?>> compareValues) throws ValidateException {
		super.validate(attrName, value, compareValues);
		
		if (compareValues.isEmpty()){
			throw new ValidateException("缺少正则表达式");
		}
		String strValue = value.toString();
		for (Comparable<?> expr : compareValues){
			if (!Pattern.matches((String)expr, strValue)){
				throw new ValidateException(
						String.format("%s值不符合规则：%s", attrName,expr));
			}
		}
		return null;
	}

}
