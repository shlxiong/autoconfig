package com.openxsl.config.startup;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.openxsl.config.Environment;

/**
 * 继承DispatcherServlet，可增加BeanFactoryPostProcessor
 * @author xiongsl
 */
@SuppressWarnings("serial")
public class EnvirBootServlet extends DispatcherServlet {
	private final List<BeanFactoryPostProcessor> postProcessors =
					new ArrayList<BeanFactoryPostProcessor>(4);
	
	public EnvirBootServlet(Properties properties) {
		if (properties!=null && properties.size()>0) {
			PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
			configurer.setProperties(properties);
			postProcessors.add(configurer);
		}
	}
	
	public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor factoryProcessor) {
		postProcessors.add(factoryProcessor);
	}
	
	@Override
	protected WebApplicationContext createWebApplicationContext(ApplicationContext parent) {
		Class<?> contextClass = getContextClass();
		if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
			throw new ApplicationContextException(
					"Fatal initialization error in servlet with name '" + getServletName() +
					"': custom WebApplicationContext class [" + contextClass.getName() +
					"] is not of type ConfigurableWebApplicationContext");
		}
		ConfigurableWebApplicationContext wac =
				(ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);

		wac.setEnvironment(Environment.getSpringEnvironment());
		wac.setParent(parent);
		wac.setConfigLocation(getContextConfigLocation());
		
		for (BeanFactoryPostProcessor processor : postProcessors) {  //TODO("xiongsl")
			wac.addBeanFactoryPostProcessor(processor);
		}
		configureAndRefreshWebApplicationContext(wac);

		return wac;
	}

}
