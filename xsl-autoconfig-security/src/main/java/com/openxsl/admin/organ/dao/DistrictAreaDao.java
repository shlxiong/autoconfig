package com.openxsl.admin.organ.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.openxsl.admin.organ.entity.DistrictArea;
import com.openxsl.config.dal.jdbc.BaseMapper;

public interface DistrictAreaDao extends BaseMapper<DistrictArea> {
	
	List<DistrictArea> queryProvinces();
	
	List<DistrictArea> querySubAreas(String parentCode);
	
	DistrictArea getByCode(String code);
	
	DistrictArea findByShortName(@Param("shortName")String shortName, @Param("level")int level);
	
	List<DistrictArea> queryByLevel(@Param("level")int level);

}
