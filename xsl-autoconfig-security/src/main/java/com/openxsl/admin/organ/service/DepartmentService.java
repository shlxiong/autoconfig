package com.openxsl.admin.organ.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.openxsl.admin.organ.dao.DepartmentDao;
import com.openxsl.admin.organ.entity.Department;
import com.openxsl.admin.organ.entity.OrganTreeView;
import com.openxsl.config.dal.jdbc.BaseService;
import com.openxsl.config.util.TreeView;
import com.openxsl.config.util.TreeView.TreeNode;
import com.openxsl.config.util.TreeView.UTreeNode;

/**
 * 
 * @author shuilin.xiong
 */
@Service
public class DepartmentService extends BaseService<DepartmentDao, Department, Integer> {
	
	/**
	 * 查询机构的所有垂直部门
	 * @param corpId
	 */
	public List<UTreeNode> getDepartsOfCorp(String corpId){
		List<UTreeNode> children = new ArrayList<UTreeNode>();
		List<Department> all = super.list(new Department());
		for (Department dept : this.getDirectDeparts(corpId)) {
			UTreeNode node = TreeView.toNode(dept);
			if (children.contains(node)) {
				continue;
			}
			if (node.getParentId() == null) {
				node.setParentId(TreeNode.getNodeId(corpId,OrganTreeView.CORP), true);  //TreeNode.getNodeId
			}
			children.add(node);
			children.addAll(TreeView.getSubNodes(dept.getNodeId(), all));
		}
		all.clear();
		return children;
	}
	
	/**
	 * 查询某个部门的垂直子部门
	 * @param deptId
	 * @return
	 */
	public List<UTreeNode> getDepartTree(String deptId){
		List<Department> all = super.list(new Department());
		return TreeView.getSubNodes(deptId, all);
	}
	
	/**
	 * 查询机构的直属部门
	 * @param corpId
	 */
	public List<Department> getDirectDeparts(String corpId){
		Department department = new Department();
		department.setCorpId(corpId);
		return super.list(department);
	}
	
	/**
	 * 查询机构或部门的垂直子部门
	 * @param nodeId 树节点ID
	 */
	public List<UTreeNode> getDepartsByNodeId(String nodeId){
		if (nodeId.endsWith(OrganTreeView.CORP)) {
			String corpId = nodeId.substring(0, nodeId.length()-5);
			return this.getDepartsOfCorp(corpId);
		} else {
			if (nodeId.endsWith(OrganTreeView.DEPT)){
				nodeId = nodeId.substring(0, nodeId.length()-5);
			}
			List<UTreeNode> children = new ArrayList<UTreeNode>(2);
			Department department = this.get(nodeId);
			if (department != null) {
				children.add(TreeView.toNode(department));
				children.addAll(this.getDepartTree(nodeId));
			}
			return children;
		}
	}
	
}
