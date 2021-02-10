package com.openxsl.config.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.openxsl.config.config.Alias;
import com.openxsl.config.statis.NameValue;

/**
 * 取常量类或枚举类的属性
 * @author shuilin.xiong
 */
public class ConstantUtils {
	
	/**
	 * 根据枚举值取名称
	 * @param clazz
	 * @param value
	 * @return
	 */
	public static String getText(Class<?> clazz, int value) {
    	Object instance = new Object();
    	try {
	    	for (Field field : clazz.getFields()) {
	    		if (field.getInt(instance) == value) {
	    			return field.getAnnotation(Alias.class).value();
	    		}
	    	}
    	} catch (Exception e) {
    	}
    	return null;
    }
	
	/**
	 * 根据属性名取描述值
	 * @param clazz
	 * @param name
	 */
    public static String getText(Class<?> clazz, final String name) {
    	try {
	    	Field field = clazz.getField(name);
	    	if (field != null) {
	    		return field.getAnnotation(Alias.class).value();
	    	}
    	} catch (Exception e) {
    	}
    	return null;
    }
    
    public static List<NameValue> getNameValues(Class<?> constClazz){
    	List<NameValue> nameValues = new ArrayList<NameValue>();
    	try {
    		Object instance = new Object();
	    	for (Field field : constClazz.getFields()) {
	    		String name = String.valueOf(field.getInt(instance));
	    		String text = field.getAnnotation(Alias.class).value();
	    		nameValues.add(new NameValue() {
					@Override
					public String getName() {
						return name;
					}
					@SuppressWarnings("unchecked")
					@Override
					public String getValue() {
						return text;
					}
				});
	    	}
    	} catch (Exception e) {
    	}
    	return nameValues;
    }

}
