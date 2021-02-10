package com.openxsl.admin.organ.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import javax.persistence.Table;

import com.openxsl.config.dal.jdbc.BaseEntity;
import com.openxsl.config.dal.jdbc.anno.InitialData;

@SuppressWarnings("serial")
@InitialData
@ApiModel("中国行政区划")
@Table(name="dict_area")
public class DistrictArea extends BaseEntity<Integer> {
	@ApiModelProperty("地区编码")
	@Column
	private String areaCode;
	
	@ApiModelProperty("上级编码")
	@Column
	private String parentCode;
	
	@ApiModelProperty("地区名称")
	@Column
	private String areaName;
	
	@ApiModelProperty("缩写")
	@Column
	private String shortName;
	
	@ApiModelProperty("经度")
	@Column
	private float longitude;
	
	@ApiModelProperty("纬度")
	@Column
	private float latitude;
	
	@ApiModelProperty("级别(1省/直辖市,2地级市,3区县,4镇/街道)")
	@Column
	private short level;
	
	@ApiModelProperty("排序")
	@Column
	private short sortNo;
	
	@ApiModelProperty("状态(0禁用/1启用)")
	@Column
	private boolean status;
	
	public String getAreaCode() {
		return areaCode;
	}
	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}
	public String getParentCode() {
		return parentCode;
	}
	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}
	public String getAreaName() {
		return areaName;
	}
	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public float getLongitude() {
		return longitude;
	}
	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}
	public float getLatitude() {
		return latitude;
	}
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}
	public short getLevel() {
		return level;
	}
	public void setLevel(short level) {
		this.level = level;
	}
	public short getSortNo() {
		return sortNo;
	}
	public void setSortNo(short sort) {
		this.sortNo = sort;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}

}
