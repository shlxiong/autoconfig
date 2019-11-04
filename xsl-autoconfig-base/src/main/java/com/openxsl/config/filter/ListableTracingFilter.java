package com.openxsl.config.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import com.openxsl.config.filter.invoke.InvocationHolder;
import com.openxsl.config.filter.invoke.ServiceMethodInvocation;

public class ListableTracingFilter { //implements TracingFilter {
	private static final Logger logger = LoggerFactory.getLogger(ListableTracingFilter.class);
	private final List<TracingFilter> filters = new ArrayList<TracingFilter>(2);
	private static ApplicationContext springContext;
	
	public void load(String type) {
		Iterator<TracingFilter> itr = ServiceLoader.load(TracingFilter.class).iterator();
		while (itr.hasNext()) {
			try {
				TracingFilter filter = itr.next();
				if (type==null || type.equals(filter.getType())) {
					filters.add(filter);
				}
			}catch(java.util.ServiceConfigurationError e) {
				logger.warn("*****load [{}] error: {}", type, e.getMessage());
			}
		}
	}
	
	public void add(TracingFilter filter) {
		this.filters.add(filter);
	}
	
	public void before(String service, String method, Object... args) {
		MethodInvocation mi = new ServiceMethodInvocation(service,method,args);
		InvocationHolder.set(mi);
		this.before(mi);
	}
	
	public void after(Object result) {
		try {
			this.after(InvocationHolder.get(), result);
		}finally {
			InvocationHolder.remove();
		}
	}
	
	public List<TracingFilter> getFilters(){
		return filters;
	}
	
	public void setSpringContext(ApplicationContext context) {
		springContext = context;
	}
	@SuppressWarnings("unchecked")
	public static <T> T getSpringBean(String beanId, Class<T> beanType) {
		Assert.notNull(springContext, "The 'springContext' is null, use setSpringContext(..) before do it");
		try {
			if (beanType == null) {
				return (T)springContext.getBean(beanId);
			} else if (beanId == null){
				return springContext.getBean(beanType);
			} else {
				return springContext.getBean(beanId, beanType);
			}
		} catch (NoSuchBeanDefinitionException e) {
			logger.info(e.getMessage());
			return null;
		}
	}

//	@Override
	private void before(MethodInvocation mi) {
		for (TracingFilter filter : filters) {
			try {
				filter.before(mi);
			}catch(Throwable t) {
				logger.warn("TraceFilter Error: ", t);
			}
		}
	}

//	@Override
	private void after(MethodInvocation mi, Object result) {
		for (TracingFilter filter : filters) {
			try {
				filter.after(mi, result);
			}catch(Throwable t) {
				logger.warn("TraceFilter Error: ", t);
			}
		}
//	}
//
//	@Override
//	public String getType() {
//		return "";
	}
	
}
