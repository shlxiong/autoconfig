package com.openxsl.admin.organ.dao;

import java.util.List;

import com.openxsl.admin.organ.entity.Department;
import com.openxsl.config.dal.jdbc.BaseMapper;

public interface DepartmentDao extends BaseMapper<Department> {
	
	public List<Department> findByIds(List<Integer> deptIds);
	
	public int insertQuick(Department department);

}
