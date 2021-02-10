package com.openxsl.admin.security.config;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security")
public class ExSecurityProperties extends SecurityProperties {
	private String authenLoginUrl;
	private String authenSuccessUrl;
	private String authenFailedUrl;
	private String passwordenSalt;
	private int sessionTimeout;
	
	public String getAuthenLoginUrl() {
		return authenLoginUrl;
	}
	public void setAuthenLoginUrl(String authenLoginUrl) {
		this.authenLoginUrl = authenLoginUrl;
	}
	public String getAuthenSuccessUrl() {
		return authenSuccessUrl;
	}
	public void setAuthenSuccessUrl(String authenSuccessUrl) {
		this.authenSuccessUrl = authenSuccessUrl;
	}
	public String getAuthenFailedUrl() {
		return authenFailedUrl;
	}
	public void setAuthenFailedUrl(String authenFailedUrl) {
		this.authenFailedUrl = authenFailedUrl;
	}
	public String getPasswordSalt() {
		return passwordenSalt;
	}
	public void setPasswordSalt(String passwordencoderSalt) {
		this.passwordenSalt = passwordencoderSalt;
	}
	public int getSessionTimeout() {
		return sessionTimeout;
	}
	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

}
