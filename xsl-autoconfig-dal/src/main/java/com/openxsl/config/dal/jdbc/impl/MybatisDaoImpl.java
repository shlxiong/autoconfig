package com.openxsl.config.dal.jdbc.impl;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.condition.ConditionalProperty;
import com.openxsl.config.dal.jdbc.sqlparse.BaseSqlParser;
import com.openxsl.config.dal.jdbc.sqlparse.MybatisSqlParser;
import com.openxsl.config.dal.jdbc.sqlparse.SqlParser;
import com.openxsl.config.rpcmodel.Page;
import com.openxsl.config.rpcmodel.QueryMap;
import com.openxsl.config.rpcmodel.QueryMap.Orderby;

/**
 * 因Mybatis限制，方法中的变长参数只能传一个Model或Map，sql都是namedSql
 * 
 * @author 001327
 * @param <T>
 */
@ScanConfig
@Scope("prototype")
@ConditionalProperty(name="jdbc.persistence.api", havingValue="mybatis")
@SuppressWarnings({"unchecked","rawtypes"})
public class MybatisDaoImpl<T> extends BasePagedDaoImpl<T>{
	private MybatisSqlParser parser;
	@Autowired
	private SqlSession sqlSession;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public MybatisDaoImpl(){
		try{
			//适合于直接继承 本类的，不推荐这样使用
			ParameterizedType pt = (ParameterizedType)getClass().getGenericSuperclass();
			Class<T> entityClass = (Class<T>)pt.getActualTypeArguments()[0];
			this.setGenericParameterType(entityClass);
		}catch(Exception e){
			//
		}
	}
	@Override
	public void setGenericParameterType(Class<T> entityClass) {
		parser = new MybatisSqlParser(entityClass);
		parser.setConfiguration(sqlSession.getConfiguration());
		sqlSession.getConfiguration().setUseGeneratedKeys(true);
	}

	@Override
	public T find(Serializable id, String... fields) {
		logger.debug("SQL: {}", parser.getFindSql());
		return sqlSession.selectOne(parser.getFindStatement(), id);
	}

	@Override
	public T find(QueryMap<?> wheres, String orders, String... fields) {
		Set<String> keySet = (wheres==null) ? null : wheres.keySet();
		String sql = parser.getQuerySql(keySet, orders);
		String sqlId = "findBySql";
		sqlId = parser.addQueryStatement(sqlId, sql, sqlSession.getConfiguration());
		return sqlSession.selectOne(sqlId, this.rewriteQueryMap(wheres));
	}

	@Override
	public Map<String, Object> findMapBySql(String sql, Object... args) {
		String sqlId = "findMapBySql";
		sqlId = parser.addQueryStatement(sqlId, sql, sqlSession.getConfiguration());
		return sqlSession.selectOne(sqlId, args.length>0?args[0]:null);
	}

	@Override
	public List<T> query(QueryMap<?> wheres, String... fields) {
		Orderby[] orders = null;
		return this.query(wheres, orders, fields);
	}

	@Override
	public List<T> query(QueryMap<?> wheres, Orderby[] orders, String... fields) {
		String orderStr = null;
		if (orders != null){
			StringBuilder buffer = new StringBuilder();
			for (Orderby order : orders){
				buffer.append(order.toString()).append(SqlParser.SP_FIELD);
			}
			orderStr = buffer.substring(0, buffer.lastIndexOf(SqlParser.SP_FIELD));
		}
		Set<String> fieldOperSet = (wheres != null) ? wheres.keySet() : null;
		String sql = parser.getQuerySql(fieldOperSet, orderStr, fields);
		String sqlId = "findBySql";
		sqlId = parser.addQueryStatement(sqlId, sql, sqlSession.getConfiguration());
//		if (wheres != null) {   //去掉分页参数
//			fieldOperSet = wheres.keySet();
//			for (Map.Entry<String, ?> entry : wheres.entrySet()) {
//				if (entry.getValue()!=null && entry.getValue() instanceof Page) {
//					fieldOperSet.remove(entry.getKey());
//					break;
//				}
//			}
//		}
		return sqlSession.selectList(sqlId, this.rewriteQueryMap(wheres));
	}
	
	@Override
	public final List<T> queryByNamed(String sqlId, Object... args) {
		if (args.length > 0){
			return sqlSession.selectList(sqlId, args[0]);
		}else{
			return sqlSession.selectList(sqlId);
		}
	}

	@Override
	public List<T> queryBySql(String sql, Object... args) {
		String sqlId = "queryBySql";
		sqlId = parser.addQueryStatement(sqlId, sql, sqlSession.getConfiguration());
		return this.queryByNamed(sqlId, args);
	}

	@Override
	public List<Map<String, Object>> queryMapBySql(String sql, Object... args) {
		String sqlId = "queryMapBySql";
		sqlId = parser.addQueryStatement(sqlId, sql, sqlSession.getConfiguration());
		return (List)this.queryByNamed(sqlId, args);
	}
	
	@Override
	public boolean exists(String sql, Object... args) {
		return this.getTotal(sql, args) > 0;
	}

	@Override
	public Serializable insert(T entity) {
		//mybatis不方便，需要<selectKey keyProperty="id" resultType="_long" order="BEFORE">
		//在外面设置entity的Id
		sqlSession.insert(parser.getInsertStatement(), entity);
		return (Serializable)parser.getId(entity);
	}

	@Override
	public int deleteById(Serializable id) {
		logger.debug("SQL: {}", parser.getDeleteSql());
		return sqlSession.delete(parser.getDeleteStatement(), id);
	}

	@Override
	public int delete(QueryMap<?> wheres) {
		String sqlId = "delete";
		String sql = parser.getNamedDeleteSql(wheres);
		sqlId = parser.addQueryStatement(sqlId, sql, sqlSession.getConfiguration());
		return sqlSession.delete(sqlId, wheres);
	}

	@Override
	public int updateById(T model, Serializable... id) {
		Object key = (id.length > 0) ? id[0] : parser.getId(model);
		QueryMap<?> wheres = new QueryMap<Object>(parser.getIdField(), key);
		return this.update(parser.getPropertyMap(model), wheres);
	}

	@Override
	public int update(T model, QueryMap<?> wheres) {
		QueryMap<?> values = parser.getPropertyMap(model);
		try{
			return this.update(values, wheres);
		}finally{
			values.clear();
		}
	}

	@Override
	public int update(QueryMap<?> values, QueryMap<?> wheres) {
		values.remove(parser.getIdField());   //id不能改
		String sqlId = "update";
		String sql = parser.getNamedUpdateSql(values, wheres);
		sqlId = parser.addQueryStatement(sqlId, sql, sqlSession.getConfiguration());
		
		QueryMap<Object> paramMap = new QueryMap<Object>();
		paramMap.putAll(values);
		paramMap.putAll(wheres);
		return sqlSession.update(sqlId, paramMap); 
	}

	@Override
	public int update(QueryMap<?> values, Serializable id) {
		QueryMap<?> wheres = new QueryMap<Object>(parser.getIdField(), id);
		return this.update(values, wheres);
	}

	@Override
	public int executeSql(String sql, Object... args) {
		String sqlId = parser.addQueryStatement("execute", sql, sqlSession.getConfiguration());
		return executeByNamed(sqlId, args);
	}

	@Override
	public int executeByNamed(String sqlId, Object... args) {
		String sqlType = parser.getSqlType(sqlId, sqlSession.getConfiguration());
		Object parameter = args.length>0 ? args[0] : null;
		if ("DELETE".equals(sqlType)){
			return sqlSession.delete(sqlId, parameter);
		}else if ("UPDATE".equals(sqlType)){
			return sqlSession.update(sqlId, parameter);
		}else if ("INSERT".equals(sqlType)){
			return sqlSession.insert(sqlId, parameter);
		}else{
			throw new IllegalArgumentException("只支持CDU操作");
		}
	}
	
	private QueryMap<?> rewriteQueryMap(QueryMap<?> wheres) {
		if (wheres == null) {
			return null;
		}
		QueryMap<?> wheres2 = new QueryMap<>(wheres);
		parser.rewriteQueryMap(wheres2);
		return wheres2;
	}
	
	//======================== 分页查询  =============================

	@Override
	public Page<T> queryForPage(QueryMap<?> wheres, String orders, int pageNo,
								int pageSize, String... fields) {
		Set<String> fieldOperSet = (wheres==null) ? null : wheres.keySet();
		String sql = parser.getQuerySql(fieldOperSet, orders, fields);
		return super.queryForPageBySql(sql, pageNo, pageSize, this.rewriteQueryMap(wheres));
	}

	@Override
	public Page<T> queryForPageByNamed(String sqlId, int pageNo, int pageSize,
								Object... args) {
		//逻辑分页：sqlSession.selectList(sqlId, args, new RowBounds(pageNo, pageSize));
		String sql = sqlSession.getConfiguration().getMappedStatement(sqlId)
						.getBoundSql(args).getSql();
		return super.queryForPageBySql(sql, pageNo, pageSize, args);
	}
	
	@Override
	public BaseSqlParser getSqlParser() {
		return parser;
	}

}
