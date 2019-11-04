package com.openxsl.config.loader;

import java.util.Properties;

import com.openxsl.config.EnvironmentLoader;

/**
 * 加载Dubbo属性
 * @author xiongsl
 */
public class DubboPropertiesLoader {
	
	/**
	 * @see EnvironmentLoader.load(Constants.DUBBO_PROPERTIES_KEY, "spring.dubbo", "dubbo");
	 */
	public static Properties loadProperties() {
		return EnvironmentLoader.load("dubbo.properties.file", "spring.dubbo", "dubbo");
	}

}
