package com.openxsl.tracing.registry.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.openxsl.config.Environment;
import com.openxsl.tracing.registry.model.ServiceFeature.BaseServiceFeature;

/**
 * 保存到注册中心的实体对象
 * @author xiongsl
 */
public class Registration {
	private String application;
	private String owner;
	private String instanceId;   // http:hostPort, esjob:ip@_@pid
	
	public Registration() {
		application = Environment.getApplication();
		owner = Environment.getOwner();
		String address = Environment.getAddress();
		String contextPath = Environment.getProperty("server.context-path", "");
		if (contextPath.length()>0 && contextPath.charAt(0) == '/') {
			contextPath = contextPath.substring(1);
		}
		instanceId = String.format("%s|%s", address,contextPath);
//		String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
//		instanceId = String.format("%s@_@%s", ipAddr,pid);
	}
	
	/**
	 * 返回ZooKeeper节点名，交给子类去实现
	 */
	public ServiceKey getServiceKey() {
		throw new IllegalStateException("No implement-method yet");
	}
	/**
	 * 写入ZooKeeper节点的内容，交给子类去实现
	 */
	@SuppressWarnings("serial")
	public ServiceFeature getServiceFeature() {
		return new BaseServiceFeature() {
			private Map<String, Serializable> data = null;
			@Override
			public Map<String, Serializable> deserialize() {
				if (data == null) {
					data = new HashMap<String, Serializable>(4);
					data.put("application", getApplication());
					data.put("owner", getOwner());
					data.put("instanceId", getInstanceId());
				}
				return data;
			}
		};
	}
	
	public String toString() {
		return this.getServiceFeature().serialize();
	}
	
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public String getHost() {
		if (instanceId != null) {
			return instanceId.split("|")[0].split(":")[0];
		}
		return "UNKOWN";
	}

}
