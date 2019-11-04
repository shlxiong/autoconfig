package com.openxsl.config.kafka.loader;

import java.util.Properties;

import com.openxsl.config.EnvironmentLoader;
import com.openxsl.config.loader.Autoload;
import com.openxsl.config.loader.DomainPropertyLoader;

/**
 * 加载Kafka属性
 * 
 * @author xiongsl
 */
@Autoload(presentClass="org.springframework.kafka.core.KafkaTemplate")
public class KafkaPropertiesLoader implements DomainPropertyLoader {

	@Override
	public Properties loadProperties() {
		EnvironmentLoader.setKeyConverter();
		return EnvironmentLoader.load(null, "spring.kafka", "kafka");  //EnvironmentLoader
	}

}
