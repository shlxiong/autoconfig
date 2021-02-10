package com.openxsl.config.dal.jdbc;

import java.util.List;
import java.util.Map;

import tk.mybatis.mapper.common.Mapper;

import com.openxsl.config.rpcmodel.QueryMap;

public interface BaseMapper<E> extends Mapper<E> {
	
	public List<E> queryForPage(QueryMap<?> params);
	
	public List<Map<String,String>> getKeyValue(QueryMap<?> params);
	
}
