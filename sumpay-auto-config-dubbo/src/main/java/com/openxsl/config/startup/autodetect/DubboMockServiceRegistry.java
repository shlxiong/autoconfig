package com.openxsl.config.startup.autodetect;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.AnnotationConfigRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ProviderConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;

import com.openxsl.config.Environment;

/**
 * 根据Mock注解 ，暴露Dubbo服务的“Mock”分组  
 * 作为一个新的上下文，避免污染
 * @see AnnotationConfigApplicationContext
 * @author xiongsl
 */
public class DubboMockServiceRegistry extends GenericApplicationContext
			implements AnnotationConfigRegistry {
	public static final String GROUP = "mock";
	public static final String PROP_MOCK = "dubbo.mock";
	
	private final AnnotatedBeanDefinitionReader reader;
	private final ClassPathBeanDefinitionScanner scanner;
	private Properties dubboProps;
	
	public DubboMockServiceRegistry() {
		this.reader = new AnnotatedBeanDefinitionReader(this);
		this.scanner = new ClassPathBeanDefinitionScanner(this, false);
		this.scanner.addIncludeFilter(new AnnotationTypeFilter(DubboMock.class));
		this.setEnvironment(Environment.getSpringEnvironment());
	}
	
	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		super.setEnvironment(environment);
		this.reader.setEnvironment(environment);
		this.scanner.setEnvironment(environment);
		this.dubboProps = this.getDubboProperties(environment, "spring.dubbo.");
		System.setProperty(PROP_MOCK, dubboProps.getProperty(PROP_MOCK, "false"));
	}
	
	@Override
	public void register(Class<?>... annotatedClasses) {
		this.reader.register(annotatedClasses);
	}

	@Override
	public void scan(String... basePackages) {
		this.scanner.scan(basePackages);
	}
	
	@Override
	protected void prepareRefresh() {
		this.scanner.clearCache();
		super.prepareRefresh();
	}

//	override BeanDefinitionRegistry
//	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
//		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(
//					registry, false);
//		scanner.setEnvironment(environ);
//		scanner.setIncludeAnnotationConfig(true);
//		scanner.addIncludeFilter(new AnnotationTypeFilter(DubboMock.class));
//		scanner.scan(packages.split(","));
//	}
	
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		String packages = dubboProps.getProperty("dubbo.scan", "");
		if (packages.length() == 0) {
			return;
		}
		
		System.out.println("    scan dubbo packages: " + packages);
		this.scan(StringUtils.split(packages, ","));
		
		String[] beanNames = beanFactory.getBeanNamesForAnnotation(DubboMock.class);
		if (beanNames==null || beanNames.length<1) {
			return;
		}
		for (String name : beanNames) {
			this.registryDubbo(beanFactory.getBean(name));
		}
	}
	
	private Properties getDubboProperties(ConfigurableEnvironment environ, String prefix) {
		Properties target = new Properties();
		Set<String> keySet;
		for (PropertySource<?> source : environ.getPropertySources()) {
			if (source.getSource() instanceof Map) {  //systemProperties, systemEnv, or Mapped
				keySet = ((Map<String,?>)source.getSource()).keySet();
				for (String key : keySet) {
					if (key.startsWith(prefix) && !target.containsKey(key)) {
						target.setProperty(key.replaceFirst(prefix, "dubbo."),
									environ.getProperty(key));
					}
				}
			}
		}
		return target;
	}
	
	private void registryDubbo(Object localService) {
		Class<?> clazz = localService.getClass();
		if (localService.getClass().isInterface()) {
			return;
		}
		ServiceConfig<Object> service = new ServiceConfig<Object>();
		service.setRef(localService);
		service.setGroup(GROUP);
		service.setInterface(clazz.getInterfaces()[0]);
		service.setVersion(clazz.getAnnotation(DubboMock.class).version());
		
		String appName = dubboProps.getProperty("dubbo.application.name", "MockDemo");
		service.setApplication(new ApplicationConfig(appName));
		
		String address = dubboProps.getProperty("dubbo.registry.address", "zookeeper://127.0.0.1:2181");
		RegistryConfig registryConfig = new RegistryConfig(address);
		registryConfig.setUsername(dubboProps.getProperty("dubbo.registry.username"));
		registryConfig.setPassword(dubboProps.getProperty("dubbo.registry.password"));
		service.setRegistry(registryConfig);
		
		int port = 20880;
		try{
			port = Integer.parseInt(dubboProps.getProperty("dubbo.protocol.port"));
		}catch(Exception e) {
		}
		service.setProtocol(new ProtocolConfig("dubbo",port));
		
		ProviderConfig provider = new ProviderConfig();
		provider.setFilter(dubboProps.getProperty("dubbo.provider.filter"));
		provider.setHost(dubboProps.getProperty("dubbo.protocol.host"));
		int timeout = Constants.DEFAULT_TIMEOUT;
		try {
			timeout = Integer.parseInt(dubboProps.getProperty("dubbo.provider.timeout"));
		}catch(Exception e) {
		}
		provider.setTimeout(timeout);
		service.setProvider(provider);
		
		service.export();
	}

}
