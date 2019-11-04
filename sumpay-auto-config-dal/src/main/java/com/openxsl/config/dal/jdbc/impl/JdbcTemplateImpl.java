package com.openxsl.config.dal.jdbc.impl;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import com.openxsl.config.autodetect.ScanConfig;
import com.openxsl.config.condition.ConditionalProperty;
import com.openxsl.config.dal.jdbc.BatchDaoTemplate;
import com.openxsl.config.dal.jdbc.QueryMap;
import com.openxsl.config.dal.jdbc.QueryMap.Orderby;
import com.openxsl.config.dal.jdbc.sqlparse.BaseSqlParser;
import com.openxsl.config.dal.jdbc.sqlparse.JdbcSqlParser;
import com.openxsl.config.rpcmodel.Page;
import com.openxsl.config.util.Patterns;

/**
 * Spring jdbcTemplate的实现
 * @author xiongsl
 *
 * @param <T>
 */
@ScanConfig
@Scope("prototype")
//@Conditional(OnPropertyCondition.class)
@ConditionalProperty(name="jdbc.persistence.api", havingValue="springJdbc")
@SuppressWarnings({"unchecked","rawtypes"})
public class JdbcTemplateImpl<T> extends BasePagedDaoImpl<T>
			implements BatchDaoTemplate<T>{
	private JdbcSqlParser parser;
	private RowMapper<T> rowMapper;
	
	@Resource
	private JdbcTemplate jdbcTemplate;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public JdbcTemplateImpl(){
		try{
			//适合于直接继承 本类的，不推荐这样使用
			ParameterizedType pt = (ParameterizedType)getClass().getGenericSuperclass();
			Class<T> entityClass = (Class)pt.getActualTypeArguments()[0];
			parser = new JdbcSqlParser(entityClass);
			rowMapper = (RowMapper<T>)parser.getRowMapper(); 
				//new BeanPropertyRowMapper<T>(entityClass);
		}catch(Exception e){
			//e.printStackTrace();
		}
	}
	public void setGenericParameterType(Class<T> entityClass){
		parser = new JdbcSqlParser(entityClass);
		rowMapper = (RowMapper<T>)parser.getRowMapper(); 
			//new BeanPropertyRowMapper<T>(entityClass);
	}
	
	@Override
	public T find(Serializable id, String... fields) {
		String sql = parser.getFindSql(fields);
		logger.debug("SQL: {}", sql);
		try{
			return jdbcTemplate.queryForObject(sql, rowMapper, id);
		}catch(EmptyResultDataAccessException dae){
			return null;
		}
	}
	
	@Override
	public T find(QueryMap<?> wheres, String orders, String... fields) {
		return jdbcTemplate.query( 
					parser.getQueryStatementCreator(wheres,orders,fields), 
					new ResultSetExtractor<T>(){
						@Override
						public T extractData(ResultSet rs) throws SQLException,
								DataAccessException {
							T object = null;
							if (rs.next()){
								object = rowMapper.mapRow(rs, 0);
								rs.close();
							}
							return object;
						}
				});
	}
	@Override
	public Map<String,Object> findMapBySql(String sql, Object... args){
		logger.debug("SQL: {}", sql);
		return jdbcTemplate.queryForMap(sql, args);
	}

	@Override
	public List<T> query(QueryMap<?> wheres, String... fields) {
		return jdbcTemplate.query(
					parser.getQueryStatementCreator(wheres,null,fields), rowMapper);
	}
	
	@Override
	public List<T> query(QueryMap<?> wheres, Orderby[] orders, String... fields){
		String orderStr = null;
		if (orders!=null && orders.length>0){
			StringBuilder orderBuf = new StringBuilder();
			for (Orderby order : orders){
				orderBuf.append(order.toString()).append(",");
			}
			orderStr = orderBuf.substring(0, orderBuf.length()-1);
		}
		return jdbcTemplate.query(
					parser.getQueryStatementCreator(wheres,orderStr,fields), rowMapper);
	}

	@Override
	public List<T> queryByNamed(String sqlId, Object... args) {
		String sql = parser.getNamedSql(sqlId);
		logger.debug("SQL: {}", sql);
		return jdbcTemplate.query(sql, rowMapper, args);
	}

	@Override
	public List<T> queryBySql(String sql, Object... args) {
		logger.debug("SQL: {}", sql);
		return jdbcTemplate.query(sql, rowMapper, args);
	}
	
	@Override
	public List<Map<String,Object>> queryMapBySql(String sql, Object... args){
		logger.debug("SQL: {}", sql);
		return jdbcTemplate.queryForList(sql, args);
	}
	
	@Override
	public boolean exists(String sql, Object... args) {
		return jdbcTemplate.queryForRowSet(sql).next();
	}

	@Override
	public Serializable insert(T entity) {
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(parser.getInsertStatementCreator(entity), keyHolder);
		return keyHolder.getKey();
	}

	@Override
	public int deleteById(Serializable id) {
		String sql = parser.getDeleteSql();
		logger.debug("SQL: {}", sql);
		return jdbcTemplate.update(sql, id);
	}
	@Override
	public int delete(QueryMap<?> wheres) {
		return jdbcTemplate.update( parser.getDeleteStatementCreator(wheres) );
	}

	@Override
	public int updateById(T model, Serializable... id) {
		Object key = (id.length > 0) ? id[0] : parser.getId(model);
		QueryMap<?> wheres = new QueryMap<Object>(parser.getIdField(), key);
		try{
			return this.update(model, wheres);
		}finally{
			wheres.clear();
		}
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
		return jdbcTemplate.update(
					parser.getUpdateStatementCreator(values,wheres));
	}

	@Override
	public int update(QueryMap<?> values, Serializable id) {
		QueryMap<Object> wheres = new QueryMap<Object>(parser.getIdField(), id);
		try{
			return jdbcTemplate.update(
						parser.getUpdateStatementCreator(values,wheres));
		}finally{
			wheres.clear();
		}
	}

	@Override
	public int executeSql(String sql, Object... args) {
		logger.debug("SQL: {}", sql);
		return jdbcTemplate.update(sql, args);
	}
	@Override
	public int executeByNamed(final String sqlId, final Object...args){
		String sql = parser.getNamedSql(sqlId);
		logger.debug("SQL: {}", sql);
		return jdbcTemplate.update(sql, args);
	}
	
	@Override
	public BaseSqlParser getSqlParser() {
		return parser;
	}
	
	//======================== 分页查询  =============================
	@Override
	public long getTotal(String sql, Object... args){
		if (sql==null || sql.trim().length()<1){
			sql = parser.getTableName();
		}
		return super.getTotal(sql, args);
	}
	
	@Override
	public Page<T> queryForPage(QueryMap<?> wheres, String orders,
								int pageNo, int pageSize, String... fields){
		Set<String> fieldOperSet = (wheres==null) ? null : wheres.keySet();
		String sql = parser.getQuerySql(fieldOperSet, orders, fields);
		return this.queryForPageBySql(sql, pageNo, pageSize, wheres.values().toArray());
	}
	
	@Override
	public Page<T> queryForPageByNamed(String sqlId, int pageNo, int pageSize,
									Object... args) {
		String sql = parser.getNamedSql(sqlId);
		return queryForPageBySql(sql, pageNo, pageSize, args);
	}

	//============================ 批量接口 ===========================
	@Override
	public boolean bulkInsert(final List<T> entities) {
		if (entities==null || entities.size()<1){
			return false;
		}
		String sql = parser.getInsertSql(entities.get(0), null);
		//1. PreparedStatement.executeBatch();
		jdbcTemplate.batchUpdate(sql, parser.getBatchInsertStatementSetter(entities));
		//2. 拼很长的SQL  values(?,?),(?,?),(?,?)
//		jdbcTemplate.update(sql, lstArgs.toArray());
		return true;
	}
	@Override
	public boolean batchUpdate(String sql, List<T> entities) {
		Matcher matcher = Patterns.SQL_UPDATE.matcher(sql);
		matcher.matches();
		List<String> columns = this.retrieveArguments(matcher.group(2), 1);
		columns.addAll( this.retrieveArguments(matcher.group(3),0) );
		jdbcTemplate.batchUpdate(sql, 
				parser.getBatchUpdateStatementSetter(columns, entities));
		return false;
	}
	private List<String> retrieveArguments(String sqlexprs, int whereOrSet){
		List<String> columns = new ArrayList<String>();
		String splitor = (whereOrSet == 0) ? "\\s+(AND|OR)\\s+" : ",";
		Matcher matcher;
		for (String expr : sqlexprs.split(splitor)){
			matcher = Patterns.SQL_EXPR.matcher(expr);
			if (matcher.matches() && matcher.group(3).equals("?")){
				columns.add(matcher.group(1));
			}
		}
		return columns;
	}
	
}
