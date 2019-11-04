package com.openxsl.config.util.expr.impl;

import java.util.Map;

import com.openxsl.config.util.expr.AbstractExpression;

public class NotNullExpression extends AbstractExpression {

	public NotNullExpression(String field) {
		super(field);
		this.operator = OPER_NOTNULL;
	}

	@Override
	public boolean evaluate(Map<String, ?> valueMap) {
		Object value = this.getEnvirValue(valueMap);
		return value != null;
	}

	@Override
	public String toSql() {
		return getField() + " IS NOT null";
	}

}
