package com.openxsl.config.condition;

import java.util.Map;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnMissingBeanCondition implements Condition {

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		Map<String,?> attributeMap = AnnotationMetaUtils.getAnnotationAttributes(
							metadata, ConditionalOnMissingBean.class);
		ConfigurableListableBeanFactory factory = context.getBeanFactory();
		String[] beanIds = (String[])attributeMap.get("name");
		for (String beanId : beanIds) {
			if (factory.containsBean(beanId)) {
				return false;
			}
		}
		Class<?>[] types = (Class<?>[])attributeMap.get("value");
		for (Class<?> type : types) {  // !import(allowEagerInit=false)
			if (factory.getBeanNamesForType(type, true, false).length > 0) {
				return false;
			}
		}
		
		attributeMap.clear();
		factory = null;
		return true;
	}

}
