package com.openxsl.config.config;

import java.util.Arrays;
import java.util.Map;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.util.CollectionUtils;

/**
 * JSR-303(bean-validator)判断非空
 * @author xiongsl
 */
public class NotEmptyValidator implements ConstraintValidator<NotEmpty, Object> {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		if (value == null) {
			return false;
		}
		if (value instanceof Iterable) {
			return ((Iterable<?>)value).iterator().hasNext();
		} else if (value.getClass().isArray()) {
			return Arrays.asList(value).size() > 0;
		} else if (value instanceof Map) {
			return !CollectionUtils.isEmpty((Map)value);
		} else if (value instanceof String) {
			return ((String)value).length() > 0;
		}
		return true;
	}

}
