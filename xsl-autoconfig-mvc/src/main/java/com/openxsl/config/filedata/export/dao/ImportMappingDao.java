package com.openxsl.config.filedata.export.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.openxsl.config.dal.jdbc.BaseMapper;
import com.openxsl.config.filedata.export.entity.ImportMapping;

public interface ImportMappingDao extends BaseMapper<ImportMapping> {
	
	public List<ImportMapping> getMappings(@Param("name") String name,
						@Param("scenicCode") String scenicCode);
	
	public int deleteByConfigName(@Param("configName") String configName,
						@Param("scenicCode") String scenicCode);

}
