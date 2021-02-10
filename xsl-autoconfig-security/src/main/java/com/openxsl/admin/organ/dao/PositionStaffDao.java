package com.openxsl.admin.organ.dao;

import java.util.List;

import com.openxsl.admin.organ.entity.Staff;
import com.openxsl.admin.organ.entity.joint.PositionStaff;
import com.openxsl.config.dal.jdbc.BaseMapper;

public interface PositionStaffDao extends BaseMapper<PositionStaff> {

	void deleteByStaffId(Integer posiId);
	
	List<Staff> queryStaffsByPosit(Integer posiId);
	
}

