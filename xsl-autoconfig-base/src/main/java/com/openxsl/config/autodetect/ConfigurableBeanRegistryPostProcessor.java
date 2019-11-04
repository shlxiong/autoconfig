package com.openxsl.config.autodetect;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

/**
 * 方便在启动时往Spring容器中添加xml配置文件
 * @author xiongsl
 */
public class ConfigurableBeanRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
	private String[] resources;
	public ConfigurableBeanRegistryPostProcessor(String[] resources) {
		this.resources = resources;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		new XmlBeanDefinitionReader(registry).loadBeanDefinitions(resources);
	}

}
