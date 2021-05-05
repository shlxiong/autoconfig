package com.openxsl.config.filedata.export.dao;

import org.apache.ibatis.annotations.Param;

import com.openxsl.config.dal.jdbc.BaseMapper;
import com.openxsl.config.filedata.export.entity.ImportConfig;

public interface ImportConfigDao extends BaseMapper<ImportConfig>{
	
//	public int insert(ImportConfig config);
//	
//	public int update(ImportConfig config);
	
	public ImportConfig getImportConfig(@Param("importName")String importName,
									@Param("scenicCode") String scenicCode);

}