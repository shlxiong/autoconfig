package com.openxsl.config.test;

import java.util.Properties;

import javax.annotation.Resource;

import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import com.openxsl.config.autodetect.PrefixProps;
import com.openxsl.config.autodetect.PrefixPropsRegistrar;

@Import(PrefixPropsRegistrar.class)
@PropertySource(value="file:/openxsl/conf/springboot-test/application.properties")  //可以省略
@Service
public class TestBean {
	
	@Resource
	@PrefixProps(prefix="spring.jdbc")
	private Properties jdbcProps;
	
	@Resource
	@PrefixProps(regexp="springboot-test.(.*).verify", prefix="${spring.application.name}")
	private Properties verifyProps;
	
	@Resource
	@PrefixProps(regexp="springboot-test.(.*).verify", prefix="${spring.application.name}", rewrite=false)
	private Properties verifyProps2;
	
	@Resource
	@PrefixProps(rewrite=false)
	private Properties allProps;
	
	public Properties getJdbcProperties() {
		return jdbcProps;
	}
	public Properties getVerifyProperties() {
		return verifyProps;
	}
	public Properties getVerifyPropertiesWithPrefix() {
		return verifyProps2;
	}
	public Properties getAllProperties() {
		return allProps;
	}

}
