package com.openxsl.config.config;

import java.util.Properties;

import com.openxsl.config.EnvironmentLoader;
import com.openxsl.config.loader.DomainPropertyLoader;

public class ZooKeeperPropertiesLoader implements DomainPropertyLoader {

	@Override
	public Properties loadProperties() {
		return EnvironmentLoader.load(null, "spring.zookeeper", "zookeeper");
	}

}
