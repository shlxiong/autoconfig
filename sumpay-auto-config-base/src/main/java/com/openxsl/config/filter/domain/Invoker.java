package com.openxsl.config.filter.domain;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Invoker implements Serializable{
	private String protocol;
	private String service;
	private String method;
	private String application;
	private String owner;
	private String host;
	private String side;    //C:客户端，S:服务端
	
	public Invoker(String protocol, String service, String method) {
		this.setProtocol(protocol);
		this.setService(service);
		this.setMethod(method);
	}
	
	@Override
	public String toString() {
		if (method==null || method.length()==0) {
			return service;
		}
		return String.format("[%s][%s] %s.%s()", protocol,application,service, method);
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
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getSide() {
		return side;
	}
	public void setSide(String side) {
		this.side = side;
	}

}
