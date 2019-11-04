package com.openxsl.config.retry.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component    //@Configuration
@Import(QueueRegistrar.class)
public @interface ConfigQueue {
	
	int queueSize() default 1024;
	
	int consumerSize() default 5;
	
	boolean nonRetry() default false;
	
	String retryRef() default "";
	
	int retries() default 2;
	
	int interval() default 2000;
	
	int increament() default 1000;
	
	int timeout() default 60000;
	
	String[] requires() default {};

}
