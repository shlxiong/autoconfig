package com.openxsl.admin.dao;

import com.openxsl.admin.entity.OperationLog;
import com.openxsl.config.dal.jdbc.BaseMapper;

public interface OperationLogDao extends BaseMapper<OperationLog>{
	
	public int insert(OperationLog log);
	
//	public Page<OperationLog> queryPage(QueryMap<String> params);

}
