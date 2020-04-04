package com.openxsl.admin.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openxsl.admin.api.IAuthorize;
import com.openxsl.admin.api.IRestrictedSource;
import com.openxsl.admin.dao.RoleResourceDao;
import com.openxsl.admin.dao.UserRoleDao;
import com.openxsl.admin.entity.Resource;
import com.openxsl.admin.entity.joint.RoleResource;
import com.openxsl.admin.entity.joint.UserRole;

/**
 * 授权服务类
 * 
 * @author xiongsl
 */
@Service
public class AuthorizeService implements IAuthorize {
	@Autowired
	private UserRoleDao roleDao;
	@Autowired
	private RoleResourceDao resourceDao;

	@Override
	public void grantUserRole(String userId, String... roleIds) {
		int usrid = Integer.parseInt(userId);
		for (String roleId : roleIds) {
			roleDao.insert(new UserRole(usrid, Integer.parseInt(roleId)));
		}
	}

	@Override
	public void revokeRolesOfUser(String userId) {
		roleDao.deleteOfUser(Integer.parseInt(userId));
	}

	@Override
	public void revokeUsersOfRole(String roleId) {
		roleDao.deleteOfRole(Integer.parseInt(roleId));
	}

	@Override
	public List<String> queryUserRoles(String userId) {
		return roleDao.queryUserRoles(Integer.valueOf(userId));
	}
	
	@Override
	public boolean isUserInRole(String userId, String roleId) {
		int id = Integer.parseInt(roleId);
		for (String role : this.queryUserRoles(userId)) {
			if (id == Integer.parseInt(role)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<String> queryRoleUsers(String roleId) {
		return roleDao.queryRoleUsers(Integer.parseInt(roleId));
	}

	@Override
	public void bindResources(String roleId, String... resourceIds) {
		int id = Integer.parseInt(roleId);
		for (String resId : resourceIds) {
			resourceDao.insert(new RoleResource(id, Integer.parseInt(resId)));
		}
	}

	@Override
	public void unbindResources(String roleId, String... resourceIds) {
		int id = Integer.parseInt(roleId);
		for (String resId : resourceIds) {
			resourceDao.insert(new RoleResource(id, Integer.parseInt(resId)));
		}
	}

	@Override
	public List<? extends IRestrictedSource> queryRoleResources(String roleId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@SuppressWarnings({"unchecked"})
	public List<? extends IRestrictedSource> queryUserResources(String userId) {
		List<Resource> lstResource = new ArrayList<Resource>();
		for (String roleId : this.queryUserRoles(userId)) {
			lstResource.addAll((List<Resource>)this.queryRoleResources(roleId));
		}
		return lstResource;
	}
}
