package com.openxsl.config.startup;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

/**
 * Rocketmq Spring容器
 * @author xiongsl
 */
@Order(300)
public class RocketContextInitializer implements WebApplicationInitializer{

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		System.out.println("RocketmqContextInitializer start, order=300");
		
//		Properties properties = RocketPropertiesLoader.loadProperties();
//		ServletRegistration.Dynamic serlvet = servletContext.addServlet(
//					"rocketmq", new EnvirBootServlet(properties));
//		serlvet.setLoadOnStartup(2);
	}
	
}
