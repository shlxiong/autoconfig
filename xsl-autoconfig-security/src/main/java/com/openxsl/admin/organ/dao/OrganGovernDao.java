package com.openxsl.admin.organ.dao;

import java.util.List;

import com.openxsl.admin.organ.entity.joint.OrganGovern;
import com.openxsl.config.dal.jdbc.BaseMapper;

public interface OrganGovernDao extends BaseMapper<OrganGovern> {
	
	List<String> getScenicCodes(String governCode);
	
	String getGovernCode(String scenicCode);

}