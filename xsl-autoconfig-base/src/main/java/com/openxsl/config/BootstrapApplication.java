package com.openxsl.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ResourceUtils;

import ch.qos.logback.core.joran.spi.JoranException;
import com.openxsl.config.filter.tracing.TraceContext;
import com.openxsl.config.filter.tracing.TracingCollector;
import com.openxsl.config.loader.DomainPropertyLoadSpi;
import com.openxsl.config.loader.DubboPropertiesLoader;
import com.openxsl.config.loader.OtherPropertiesLoader;
import com.openxsl.config.loader.RedisPropertiesLoader;
import com.openxsl.config.loader.RocketPropertiesLoader;
import com.openxsl.config.logger.LogbackConfigurer;
import com.openxsl.config.logger.context.LoggerContext;
import com.openxsl.config.util.NetworkUtils;
import com.openxsl.config.util.SpringRegistry;
import com.openxsl.config.util.StringUtils;

/**
 * 设置测试用例的环境
 * @author xiongsl
 * @modify 2019-03-06 增加 context（springboot环境）
 * @modify 2019-05-06 增加Apollo ConfigServer
 * @modify 2019-05-27 由于Apollo的需要，提前初始化logging
 */
public class BootstrapApplication {
	public static String CONF_PATH = "file:/openxsl/conf/";  //testuse.AutoConfigContextLoader
	private static ConfigurableApplicationContext context;
	
	/**
	 * 初始化日志上下文
	 */
	public static void initLogging() {
		String logConfig = initLoggingConfig();
		try {
			LogbackConfigurer.initLogging(logConfig);
			LoggerContext.initialize();   //traceId
		} catch (FileNotFoundException e) {
			System.err.println("No logback configuration file found at [" + logConfig + "]");
		} catch (JoranException e) {
			throw new RuntimeException("Unexpected error while configuring logback", e);
		}
		setEnvironSource();
		initTracing();
	}
	
	static void initTracing() {
		TraceContext.initiate(null, null);
		TracingCollector.setT1(null);
	}
	
	/**
	 * 初始化Spring的属性，并返回context根目录
	 */
	public static String initEnvironment(ConfigurableApplicationContext applicationContext) {
		final Properties props = new Properties();
		props.putAll(OtherPropertiesLoader.loadProperties());
		props.putAll(RedisPropertiesLoader.loadProperties());
		if (Environment.exists("com.openxsl.config.startup.RocketContextInitializer",
						       "cn.openxsl.rocketmq.Producer")) { //老的Producer(alibaba.rocketmq)
			props.putAll(RocketPropertiesLoader.loadProperties());
		}
		
		//查找Root上下文
		String config = String.format("classpath*:spring/%s-*.xml",
			 					Environment.getApplication());
		config = Environment.getProperty(Environment.ROOT_CONTEXT, config);
		config += loadJdbcContext(props);
		config += loadDubboContext(props);
		
		props.putAll(DomainPropertyLoadSpi.loadProperties());
		
		if (props.size() > 0) {
			SpringRegistry.addPlaceholderConfigurer(props, applicationContext);
		}
		ApolloConfigAdaptor.registerPropertySourcesProcessors(applicationContext);
		context = applicationContext;
		return config;
	}
	
	public static ConfigurableApplicationContext getApplicationContext() {
		return context;
	}
	public static void setApplicationContext(ConfigurableApplicationContext springCtx) {
		context = springCtx;
	}
	
	
	//LoggingContextInitializer
	private static String initLoggingConfig() {
		globalEnviron();
		
		String logConfig = Environment.getProperty("logging.config");
		String[] configLocations = {
				Environment.getConfigPath() + ("".equals(logConfig)?"logback-spring.xm":logConfig),
					"classpath:logback-spring.xml", "classpath:logback.xml"
				};
		for (String location : configLocations) {
			try {
				ResourceUtils.getURL(location).openStream().close();  //exists file
				logConfig = location;
				break;
			}catch(Exception fe) {
				//FileNotFoundException，Assert
			}
		}
		
		System.out.println("    logging[logback] configuration: " + logConfig);
		return logConfig;
	}
	
	/**
	 * 2019-05-27: 一分为二，将logging配置提前加载进来
	 */
	private static void globalEnviron() {
		String globalConfig = Environment.getProperty("spring.config.location", CONF_PATH);
		if (globalConfig.charAt(globalConfig.length()-1) != '/') {
			globalConfig += '/';
		}
		Environment.setConfigPath(globalConfig);
		globalConfig += "application.properties";
		System.out.println("    springframework configuration: " + globalConfig);
		
		try {
			Environment.mergeProperties(
					EnvironmentLoader.readProperties(globalConfig));
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
//		setEnvironSource(globalConfig);
	}
	private static void setEnvironSource() {
		String globalConfig = Environment.getConfigPath() + "application.properties";
		Environment.addResource(globalConfig);
		ConfigurableEnvironment environ = Environment.getSpringEnvironment();
		Environment.setApplication(getApplication(environ));
		Environment.setOwner(getOwner(environ));
		Environment.setAddress(getAddress(environ));
	}
	
	private static String loadJdbcContext(Properties properites) {
		if (Environment.getProperty("spring.jdbc.autowired", Boolean.class, false)) {
			if (!Environment.hasProperty("spring.mybatis.mapper.scanpackage")) {
				throw new IllegalStateException("Can't find placeholder 'mybatis.mapper.scanpackage");
			}
//			System.setProperty("jdbc.dialect", Environment.getProperty("spring.jdbc.dialect"));
//			System.setProperty("jdbc.transaction.proxyClass",
//							Environment.getProperty("spring.jdbc.transaction.proxyClass"));
			return ", classpath*:spring/dal/druid.xml";
		}
		return "";
	}
	private static String loadDubboContext(Properties properites) {
		if (Environment.exists("com.openxsl.config.startup.DubboContextInitializer")) {
			Properties dubboProps = DubboPropertiesLoader.loadProperties();
			if (dubboProps==null || dubboProps.isEmpty()) {
				throw new IllegalStateException("NO dubbo-properties found!");
			}
			//<context: component-scan>
			if (dubboProps.containsKey("dubbo.scan")) { //Hashtable NPE
				System.setProperty("dubbo.scan", dubboProps.getProperty("dubbo.scan"));
			}
			properites.putAll(dubboProps);
			return ", classpath*:dubbo/dubbo-common.xml";
		}
		return "";
	}
	
	static String getApplication(ConfigurableEnvironment environ) {
		String application = environ.getProperty("spring.application.name");
		if (StringUtils.isEmpty(application)) {
			application = environ.getProperty("spring.dubbo.application.name");
		}
		return StringUtils.isEmpty(application) ? "ROOT" : application;
	}
	static String getOwner(ConfigurableEnvironment environ) {
		String owner = environ.getProperty("spring.application.owner");
		if (StringUtils.isEmpty(owner)) {
			owner = environ.getProperty("spring.dubbo.application.owner");
		}
		return StringUtils.isEmpty(owner) ? "ADMIN" : owner;
	}
	static String getAddress(ConfigurableEnvironment environ) {
		String host = environ.getProperty("server.address");
		if (StringUtils.isEmpty(host)) {
			host = environ.getProperty("spring.dubbo.protocol.host");
		}
		if (StringUtils.isEmpty(host)) {
			host = NetworkUtils.LOCAL_IP;
		}
		String port = environ.getProperty("server.port");
		if (StringUtils.isEmpty(port)) {
			port = "80";
		}
		return String.format("%s:%s", host,port);
	}

}
