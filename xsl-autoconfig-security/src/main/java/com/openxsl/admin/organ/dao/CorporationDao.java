package com.openxsl.admin.organ.dao;

import java.util.List;

import com.openxsl.admin.organ.entity.Corporation;
import com.openxsl.config.dal.jdbc.BaseMapper;

public interface CorporationDao extends BaseMapper<Corporation> {
	
//	public Corporation findById(int corpId);
	
	public List<Corporation> findSubCorps(int corpId);
	
	public List<Corporation> queryTopCorps();

}
