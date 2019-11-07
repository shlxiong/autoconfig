package com.openxsl.config.condition;

import org.springframework.context.annotation.Conditional;

@Conditional(OnMissingBeanCondition.class)
public @interface ConditionalOnMissingBean {
	
	/**
	 * Bean types
	 */
	Class<?>[] value() default {};

	/**
	 * Bean names
	 */
	String[] name() default {};

}
