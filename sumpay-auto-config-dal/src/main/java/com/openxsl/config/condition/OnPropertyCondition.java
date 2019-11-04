package com.openxsl.config.condition;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.MethodMetadata;

import com.openxsl.config.Environment;

/**
 * 基于属性的判断条件（@ConditionalProperty）
 * @see com.openxsl.config.dal.jdbc.impl.MybatisDaoImpl
 * 
 * @author xiongsl
 */
public class OnPropertyCondition implements Condition {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * @param context ConditionContext
	 * @param metadata AnnotatedTypeMetadata 所有的注解信息
	 */
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		String classOrMethodName = getClassOrMethodName(metadata);  //标注注解的类或方法
		try {
			String annotationClass = ConditionalProperty.class.getName();
			Map<String,?> attributeMap = metadata.getAnnotationAttributes(annotationClass);
			String attrPrefix = (String)attributeMap.get("prefix");
			attrPrefix = "".equals(attrPrefix) ? "spring" : attrPrefix;
			String expectValue = (String)attributeMap.get("havingValue");
			boolean defaultMatches = (Boolean)attributeMap.get("matchIfMissing");
			boolean flag = false;
			String property = "", actualValue = null;
			for (String name : (String[])attributeMap.get("name")) {
				property = String.format("%s.%s", attrPrefix,name);
				actualValue = Environment.getProperty(property);
							//context.getEnvironment().getProperty(name)
				flag = flag || expectValue.equals(actualValue);
				if (logger.isDebugEnabled()) {
					logger.debug("looking up on-property ['{}'='{}'], but found: {}", 
								property,expectValue, actualValue);
				}
			}
			flag = flag || (actualValue==null && defaultMatches);
			if (flag) {
				logger.info("loading {} on-property ['{}'={}]",
							classOrMethodName, property,expectValue);
			}
			return flag;
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
	
	private String getClassOrMethodName(AnnotatedTypeMetadata metadata) {
		if (metadata instanceof ClassMetadata) {
			ClassMetadata classMetadata = (ClassMetadata) metadata;
			return classMetadata.getClassName();
		}
		MethodMetadata methodMetadata = (MethodMetadata) metadata;
		return methodMetadata.getDeclaringClassName() + "#"
				+ methodMetadata.getMethodName();
	}
	
	//TODO
//	public void getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
//		String annotationClass = ConditionalProperty.class.getName();
//		MultiValueMap<String, Object> attributeMap = metadata.getAllAnnotationAttributes(annotationClass);
//		List<AnnotationAttributes> allAnnotationAttributes = 
//						this.getAttributesFromMultiValueMap(attributeMap);
//		List<ConditionMessage> noMatch = new ArrayList<ConditionMessage>();
//		List<ConditionMessage> match = new ArrayList<ConditionMessage>();
//		for (AnnotationAttributes annotationAttributes : allAnnotationAttributes) {
//			ConditionOutcome outcome = determineOutcome(annotationAttributes,
//					context.getEnvironment());
//			(outcome.isMatch() ? match : noMatch).add(outcome.getConditionMessage());
//		}
//		if (!noMatch.isEmpty()) {
//			return ConditionOutcome.noMatch(ConditionMessage.of(noMatch));
//		}
//		return ConditionOutcome.match(ConditionMessage.of(match));
//	}
	
//	private void recordEvaluation(ConditionContext context, String classOrMethodName,
//							ConditionOutcome outcome) {
//		if (context.getBeanFactory() != null) {
//			ConditionEvaluationReport.get(context.getBeanFactory())
//					.recordConditionEvaluation(classOrMethodName, this, outcome);
//		}
//	}

}
