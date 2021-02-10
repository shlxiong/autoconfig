package com.openxsl.admin.dao;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import com.openxsl.admin.entity.Resource;
import com.openxsl.config.dal.jdbc.BaseMapper;

public interface ResourceDao extends BaseMapper<Resource> {
	
	public List<Resource> queryAll(String domain, String disabled, Integer level);
	
	public List<Resource> getTopMenu(String domain);
	
	/**
	 * 下级菜单
	 */
	public List<Resource> queryByParent(int parentId);
	
	/**
	 * 所有受限资源
	 */
	public List<Resource> queryRestricted(Set<String> roleIds);
	
	/**
	 * 所有公共资源
	 */
	public List<Resource> queryNonRestricted();
	
//	public int delete(List<Integer> ids);  //super.delete()
//	public Resource getResource(int id);   //super.get()
	
	public int setDisable(@Param("id")int id, @Param("disabled")boolean disabled);

}
