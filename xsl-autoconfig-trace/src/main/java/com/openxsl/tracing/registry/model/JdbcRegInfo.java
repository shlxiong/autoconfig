package com.openxsl.tracing.registry.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.druid.util.JdbcUtils;

public class JdbcRegInfo extends Registration {
	private String jdbcUrl;
	private String user;
	
	public JdbcRegInfo(String uri, String user) {
		String dbType = JdbcUtils.getDbType(uri, null);
		if (JdbcConstants.MYSQL.equals(dbType)) {
			jdbcUrl = uri.replace("jdbc:mysql://", "mysql-");
		} else if (JdbcConstants.ORACLE.equals(dbType)) {
			jdbcUrl = uri.replace("jdbc:oracle:thin:@", "oracle-");
		}
		this.setJdbcUrl(jdbcUrl);
		this.setUser(user);
	}

	@SuppressWarnings("serial")
	@Override
	public ServiceKey getServiceKey() {
		return new ServiceKey() {
			@Override
			public String serialize() {  //mysql-host:port|catalog:user
				return new StringBuilder(jdbcUrl).append(":").append(user)
						.toString().replace("/", "|");
			}
			@Override
			public Map<String, Serializable> deserialize() {
				Map<String, Serializable> map = new HashMap<String, Serializable>(2);
				map.put("jdbcUrl", jdbcUrl);
				map.put("user", user);
				return map;
			}
			
		};
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}
	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}

}
