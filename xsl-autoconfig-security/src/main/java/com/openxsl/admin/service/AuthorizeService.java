package com.openxsl.admin.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openxsl.admin.api.IAuthorize;
import com.openxsl.admin.api.IRestrictedSource;
import com.openxsl.admin.api.IUserGroup;
import com.openxsl.admin.context.LocalUserHolder;
import com.openxsl.admin.dao.RoleResourceDao;
import com.openxsl.admin.dao.UserRoleDao;
import com.openxsl.admin.entity.Resource;
import com.openxsl.admin.entity.joint.RoleResource;
import com.openxsl.admin.entity.joint.UserRole;

/**
 * 授权服务
 *    user-role-resource
 *    post-role-resource
 * @author shuilin.xiong
 */
@Service
public class AuthorizeService implements IAuthorize {
	@Autowired
	private UserRoleDao roleDao;
	@Autowired
	private RoleResourceDao resourceDao;
	@Autowired(required=false)
	private IUserGroup userGroup;
	@Autowired
	private ResourceService service;

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
		List<String> roleIds = roleDao.queryUserRoles(Integer.valueOf(userId));
		if (userGroup != null) {
			roleIds.addAll(userGroup.getUserRoles(userId));
		}
		return roleIds;
	}
	
	@Override
	public boolean isUserInRole(String userId, String roleId) {
		for (String role : this.queryUserRoles(userId)) {
			if (roleId.equals(role)) {
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
		if (resourceIds.length < 1) {
			resourceDao.deleteByRole(roleId);
		} else {
			int id = Integer.parseInt(roleId);
			for (String resId : resourceIds) {
				resourceDao.delete(new RoleResource(id, Integer.parseInt(resId)));
			}
		}
	}

	@Override
	public List<? extends IRestrictedSource> queryRoleResources(String roleId) {
		Set<String> roleSet = new HashSet<String>(2);
		roleSet.add(roleId);
		return service.queryRestricted(roleSet);
	}
	
	public List<? extends IRestrictedSource> queryUserResources() {
		List<Resource> results = service.queryNonRestricted();
		
		Set<String> roleIds = new HashSet<String>(
					LocalUserHolder.getUser().getUser().getRoles() );
//		String userId = LocalUserHolder.getUserId();
//		Set<String> roleIds = new HashSet<String>();
//		roleIds.addAll(this.queryUserRoles(userId));
		results.addAll(service.queryRestricted(roleIds));
		results.sort((u,v) -> {
			int rst = u.getLevel() - v.getLevel();
			if (rst == 0) {
				rst = u.getSeqNo() - v.getSeqNo();
			}
			return rst;
		});
		return results;
	}

}
