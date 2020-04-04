package com.openxsl.admin.dao;

import java.util.List;

import com.openxsl.admin.entity.Role;

public interface RoleDao {
	
	public List<Role> queryAll(String disabled);
	
	public Role getRole(int roleId);
	
	public List<Role> queryUserRoles(int userId);
	
}
