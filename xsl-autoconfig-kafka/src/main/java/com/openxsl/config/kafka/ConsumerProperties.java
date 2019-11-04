package com.openxsl.config.kafka;

import java.util.Map;

/**
 * 包装Kafaka配置属性，方便注入
 * @author xiongsl
 */
public class ConsumerProperties {
	private final Map<String, Object> configs;
	
	public ConsumerProperties(Map<String, Object> configs) {
		this.configs = configs;
	}
	
	public Object getValue(String name) {
		return configs.get(name);
	}
	public void setValue(String name, Object value) {
		configs.put(name, value);
	}
	public void putIfAbsent(String name, Object value) {
		configs.putIfAbsent(name, value);
	}

	public Map<String, Object> getConfigs() {
		return configs;
	}

}
