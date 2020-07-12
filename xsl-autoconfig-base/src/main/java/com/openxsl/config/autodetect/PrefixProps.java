package com.openxsl.config.autodetect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 取文件中的含特定前缀的属性，功能同springboot的 "@ConfigurationProperties"
 * @author xiongsl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface PrefixProps {
	
	String name() default "";
	
	String prefix() default "";
	
	String location() default "";
	
	boolean rewrite() default true;
	
	String regexp() default "";

}
