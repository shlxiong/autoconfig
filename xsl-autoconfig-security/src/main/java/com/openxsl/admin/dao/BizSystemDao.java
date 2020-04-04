package com.openxsl.admin.dao;

import java.util.List;

import com.openxsl.admin.entity.BizSystem;

public interface BizSystemDao {
	
	public List<BizSystem> queryAll(String visible);
	
	public BizSystem find(int id);

}
