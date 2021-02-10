package com.openxsl.admin.organ.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.openxsl.admin.organ.dao.OrganGovernDao;
import com.openxsl.admin.organ.entity.joint.OrganGovern;
import com.openxsl.config.dal.jdbc.BaseService;

@Service
public class OrganGovernService extends BaseService<OrganGovernDao, OrganGovern, Integer> {
	
	public List<String> getScenicCodes(String governCode) {
		return mapper.getScenicCodes(governCode);
	}
	
	public String getGovernCode(String scenicCode) {
		return mapper.getGovernCode(scenicCode);
	}

}
