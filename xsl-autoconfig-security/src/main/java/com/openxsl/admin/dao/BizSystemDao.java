package com.openxsl.admin.dao;

import java.util.List;

import com.openxsl.admin.entity.BizSystem;
import com.openxsl.config.rpcmodel.QueryMap;

public interface BizSystemDao {
	
	public List<BizSystem> queryAll(QueryMap<?> params);
	
	public BizSystem find(int id);
	
	public int delete(List<Integer> ids);

}
