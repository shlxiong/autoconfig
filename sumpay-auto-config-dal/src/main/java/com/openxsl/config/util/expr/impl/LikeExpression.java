package com.openxsl.config.util.expr.impl;

import java.util.Map;
import java.util.regex.Pattern;

import com.openxsl.config.util.expr.AbstractExpression;

/**
 * @author xiongsl
 */
public class LikeExpression extends AbstractExpression {
	
	public LikeExpression(String field, String operant){
		super(field, OPER_LIKE, operant);
	}

	@Override
	public boolean evaluate(Map<String, ?> valueMap) {
		Object value = this.getEnvirValue(valueMap);
		if (value != null){
			if (operant == null) {
				return false;
			}
			String regex = operant.toString().replace("%", ".*");
			return Pattern.matches(regex, value.toString());
		}else{
			return operant == null;
		}
	}

	@Override
	public String toSql() {
		return new StringBuilder(getField()).append(" LIKE ").append(operant)
				.toString();
	}
	
	public static void main(String[] args) {
		System.out.println(new LikeExpression("hello,xiong", "%xiong").evaluate(null));
		System.out.println(new LikeExpression("xiong shuilin", "xiong%").evaluate(null));
		System.out.println(new LikeExpression("hello, xiong shuilin", "%xiong%").evaluate(null));
		System.out.println(new LikeExpression("hello, xiong shuilin", "%xiong%lin").evaluate(null));
	}

}
