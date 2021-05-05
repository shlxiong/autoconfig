package com.openxsl.config.filedata.export.entity;

import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import javax.persistence.Table;

import com.openxsl.config.dal.jdbc.BaseEntity;

/**
 * Excel数据导入数据库中
 */
@SuppressWarnings("serial")
@Table(name = "IMPORT_MAPPING")
public class ImportMapping extends BaseEntity<Long> {
	/**
     * 名称
     */
    @ApiModelProperty(value = "名称")
	@Column
    private String configName;
	/**
     * 字段名
     */
    @ApiModelProperty(value = "字段名")
	@Column
    private String columnName;
    /**
     * 关联表字段
     */
    @ApiModelProperty(value = "关联表字段")
	@Column
    private String excelColumnNo;
    /**
     * 数据类型
     */
    @ApiModelProperty(value = "数据类型")
    @Column
    private String dataType;
    /**
     * 最大长度
     */
    @ApiModelProperty(value = "最大长度")
	@Column
    private Integer maxLen;
	/**
     * 关联表字段
     */
    @ApiModelProperty(value = "关联表字段")
	@Column
    private String reference;
	/**
     * 校验格式
     */
    @ApiModelProperty(value = "校验格式（1日期 2时间 3身份证 4手机号 5电话号[包括手机] ）")
	@Column
    private String formatCheck;
    
    @Column
	@ApiModelProperty("景区编号")
	private String scenicCode;

	@Column
	@ApiModelProperty("集团编号")
	private String corpCode;
    
	public String getConfigName() {
		return configName;
	}
	public void setConfigName(String configName) {
		this.configName = configName;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public Integer getMaxLen() {
		return maxLen;
	}
	public void setMaxLen(Integer maxLen) {
		this.maxLen = maxLen;
	}
	public String getReference() {
		return reference;
	}
	public void setReference(String reference) {
		this.reference = reference;
	}
	public String getFormatCheck() {
		return formatCheck;
	}
	public void setFormatCheck(String formatCheck) {
		this.formatCheck = formatCheck;
	}
	public String getExcelColumnNo() {
		return excelColumnNo;
	}
	public void setExcelColumnNo(String excelColumnNo) {
		this.excelColumnNo = excelColumnNo;
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
