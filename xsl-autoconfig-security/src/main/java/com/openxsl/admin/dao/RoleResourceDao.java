package com.openxsl.admin.dao;

import java.util.List;

import com.openxsl.admin.entity.joint.RoleResource;

public interface RoleResourceDao {
	
	public int insert(RoleResource roleSource);
	
	public int delete(RoleResource roleSource);
	
	public List<RoleResource> queryAll();
	
	public List<RoleResource> listByRole(String roleId);

	public int deleteByRole(String roleId);

}
