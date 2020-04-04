package com.openxsl.admin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.openxsl.config.dal.jdbc.BaseEntity;

@Entity
@Table(name = "admin_biz_system")
@SuppressWarnings("serial")
public class BizSystem extends BaseEntity<Integer> {
//	@Column
//	private int id;
	@Column(name="sys_code")
	private String sysCode;
	@Column(name="sys_name")
	private String sysName;
	@Column(name="sys_url")
	private String sysUrl;
	@Column
	private String logo;
	@Column(name="seq_no")
	private int seqNo;
	@Column
	private boolean visible;
	
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
	public boolean isVisible() {
		return visible;
	}
	public boolean isShow() {
		return visible;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

}
