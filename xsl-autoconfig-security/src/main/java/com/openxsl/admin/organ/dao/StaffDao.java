package com.openxsl.admin.organ.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.openxsl.admin.entity.UserDetail;
import com.openxsl.admin.organ.entity.Corporation;
import com.openxsl.admin.organ.entity.Staff;
import com.openxsl.config.dal.jdbc.BaseMapper;
import com.openxsl.config.rpcmodel.Pagination;

public interface StaffDao extends BaseMapper<Staff> {
	
	public Staff getStaff(int id);
	
	public Staff getByUserId(int userId);
	
	public Staff findByName(String name);
	
//	public int insert(Staff staff);
	
	public int update(Staff staff);
	
	public int delete(int[] ids);
	
	public Integer getUserType(int staffId);
	
	public List<UserDetail> queryNewUsers(@Param("page")Pagination page);
	
	public int insertQuick(Staff staff);
	
	public int insertQuickCorp(Corporation corp);

}

