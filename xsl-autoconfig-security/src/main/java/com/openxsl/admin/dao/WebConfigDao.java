package com.openxsl.admin.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.openxsl.admin.entity.WebConfig;
import com.openxsl.config.dal.jdbc.BaseMapper;

public interface WebConfigDao extends BaseMapper<WebConfig> {
	
	public List<WebConfig> getAll(String corpCode);
	
	public WebConfig get(@Param("webType")String webType, @Param("corpCode")String corpCode);

}
