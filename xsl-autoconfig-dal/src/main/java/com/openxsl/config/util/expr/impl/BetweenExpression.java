package com.openxsl.config.util.expr.impl;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

import com.openxsl.config.util.expr.AbstractExpression;

@SuppressWarnings({"rawtypes","unchecked"})
public class BetweenExpression extends AbstractExpression {
	private Comparable[] args = new Comparable[2];

	public BetweenExpression(String field, Object operant) {
		super(field, OPER_BETWEEN, operant);
		
		String errorMsg = "'Between' has at least 2 operants";
		if (operant == null){
			throw new IllegalArgumentException(errorMsg);
		}else{
			try{
				if (operant.getClass().isArray()){
					args[0] = (Comparable)Array.get(operant, 0);
					args[1] = (Comparable)Array.get(operant, 1);
				}else if (operant instanceof List){
					((List<?>)operant).toArray(args);
				}
				if (args[0]==null || args[1]==null){
					throw new IndexOutOfBoundsException();
				}
			}catch(IndexOutOfBoundsException oe){
				throw new IllegalArgumentException(errorMsg);
			}
		}
	}

	@Override
	public boolean evaluate(Map<String,?> valueMap) {
		Object value = this.getEnvirValue(valueMap);
		if (value != null){
			return args[0].compareTo(value)<=0 && args[1].compareTo(value)>=0;
		}
		return false;
	}

	@Override
	public String toSql() {
		return new StringBuilder(getField()).append(" (BETWEEN ")
				.append(args[0]).append(" and ").append(args[1])
				.append(")").toString();
	}

}
