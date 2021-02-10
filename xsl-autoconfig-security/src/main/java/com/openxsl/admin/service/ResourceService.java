package com.openxsl.admin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.openxsl.admin.dao.ResourceDao;
import com.openxsl.admin.entity.Resource;
import com.openxsl.config.dal.jdbc.BaseService;
import com.openxsl.config.statis.StatisGroupMap;
import com.openxsl.config.util.TreeView;
import com.openxsl.config.util.TreeView.UTreeNode;

@Service
public class ResourceService extends BaseService<ResourceDao, Resource, Integer> {
	@Autowired
	private ResourceDao dao;
	
	/**
	 * 查询指定角色的资源
	 * @param roleId
	 */
	public List<Resource> queryRestricted(Set<String> roleIds) {
		List<Resource> resources = dao.queryRestricted(roleIds);  //order by id
		return this.mergeByRole(resources);
	}
	
	/**
	 * 查询所有没权限控制的资源
	 */
	public List<Resource> queryNonRestricted(){
		return dao.queryNonRestricted();
	}
	
	public List<Resource> getTopMenu(String domain) {
		return dao.getTopMenu(domain);
	}
	
	public List<Resource> getSubTree(Integer parentId){
		List<Resource> results = new ArrayList<Resource>();
		if (parentId != null) {
			List<Resource> temps = dao.queryByParent(parentId);
			if (temps != null) {  //tk-mapper
				for (Resource elt : temps) {
					results.add(elt);
					results.addAll(this.getSubTree(elt.getId()));
				}
				temps.clear();
			}
		}
		return results;
	}
	
	/**
	 * 获取菜单树
	 */
	public List<UTreeNode> queryAsTree(String domain){
		return TreeView.getTree(dao.queryAll(domain, "0", null));
	}
	
//	public int delete(List<Integer> ids) {  //super.delete(ids)
//		Assert.notEmpty(ids, "没有提交任何数据");
//		return dao.delete(ids);
//	}
	
	public int setDisable(int id, boolean disabled) {
		return dao.setDisable(id, disabled);
	}
	
	/**
	 * 获取下一级菜单（含权限）
	 * @param parentId
	 */
	public List<Resource> queryByParent(int parentId){
		return dao.queryByParent(parentId);
	}
	
	public List<UTreeNode> queryGrandParents(String id){
		List<UTreeNode> allNodes = this.queryAsTree(null);
		return TreeView.getGrandParents(id, allNodes);
	}
//	
//	/**
//	 * 取一个资源的权限，如果是非叶子节点，则取其子节点的权限
//	 * @param resourceId
//	 */
//	private List<String> getResourceRoles(int resourceId){
//		List<String> roles = new ArrayList<String>();
//		List<Resource> resources = dao.queryByParent(resourceId);
//		for (Resource resource : resources) {
//			if (resource.isLeaf()) {
//				roles.add(String.valueOf(resource.getRoleId()));
//			} else {
//				roles.addAll(this.getResourceRoles(resource.getId()));
//			}
//		}
//		return roles;
//	}
	
	/**
	 * 按照Resource.id合并roleId
	 * @param resources
	 */
	private List<Resource> mergeByRole(List<Resource> resources) {
		List<Resource> results = new ArrayList<Resource>();
		if (CollectionUtils.isEmpty(resources)) {
			return results;
		}
		
		return StatisGroupMap.groupBy(resources, Resource::getId, (u,v)->{
				u.getRoles().add(String.valueOf(v.getRoleId()));
			});
	}
	
}
