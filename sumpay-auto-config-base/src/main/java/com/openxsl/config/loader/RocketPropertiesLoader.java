package com.openxsl.config.loader;

import java.util.Properties;

import com.openxsl.config.EnvironmentLoader;

/**
 * 加载rocketmq属性
 * @author xiongsl
 */
public class RocketPropertiesLoader {
	private static final String CONFIG_FILE = "rocketmq.properties.file";
	
	public static Properties loadProperties() {
		return EnvironmentLoader.load(CONFIG_FILE, "spring.rocketmq", "rocketmq");
	}

}
