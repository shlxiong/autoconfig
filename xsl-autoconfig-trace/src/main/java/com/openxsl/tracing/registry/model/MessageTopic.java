package com.openxsl.tracing.registry.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MessageTopic extends Registration {
	private String namesrv;
	private String topic;
	private String tag;
	
	//namesrv/topic:tags
	public MessageTopic(String namesrv, String topic, String tag) {
		this.namesrv = namesrv;
		this.topic = topic;
		this.tag = tag;
	}
	
	@SuppressWarnings("serial")
	@Override
	public ServiceKey getServiceKey() {
		return new ServiceKey() {
			@Override
			public String serialize() {
				return new StringBuilder(namesrv).append("/")
						.append(topic.replace("/", "|"))
						.append(":").append(tag)
						.toString();
			}
			@Override
			public Map<String, Serializable> deserialize() {
				Map<String, Serializable> map = new HashMap<String, Serializable>(3);
				map.put("namesrv", namesrv);
				map.put("topic", topic);
				map.put("tag", tag);
				return map;
			}
			
		};
	}
	
}
