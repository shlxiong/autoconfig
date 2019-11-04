package com.openxsl.config.loader;

import java.util.Properties;

import com.openxsl.config.EnvironmentLoader;

/**
 * 加载redis属性
 * @author xiongsl
 */
public class RedisPropertiesLoader {
	private static final String CONFIG_FILE = "redis.properties.file";
	
	public static Properties loadProperties() {
		return EnvironmentLoader.load(CONFIG_FILE, "spring.redis", "redis");
	}

}
