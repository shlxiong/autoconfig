package com.openxsl.admin.organ.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openxsl.admin.api.IUserGroup;
import com.openxsl.admin.organ.dao.PositionRoleDao;

@Service
public class PositionRoleService implements IUserGroup {
	@Autowired
	private PositionRoleDao dao;
	
	@Override
	public List<String> getUsers(String groupId) {
		return dao.queryPostUsers(groupId);
	}

	@Override
	public List<String> getRoles(String groupId) {
		return dao.queryPostRoles(groupId);
	}

	@Override
	public List<String> getUserRoles(String userId) {
		return dao.queryUserRoles(userId);
	}

}
