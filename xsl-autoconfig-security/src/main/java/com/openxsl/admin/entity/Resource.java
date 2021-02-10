package com.openxsl.admin.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Table;

import com.openxsl.admin.api.IRestrictedSource;
import com.openxsl.config.dal.jdbc.BaseEntity;
import com.openxsl.config.dal.jdbc.anno.InitialData;

/**
 * 权限资源
 * 
 * @author shuilin.xiong
 */
@SuppressWarnings("serial")
@InitialData
@ApiModel("系统资源表")
@Table(name = "admin_resource")
public class Resource extends BaseEntity<Integer> implements IRestrictedSource {
//	@Column
//	private int id;
	@Column
	@ApiModelProperty("功能编码")
	private String funcCode;
	@Column
	@ApiModelProperty("名称")
	private String funcName;
	@Column
	@ApiModelProperty("类型：菜单、按钮、数据列")
	private String funcType;  //'menu','button','data'
	@Column
	@ApiModelProperty("图标（菜单）")
	private String funcIco;
	@Column
	@ApiModelProperty("地址")
	private String funcUrl;
	@Column
	@ApiModelProperty("上级ID")
	private Integer parentId;
	@Column
	@ApiModelProperty("层级")
	private int level;
	@Column
	@ApiModelProperty("同级序号")
	private int seqNo;
	@Column
	@ApiModelProperty("是否叶子节点")
	private boolean isLeaf;
	@Column
	@ApiModelProperty("业务系统")
	private String domain;
	@Column
	@ApiModelProperty("前端打开方式（菜单）")
	private String openType;
	@Column
	@ApiModelProperty("是否禁用")
	private boolean disabled;
	
	private transient Integer roleId;   //RoleResource.role_id
	private transient List<String> roles = new ArrayList<String>(0);
	
	@Override
	public String getUrl() {
		return funcUrl;
	}
	@Override
	public List<String> getAuthorities() {
		List<String> authorities = new ArrayList<String>();
		if (roles != null) {
			authorities.addAll(roles);
		} 
		return authorities;
	}
	
	public String getNodeId() {
		return String.valueOf(this.getId());
	}
	public String getName() {
		return this.getFuncName();
	}
	
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	public String getFuncCode() {
		return funcCode;
	}
	public void setFuncCode(String funcCode) {
		this.funcCode = funcCode;
	}
	public String getFuncName() {
		return funcName;
	}
	public void setFuncName(String funcName) {
		this.funcName = funcName;
	}
	public String getFuncType() {
		return funcType;
	}
	public void setFuncType(String funcType) {
		this.funcType = funcType;
	}
	public String getFuncIco() {
		return funcIco;
	}
	public void setFuncIco(String funcIco) {
		this.funcIco = funcIco;
	}
	public String getFuncUrl() {
		return funcUrl;
	}
	public void setFuncUrl(String funcUrl) {
		this.funcUrl = funcUrl;
	}
	public Integer getParentId() {
		return parentId;
	}
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getSeqNo() {
		return seqNo;
	}
	public void setSeqNo(int seqNo) {
		this.seqNo = seqNo;
	}
	public String getOpenType() {
		return openType;
	}
	public void setOpenType(String openType) {
		this.openType = openType;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public boolean isLeaf() {
		return isLeaf;
	}
	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}
	public boolean isDisabled() {
		return disabled;
	}
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	public Integer getRoleId() {
		return roleId;
	}
	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
		this.roles.add(String.valueOf(roleId));
	}

}
