package com.openxsl.admin.organ.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.openxsl.config.dal.jdbc.BaseEntity;
import com.openxsl.config.util.TreeView.TreeNode;

@Entity
@Table(name = "organ_department")
@SuppressWarnings("serial")
public class Department extends BaseEntity<Integer> {
//	@Column
//	private int id;
	@Column
	private String code;
	@Column
	private String name;
	@Column
	private String duty;
	@Column
	private Integer parentId;
	@Column
	private String corpId;
	@Column
	private String areaCode;
	@Column
	private String director;
	@Column
	private Integer seqNo;
	
	public String getNodeType() {
		return OrganTreeView.DEPT;
	}
	public String getNodeId() {
		return TreeNode.getNodeId(String.valueOf(this.getId()), this.getNodeType());
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDuty() {
		return duty;
	}
	public void setDuty(String duty) {
		this.duty = duty;
	}
	public Integer getParentId() {
		return parentId;
	}
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
	public String getCorpId() {
		return corpId;
	}
	public void setCorpId(String corpCode) {
		this.corpId = corpCode;
	}
	public String getAreaCode() {
		return areaCode;
	}
	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}
	public String getDirector() {
		return director;
	}
	public void setDirector(String director) {
		this.director = director;
	}
	public Integer getSeqNo() {
		return seqNo;
	}
	public void setSeqNo(Integer seqNo) {
		this.seqNo = seqNo;
	}
	
}
