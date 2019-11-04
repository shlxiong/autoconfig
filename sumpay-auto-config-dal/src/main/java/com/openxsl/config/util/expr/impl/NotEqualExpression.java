package com.openxsl.config.util.expr.impl;

import java.util.Arrays;
import java.util.Map;

import com.openxsl.config.util.expr.AbstractExpression;

@SuppressWarnings({"rawtypes","unchecked"})
public class NotEqualExpression extends AbstractExpression {
	
	public NotEqualExpression(String field, String operator, Object operant){
		super(field, operator, operant);
		
		if (operant == null){
			throw new IllegalArgumentException("operant must NOT be null");
		}else{
			String[] supported = {OPER_GT, OPER_GE, OPER_LT, OPER_LE, OPER_NE};
			if (Arrays.asList(supported).indexOf(operator) == -1){
				throw new IllegalArgumentException("Unsupported operator: "+operator);
			}
		}
	}

	@Override
	public boolean evaluate(Map<String,?> valueMap) {
		Object value = this.getEnvirValue(valueMap);
		if (value != null){
			if (OPER_GT.equals(operator)){
				return ((Comparable)operant).compareTo(value) <= 0;
			}else if (OPER_GE.equals(operator)){
				return ((Comparable)operant).compareTo(value) < 0;
			}else if (OPER_LT.equals(operator)){
				return ((Comparable)operant).compareTo(value) >= 0;
			}else if (OPER_LE.equals(operator)){
				return ((Comparable)operant).compareTo(value) > 0;
			}else{
				return !value.equals(operant);
			}
		}else{ //value==null
			return operant != null;
		}
	}
	
	@Override
	public String toSql(){
		return new StringBuilder(getField()).append(" ")
				.append(operator).append(" ").append(operant)
				.toString();
	}
	
	public static void main(String[] args) {
		System.setProperty("hello", "express");
		System.out.println(new EqualExpression("hello","express").evaluate(null));
		System.out.println(new NotEqualExpression("hello","<>","express").evaluate(null));
		System.out.println(new NotEqualExpression("hello",">","experss").evaluate(null));
		System.out.println(new NotEqualExpression("hello","<","experss").evaluate(null));
	}

}
