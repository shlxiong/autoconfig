package com.openxsl.config.testuse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoConfig {

	String application();
	
	String configPath() default "file:/openxsl/conf/";
	
	String loggingPath() default "file:/openxsl/logs";
	
}
