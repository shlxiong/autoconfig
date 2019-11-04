package com.openxsl.config.dal.jdbc.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.condition.ConditionalProperty;
import com.openxsl.config.dal.jdbc.QueryMap;
import com.openxsl.config.dal.jdbc.QueryMap.Orderby;
import com.openxsl.config.dal.jdbc.sqlparse.BaseSqlParser;
import com.openxsl.config.rpcmodel.Page;

/**
 * Hibernate实现
 * @author xiongsl
 *
 * @param <T>
 */
@ScanConfig
//@Conditional(OnPropertyCondition.class)
@ConditionalProperty(name="jdbc.persistence.api", havingValue="hibernate")
public class HibernateDaoImpl<T> extends BasePagedDaoImpl<T> {

	@Override
	public void setGenericParameterType(Class<T> entityClass) {
		// TODO Auto-generated method stub

	}

	@Override
	public T find(Serializable id, String... fields) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T find(QueryMap<?> wheres, String orders, String... fields) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> findMapBySql(String sql, Object... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> query(QueryMap<?> wheres, String... fields) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> query(QueryMap<?> wheres, Orderby[] orders, String... fields) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> queryByNamed(String sqlId, Object... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> queryBySql(String sql, Object... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> queryMapBySql(String sql, Object... args) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean exists(String sql, Object... args) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Serializable insert(T entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int deleteById(Serializable id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(QueryMap<?> wheres) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateById(T model, Serializable... id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(T model, QueryMap<?> wheres) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(QueryMap<?> values, QueryMap<?> wheres) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(QueryMap<?> values, Serializable id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int executeSql(String sql, Object... args) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int executeByNamed(String sqlId, Object... args) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public BaseSqlParser getSqlParser() {
		// TODO Auto-generated method stub
		return null;
	}

	//=========================== 分页接口 =============================
	@Override
	public Page<T> queryForPage(QueryMap<?> wheres, String orders, int pageNo,
								int pageSize, String... fields) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<T> queryForPageByNamed(String sqlId, int pageNo, int pageSize,
								Object... args) {
		// TODO Auto-generated method stub
		return null;
	}

}
