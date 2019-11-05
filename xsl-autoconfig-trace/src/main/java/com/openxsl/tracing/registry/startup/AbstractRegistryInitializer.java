package com.openxsl.tracing.registry.startup;

import java.lang.reflect.ParameterizedType;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.web.context.WebApplicationContext;

import com.openxsl.config.thread.GrouppedThreadFactory;
import com.openxsl.config.tracing.service.RegistryService;

public abstract class AbstractRegistryInitializer<T> /*implements WebApplicationInitializer*/{
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected final int MAX_WAIT_TIME = 180 * 1000;
	protected RegistryService service;
	private final Class<T> entityClass;
	
	@SuppressWarnings("unchecked")
	public AbstractRegistryInitializer() {
		ParameterizedType pt = (ParameterizedType)this.getClass().getGenericSuperclass();
		entityClass = (Class<T>) pt.getActualTypeArguments()[0];
	}

	public void onStartup(ServletContext sc) throws ServletException {
		//executor
		new GrouppedThreadFactory(getThreadName()).execute(new Runnable() {
			@Override
			public void run() {
				WebApplicationContext wac = findWebApplicationContext(sc);
				service = wac.getBean(getServiceClass());
				logger.info("start to do {}....", getThreadName());
				Map<String, T> beanMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(
												wac, entityClass, true, false);
				for (Map.Entry<String,T> entry : beanMap.entrySet()) {
					register(entry.getValue());
				}
			}
		});
	}
	
	/**
	 * 等待WebApplicationContext初始化完成，如果没有定义DispatcherServlet会等待比较长
	 */
	protected WebApplicationContext findWebApplicationContext(ServletContext sc) {
		return ComponentsRegistryInitializer.findWebApplicationContext(sc, false);
	}
	
	protected abstract String getThreadName();
	
	/**
	 * RegistryService
	 */
	protected abstract Class<? extends RegistryService> getServiceClass();
	
	protected abstract void register(T contextBean);

}
