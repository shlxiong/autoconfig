package com.openxsl.config.dal.jdbc.anno;

public @interface JpaResultMaps {
	
	JpaResultMap[] value() default {};

}
