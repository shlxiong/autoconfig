package com.openxsl.tracing.registry.web;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.openxsl.config.util.BeanUtils;
import com.openxsl.config.util.StringUtils;

public class ResteasyServer {
	public static final String REST_SCAN_PAKGS = "org.jboss.resteasy.deploy.packages";
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Server server;
	private final ServletContextHandler contextHandler;
	  
	public ResteasyServer(int port, String contextPath) {
	    this.server = new Server(port);
	    this.contextHandler = new ServletContextHandler(1);
	    this.contextHandler.setContextPath(contextPath);
	}
	  
	public void start(String packages, String resourcePath, String servletPath)
					throws Exception {
	    this.contextHandler.setInitParameter("resteasy.servlet.mapping.prefix", servletPath);
//	    System.setProperty("resteasy.resources", packages);
	    System.setProperty(REST_SCAN_PAKGS, packages);
	    this.contextHandler.setInitParameter("javax.ws.rs.Application", RestResourceScanner.class.getName());
	    
	    this.contextHandler.addEventListener(new ResteasyBootstrap());
	    if (!StringUtils.isEmpty(resourcePath)) {
	    	this.contextHandler.setBaseResource(Resource.newClassPathResource(resourcePath));
	    	this.contextHandler.addServlet(new ServletHolder(DefaultServlet.class), "/*");
	    }
	    String servletPathStr = (StringUtils.isEmpty(servletPath) ? "" : servletPath) + "/*";
	    this.contextHandler.addServlet(getServletHolder(packages), servletPathStr);
	    
	    HandlerList handlers = new HandlerList();
	    handlers.addHandler(this.contextHandler);
	    this.server.setHandler(handlers);
	    try {
	    	this.server.start();
	    	this.logger.info("Start RESTful server");
	    } catch (Exception e) {
	    	logger.error("Start RESTful server error", e);
	    }
	}
	  
	public ResteasyServer addFilter(Class<? extends Filter> filterClass, String urlPattern) {
	    this.contextHandler.addFilter(filterClass, urlPattern, 
	    							EnumSet.of(DispatcherType.REQUEST));
	    return this;
	}
	  
	public ServletContext getServletContext() {
	    return this.contextHandler.getServletContext();
	}
	  
	private ServletHolder getServletHolder(String packages) {
	    ServletHolder holder = new ServletHolder(HttpServletDispatcher.class);
	    
	    holder.setInitOrder(1);
	    holder.setAsyncSupported(true);
	    return holder;
	}
	  
	 public void stop() {
	    logger.info("Stop RESTful server");
	    try {
	    	this.server.stop();
	    } catch (Exception e) {
	    	logger.error("Stop RESTful server error", e);
	    }
	}
	
	/**
	 * createApplication()
	 * POJOResourceFactory.createResource()创建对象是prototype
	 * @author xiongsl
	 */
	public static class RestResourceScanner  extends Application {
	    private Set<Object> singletons = new HashSet<Object>();
	    
	    public RestResourceScanner() throws Exception {
	    	String packages = System.getProperty(REST_SCAN_PAKGS);
	    	Set<String> scanPakages = new HashSet<String>(2);
	    	scanPakages.add(getClass().getPackage().getName());
	    	if (!StringUtils.isEmpty(packages)) {
	    		scanPakages.add(packages);
	    	}
	    	ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
	    	provider.addIncludeFilter(new AnnotationTypeFilter(Path.class));
	    	for (String scanPkge : scanPakages) {
		    	for (BeanDefinition define : provider.findCandidateComponents(scanPkge)) {
		    		this.singletons.add(
		    					BeanUtils.instantiate(define.getBeanClassName(), Object.class)
		    				);
		    	}
	    	}
	    }
	    
	    public Set<Object> getSingletons() {
	      return this.singletons;
	    }
	  }
}
