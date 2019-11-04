package com.openxsl.config.startup;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.impl.StaticLoggerBinder;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import ch.qos.logback.core.Appender;
import com.openxsl.config.BootstrapApplication;
import com.openxsl.config.Environment;
import com.openxsl.config.logger.LogbackConfigurer;
import com.openxsl.config.util.Version;

/**
 * @author xiongsl
 *
 * 自定义参数：-Dspring.config.location指定application.properties的地址
 * 在这个配置文件中定义 LOG的一些变量参数：
 *      logging.config，logging.path，logging.file，logging.level
 * 		最后logback文件路径：${logPath}/${appname}/${logFile}
 */
@Order(-999999)
public class LoggingContextInitializer implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		if (Environment.existSpringBoot()) {
			return;
		}
		
		Version.checkDuplicate(StaticLoggerBinder.class, true);
		Version.expectVersion(Appender.class, "1.1.11", true);
    	if (Version.hasResource("slf4j-log4j*.jar", false)) {
    		throw new IllegalStateException("Please remove 'slf4j-log4j*.jar', otherwise will conflict with logback!");
    	}
    	if (Version.hasResource("log4j-1.2*.jar", false)) {
    		throw new IllegalStateException("Please remove 'log4j-1.2*.jar', otherwise will conflict with logback!");
    	}
		
		System.out.println("LoggingContextInitializer start, order=-999999");
		this.setInitialParams(servletContext);
	}
	
	private void setInitialParams(ServletContext servletContext) {
		try {
			LogbackConfigurer.shutdownLogging();
		}catch(Throwable t) {
		}
		BootstrapApplication.initLogging();
//		servletContext.setInitParameter(WebLogbackConfigurer.EXPOSE_WEB_APP_ROOT_PARAM, "false");
//		String logConfig = BootstrapApplication.initLoggingConfig();
//		servletContext.setInitParameter(WebLogbackConfigurer.CONFIG_LOCATION_PARAM, logConfig);
		//问题：过一段时间才会执行contextInitialized()
//		servletContext.addListener(new LogbackConfigListener());  //auto-activate
	}
	
//	private void resetLogger() { //version-1.1.11  servlet包会初始化
//	}
	
//	class LogbackConfigListener implements ServletContextListener {
//		
//		@Override
//	    public void contextInitialized(ServletContextEvent event) {
//	    	//"webAppRootKey"
//			String logConfig = event.getServletContext().getInitParameter(
//									WebLogbackConfigurer.CONFIG_LOCATION_PARAM);
//	        try {
//				LogbackConfigurer.initLogging(logConfig);
//				LoggerContext.initialize();   //traceId
//			} catch (FileNotFoundException e) {
//				System.err.println("No logback configuration file found at [" + logConfig + "]");
//			} catch (JoranException e) {
//				throw new RuntimeException("Unexpected error while configuring logback", e);
//			}
//	    }
//		
//		@Override
//	    public void contextDestroyed(ServletContextEvent event) {
//	        LogbackConfigurer.shutdownLogging();
//	    }
//	}

}
