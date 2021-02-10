package com.openxsl.admin.entity.joint;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.openxsl.config.dal.jdbc.BaseEntity;

@Entity
@Table(name = "admin_user_role")
@SuppressWarnings("serial")
public class UserRole extends BaseEntity<Integer> {
//	@Column
//	private int id;
	@Column
	private int userId;
	@Column
	private int roleId;
	@Column
	private Date expires;
	@Column
	private boolean disabled;
	
	public UserRole() {}
	public UserRole(int userId, int roleId) {
		this.setUserId(userId);
		this.setRoleId(roleId);
	}
	
	public long getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public long getRoleId() {
		return roleId;
	}
	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}
	public Date getExpires() {
		return expires;
	}
	public void setExpires(Date expires) {
		this.expires = expires;
	}
	public boolean isDisabled() {
		return disabled && (expires==null || expires.after(new Date()));
	}
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

}
