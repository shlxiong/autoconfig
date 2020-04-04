package com.openxsl.admin.dao;

import java.util.List;

import com.openxsl.admin.entity.joint.UserRole;

public interface UserRoleDao {
	
	public int insert(UserRole userRole);
	
	public int deleteOfUser(int userId);
	
	public int deleteOfRole(int roleId);
	
	public List<String> queryUserRoles(int userId);
	
	public List<String> queryRoleUsers(int roleId);

}
