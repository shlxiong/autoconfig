package com.openxsl.config.loader;

import java.util.Properties;

import com.openxsl.config.Environment;
import com.openxsl.config.EnvironmentLoader;

/**
 * 加载本项目的配置属性
 * @author xiongsl
 */
public class OtherPropertiesLoader {
	
	public static Properties loadProperties() {
		String applicationPrefix = Environment.getApplication() + ".";
		return EnvironmentLoader.load(applicationPrefix+"properties.file",
						applicationPrefix, "");
	}

}
