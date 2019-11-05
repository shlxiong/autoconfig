package com.openxsl.config.tracing.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RegisterService静态工厂类，通过protocol映射出来
 * @author xiongsl
 */
public class RegisterServiceFactory {
	private static final Map<String,RegistryService> SERVICE_MAP =
				new ConcurrentHashMap<String,RegistryService>();
	
	public static RegistryService getRegistryService(String protocol) {
		return SERVICE_MAP.get(protocol);
	}
	
	public static void registerService(String protocol, RegistryService service) {
		SERVICE_MAP.put(protocol, service);
	}
	
}
