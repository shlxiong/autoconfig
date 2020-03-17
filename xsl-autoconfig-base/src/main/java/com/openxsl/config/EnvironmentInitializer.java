package com.openxsl.config;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.impl.StaticLoggerBinder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;

import ch.qos.logback.core.Appender;
import com.openxsl.config.autodetect.AutoConfigRegistryPostProcessor;
import com.openxsl.config.loader.DomainPropertyLoadSpi;
import com.openxsl.config.logger.LogbackConfigurer;
import com.openxsl.config.logger.context.LoggerContext;
import com.openxsl.config.util.SpringRegistry;
import com.openxsl.config.util.Version;

/**
 * 用在Spring-boot项目中初始化Environment
 * @author xiongsl
 */
public class EnvironmentInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>{

	@Override //AnnotationConfigApplicationContext
	public void initialize(ConfigurableApplicationContext applicationContext) {
		if (Environment.existSpringBoot() &&
				!Environment.exists("com.openxsl.config.boot.OpenxslApplication")) {
			ConfigurableEnvironment environ	= applicationContext.getEnvironment();
			Environment.getSpringEnvironment().merge(environ);
			
			Environment.setConfigPath(this.configPath());
			Environment.setApplication(BootstrapApplication.getApplication(environ));
			Environment.setOwner(BootstrapApplication.getOwner(environ));
			Environment.setAddress(BootstrapApplication.getAddress(environ));
			
			this.addAutoConfigRegistry(applicationContext);
			BootstrapApplication.setApplicationContext(applicationContext);
			LoggerContext.initialize();           //Main TraceID
			BootstrapApplication.initTracing();   //initTracing
		}
	}
	
//	public static void main(String[] args) {
//		
//	}
	/**
	 * 配置文件路径
	 */
	private String configPath() {
		String systemPath = System.getProperty("spring.config.location", BootstrapApplication.CONF_PATH);
		String[] paths = { systemPath,
						  "file:./config/","file:./","classpath:/config/","classpath:/"};
		String[] files = {"application.yml","application.properties","application.yaml"};
		Resource resource;
		for (String path : paths) {
			for (String file : files) {
				file = String.format("%s%s", path,file);
				resource = EnvironmentLoader.getResource(file).getResource();
				try {
					if (resource.isReadable()) {
						String location = resource.getURI().toURL().toExternalForm();
						System.out.println("    springframework configuration: " + location);
						Properties bootstrapProps = EnvironmentLoader.readProperties(location);
						ApolloConfigAdaptor.reloadProperties(bootstrapProps);
						Environment.mergeProperties(bootstrapProps);
						return location.substring(0, location.lastIndexOf("/")+1);
					}
				} catch (IOException e) {
				}
			}
		}
		throw new IllegalStateException("Can't find config file 'application.(yml|properties)'");
	}
	
	@SuppressWarnings("unused")
	private void initLogging() {
		Version.checkDuplicate(StaticLoggerBinder.class, true);
		Version.expectVersion(Appender.class, "1.1.11", true);
    	if (Version.hasResource("slf4j-log4j*.jar", false)) {
    		throw new IllegalStateException("Please remove 'slf4j-log4j*.jar', otherwise will conflict with logback!");
    	}
    	if (Version.hasResource("log4j-1.2*.jar", false)) {
    		throw new IllegalStateException("Please remove 'log4j-1.2*.jar', otherwise will conflict with logback!");
    	}
    	try {
			LogbackConfigurer.shutdownLogging();
		}catch(Throwable t) {
		}
    	System.out.println("LoggingContextInitializer start, order=-999999");
		BootstrapApplication.initLogging();  //Environment.addResource(application.properties)
	}
	
	private void addAutoConfigRegistry(ConfigurableApplicationContext context) {
		Properties props = DomainPropertyLoadSpi.loadProperties();
		if (props.size() > 0) {
			//必须加入全部变量，@see AbstractBeanFactory.resolveEmbeddedValue()
			props.putAll(Environment.prefixProperties(""));
			SpringRegistry.addPlaceholderConfigurer(props, context);
		}
		context.addBeanFactoryPostProcessor(
				new AutoConfigRegistryPostProcessor(Environment.getSpringEnvironment()));
	}

}
