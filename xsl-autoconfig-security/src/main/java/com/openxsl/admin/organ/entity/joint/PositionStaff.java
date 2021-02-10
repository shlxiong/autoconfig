package com.openxsl.admin.organ.entity.joint;

import javax.persistence.Column;
import javax.persistence.Table;

import com.openxsl.config.dal.jdbc.BaseEntity;

@SuppressWarnings("serial")
@Table(name="organ_post_staff")
public class PositionStaff extends BaseEntity<Integer>{
	@Column
	private Integer positId;
	@Column
	private Integer staffId;
	
	public PositionStaff(Integer positId, Integer staffId) {
		this.positId = positId;
		this.staffId = staffId;
	}
	
	public Integer getPositId() {
		return positId;
	}
	public void setPositId(Integer positId) {
		this.positId = positId;
	}
	public Integer getStaffId() {
		return staffId;
	}
	public void setStaffId(Integer staffId) {
		this.staffId = staffId;
	}
	
}
