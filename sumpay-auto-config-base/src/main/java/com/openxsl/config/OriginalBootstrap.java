package com.openxsl.config;

import java.io.FileNotFoundException;

import javax.servlet.ServletContext;

import org.slf4j.impl.StaticLoggerBinder;
import org.springframework.web.context.ContextLoaderListener;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.spi.JoranException;
import com.openxsl.config.logger.LogbackConfigurer;
import com.openxsl.config.logger.context.LoggerContext;
import com.openxsl.config.util.Version;

/**
 * 既不是springboot，又没遵照配置分离的（如同一个tomcat下部署两个应用）
 * @author xiongsl
 */
public class OriginalBootstrap {
	
	public static void initialize(ServletContext sc) {
		if (Environment.getApplication() == null) {
			initLogging(sc);
			
			String contextPath = sc.getContextPath();
			if (contextPath.length() >0 && contextPath.charAt(0)=='/') {
				contextPath = contextPath.substring(1);
			}
			Environment.setApplication(contextPath);
			Environment.addResource(sc.getInitParameter("contextPropertyLocation"));
			BootstrapApplication.initEnvironment(Environment.getSpringContext());   //PlaceholderConfigurer
			BootstrapApplication.initTracing();
			Environment.setSpringContextLoader();   //AutoConfigRegistry
			sc.addListener(new ContextLoaderListener(Environment.getSpringContext()));
		}
	}
	
	public static void initLogging(ServletContext sc) {
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
    	String logConfig = sc.getInitParameter("logbackConfigLocation");
		try {
			LogbackConfigurer.initLogging(logConfig);
			LoggerContext.initialize();   //traceId
		} catch (FileNotFoundException e) {
			System.err.println("No logback configuration file found at [" + logConfig + "]");
		} catch (JoranException e) {
			throw new RuntimeException("Unexpected error while configuring logback", e);
		}
	}

}
