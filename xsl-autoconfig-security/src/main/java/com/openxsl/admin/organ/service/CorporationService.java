package com.openxsl.admin.organ.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openxsl.admin.organ.dao.CorporationDao;
import com.openxsl.admin.organ.entity.Corporation;
import com.openxsl.admin.organ.entity.Department;
import com.openxsl.admin.organ.entity.OrganTreeView;
import com.openxsl.admin.organ.vo.CorpDeptVO;
import com.openxsl.config.dal.jdbc.BaseService;
import com.openxsl.config.util.TreeView;
import com.openxsl.config.util.TreeView.TreeNode;
import com.openxsl.config.util.TreeView.UTreeNode;

/**
 * 组织机构
 * @author shuilin.xiong
 */
@Service
public class CorporationService extends BaseService<CorporationDao, Corporation, Integer> {
	@Autowired
	private DepartmentService departService;

	public List<Corporation> queryTopCorprs(){
		return mapper.queryTopCorps();
	}
	
	/**
	 * 查询机构及下属机构和部门，以树的形式展现
	 * @param parentId 上级机构，如果为null，则返回全体树
	 */
	public List<UTreeNode> queryAsTree(String parentId){
		if (parentId != null) {
			List<UTreeNode> results = departService.getDepartsOfCorp(parentId);
			results.add(TreeView.toNode(this.get(parentId)));
			List<Corporation> all = this.list(new Corporation());
			for (UTreeNode node : TreeView.getSubNodes(parentId, all)) {
				results.add(node);
				results.addAll(this.getCorpDeptSons(node.getId().toString(), all));
			}
			all.clear();
			return results;
		} else {
//			return TreeView.getTree(this.selectAll());   //只有机构
			List<Corporation> topCorps = this.queryTopCorprs();
			if (topCorps == null) {
				return Collections.emptyList();
			}
			List<UTreeNode> results = new ArrayList<UTreeNode>();
			for (Corporation corp : topCorps) {
				results.add(TreeView.toNode(corp));
				for (UTreeNode node : this.queryAsTree(corp.getId().toString())) {
					if (!results.contains(node)) {  //可能会有重复
						results.add(node);
					}
				}
			}
			return results;
		}
	}
	
	/**
	 * 查询机构的下级机构和部门
	 * @param corpId 上级机构ID
	 */
	public List<CorpDeptVO> queryDirectOrgans(String corpId){
		List<CorpDeptVO> results = new ArrayList<CorpDeptVO>();
		Corporation example = new Corporation();
		example.setParentId(Integer.parseInt(corpId));
		for (Corporation corp : super.list(example)) {
			results.add(CorpDeptVO.fromCorporation(corp));
		}
		for (Department dept : departService.getDirectDeparts(corpId)) {
			results.add(CorpDeptVO.fromDepartment(dept));
		}
		return results;
	}
	
	/**
	 * 取部门所在的机构ID
	 * @param nodeId
	 * @param organMap
	 */
	public String[] getCorporationId(Integer... deptIds) {
		String[] corpIds = new String[deptIds.length];
		Map<String, String> organMap = new HashMap<String, String>();
		for (UTreeNode node : this.queryAsTree(null)) {
			organMap.put(node.getNodeId(), node.getParentId());
		}
		int i = 0;
		for (Integer deptId : deptIds) {
			Department department = departService.get(deptId);
			if (department != null) {
				String nodeId = TreeNode.getNodeId(String.valueOf(deptId), OrganTreeView.DEPT);
				corpIds[i] = this.innerGetCorpId(nodeId, organMap);
			}
			i++;
		}
		return corpIds;
	}
	
	/**
	 * 获取员工所在部门的子机构号
	 * @param staffDepts 部门IDs
	 */
	public List<String> getSubCorpCodes(Integer... staffDepts) {
		Set<String> corpCodeSet = new HashSet<String>();
		List<Corporation> allCorps = this.list(new Corporation());
		Map<Integer, String> codeMap = allCorps.stream().collect(
					Collectors.toMap(Corporation::getId, Corporation::getCode));
		for (String parentId : this.getCorporationId(staffDepts)) {
			if (parentId == null) {
				continue;
			}
			corpCodeSet.add(codeMap.get(Integer.valueOf(parentId)));
			TreeView.getSubNodes(parentId, allCorps).forEach(e -> {
				corpCodeSet.add(codeMap.get(e.getId()));
			});
		}
		return new ArrayList<String>(corpCodeSet);
	}
	
	/**
	 * @see CorporationService#getCorporationId(Integer...)
	 */
	private String innerGetCorpId(String nodeId, Map<String, String> organMap) {
		String parentId = organMap.get(nodeId);
		if (parentId == null) {
			return null;
		}
		if (parentId.endsWith(OrganTreeView.CORP)) {
			return parentId.substring(0, parentId.indexOf("-"));
		} else {
			nodeId = TreeNode.getNodeId(parentId, OrganTreeView.DEPT);
			return this.innerGetCorpId(nodeId, organMap);
		}
	}
	
	private List<UTreeNode> getCorpDeptSons(String corpId, List<Corporation> all){
		List<UTreeNode> results = new ArrayList<UTreeNode>();
		results.addAll(departService.getDepartsOfCorp(corpId));
		for (Corporation corp : all) {
			if (corp.getParentId() == Integer.valueOf(corpId)) {
				results.add(TreeView.toNode(corp));
			}
		}
		return results;
	}

}
