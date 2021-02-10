package com.openxsl.admin.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.openxsl.config.dal.jdbc.BaseEntity;

@Entity
@Table(name = "admin_biz_system")
@ApiModel("业务子系统表")
@SuppressWarnings("serial")
public class BizSystem extends BaseEntity<Integer> {
//	@Column
//	private int id;
	@Column(length=16)
	@ApiModelProperty("系统编码")
	private String sysCode;
	@Column
	@ApiModelProperty("系统名称")
	private String sysName;
	@Column(length=64)
	@ApiModelProperty("系统首页")
	private String sysUrl;
	@Column
	@ApiModelProperty("LOGO")
	private String logo;
	@Column
	@ApiModelProperty("显示顺序")
	private int seqNo;
//	@Column(length=256)
//	@ApiModelProperty("角色")
//	private String roles;
	
	public String getSysCode() {
		return sysCode;
	}
	public void setSysCode(String sysCode) {
		this.sysCode = sysCode;
	}
	public String getSysName() {
		return sysName;
	}
	public void setSysName(String sysName) {
		this.sysName = sysName;
	}
	public String getSysUrl() {
		return sysUrl;
	}
	public void setSysUrl(String sysUrl) {
		this.sysUrl = sysUrl;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public int getSeqNo() {
		return seqNo;
	}
	public void setSeqNo(int seqNo) {
		this.seqNo = seqNo;
	}
	
}
