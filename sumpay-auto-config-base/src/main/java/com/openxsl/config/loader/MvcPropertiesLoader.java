package com.openxsl.config.loader;

import java.util.Properties;

import com.openxsl.config.EnvironmentLoader;

/**
 * 加载spring-mvc配置属性
 * @author xiongsl
 */
public class MvcPropertiesLoader {
	
	public static Properties loadProperties() {
		Properties mvcProps = EnvironmentLoader.load("", "spring.mvc", null);
		mvcProps.putAll(EnvironmentLoader.load("", "spring.freemarker", "freemarker"));
		return mvcProps;
	}

}
