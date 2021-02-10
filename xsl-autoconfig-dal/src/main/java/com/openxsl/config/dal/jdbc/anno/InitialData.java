package com.openxsl.config.dal.jdbc.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 初始化数据表
 * 
 * @author shuilin.xiong
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface InitialData {
	
	String table() default "";
	
	String sql() default "";

}
