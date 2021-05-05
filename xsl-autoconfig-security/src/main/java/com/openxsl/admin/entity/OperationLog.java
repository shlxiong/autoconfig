package com.openxsl.admin.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.openxsl.config.dal.jdbc.BaseEntity;
import com.openxsl.config.dal.jdbc.anno.Index;

/**
 * 操作日志
 * 
 * @author shuilin.xiong
 */
@Entity
@Table(name = "admin_access_log")
@SuppressWarnings("serial")
@ApiModel("用户操作日志")
public class OperationLog extends BaseEntity<Long> {
//	@Column
//	private long id;
	@Column(length=16)
	@Index
	@ApiModelProperty("用户ID")
	private String userName;
	@Column
	@ApiModelProperty("IP地址")
	private String hostIp;
	@Column
	@ApiModelProperty("操作系统")
	private String operSys;
	@Column
	@ApiModelProperty("浏览器")
	private String browser;
	@Column(length=64)
	@ApiModelProperty("操作资源")
	private String operation;     //resource
	@Column
	@Index
	@ApiModelProperty("日期")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date operateDate = new Date();
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
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
	
	public static void main(String[] args) {
		System.out.println(new OperationLog().generDDLSql());
	}
}
