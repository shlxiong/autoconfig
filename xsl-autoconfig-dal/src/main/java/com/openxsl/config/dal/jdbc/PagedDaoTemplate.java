package com.openxsl.config.dal.jdbc;

import com.openxsl.config.rpcmodel.Page;
import com.openxsl.config.rpcmodel.QueryMap;

/**
 * 分页查询
 * @author 001327-xiongsl
 * @param <T>
 */
public interface PagedDaoTemplate<T> extends DaoTemplate<T> {
	
	/**
	 * 分页查询
	 * @param wheres
	 * @param orders 排序字段(比如：id desc,name asc)
	 * @param pageNo 页码
	 * @param pageSize 每页记录数
	 * @param args 参数
	 * @param fields 返回字段，为空时默认为全部字段
	 */
	public Page<T> queryForPage(QueryMap<?> wheres, String orders,
								int pageNo, int pageSize, String... fields);
	
	/**
	 * 按sqlId查询
	 * @param sqlId
	 * @param pageNo 页码
	 * @param pageSize 每页记录数
	 * @param args 参数
	 */
	public Page<T> queryForPageByNamed(String sqlId, int pageNo, int pageSize, Object...args);
	
	/**
	 * 执行SQL查询
	 * @param sql
	 * @param pageNo 页码
	 * @param pageSize 每页记录数
	 * @param args 参数
	 */
	public Page<T> queryForPageBySql(String sql, int pageNo, int pageSize, Object... args);
	
	/**
	 * 返回查询分页的SQL
	 */
	public String getPagedSql(String sql, int pageNo, int pageSize);
	
	/**
	 * 生成分页语句
	 */
	public interface PageLimiter{
		
		/**
		 * 物理分页SQL语句
		 */
		public String getPagedSql(String sql, int pageNo, int pageSize);
		/**
		 * 数据库表是否存在
		 * @param tableName 表名
		 * @return SQL
		 */
		public String existTable(String tableName);
		
	}

}
