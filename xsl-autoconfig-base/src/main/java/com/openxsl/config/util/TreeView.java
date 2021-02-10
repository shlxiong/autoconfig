package com.openxsl.config.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.fastjson.JSON;

/**
 * 根节点的 parent为null
 * 
 * @author shuilin.xiong
 */
public class TreeView {
	
	public static List<UTreeNode> getTree(List<?> all){
		return getSubNodes(null, all);
	}
	
	public static List<UTreeNode> getSubNodes(String parentId, List<?> all){
		List<UTreeNode> children = new ArrayList<UTreeNode>();
		for (Object elt : all) {
			UTreeNode node = toNode(elt);
			if (parentId == null) {
				if (node.getParentId() == null) {
					children.add(node);
					children.addAll(getSubNodes(node.getNodeId(), all));
				}
			} else {
				parentId = TreeNode.getNodeId(parentId, node.getNodeType());
				if (parentId.equals(node.getParentId())) {
					children.add(node);
					children.addAll(getSubNodes(node.getNodeId(), all));
				}
			}
		}
		return children;
	}
	
	public static List<UTreeNode> getGrandParents(String nodeId, List<?> all){
		List<UTreeNode> results = new ArrayList<UTreeNode>();
		Collections.reverse(all);
		TreeNode _self = null;
		for (Object elt : all) {
			UTreeNode node = toNode(elt);
			if (_self == null) {
				if (nodeId.equals(node.getNodeId())) {
					_self = node;
				}
			} else {
				if (_self.getParentId().equals(node.getNodeId())) {
					_self = node;
					results.add(0, node);
				}
			}
		}
		return results;
	}
	
	//利用fastjson做类型转换
	public static UTreeNode toNode(Object data) {
		if (data==null || data instanceof UTreeNode) {
			return (UTreeNode)data;
		}
		return JSON.parseObject(JSON.toJSONString(data), UTreeNode.class);
	}
	
	public interface TreeNode {
		
		public String getNodeId();
		
		public String getParentId();
		
		static String getNodeId(String id, String suffix) {
			return (suffix==null || id==null || id.endsWith("-"+suffix)) ? id
					: (id+"-"+suffix);
		}
		
	}
	
	public static class UTreeNode implements TreeNode{
		private Integer id;
		private String nodeId;
		private String parentId;
		private String name;
		private String url;
		private String nodeType;
		private boolean isLeaf = true;
		private transient boolean reset;   //是否手动设置的
		
		public Integer getId() {
			return id;
		}
		
		@Override
		public String getNodeId() {
			if (nodeId == null) {
				nodeId = TreeNode.getNodeId(String.valueOf(id), nodeType);
			}
			return nodeId;
		}

		@Override
		public String getParentId() {
			return reset ? parentId : TreeNode.getNodeId(parentId, nodeType);
		}
		
		@Override
		public boolean equals(Object that) {
			if (that == null || !(that instanceof UTreeNode)) {
				return false;
			}
			return this.getNodeId().equals(((UTreeNode)that).getNodeId());
		}

		public void setNodeId(String nodeId) {
			this.nodeId = nodeId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getNodeType() {
			return nodeType;
		}

		public void setNodeType(String nodeType) {
			this.nodeType = nodeType;
		}

		public boolean isIsLeaf() {
			return isLeaf;
		}

		public void setIsLeaf(boolean isLeaf) {
			this.isLeaf = isLeaf;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public void setParentId(String parentId) {
			this.parentId = parentId;
		}
		public void setParentId(String parentId, boolean reset) {
			this.parentId = parentId;
			this.reset = reset;
		}
		
	}

}
