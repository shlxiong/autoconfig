package com.openxsl.admin.organ.entity.joint;

import javax.persistence.Column;
import javax.persistence.Table;

import com.openxsl.config.dal.jdbc.BaseEntity;

/**
 * 政府与企业管控表
 * 
 * @author shuilin.xiong
 */
@SuppressWarnings("serial")
@Table(name = "organ_govern_scenic")
public class OrganGovern extends BaseEntity<Integer>{
	@Column
	private String govern_code;
	@Column
	private String scenic_code;
	
	public String getGovern_code() {
		return govern_code;
	}
	public void setGovern_code(String govern_code) {
		this.govern_code = govern_code;
	}
	public String getScenic_code() {
		return scenic_code;
	}
	public void setScenic_code(String scenic_code) {
		this.scenic_code = scenic_code;
	}

}
