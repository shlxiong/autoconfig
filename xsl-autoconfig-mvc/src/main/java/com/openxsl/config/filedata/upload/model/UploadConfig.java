package com.openxsl.config.filedata.upload.model;

import io.swagger.annotations.ApiModel;

import javax.persistence.Column;
import javax.persistence.Table;

import com.openxsl.config.dal.jdbc.BaseEntity;

@ApiModel("上传文件的配置属性")
@SuppressWarnings("serial")
@Table(name = "upload_config")
public class UploadConfig extends BaseEntity<Integer>{
	@Column
	private String serviceId;
	@Column
	private String password;
	@Column
	private String contentType = "*";
	@Column
	private String allowFileExts;
	@Column(length=32)
	private String basePath;
	@Column
	private long maxFileSize;
	@Column
	private long maxRequestSize;
	@Column(length=128)
	private String serviceClass;
	
	public static void main(String[] args) {
		System.out.println(new UploadConfig().generDDLSql());
		System.out.println(new FileItem().generDDLSql());
	}
	
	public boolean supports(String contentType) {
		if ("*".equals(this.contentType) || this.contentType.equals(contentType)) {
			return true;
		} else {
			if (contentType != null) {
				String header = contentType.split("\\/")[0];
				return this.contentType.startsWith(header+"/");
			}
			return false;
		}
	}
	
	public boolean allowsFile(String fileExt) {
		if (allowFileExts == null || "".equals(allowFileExts) || "*".equals(allowFileExts)) {
			return true;
		}
		for (String each : allowFileExts.split(",")) {
			if (each.trim().equalsIgnoreCase(fileExt)) {
				return true;
			}
		}
		return false;
	}
	
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getAllowFileExts() {
		return allowFileExts;
	}
	public void setAllowFileExts(String allowFileExts) {
		this.allowFileExts = allowFileExts;
	}
	public long getMaxFileSize() {
		return maxFileSize;
	}
	public void setMaxFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}
	public long getMaxRequestSize() {
		return maxRequestSize;
	}
	public void setMaxRequestSize(long maxRequestSize) {
		this.maxRequestSize = maxRequestSize;
	}
	public String getServiceClass() {
		return serviceClass;
	}
	public void setServiceClass(String serviceClass) {
		this.serviceClass = serviceClass;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getBasePath() {
		return basePath;
	}
	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

}
