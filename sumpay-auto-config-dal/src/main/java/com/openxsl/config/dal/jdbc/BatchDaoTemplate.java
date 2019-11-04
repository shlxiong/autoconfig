package com.openxsl.config.dal.jdbc;

import java.util.List;

public interface BatchDaoTemplate<T> extends DaoTemplate<T>{
	
	public boolean bulkInsert(final List<T> entities);
	
	public boolean batchUpdate(String sql, final List<T> entities);

}
