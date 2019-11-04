package com.openxsl.config.webmvc;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.openxsl.config.Environment;
import com.openxsl.config.logger.context.LoggerContext;
import com.openxsl.config.util.StringUtils;

/**
 * MVC-拦截器
 * @author xiongsl
 */
public class DispatchHandlerInterceptor implements HandlerInterceptor,
				InitializingBean, ApplicationContextAware{
	private List<HandlerInterceptor> interceptors = new ArrayList<HandlerInterceptor>();
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private ApplicationContext context;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		interceptors.add(0, new LoggingInterceptor());
		String interceptors = //System.getProperty("spring.mvc.interceptor");
								Environment.getProperty("spring.mvc.interceptor");
		if (interceptors!=null && !interceptors.equals("")){
			for (String className : StringUtils.split(interceptors,",")){
				logger.info("Find a customized-interceptor: {} ....", className);
				HandlerInterceptor interceptor = null;
				try{
					interceptor = context.getBean(className, HandlerInterceptor.class);
				}catch(NoSuchBeanDefinitionException e){
					try {
						interceptor = (HandlerInterceptor)
									ClassUtils.getClass(className).newInstance();
					} catch (Exception ce) {
						//e.printStackTrace();
					}
//					((ConfigurableApplicationContext)context).getBeanFactory()
//								.registerSingleton(beanName, interceptor);
				}
				if (interceptor != null){
					this.interceptors.add(interceptor);
				}
			}
			logger.info("Found HandlerInteceptors_________: {}", interceptors);
		}
	}

	@Override
	public boolean preHandle(HttpServletRequest request,
					HttpServletResponse response, Object handler) throws Exception {
		LoggerContext.setTraceId(request.getHeader(LoggerContext.TRACE_ID_KEY));
		LoggerContext.setParentId(request.getHeader(LoggerContext.SPAN_ID_KEY));
		LoggerContext.initialize();
		
		for (HandlerInterceptor filter : interceptors){
			logger.info("pass interceptor.preHandle: {} ....", filter);
			if (!filter.preHandle(request, response, handler)){
				return false;
			}
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request,
					HttpServletResponse response, Object handler,
					ModelAndView modelAndView) throws Exception {
		for (HandlerInterceptor filter : interceptors){
			filter.postHandle(request, response, handler, modelAndView);
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request,
					HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		for (HandlerInterceptor filter : interceptors){
			filter.afterCompletion(request, response, handler, ex);
		}
		LoggerContext.clear();
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
					throws BeansException {
		this.context = applicationContext;
	}
	
	public void setInterceptors(List<HandlerInterceptor> filters){
		if (filters != null){
			this.interceptors.addAll(filters);
		}
	}
	
	private class LoggingInterceptor implements HandlerInterceptor{

		@Override
		public boolean preHandle(HttpServletRequest request,
						HttpServletResponse response, Object handler) throws Exception {
			if (logger.isDebugEnabled()) {
				if (handler instanceof HandlerMethod){
					logger.debug("SpringMVC todo: {}", handler);
				}else{
					logger.debug("Spring unkown handler: {}", handler);
				}
			}
			return true;
		}

		@Override
		public void postHandle(HttpServletRequest request,
						HttpServletResponse response, Object handler,
						ModelAndView modelAndView) throws Exception {
			if (logger.isDebugEnabled()) {
				if (modelAndView != null){
					logger.debug("Controller done, goto view: {}", modelAndView.getViewName());
				}else{
					logger.debug("Controller done, return responseBody");
				}
			}
		}

		@Override
		public void afterCompletion(HttpServletRequest request,
						HttpServletResponse response, Object handler, Exception ex)
					throws Exception {
			if (logger.isDebugEnabled()) {
				logger.debug("complete view rendering.");
			}
		}
		
	}

}
