package com.openxsl.admin.organ.entity.joint;

import javax.persistence.Column;
import javax.persistence.Table;

import com.openxsl.config.dal.jdbc.BaseEntity;

@SuppressWarnings("serial")
@Table(name="organ_dept_staff")
public class DeptStaff extends BaseEntity<Integer>{
	@Column
	private Integer deptId;      //部门
	@Column
	private Integer staffId;
	
	public DeptStaff(Integer deptId, Integer staffId) {
		this.deptId = deptId;
		this.staffId = staffId;
	}
	
	public Integer getDeptId() {
		return deptId;
	}
	public void setDeptId(Integer deptId) {
		this.deptId = deptId;
	}
	public Integer getStaffId() {
		return staffId;
	}
	public void setStaffId(Integer staffId) {
		this.staffId = staffId;
	}
	
}
