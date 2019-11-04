package com.openxsl.config.loader;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.core.annotation.AliasFor;

/**
 * 加载SPI时的条件
 * @see GraceServiceLoader
 * @author xiongsl
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Autoload {
	
	@AliasFor("value")
	String property() default "";
	
	@AliasFor("property")
	String value() default "";
	
	String presentClass() default "";
	
	String missingClass() default "";

}
