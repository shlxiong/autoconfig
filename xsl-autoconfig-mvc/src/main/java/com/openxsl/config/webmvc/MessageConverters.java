package com.openxsl.config.webmvc;

//import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.openxsl.config.Environment;
import com.openxsl.config.util.StringUtils;

/**
 * HttpMessageConverter
 * @author xiongsl
 */
public class MessageConverters implements FactoryBean<List<HttpMessageConverter<?>>>, 
						InitializingBean, ApplicationContextAware{
//	private final Charset CHARSET = Charset.forName("UTF-8");
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private List<HttpMessageConverter<?>> messageConverters;
	private String position;
	private ApplicationContext context;
	
	public MessageConverters(String position){
		this.position = position;
	}
	
	public void afterPropertiesSet() throws Exception {
		String converters = //System.getProperty("springmvc.messageconverter");
							Environment.getProperty("springmvc.messageconverter");
		if (converters!=null && !converters.equals("")){
			messageConverters = new ArrayList<HttpMessageConverter<?>>();
			for (String className : StringUtils.split(converters,",")){
				logger.info("Find a converter: {} ....", className);
				HttpMessageConverter<?> converter = null;
				try{
					converter = context.getBean(className, HttpMessageConverter.class);
				}catch(NoSuchBeanDefinitionException e){
					try {
						converter = (HttpMessageConverter<?>)
									ClassUtils.getClass(className).newInstance();
					} catch (Exception ce) {
						//e.printStackTrace();
					}
				}
				if (converter != null){
					logger.info("Converter found!");
					messageConverters.add(converter);
				}
			}
			
			RequestMappingHandlerAdapter handlerAdapter = context.getBean(RequestMappingHandlerAdapter.class);
			if (handlerAdapter!=null && messageConverters.size() > 0){
				if ("before".equals(position.toLowerCase())){
					handlerAdapter.getMessageConverters().addAll(0, messageConverters);
				}else{
					handlerAdapter.getMessageConverters().addAll(messageConverters);
				}
			}
			logger.warn("messageConverters____________"+handlerAdapter.getMessageConverters().size());
		}
	}

	@Override
	public List<HttpMessageConverter<?>> getObject() throws Exception {
		return messageConverters;
	}

	@Override
	public Class<?> getObjectType() {
		return ArrayList.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
				throws BeansException {
		this.context = applicationContext;
	}

}
