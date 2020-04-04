package com.openxsl.admin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.openxsl.config.dal.jdbc.BaseEntity;

@Entity
@Table(name = "admin_role")
@SuppressWarnings("serial")
public class Role extends BaseEntity<Integer> {
	public static final String ADMIN = "admin";
	public static final String ADMIN_ID = "0";
	
//	@Column
//	private int id;
	@Column(name="role_code")
	private String roleCode;
	@Column(name="role_name")
	private String roleName;
	@Column
	private String remark;
	@Column
	private boolean disabled;
	@Column
	private String domain;             //@sysId 来自哪个系统
	
	public String getRoleCode() {
		return roleCode;
	}
	public void setRoleCode(String roleCode) {
		this.roleCode = roleCode;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public boolean isDisabled() {
		return disabled;
	}
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
}
