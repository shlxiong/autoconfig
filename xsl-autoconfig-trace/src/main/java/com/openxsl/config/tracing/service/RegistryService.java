package com.openxsl.config.tracing.service;

import com.openxsl.tracing.registry.model.Registration;

public interface RegistryService {
	
	/**
	 * 注册服务端
	 * @param registration
	 */
	public void registerServer(Registration registration);
	
	/**
	 * 注册客户端
	 * @param registration
	 */
	public void registerClient(Registration registration);
	
	/**
	 * 根据ServiceKey查找注册信息
	 * @param serviceKey
	 */
	public Registration find(String serviceKey);
	
	/**
	 * 订阅服务
	 * @param serviceKey
	 */
	public void subscribe(String serviceKey);
	
	/**
	 * 注册组件类型
	 */
	@SuppressWarnings("rawtypes")
	public Class getContextBeanType();
	
	public Registration convertRegistra(Object bean);

}
