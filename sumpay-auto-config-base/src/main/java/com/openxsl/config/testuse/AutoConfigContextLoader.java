package com.openxsl.config.testuse;

import java.util.Properties;

import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.support.AbstractGenericContextLoader;

import com.openxsl.config.BootstrapApplication;
import com.openxsl.config.Environment;
import com.openxsl.config.autodetect.AutoConfigRegistryPostProcessor;
import com.openxsl.config.filter.tracing.TraceContext;
import com.openxsl.config.util.BeanUtils;
import com.openxsl.config.util.StringUtils;

/**
 * @author xiongsl
 * 
 * loadContext(): ConfigurableApplicationContext
 *      GenericApplicationContext context = new GenericApplicationContext();

		ApplicationContext parent = mergedConfig.getParentApplicationContext();
		if (parent != null) {
			context.setParent(parent);
		}
		prepareContext(context);
		prepareContext(context, mergedConfig);
		customizeBeanFactory(context.getDefaultListableBeanFactory());
		loadBeanDefinitions(context, mergedConfig);
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		customizeContext(context);
		customizeContext(context, mergedConfig);
		context.refresh();
		context.registerShutdownHook();
		return context;
 */
public class AutoConfigContextLoader extends AbstractGenericContextLoader {
	
	@Override
	protected void prepareContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
		Class<?> testClass = mergedConfig.getTestClass();
		AutoConfig application = AnnotationUtils.findAnnotation(testClass, AutoConfig.class);
		if (application != null) {
			String parentPath = application.configPath();
			if (parentPath.charAt(parentPath.length()-1) != '/') {
				parentPath += '/';
			}
			String appName = application.application();
			if (appName.charAt(appName.length()-1) != '/') {
				appName += '/';
			}
			BootstrapApplication.CONF_PATH = parentPath + appName;
		}else {
			System.out.println(testClass.getName()+" use default CONF_PATH as 'file:/openxsl/conf/'");
		}
		
		Environment.getSpringEnvironment().setActiveProfiles(mergedConfig.getActiveProfiles());
		//给其他框架使用，比如 apollo
		this.setApolloEnviron();
		
		BootstrapApplication.initLogging();
		String resources = BootstrapApplication.initEnvironment(context);
		context.setEnvironment(Environment.getSpringEnvironment());
		
		String[] locations = mergedConfig.getLocations();
		if (locations.length < 1) {
			locations = StringUtils.split(resources, ",");
			BeanUtils.setPrivateField(mergedConfig, "locations", locations);
		}
		System.out.println("    spring context locations: " + StringUtils.join(locations));
		
		super.prepareContext(context, mergedConfig);
	}
	
//	protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory){
//		
//	}
	
//	@Override
//	protected void customizeContext(GenericApplicationContext context) {
//		System.out.println("======================loader"+context);
//	}
	
	@Override
	protected void loadBeanDefinitions(GenericApplicationContext context, MergedContextConfiguration mergedConfig) {
		if (mergedConfig.getLocations().length > 0) {
			new XmlBeanDefinitionReader(context).loadBeanDefinitions(mergedConfig.getLocations());
		}
		if (mergedConfig.getClasses().length > 0) {
			new AnnotatedBeanDefinitionReader(context).register(mergedConfig.getClasses());
		}
		
		//测试程序制定一些属性的值，优先级高于Environment
		this.processPropertySource(mergedConfig);
		//开启自动扫描
		context.addBeanFactoryPostProcessor(
					new AutoConfigRegistryPostProcessor(Environment.getSpringEnvironment()));
	}

	@Override
	protected BeanDefinitionReader createBeanDefinitionReader(GenericApplicationContext context) {
		return new XmlBeanDefinitionReader(context);
	}

	@Override
	protected String getResourceSuffix() {
		return ".xml";
	}
	
	private void processPropertySource(MergedContextConfiguration mergedConfig) {
		Properties testProps = new Properties();
		for (String kv : mergedConfig.getPropertySourceProperties()) {
			int idx = kv.indexOf("=");
			if (idx > -1) {
				testProps.setProperty(kv.substring(0,idx), kv.substring(idx+1));
			} else {
				testProps.setProperty(kv, "");
			}
		}
		if (testProps.size() > 0) {
			Environment.mergeProperties(testProps);
		}
		TraceContext.initiate(null, null);
	}
	
	private void setApolloEnviron() {
		System.setProperty("spring.config.location", BootstrapApplication.CONF_PATH);
		System.setProperty("spring.profiles.active", Environment.getSpringEnvironment().getActiveProfiles()[0]);
	}

}
