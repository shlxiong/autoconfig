package com.openxsl.admin.organ.dao;

import com.openxsl.admin.organ.entity.Staff;
import com.openxsl.config.dal.jdbc.BaseMapper;

public interface StaffDao extends BaseMapper<Staff> {
	
	public Staff getStaff(int id);
	
	public Staff getByUserId(int userId);
	
	public Staff findByName(String name);
	
//	public int insert(Staff staff);
	
	public int update(Staff staff);
	
	public int delete(int[] ids);
	
	public Integer getUserType(int staffId);

}

