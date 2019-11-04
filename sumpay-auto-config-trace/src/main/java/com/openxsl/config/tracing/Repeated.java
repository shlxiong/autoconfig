package com.openxsl.config.tracing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * schedule是否重复，当scheduler.scheduleAtFixedRate()或scheduleWithFixedDelay()时为true，
 * 当scheduler.schedule()为true
 * @author xiongsl
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Repeated {
	
	boolean loop() default true;

}
