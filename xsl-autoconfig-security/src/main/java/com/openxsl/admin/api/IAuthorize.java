package com.openxsl.admin.api;

import java.util.List;

/**
 * 授权接口（用户-角色）
 * @author shuilin.xiong
 */
public interface IAuthorize {

	/**
	 * 用户授权
	 * @param userId
	 * @param roleIds
	 */
    public void grantUserRole(String userId, String... roleIds);

    /**
     * 删除用户权限
     * @param userId
     */
    public void revokeRolesOfUser(String userId);

    /**
     * 删除角色的所有用户
     * @param roleId
     */
    public void revokeUsersOfRole(String roleId);

    /**
     * 判断用户是否有权限
     * @param userId
     * @param roleId
     */
    public boolean isUserInRole(String userId, String roleId);

    /**
     * 查询用户的角色
     * @param userId
     */
    public List<String> queryUserRoles(String userId);

    /**
     * 查询角色的所有用户
     * @param roleId
     */
    public List<String> queryRoleUsers(String roleId);
    
    /**
     * 角色授权
     * @param roleId
     * @param resourceIds
     */
    public void bindResources(String roleId, String... resourceIds);
    
    /**
     * 解除角色权限
     * @param roleId
     * @param resourceIds
     */
    public void unbindResources(String roleId, String... resourceIds);
    
    /**
     * 查询角色权限
     * @param roleId
     */
    public List<? extends IRestrictedSource> queryRoleResources(String roleId);

}
