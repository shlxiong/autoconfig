package com.openxsl.config.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 根节点的 parent为null
 * 
 * @author shuilin.xiong
 */
public class TreeView {
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> List<T> getTree(List<? extends TreeNode> all){
		return (List)getSubNodes(null, all);
	}
	
	public static List<? extends TreeNode> getSubNodes(Integer parentId, List<? extends TreeNode> all){
		List<TreeNode> children = new ArrayList<TreeNode>();
		for (TreeNode child : all) {
			if (child.getParentId() == parentId) {
				children.add(child);
				children.addAll(getSubNodes(child.getId(), all));
			}
		}
		return children;
	}
	
	public interface TreeNode {
		
		public Integer getId();
		
		public Integer getParentId();
		
	}

}
