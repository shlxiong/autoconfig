package com.openxsl.config;

import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringValueResolver;

import com.ctrip.framework.apollo.spring.annotation.ApolloAnnotationProcessor;
import com.ctrip.framework.apollo.spring.annotation.ApolloJsonValueProcessor;
import com.ctrip.framework.apollo.spring.config.PropertySourcesProcessor;
import com.ctrip.framework.apollo.spring.property.SpringValueDefinitionProcessor;
import com.openxsl.config.apollo.SpringValueBeanProcessor;
import com.openxsl.config.util.BeanUtils;
import com.openxsl.config.util.SpringRegistry;
//import com.openxsl.configuration.apollo.ApolloPropertyInitializer;
//import com.openxsl.configuration.startup.ApolloConfigService;

/**
 * Ctrip-Apollo配置中心适配器
 * @author xiongsl
 */
public class ApolloConfigAdaptor {
	private static Logger logger = LoggerFactory.getLogger(ApolloConfigAdaptor.class);
	private static Boolean actived = null;
	
	/**
	 * 加载配置中心（Apollo）的属性，并写入Properties对象
	 * @param source 初始的属性对象
	 * 
	 * @see EnvironmentLoader#getPropertySource(String)
	 */
	public static void reloadProperties(Properties source) {
		if (source == null) {
			return;
		}
		if (actived == null) {
			actived = ApolloConfigAdaptor.actived(source);
		}
		if (actived) {  //二方包依赖，暂时去掉
//			String[] namespaces = ApolloPropertyInitializer.getNamespaces();  //initiate System.props
//			PropertySourcesProcessor.addNamespaces(Arrays.asList(namespaces), 0);
//			
//			Config config;
//			for (String ns : namespaces) {
//				config = ApolloConfigService.getConfig(ns);
//				for (String key : config.getPropertyNames()) {
//					source.setProperty(key, config.getProperty(key, null));
//				}
//			}
		}
	}
	
	/**
	 * 将Apollo的一些BeanFactoryPostProcessor加入到Spring上线文中 
	 * @param context
	 * 
	 * @see BootstrapApplication#initEnvironment(ConfigurableApplicationContext)
	 */
	public static void registerPropertySourcesProcessors(ConfigurableApplicationContext context) {
		if (actived) {
			context.addBeanFactoryPostProcessor(new SpringValueDefinitionProcessor());
			PropertySourcesProcessor processor = new PropertySourcesProcessor();
			processor.setEnvironment(Environment.getSpringEnvironment());
			context.addBeanFactoryPostProcessor(processor);
			context.addBeanFactoryPostProcessor(new ApolloConfigRegistryProcessor());   //add BeanPostProcessor
		}
	}
	
	public static boolean usedApollo() {
		return actived;
	}
	static boolean actived(Properties environ) {
		String key = "spring.application.config";
		return Environment.exists("com.ctrip.framework.apollo.Config")
				&& environ.containsKey(key)
				&& "apollo".equalsIgnoreCase(environ.getProperty(key));
	}
	
	public static class ApolloConfigRegistryProcessor implements BeanDefinitionRegistryPostProcessor{

		@Override
		public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
			//com.openxsl.config.apollo.SpringValueBeanProcessor
			SpringRegistry.register(SpringValueBeanProcessor.class.getName(), 
									SpringValueBeanProcessor.class, null, registry);
			SpringRegistry.register(ApolloAnnotationProcessor.class.getName(), 
									ApolloAnnotationProcessor.class, null, registry);
			SpringRegistry.register(ApolloJsonValueProcessor.class.getName(), 
									ApolloJsonValueProcessor.class, null, registry);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
			//beanFactory.addEmbeddedValueResolver(new ApolloConfigValueResollver());
			//At first
			Object valResolvers = BeanUtils.getPrivateField(beanFactory, "embeddedValueResolvers");
			((List<StringValueResolver>)valResolvers).add(0, new ApolloConfigValueResollver());
		}
		
	}
	
	/**
	 * Apollo修改属性之后，同步更新Bean属性时，使用 beanFactory.resolveEmbeddedValue()
	 * @author xiongsl
	 */
	static class ApolloConfigValueResollver implements StringValueResolver{

		@Override
		public String resolveStringValue(final String strVal) {
			String value = null;
			String propKey = strVal;
			if (strVal.startsWith("${")) {
				propKey = strVal.substring(2, strVal.length()-1);
				propKey = propKey.split(":")[0];  //name:defValue
			}
//			logger.info("ApolloConfigValueResollver process....");
//			for (String namespace : ApolloPropertyInitializer.getNamespaces()) {
//				value = ConfigService.getConfig(namespace).getProperty(propKey, null);
//				if (value != null) {
//					logger.info("Config-item ({}={}) is found from namespace: {}",
//								propKey, value, namespace);
//					break;
//				}
//			}
			return (value==null) ? strVal : value;
		}
		
	}

}
