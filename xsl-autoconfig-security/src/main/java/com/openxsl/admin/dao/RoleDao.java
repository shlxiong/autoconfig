package com.openxsl.admin.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.openxsl.admin.entity.Role;
import com.openxsl.config.dal.jdbc.BaseMapper;

public interface RoleDao extends BaseMapper<Role> {
	
//	public List<Role> queryAll(String disabled);
	
	public Role getRole(int roleId);
	
	public List<Role> queryUserRoles(int userId);
	
	public int setDisabled(@Param("roleId")int roleId, @Param("disabled")boolean disabled);
	
}
