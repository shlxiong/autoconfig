package com.openxsl.config.test;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

import com.openxsl.config.logger.LogbackConfigurer;
import com.openxsl.config.testuse.AutoConfig;
import com.openxsl.config.testuse.BasicTest;

@AutoConfig(application="springboot-test")
@ContextConfiguration
public class LogbackConfigTest extends BasicTest {
	
	@Test
	public void test() {
		String locationParam = "classpath:logback-spring.xml";
		try {
			LogbackConfigurer.initLogging(locationParam);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		Logger logger = LoggerFactory.getLogger(getClass().getPackage().getName());
		logger.error("--------");
	}

}
