package com.openxsl.config.condition;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.openxsl.config.Environment;
import com.openxsl.config.util.Version;

/**
 * 基于Classpath中是否存在某个类或接口
 * @author xiongsl
 */
public class OnPresentClassCondition implements Condition {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		String classOrMethodName = AnnotationMetaUtils.getClassOrMethodName(metadata);
		try {
			String annotationClass = ConditionalOnPresent.class.getName();
			Map<String,?> attributeMap = metadata.getAnnotationAttributes(annotationClass);
			String[] classes = (String[])attributeMap.get("classes");
			for (String className : classes) {
				if (!Environment.exists(className)) {
					logger.info("ignore {} on-class-present [{}]",
								classOrMethodName, className);
					return false;
				}
			}
			String[] jars = (String[])attributeMap.get("jars");
			for (String location : jars) {
				if (!Version.hasResource(location, false)) {
					logger.info("ignore {} on-jar-present [{}]",
								classOrMethodName, location);
					return false;
				}
			}
			String[] beans = (String[])attributeMap.get("beans");
			for (String bean : beans) {
				if (!context.getBeanFactory().containsBean(bean)) {
					logger.info("ignore {} on-bean-present [{}]",
							classOrMethodName, bean);
					return false;
				}
			}
			
			return true;
		} catch (NoClassDefFoundError ex) {
			throw new IllegalStateException(
					"Could not evaluate condition on " + classOrMethodName + " due to "
							+ ex.getMessage() + " not found. ",
					ex);
		}catch (RuntimeException ex) {
			throw new IllegalStateException(
					"Error processing condition on " /*+ getName(metadata)*/, ex);
		}
	}

}
