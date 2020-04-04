package com.openxsl.admin.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.openxsl.admin.api.IRestrictedSource;
import com.openxsl.config.dal.jdbc.BaseEntity;

/**
 * 权限资源
 * 
 * @author shuilin.xiong
 */
@Entity
@Table(name = "admin_resource")
@SuppressWarnings("serial")
public class Resource extends BaseEntity<Integer> implements IRestrictedSource {
//	@Column
//	private int id;
	@Column
	private String funcCode;       //编码
	@Column
	private String funcName;       //名称
	@Column
	private String funcType;       //类型：菜单、按钮、数据列
	@Column
	private String funcIco;        //图标（菜单）
	@Column
	private String funcUrl;        //地址
	@Column
	private String parentId;       //上级ID
	@Column
	private int level;        	   //层级
	@Column
	private int seqNo;             //同级序号
	@Column
	private String domain;         //业务系统
	@Column
	private boolean disabled;      //禁用启用
	@Column
	private String openType;       //前端打开方式（菜单）
	
	private transient List<String> roles;
	
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
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
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
	public boolean isDisabled() {
		return disabled;
	}
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
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

}
