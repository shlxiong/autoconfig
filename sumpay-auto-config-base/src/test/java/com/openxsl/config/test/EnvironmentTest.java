package com.openxsl.config.test;

import com.openxsl.config.Environment;
import com.openxsl.config.loader.PrefixProperties;

import junit.framework.TestCase;

public class EnvironmentTest extends TestCase {
	
	public void testYaml() {
		String location = "file:/openxsl/conf/withdraw/application.yml";
		Environment.addResource(location);
		System.out.println(Environment.getProperty("redis.host"));
		System.out.println(Environment.prefixProperties("redis"));
		location = "file:/openxsl/conf/withdraw/application-dev.yml";
		System.out.println(PrefixProperties.get(location, "redis", false));
		
		location = "file:/openxsl/conf/openxsl-sms/application.yml";
		Environment.addResource(location);
		System.out.println(Environment.getProperty("redis.host"));
	}

}
