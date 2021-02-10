package com.openxsl.admin.organ.entity;

import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.openxsl.config.dal.jdbc.BaseEntity;

@Entity
@Table(name = "organ_position")
@SuppressWarnings("serial")
public class Position extends BaseEntity<Integer> {
//	@Column
//	private int id;
	@ApiModelProperty("岗位（分组）名称")
	@Column
	private String name;   //名称
	
	@ApiModelProperty("职责描述")
	@Column
	private String duty;   //职责
	
	@ApiModelProperty("状态：0-未生效；1-生效")
	@Column
	private Boolean status;
	
	@ApiModelProperty("备注：如'应急指挥'")
	@Column
	private String remark;
	
	@ApiModelProperty("组长（应急指挥）")
	@Column
	private String leader;  //组长（应急指挥）
	
	@ApiModelProperty("机构编码")
	@Column
	private String corpCode;  //机构编码
	
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
	public Boolean isStatus() {
		return status;
	}
	public void setStatus(Boolean status) {
		this.status = status;
	}
	public String getLeader() {
		return leader;
	}
	public void setLeader(String leader) {
		this.leader = leader;
	}
	public String getCorpCode() {
		return corpCode;
	}
	public void setCorpCode(String corpCode) {
		this.corpCode = corpCode;
	}

}
