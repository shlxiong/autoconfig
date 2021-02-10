package com.openxsl.admin.api;

import java.util.List;

/**
 * 用户组
 * @author shuilin.xiong
 */
public interface IUserGroup {
	
	/**
	 * 组内有哪些用户
	 * @param groupId
	 */
	public List<String> getUsers(String groupId);
	
	/**
	 * 获取用户组的角色ID
	 * @param groupId
	 */
	public List<String> getRoles(String groupId);
	
	/**
	 * 通过用户组关联的某个用户的角色
	 * @param groupId
	 */
	public List<String> getUserRoles(String groupId);

}
