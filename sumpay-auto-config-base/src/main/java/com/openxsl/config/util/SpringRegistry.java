package com.openxsl.config.util;

import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

import com.openxsl.config.autodetect.ConfigurableBeanRegistryPostProcessor;

/**
 * Spring注册
 * @author xiongsl
 */
public final class SpringRegistry {
//	private static AtomicInteger seq = new AtomicInteger(0);
	
	public static boolean register(String name, Class<?> beanClass, Map<String,?> propertyMap,
						BeanDefinitionRegistry registry) {
		if (registry.containsBeanDefinition(name)) {
		    return false;
		}
		
//		BeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(beanClass)
//							.getBeanDefinition();
//		if (propertyMap!=null && propertyMap.size()>0) {
//			for (Map.Entry<String, ?> entry : propertyMap.entrySet()) {
//				definition.getPropertyValues().add(entry.getKey(), entry.getValue());
//		    }
//		}
		registry.registerBeanDefinition(name, buildDefinition(beanClass,propertyMap));
		return true;
	}
	public static <T> T register(String name, Class<T> beanClass, Map<String,?> propertyMap,
					ConfigurableApplicationContext applicationContext) {
		getBeanFactory(applicationContext).registerBeanDefinition(
					name, buildDefinition(beanClass, propertyMap) );
		return applicationContext.getBean(name, beanClass);
	}
	static BeanDefinition buildDefinition(Class<?> beanClass, Map<String,?> propertyMap) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(beanClass);
		if (propertyMap!=null && propertyMap.size()>0) {
			for (Map.Entry<String, ?> entry : propertyMap.entrySet()) {
				builder.addPropertyValue(entry.getKey(), entry.getValue());
			}
			builder.setInitMethodName((String)propertyMap.remove("initMethod"));
			builder.setDestroyMethodName((String)propertyMap.remove("destroyMethod"));
		}
		return builder.getBeanDefinition();
	}
	
	public static DefaultListableBeanFactory getBeanFactory(
						ConfigurableApplicationContext applicationContext) {
		return (DefaultListableBeanFactory) applicationContext.getBeanFactory();
	}
	
	public static void addPlaceholderConfigurer(Properties properties,
					ConfigurableApplicationContext applicationContext) {
		if (properties!=null && properties.size()>0) {
			PropertyPlaceholderConfigurer placeHolder = new PropertyPlaceholderConfigurer();
			placeHolder.setProperties(properties);
			applicationContext.addBeanFactoryPostProcessor(placeHolder);
		}else {
			System.out.println("WARNING: SpringRegistry.addPlaceholderConfigurer()-> properties is empty!");
		}
	}
	/**
	 * 增加spring-bean Xml文件
	 * @param resources
	 */
	public static void addConfigLocations(String[] resources,
					ConfigurableApplicationContext applicationContext) {
		if (resources!=null && resources.length>0) {
			applicationContext.addBeanFactoryPostProcessor(new ConfigurableBeanRegistryPostProcessor(resources));
		}
	}

}
