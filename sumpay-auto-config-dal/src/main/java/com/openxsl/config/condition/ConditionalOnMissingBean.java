package com.openxsl.config.condition;

import org.springframework.context.annotation.Conditional;

@Conditional(OnMissingBeanCondition.class)
public @interface ConditionalOnMissingBean {
	
	Class<?>[] value() default {};

	String[] name() default {};

}
