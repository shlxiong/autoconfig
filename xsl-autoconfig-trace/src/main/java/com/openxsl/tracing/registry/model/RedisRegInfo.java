package com.openxsl.tracing.registry.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RedisRegInfo extends Registration {
	private String nodes;
	private String database;
	
	/**
	 * 注册地址的格式：maslaver-host:port/database
	 * @param redisUrl "redis-maslaver://host:port,host:port/database"
	 */
	public RedisRegInfo(String redisUrl) {
		redisUrl = redisUrl.replace("redis-", "").replace("://", "-");
		int idx = redisUrl.indexOf('/');
		nodes = redisUrl.substring(0, idx);
		database = redisUrl.substring(idx+1);
	}
	
	@SuppressWarnings("serial")
	@Override
	public ServiceKey getServiceKey() {
		return new ServiceKey() {
			@Override
			public String serialize() {
				return new StringBuilder(nodes).append("|").append(database)
						.toString();
			}
			@Override
			public Map<String, Serializable> deserialize() {
				Map<String, Serializable> map = new HashMap<String, Serializable>(2);
				map.put("nodes", nodes);
				map.put("database", database);
				return map;
			}
		};
	}
	
}
