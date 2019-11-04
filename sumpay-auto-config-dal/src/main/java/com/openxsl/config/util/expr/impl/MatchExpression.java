package com.openxsl.config.util.expr.impl;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

import com.openxsl.config.util.expr.AbstractExpression;

public class MatchExpression extends AbstractExpression {
	
	public MatchExpression(String field, String regexpr){
		super(field, OPER_MATCH, regexpr);
		Assert.notNull(regexpr, "regexpr must not empty");
	}

	@Override
	public boolean evaluate(Map<String, ?> valueMap) {
		Object value = this.getEnvirValue(valueMap);
		if (value != null){
			return Pattern.matches(operant.toString(), value.toString());
		}else{
			return false;
		}
	}

	@Override
	public String toSql() {
		// TODO Auto-generated method stub
		return null;
	}

}
