package com.openxsl.config.dal.freemarker.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import com.openxsl.config.util.Patterns;

public class Validators {
	private Map<String, Validator> validatorMap;
	
	public List<String> validate(List<String> expressions, Map<String,?> argsMap){
		List<String> exceptions = new ArrayList<String>(expressions.size());
		for (String expr : expressions){
			try{
				this.validate(expr, argsMap);
			}catch (ValidateException ve){
				exceptions.add(ve.getMessage());
			}
		}
		return exceptions;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void validate(String expr, Map argsMap) throws ValidateException{
		Matcher matcher = Patterns.FUNCTION.matcher(expr);
		if (!matcher.matches() || matcher.group(2).trim().length()<1){
			throw new IllegalArgumentException("错误的校验表达式："+expr);
		}
		String name = matcher.group(1);
		if (validatorMap==null || !validatorMap.containsKey(name)){
			throw new IllegalStateException("没有定义参数校验器：" + name);
		}
		
		String key = null;
		Object value = null;
		List<Comparable<?>> compareValues = new ArrayList<Comparable<?>>(); 
		for (String param : matcher.group(2).split(",")){
			matcher = Patterns.PLACE_HOLDER.matcher(param);
			if (matcher.matches()){ 
				if (key == null){ //Map.key，第一个是变量
					key = matcher.group(1);
					value = argsMap.get(key);
				}else{ //后面的当做引用值
					compareValues.add((Comparable<?>)argsMap.get(key));
				}
			}else{ //常量
				compareValues.add(param);
			}
		}
		if (key == null){
			throw new IllegalStateException("参数校验器缺少变量：" + name);
		}
		
		Object newValue = validatorMap.get(name).validate(key,value, compareValues);
		if (newValue != null){
			argsMap.put(key, newValue);
		}
	}
	
	public void setValidators(Map<String, Validator> validatorMap){
		this.validatorMap = validatorMap;
	}
	
	public interface Validator{
		
		/**
		 * 校验参数
		 * @param attr  参数名
		 * @param value 值
		 * @param compareValues 比较值，可以为空
		 * @throws ValidateException
		 * @return 处理后的新值
		 */
		public Object validate(String attrName, Object value, List<Comparable<?>> compareValues)
				throws ValidateException;
		
	}
	
}
