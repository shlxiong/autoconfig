package com.openxsl.tracing.registry.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.web.context.support.XmlWebApplicationContext;

import com.openxsl.config.BootstrapApplication;
import com.openxsl.config.Environment;
import com.openxsl.config.util.StringUtils;

public final class ConsoleBootstrap {
	private static final Logger logger = LoggerFactory.getLogger(ConsoleBootstrap.class);
	private static final String RESOURCE_PATH = "web";
	private static XmlWebApplicationContext context;
  
	public static void main(String[] args) throws Exception {
		int port = 8899;
		if (1 == args.length) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException ex) {
	    	  logger.warn("Wrong port format, using default port 8899 instead.");
			}
		}
		System.setProperty("spring.config.location", "file:/openxsl/conf/springboot-test");
		System.setProperty("spring.profiles.active", "dev");
		BootstrapApplication.initLogging();
		Environment.setSpringContextLoader();
		context = Environment.getSpringContext();
		BootstrapApplication.initEnvironment(context);
		context.setConfigLocations(new String[0]);
    
		String contextPath = Environment.getProperty("server.contextPath", "/");
		ResteasyServer restfulServer = new ResteasyServer(port, contextPath);
		context.setServletContext(restfulServer.getServletContext());
		context.refresh();
    
		String restPackage = Environment.getProperty("spring.rest.scanpackage", "");
		String servletPath = Environment.getProperty("spring.rest.path", "/rest");
		restfulServer.addFilter(WwwAuthFilter.class, "/")
					.addFilter(WwwAuthFilter.class, "*.html")
      				.start(restPackage, RESOURCE_PATH, servletPath);
	}
  
  	public static XmlWebApplicationContext getSpringContext() {
  		return context;
  	}
  
  	@SuppressWarnings("unchecked")
	public static <T> T getBean(String beanId, Class<T> beanType) {
  		ApplicationContext springCtx = (context==null) ? Environment.getSpringContext()
  					: context;
  		Assert.notNull(springCtx, "ConsoleBootstrap or Environment has not been initiated");
	    if (StringUtils.isEmpty(beanId)) {
	    	return springCtx.getBean(beanType);
	    }
	    if (beanType == null) {
	    	return (T)springCtx.getBean(beanId);
	    }
	    return springCtx.getBean(beanId, beanType);
  	}
}
