package com.openxsl.config.startup.autodetect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 为Dubbo提供mock分组
 * @author xiongsl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DubboMock {

	String version() default "";
	
}
