package com.openxsl.tracing.registry.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import com.openxsl.config.Environment;
import com.openxsl.config.util.StringUtils;

@Order(10)
public class RestContainerIntializer implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext sc) throws ServletException {
		System.out.println("RestContainerIntializer start, order=10");
	    String restPackage = Environment.getProperty("spring.rest.scanpackage", "");
	    String servletPath = Environment.getProperty("spring.rest.path", "/rest");
	    String restEasyClass = "org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher";
	    if (Environment.exists(restEasyClass) && !StringUtils.isEmpty(restPackage)) {
	    	sc.setInitParameter("resteasy.servlet.mapping.prefix", servletPath);
	     	System.setProperty(ResteasyServer.REST_SCAN_PAKGS, restPackage);
	     	sc.setInitParameter("javax.ws.rs.Application", ResteasyServer.RestResourceScanner.class.getName());
	     	sc.addListener("org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap");
	     	Dynamic registry = sc.addServlet("Resteasy", restEasyClass);
	     	if (registry != null) {
	     		registry.addMapping(servletPath+"/*");
	     		registry.setAsyncSupported(true);
	     		registry.setLoadOnStartup(10);
	     	}
	    } else {
	    	//jersey
	    }
	}

}
