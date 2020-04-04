package com.openxsl.admin.entity.joint;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.openxsl.config.dal.jdbc.BaseEntity;

@Entity
@Table(name = "admin_role_resource")
@SuppressWarnings("serial")
public class RoleResource extends BaseEntity<Integer> {
//	@Column
//	private int id;
	private int roleId;
	private int resourceId;
	private boolean disabled;
	
	public RoleResource() {}
	public RoleResource(int roleId, int resourceId) {
		this.setRoleId(roleId);
		this.setResourceId(resourceId);
	}
	
	public int getRoleId() {
		return roleId;
	}
	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}
	public int getResourceId() {
		return resourceId;
	}
	public void setResourceId(int resourceId) {
		this.resourceId = resourceId;
	}
	public boolean isDisabled() {
		return disabled;
	}
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

}
