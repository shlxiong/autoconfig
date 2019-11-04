package com.openxsl.config.config;

import java.util.Properties;

import com.openxsl.config.EnvironmentLoader;
import com.openxsl.config.loader.Autoload;
import com.openxsl.config.loader.DomainPropertyLoader;
import com.openxsl.config.util.StringUtils;

/**
 * 数据库连接池
 * @author xiongsl
 */
@Autoload("spring.jdbc.autowired")
public class JdbcDsPropertiesLoader implements DomainPropertyLoader {
	
	@Override
	public Properties loadProperties() {
		Properties jdbcProps = EnvironmentLoader.load("", "spring.datasource", "datasource");
		
		final String reserveKey = "datasource.connectionProperties";
		String connectionProps = jdbcProps.getProperty(reserveKey);  //config.url=[http://....]
		if (!StringUtils.isEmpty(connectionProps)) { //删除其他的
			jdbcProps.clear();
			jdbcProps.setProperty(reserveKey, connectionProps);
		}
		jdbcProps.putAll(EnvironmentLoader.load("", "spring.jdbc", "jdbc"));
		String api = jdbcProps.getProperty("jdbc.persistence.api", "mybatis");//springJdbc
		jdbcProps.putAll(EnvironmentLoader.load("", "spring."+api, api));
		return jdbcProps;
	}

}
