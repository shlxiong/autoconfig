package com.openxsl.tracing.registry.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import com.openxsl.config.Environment;
import com.openxsl.config.util.NetworkUtils;
import com.openxsl.config.util.StringUtils;

public class MvcWebApp extends Registration{
	private String domain;       //域名或IP地址
	
	public MvcWebApp() {
		String domain = Environment.getProperty("server.domain.url");
		if (StringUtils.isEmpty(domain)) {
			domain = Environment.getAddress();
		} else {
			domain = domain.replaceFirst("http(s)?://", "").replace("/", "|");
		}
		String contextPath = Environment.getProperty("server.context-path","");
		while (contextPath.length()>0 && contextPath.charAt(0)=='/') {
			contextPath = contextPath.substring(1);
		}
		if (contextPath.length() > 0) {
			domain += "|" + contextPath;
		}
		this.setDomain(domain);
	}
	
	public MvcWebApp(String url) {
		String serviceKey = getServiceKey(url);
		this.setDomain(serviceKey);
	}
	
	@SuppressWarnings("serial")
	public ServiceKey getServiceKey() {
		return new ServiceKey() {
			@Override
			public String serialize() {
				return domain;
			}
			@Override
			public Map<String, Serializable> deserialize() {
				return Collections.singletonMap("domain", domain);
			}
		};
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public static String getServiceKey(String url) {
		return url.replaceFirst("http(s)?://", "")
				  .replaceFirst("(localhost|127.0.0.1)", NetworkUtils.LOCAL_IP)
				  .replace("/", "|");
	}

}
