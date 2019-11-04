package com.openxsl.config.config;

import java.util.Properties;

import com.openxsl.config.EnvironmentLoader;
import com.openxsl.config.loader.DomainPropertyLoader;

public class HttpPropertiesLoader implements DomainPropertyLoader {

	@Override
	public Properties loadProperties() {
		return EnvironmentLoader.load(null, "spring.http", "http");
	}

}
