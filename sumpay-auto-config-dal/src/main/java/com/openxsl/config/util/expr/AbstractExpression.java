package com.openxsl.config.util.expr;

import java.util.Map;

/**
 * 一个表达式对象包含三部分：变量名，运算符，操作数
 * 若变量名以$开头，则实际值从环境变量(System)中取，否则变量名就是一个实际值
 *    evaluate(Map):boolean 判断表达式是否成立(true)
 *    toSql():String Sql表达式转换为sql语句
 * 
 * @author 001327
 */
public abstract class AbstractExpression {
	public static final String OPER_EQ = "=";
	public static final String OPER_NE = "<>";
	public static final String OPER_GT = ">";
	public static final String OPER_GE = ">=";
	public static final String OPER_LT = "<";
	public static final String OPER_LE = "<=";
	public static final String OPER_BETWEEN = "between";
	public static final String OPER_LIKE = "like";
	public static final String OPER_MATCH = "match";
	public static final String OPER_IN = "in";
	public static final String OPER_EMPTY = "empty";
	public static final String OPER_NOTNULL = "notNull";
	
	/**表示是一个变量，用于evaluate()*/
	public static final char FIELD_PREFIX = '$';
	
	protected String field;
	protected String operator;
	protected Object operant;
	
	public AbstractExpression(String field, String operator, Object operant){
		this.field = field;
		this.operator = operator;
		this.operant = operant;
	}
	public AbstractExpression(String field){
		this.field = field;
	}
	
	public abstract boolean evaluate(Map<String,?> valueMap);
	
	public abstract String toSql();
	
	public String getField() {
		if (field.charAt(0) == FIELD_PREFIX){
			return field.substring(1);
		}else{
			return field;
		}
	}

	public String getOperator() {
		return operator;
	}

	public Object getOperant() {
		return operant;
	}
	public AbstractExpression setOperant(Object operant){
		this.operant = operant;
		return this;
	}
	
	protected Object getEnvirValue(Map<String,?> valueMap){
		if (field.charAt(0) != FIELD_PREFIX){
			return field;   //field是一个值
		}else{
			String key = field.substring(1);
			Object value = null;
			if (valueMap != null){
				value = valueMap.get(key);
			}
			if (value == null){
				value = System.getProperty(key, System.getenv(key));
			}
			return value;
		}
	}

}
