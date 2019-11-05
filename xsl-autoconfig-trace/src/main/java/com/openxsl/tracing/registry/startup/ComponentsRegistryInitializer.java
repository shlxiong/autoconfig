package com.openxsl.tracing.registry.startup;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.DispatcherServlet;

import com.openxsl.config.Environment;
import com.openxsl.config.OriginalBootstrap;

/**
 * Servlet容器启动时初始化:
 * 
 * 1、findWebApplicationContext()
 * 2、find RegistryService & Registration(s)
 *      1). ZooKeeperTemplate
 *      2). factory.registerService
 *      3). initiateProviderMap
 * 3、service.register(registration)
 * 
 * @author xiongsl
 */
@Order(500)
public class ComponentsRegistryInitializer implements WebApplicationInitializer {
	protected static final int MAX_WAIT_TIME = 160 * 1000;

	@Override
	public void onStartup(ServletContext sc) throws ServletException {
		OriginalBootstrap.initialize(sc);
		if (Environment.exists("org.springframework.web.servlet.HandlerMapping")) {
			new MvcRegistryInitializer().onStartup(sc);
		}
		if (Environment.exists("com.alibaba.druid.pool.DruidDataSource")) {
			new JdbcRegistryInitializer().onStartup(sc);
		}
		if (Environment.exists("com.openxsl.config.redis.RedisProperties")) {
			new RedisRegistryInitializer().onStartup(sc);
		}
		if (Environment.exists("com.openxsl.config.rocketmq.core.RocketListenerContainer")) {
			new RocketRegistryInitializer().onStartup(sc);
		}
	}
	
	/**
	 * 查找WebApplicationContext，若findServlet=true，先从DispatcherServlet中
	 */
	protected static WebApplicationContext findWebApplicationContext(
						final ServletContext sc, final boolean findServlet) {
		final long start = System.currentTimeMillis();
		WebApplicationContext context = null;
		while ((System.currentTimeMillis()-start) < MAX_WAIT_TIME) {
			if (findServlet) {
				long timeout = MAX_WAIT_TIME >> 1;
				while (context==null && (System.currentTimeMillis()-start)<timeout) {
					context = getMvcApplicationContext(sc);
				}
			}
			if (context == null) {
				context = WebApplicationContextUtils.findWebApplicationContext(sc);
			}
			if (context != null) {
				break;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}
		if (context == null) {
			throw new IllegalStateException("ApplicationContext is NOT found! please instantiate a contextloader");
		}
		return context;
	}
	
	private static WebApplicationContext getMvcApplicationContext(ServletContext sc) {
//		String servletName = null;
//		Class<?> servletClass;
//		for (Entry<String,?> entry : sc.getServletRegistrations().entrySet()) {  //jetty UnSupportedException
//			String className = ((ServletRegistration)entry.getValue()).getClassName();
//			try {
//				servletClass = ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());
//				if (DispatcherServlet.class.isAssignableFrom(servletClass)) {
//					servletName = entry.getKey();
//					break;
//				}
//			} catch (ClassNotFoundException e) {
//			}
//		}
//		try {
//			Thread.sleep(100);  //wait for DispatcherServlet initiation
//		} catch (InterruptedException e) {
//		}
//		if (servletName != null) {
//			String attrName = DispatcherServlet.SERVLET_CONTEXT_PREFIX + servletName;  //publishContext=true
//			return (WebApplicationContext)sc.getAttribute(attrName);
//		}
		
		Enumeration<String> attrNames = sc.getAttributeNames();
		final String prefix = DispatcherServlet.SERVLET_CONTEXT_PREFIX;
		while (attrNames.hasMoreElements()) {
			String name = attrNames.nextElement();
			if (name.startsWith(prefix)) {
				return (WebApplicationContext)sc.getAttribute(name);
			}
		}
		return null;
	}

}
