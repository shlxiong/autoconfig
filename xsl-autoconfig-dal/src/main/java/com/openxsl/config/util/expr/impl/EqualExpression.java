package com.openxsl.config.util.expr.impl;

import java.util.Map;

import com.openxsl.config.util.expr.AbstractExpression;

public class EqualExpression extends AbstractExpression {

	public EqualExpression(String field, Object operant) {
		super(field, OPER_EQ, operant);
	}

	@Override
	public boolean evaluate(Map<String, ?> valueMap) {
		Object value = this.getEnvirValue(valueMap);
		if (value != null){
			return value.equals(operant);
		}else{
			return operant == null;
		}
	}

	@Override
	public String toSql() {
		return new StringBuilder(getField()).append(" = ").append(operant)
				.toString();
	}

}
