package com.openxsl.config.condition;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.MethodMetadata;

public class AnnotationMetaUtils {
	
	public static String getClassOrMethodName(AnnotatedTypeMetadata metadata) {
		if (metadata instanceof ClassMetadata) {
			ClassMetadata classMetadata = (ClassMetadata) metadata;
			return classMetadata.getClassName();
		}
		MethodMetadata methodMetadata = (MethodMetadata) metadata;
		return methodMetadata.getDeclaringClassName() + "#"
				+ methodMetadata.getMethodName();
	}
	
	public static Map<String,?> getAnnotationAttributes(
					AnnotatedTypeMetadata metadata,
					Class<? extends Annotation> conditional){
		//标注注解的类或方法
		String classOrMethodName = getClassOrMethodName(metadata);
		try {
			return metadata.getAnnotationAttributes(conditional.getName());
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
