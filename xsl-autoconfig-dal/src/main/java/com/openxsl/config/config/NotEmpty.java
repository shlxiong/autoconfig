package com.openxsl.config.config;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy={NotEmptyValidator.class} )
public @interface NotEmpty {
	//必须有以下三个方法
	
	String message() default "List/Array/Map不能为空";    //返回 ResourceMessage的key

	/**hibernate很变态，default必须为空*/
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default { };

}
