package com.openxsl.config.filedata.export.dao;

import com.openxsl.config.dal.jdbc.BaseMapper;
import com.openxsl.config.filedata.export.entity.ImportLog;

public interface ImportLogDao extends BaseMapper<ImportLog>{
	
//	public int insert(ImportLog importLog);
	
	public int update(ImportLog importLog);

}
