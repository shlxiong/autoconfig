package com.openxsl.config.autodetect;

/**
 * 我们的“@Component”注解，防止springboot自动扫描到
 * @author xiongsl
 */
public @interface ScanConfig {
	
	String value() default "";

}
