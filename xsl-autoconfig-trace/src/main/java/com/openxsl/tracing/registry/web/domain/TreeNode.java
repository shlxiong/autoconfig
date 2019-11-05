package com.openxsl.tracing.registry.web.domain;

/**
 * 树节点
 * @author xiongsl
 */
public class TreeNode {
	private Integer id;
	private String name;
	private String fullPath;
	
	private Integer parentId;
	private String isParent;
	private boolean open;
	

	public String getIsParent() {
		return isParent;
	}

	public void setIsParent(String isParent) {
		this.isParent = isParent;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	@Override
	public String toString() {
		return "TreeVO [parentId=" + parentId + ", id=" + id + ", name=" + name
				+ ", fullPath=" + fullPath + "]";
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}


}
