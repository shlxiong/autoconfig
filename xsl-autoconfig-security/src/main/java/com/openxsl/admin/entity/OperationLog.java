package com.openxsl.admin.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.openxsl.config.dal.jdbc.BaseEntity;

/**
 * 操作日志
 * 
 * @author shuilin.xiong
 */
@Entity
@Table(name = "admin_access_log")
@SuppressWarnings("serial")
public class OperationLog extends BaseEntity<Long> {
//	@Column
//	private long id;
	@Column
	private String userId;
	@Column
	private String hostIp;
	@Column
	private String operSys;
	@Column
	private String browser;
	@Column
	private String operation;     //resource
	@Column
	private Date operateDate = new Date();
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getHostIp() {
		return hostIp;
	}
	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}
	public String getOperSys() {
		return operSys;
	}
	public void setOperSys(String operSys) {
		this.operSys = operSys;
	}
	public String getBrowser() {
		return browser;
	}
	public void setBrowser(String browser) {
		this.browser = browser;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public Date getOperateDate() {
		return operateDate;
	}
	public void setOperateDate(Date operateDate) {
		this.operateDate = operateDate;
	}

}
