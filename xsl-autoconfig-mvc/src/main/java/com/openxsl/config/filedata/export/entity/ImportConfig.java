package com.openxsl.config.filedata.export.entity;

import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import javax.persistence.Table;

import com.openxsl.config.dal.jdbc.BaseEntity;

@SuppressWarnings("serial")
@Table(name = "IMPORT_CONFIG")
public class ImportConfig extends BaseEntity<Integer>{
	@ApiModelProperty("业务名称")
	@Column(length=64)
	private String name;
	
	@ApiModelProperty("数据表名")
	@Column(length=64)
	private String tableName;
	
	@ApiModelProperty("备注")
	@Column(length=255)
	private String remark;
	
	@ApiModelProperty("首行是否标题")
	@Column
	private boolean firstCaption;
	
	@Column
	@ApiModelProperty("景区编号")
	private String scenicCode;

	@Column
	@ApiModelProperty("集团编号")
	private String corpCode;
	
	public ImportConfig() {
		
	}
	public ImportConfig(String name) {
		this.setName(name);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public boolean isFirstCaption() {
		return firstCaption;
	}
	public void setFirstCaption(boolean firstCaption) {
		this.firstCaption = firstCaption;
	}
	public String getScenicCode() {
		return scenicCode;
	}
	public void setScenicCode(String scenicCode) {
		this.scenicCode = scenicCode;
	}
	public String getCorpCode() {
		return corpCode;
	}
	public void setCorpCode(String corpCode) {
		this.corpCode = corpCode;
	}
	
}
