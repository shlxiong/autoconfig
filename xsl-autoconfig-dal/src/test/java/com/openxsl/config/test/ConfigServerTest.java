package com.openxsl.config.test;

import java.io.IOException;

import com.alibaba.fastjson.JSON;
import com.openxsl.config.dal.zookeeper.ConfigServer;
import com.openxsl.config.test.model.RocketProperties;

public class ConfigServerTest {
	
	public static void main(String[] args) throws IOException {
		String localFile = "file:/openxsl/conf/springboot-test/application.properties";
		String zkpath = "tracing/rocketmq";
		ConfigServer server = new ConfigServer(localFile, zkpath);
		String property = "nameServer";
		
		System.out.println(server.getProperty(property, String.class));
		System.out.println(server.getConfigurer().getRemoteProperties("tracing"));
		System.out.println(server.getConfigurer().getRemoteProperties(zkpath));
		RocketProperties rocketProps = server.getPropertyBean(zkpath, RocketProperties.class);
		System.out.println("instance:");
		System.out.println(JSON.toJSONString(rocketProps));
	}

}
