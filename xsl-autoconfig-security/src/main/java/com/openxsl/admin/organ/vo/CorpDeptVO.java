package com.openxsl.admin.organ.vo;

import com.openxsl.admin.organ.entity.Corporation;
import com.openxsl.admin.organ.entity.Department;
import com.openxsl.admin.organ.entity.OrganTreeView;

/**
 * 机构和部门的公共属性
 * 
 * @author shuilin.xiong
 */
public class CorpDeptVO {
	private Integer id;
	private String code;
	private String name;
	private String areaCode;
	private String dataType;
	
	public static CorpDeptVO fromCorporation(Corporation corp) {
		CorpDeptVO vo = new CorpDeptVO();
		vo.setId(corp.getId());
		vo.setCode(corp.getCode());
		vo.setName(corp.getName());
		vo.setAreaCode(corp.getAreaCode());
		vo.setDataType(OrganTreeView.CORP);
		return vo;
	}
	public static CorpDeptVO fromDepartment(Department dept) {
		CorpDeptVO vo = new CorpDeptVO();
		vo.setId(dept.getId());
		vo.setCode(dept.getCode());
		vo.setName(dept.getName());
		vo.setAreaCode(dept.getAreaCode());
		vo.setDataType(OrganTreeView.DEPT);
		return vo;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
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
	public String getAreaCode() {
		return areaCode;
	}
	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

}
