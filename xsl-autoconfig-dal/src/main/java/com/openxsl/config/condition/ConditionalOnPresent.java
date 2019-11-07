package com.openxsl.config.condition;

import org.springframework.context.annotation.Conditional;

/**
 * 存在某些类/jars/SpringBean
 * 
 * @author xiongsl
 */
@Conditional(OnPresentCondition.class)
public @interface ConditionalOnPresent {
	
	/**
	 * 是否存在某些些类
	 */
	String[] classes() default {};
	
	/**
	 * 是否存在某些 jar
	 */
	String[] jars() default {};
	
	/**
	 * 是否存在某些spring-bean
	 */
	String[] beans() default {};
	
	/**
	 * 是否存在某些属性
	 * @return
	 */
	String[] properties() default {};

}
