package com.openxsl.admin.organ.entity.joint;

import javax.persistence.Column;
import javax.persistence.Table;

import com.openxsl.config.dal.jdbc.BaseEntity;

@SuppressWarnings("serial")
@Table(name="organ_post_role")
public class PositionRole extends BaseEntity<Integer>{
	@Column
	private Integer positId;
	@Column
	private Integer roleId;

	public PositionRole(Integer positId, Integer roleId) {
		this.positId = positId;
		this.roleId = roleId;
	}

	public Integer getPositId() {
		return positId;
	}
	public void setPositId(Integer positId) {
		this.positId = positId;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}
}

