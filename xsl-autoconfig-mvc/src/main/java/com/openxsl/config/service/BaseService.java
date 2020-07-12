package com.openxsl.config.service;

import java.io.Serializable;

import com.openxsl.config.dal.jdbc.Entity;

public interface BaseService<T extends Entity<PK>, PK extends Serializable> {
	
	public T get(PK pk);
	
	public int insert(T entity);
	
	public int update(T entity);
	
	@SuppressWarnings("unchecked")
	public int delete(PK... ids);

}
