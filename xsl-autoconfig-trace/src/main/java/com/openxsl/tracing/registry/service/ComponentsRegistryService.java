package com.openxsl.tracing.registry.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;

import com.openxsl.config.Environment;
import com.openxsl.config.tracing.service.RegistryService;
import com.openxsl.config.tracing.service.protocol.HttpRestRegistry;
import com.openxsl.config.tracing.service.protocol.JdbcRegistry;
import com.openxsl.config.tracing.service.zookeeper.MsgRegistryImpl;
import com.openxsl.config.tracing.service.zookeeper.RedisRegistryImpl;

/**
 * spring-boot环境
 * @author xiongsl
 */
public class ComponentsRegistryService {
	protected static final Logger logger = LoggerFactory.getLogger(ComponentsRegistryService.class);
	
	public static void register(ApplicationContext context) {
		if (Environment.exists("org.springframework.web.servlet.HandlerMapping")) {
			logger.info("start to do Http-Registry....");
			registerServer(context, HttpRestRegistry.class);
		}
		if (Environment.exists("com.alibaba.druid.pool.DruidDataSource")) {
			logger.info("start to do Jdbc-Registry....");
			registerClient(context, JdbcRegistry.class);
		}
		if (Environment.exists("com.openxsl.config.redis.RedisProperties")) {
			logger.info("start to do Redis-Registry....");
			registerClient(context, RedisRegistryImpl.class);
		}
		if (Environment.exists("com.openxsl.config.rocketmq.core.RocketListenerContainer")) {
			logger.info("start to do RocketMQ-Registry....");
			registerServer(context, MsgRegistryImpl.class);
		}
	}
	
	@SuppressWarnings({ "unchecked"})
	private static void registerClient(ApplicationContext context,
						Class<? extends RegistryService> registryClass) {
		RegistryService service = context.getBean(registryClass);
		Class<Object> entityClass = //(Class)BeanUtils.getRawType(registryClass, 0);
									service.getContextBeanType();
		Map<String, Object> beanMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(
							context, entityClass, true, false);
		for (Map.Entry<String,Object> entry : beanMap.entrySet()) {
			service.registerClient(service.convertRegistra(entry.getValue()));
		}
	}
	@SuppressWarnings({ "unchecked" })
	private static void registerServer(ApplicationContext context,
						Class<? extends RegistryService> registryClass) {
		RegistryService service = context.getBean(registryClass);
		Class<Object> entityClass = //(Class)BeanUtils.getRawType(registryClass, 0);
									service.getContextBeanType();
		Map<String, Object> beanMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(
							context, entityClass, true, false);
		for (Map.Entry<String,Object> entry : beanMap.entrySet()) {
			service.registerServer(service.convertRegistra(entry.getValue()));
		}
	}

}
