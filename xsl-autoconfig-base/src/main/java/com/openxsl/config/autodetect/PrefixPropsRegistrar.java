package com.openxsl.config.autodetect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.type.AnnotationMetadata;

import com.openxsl.config.Environment;
import com.openxsl.config.loader.PrefixProperties;

/**
 * 处理  "@PrefixProps"的动态注入，用法：@Import(PrefixPropsRegistrar.class)
 * @author xiongsl
 */
public class PrefixPropsRegistrar implements ImportBeanDefinitionRegistrar {
	
	/**
	 * 该方法执行在BeanFactoryAware和ApplicationContextAware之间
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		Class<?> sourceClass = null;
		try{
			List<String> importLocations = this.getImportPropertySourceLocation(importingClassMetadata);
			sourceClass = Class.forName(importingClassMetadata.getClassName());
			for (Field field : sourceClass.getDeclaredFields()) {
				if (!field.isAnnotationPresent(PrefixProps.class)) {
					continue;
				}
				PrefixProps annotation = field.getAnnotation(PrefixProps.class);
				String beanName = String.format("%s.%s", sourceClass.getName(), field.getName());
				beanName = "".equals(annotation.name()) ? beanName : annotation.name();
				if (registry.containsBeanDefinition(beanName)) {
					continue;
				}
				List<String> locations = new ArrayList<String>(importLocations);
				if (annotation.location().length() > 0){
					locations.add(annotation.location());
				} 
				
				BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.rootBeanDefinition(PrefixProperties.class);
				String prefix = annotation.prefix();
				if (prefix.startsWith("${") && prefix.endsWith("}")) {
					prefix = prefix.substring(2, prefix.length()-1);
					prefix = Environment.getProperty(prefix);
				}
				beanBuilder.addPropertyValue("prefix", prefix);
				beanBuilder.addPropertyValue("regexp", annotation.regexp());
				beanBuilder.addPropertyValue("configLocations", locations);
				beanBuilder.addPropertyValue("rewriteKeys", annotation.rewrite());
				registry.registerBeanDefinition(beanName, beanBuilder.getBeanDefinition());
			}
		} catch(ClassNotFoundException cnfe) {
			//
		}
		//Environment.prefixProperties(prefix);   //rewrite=false
	}

	private List<String> getImportPropertySourceLocation(AnnotationMetadata classMetadata) {
		List<String> locations = new ArrayList<String>(2);
		locations.add(Environment.getConfigPath() + "/application.properties");
		Map<String,Object> propertySourceAttrs = classMetadata
							.getAnnotationAttributes(PropertySource.class.getName());
		if (propertySourceAttrs != null) {
			String[] sources = (String[])propertySourceAttrs.get("value");
			locations.addAll(Arrays.asList(sources));
		} 
		return locations;
	}

}
