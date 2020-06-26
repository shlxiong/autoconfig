package com.openxsl.config.dal.jdbc.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库索引
 * 
 * @author shuilin.xiong
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Index {
	
	String column() default "";
	
	String value() default "";
	
	boolean unique() default false;

}

