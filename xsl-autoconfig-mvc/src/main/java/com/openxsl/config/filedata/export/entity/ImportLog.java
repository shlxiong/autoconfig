package com.openxsl.config.filedata.export.entity;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import com.alibaba.fastjson.JSON;
import com.openxsl.config.dal.jdbc.BaseEntity;
import com.openxsl.config.dal.jdbc.anno.Index;

/**
 * @类名: 导入日志
 * @描述: 实体对象 导入日志
 */
@SuppressWarnings("serial")
@Table(name = "IMPORT_LOG")
public class ImportLog extends BaseEntity<Long> {
    @ApiModelProperty(value = "批次号")
	@Column
    private String batchNo;
    
    @ApiModelProperty(value = "业务名称")
    @Index
	@Column
    private String importName;
    
    @ApiModelProperty(value = "文件名")
    private String sourceFile;
	
    @ApiModelProperty(value = "导入时间")
    @Index
	@Column
    private Date importTime;
    
    @ApiModelProperty(value = "操作员")
   	@Column
    private String operator;
    
    @ApiModelProperty(value = "备注或原因")
	@Column(length=64)
    private String remark;
	
    @ApiModelProperty(value = "总记录数")
	@Column
    private int totalNum;
	
    @ApiModelProperty(value = "导入成功记录数")
	@Column
    private int successNum;
	
    @ApiModelProperty(value = "导入失败记录数")
	@Column
    private int failNum;
	
    @ApiModelProperty(value = "失败记录ID")
	@Column(length=1023)
    private String failRecord = "";
    
    @Column
	@ApiModelProperty("景区编号")
	private String scenicCode;

	@Column
	@ApiModelProperty("集团编号")
	private String corpCode;
    
    public String toString() {
    	return JSON.toJSONString(this);
    }
    
	public String getBatchNo() {
		return batchNo;
	}
	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}
	public String getSourceFile() {
		return sourceFile;
	}
	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}
	public String getImportName() {
		return importName;
	}
	public void setImportName(String importName) {
		this.importName = importName;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Date getImportTime() {
		return importTime;
	}
	public void setImportTime(Date importTime) {
		this.importTime = importTime;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public int getTotalNum() {
		return totalNum;
	}
	public void setTotalNum(int totalNum) {
		this.totalNum = totalNum;
	}
	public int getSuccessNum() {
		return successNum;
	}
	public void setSuccessNum(int successNum) {
		this.successNum = successNum;
	}
	public int getFailNum() {
		return failNum;
	}
	public void setFailNum(int failNum) {
		this.failNum = failNum;
	}
	public String getFailRecord() {
		return failRecord;
	}
	public void setFailRecord(String failRecord) {
		this.failRecord += (failRecord + ", ");
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
