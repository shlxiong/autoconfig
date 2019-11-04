package com.openxsl.config.dal.jdbc;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.openxsl.config.dal.jdbc.QueryMap.Orderby;
import com.openxsl.config.dal.jdbc.sqlparse.BaseSqlParser;

public interface DaoTemplate<T> {
	
	public void setGenericParameterType(Class<T> entityClass);
	
	/**
	 * 根据主键获取一条记录
	 * @param id
	 * @param fields 返回字段
	 * @return
	 */
	public T find(Serializable id, String... fields);
	
	/**
	 * 按条件查询一条记录
	 * @param wheres  查询条件（Map.key可以包含字段和操作符）
	 * @param 排序字段
	 * @param fields 返回字段
	 * @return
	 */
	public T find(QueryMap<?> wheres, String orders, String... fields);
	
	/**
	 * 执行SQL查询，返回一条记录的Map
	 * @param sql sql语句
	 * @param args 参数
	 */
	public Map<String,Object> findMapBySql(String sql, Object... args);
	
	/**
	 * 根据条件查询
	 * @param wheres 查询条件（Map.key可以包含字段和操作符）
	 * @param fields 返回字段
	 * @return
	 */
	public List<T> query(QueryMap<?> wheres, String... fields);
	
	/**
	 * 条件查询并排序
	 * @param wheres
	 * @param orders
	 * @param fields
	 * @return
	 */
	public List<T> query(QueryMap<?> wheres, Orderby[] orders, String... fields);
	
	/**
	 * 查询预定义SQL语句
	 * @param sqlId
	 * @param args
	 * @return
	 */
	public List<T> queryByNamed(String sqlId, Object...args);
	
	/**
	 * 执行SQL查询
	 * @param sql
	 * @param args
	 * @return
	 */
	public List<T> queryBySql(String sql, Object... args);
	
	/**
	 * 执行SQL查询，返回字段名的Map列表
	 * @param sql
	 * @param args
	 * @return
	 */
	public List<Map<String,Object>> queryMapBySql(String sql, Object... args);
	
	/**
	 * 判断SQL的执行结果是否为空记录
	 * @param sql
	 * @param args
	 * @return
	 */
	public boolean exists(String sql, Object... args);
	
	/**
	 * 插入一条数据，返回主键
	 * @param model
	 * @return
	 */
	public Serializable insert(T entity);
	
	/**
	 * 根据主键删除，返回行数（1或0）
	 * @param id
	 * @return
	 */
	public int deleteById(Serializable id);
	
	/**
	 * 删除符合条件的记录
	 * @param wheres 条件
	 * @return 记录数
	 */
	public int delete(QueryMap<?> wheres);
	
	/**
	 * 修改一行，返回行数（1或0）
	 * @param model
	 * @param id 主键，可以不传，此时从第一个参数model中自动取出主键值
	 * @return
	 */
	public int updateById(T model, Serializable... id);
	
	/**
	 * 修改符合条件的记录
	 * @param wheres 修改条件
	 * @param values 修改值
	 * @return 修改记录数
	 */
	public int update(T model, QueryMap<?> wheres);
	
	/**
	 * 修改符合条件的记录
	 * @param values 修改值，也是有顺序的
	 * @param wheres 修改条件
	 */
	public int update(QueryMap<?> values, QueryMap<?> wheres);
	
	/**
	 * 修改指定行的指定属性值，返回行数（1或0）
	 * @param values 属性
	 * @param id 主键
	 */
	public int update(QueryMap<?> values, Serializable id);
	
	/**
	 * 执行SQL语句，返回行数（1或0）
	 * @param sql  SQL语句
	 * @param args  参数
	 */
	public int executeSql(final String sql, final Object...args);
	
	/**
	 * 自动创建数据库表及索引
	 */
	public void createIfNotExist();
	
	/**
	 * 执行自定义语句，返回行数（1或0）
	 * @param sqlId  自定义sql ID
	 * @param args  参数
	 */
	public int executeByNamed(final String sqlId, final Object...args);
	
	/**
	 * 总共有多少条记录，如果sql为空，则count全表
	 * @param sql 指定SQL
	 */
	public long getTotal(String sql, Object...args);
	
	/**
	 * 按照SQL查询，返回指定类型的数据
	 * @param sql SQL语句
	 * @param resultType 结果类
	 * @param args 参数
	 */
	public <R> List<R> queryBySql(String sql, Class<R> resultType, Object... args);
	
	public BaseSqlParser getSqlParser();
	
}
