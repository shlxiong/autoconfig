package com.openxsl.config.dal.jdbc.sqlparse;

import java.beans.PropertyDescriptor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.openxsl.config.rpcmodel.QueryMap;

/**
 * spring JdbcTemplate
 *  
 * @author 001327-xiongsl
 */
public class JdbcSqlParser extends BaseSqlParser{
	private RowMapper<?> rowMapper;
	
	public JdbcSqlParser(Class<?> entityClass){
		super(entityClass);
		
		this.initRowMapper(entityClass);
	}
	
	@Override
	public String getNamedSql(String sqlId){
		String sql = namedSqls.get(sqlId);
		Matcher matcher = P_NAMED_PARAM.matcher(sql);
		while (matcher.find()){
			sql = sql.replace(matcher.group(1), "?");
		}
		return sql;
	}
	
	public PreparedStatementCreator getInsertStatementCreator(Object entity){
		List<Object> lstArgs = new ArrayList<Object>();
		String sql = this.getInsertSql(entity, lstArgs);
		return this.prepareStatementCreator(sql, lstArgs, id);
	}
	public String getInsertSql(Object entity, final List<Object> lstArgs){
		Assert.notNull(entity, "'INSERT' entity can not be null");
		
		StringBuilder fields = new StringBuilder();
		StringBuilder values = new StringBuilder();
		for (PropertyDescriptor desc : descripts.values()){
			try {
				Object value = desc.getReadMethod().invoke(entity);
				if (value != null){
					if (lstArgs != null){
						lstArgs.add(value);
					}
					fields.append(SP_FIELD).append(fieldColumns.get(desc.getName()));
					values.append(SP_FIELD).append("?");
				}
			} catch (Exception e) {
				logger.warn("", e);
			}
		}
		
		return String.format(insertSql, fields.substring(1), values.substring(1));
	}
	
	public PreparedStatementCreator getDeleteStatementCreator(Map<String,?> wheresMap){
		Assert.notEmpty(wheresMap, "'DELETE-WHERE' cant not be null");  //不能无条件删除
		
		StringBuilder buffer = new StringBuilder("DELETE FROM ").append(tableName)
					.append(" WHERE ");
		final List<Object> lstArgs = new ArrayList<Object>();
		String sql = buffer.append(this.expr2Sql(wheresMap, lstArgs)).toString();
		return this.prepareStatementCreator(sql, lstArgs);
	}
	
	public PreparedStatementCreator getUpdateStatementCreator(Map<String,?> setsMap,
						Map<String,?> wheresMap){
		Assert.notEmpty(setsMap, "'UPDATE-SET' cant not be null");
		Assert.notEmpty(wheresMap, "'UPDATE-WHERE' cant not be null");  //不能无条件修改
		
		final List<Object> lstArgs = new ArrayList<Object>();
		StringBuilder values = new StringBuilder();
		for (Map.Entry<String,?> entry : setsMap.entrySet()){
			String field = entry.getKey();
			String strValue = String.valueOf(entry.getValue());
			values.append(fieldColumns.get(field));
			if (strValue.startsWith("$this.")) { //复制其他字段
				values.append(strValue.substring("$this.".length()));
			} else {
				values.append(" = ?");
				lstArgs.add(entry.getValue());
			}
			values.append(SP_FIELD);
		}
		String valueSql = values.substring(0, values.lastIndexOf(SP_FIELD));
		String wheres = this.expr2Sql(wheresMap, lstArgs);
		String sql = String.format(updateSql, valueSql, wheres);
		return this.prepareStatementCreator(sql, lstArgs);
	}
	
	public PreparedStatementCreator getQueryStatementCreator(
					QueryMap<?> wheresMap, String orders, String... fields){
		if (wheresMap == null){
			wheresMap = new QueryMap<Object>(0);
		}
		String sql = this.getQuerySql(wheresMap.keySet(), orders, fields);
		return this.prepareStatementCreator(sql, wheresMap.values());
	}
	
	public BatchPreparedStatementSetter getBatchInsertStatementSetter(
						final List<?> entities){
		return new BatchPreparedStatementSetter(){
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Object entity = entities.get(i);
				int col = 1;
				for (PropertyDescriptor desc : descripts.values()){
					try {
						Object value = desc.getReadMethod().invoke(entity);
						ps.setObject(col++, value);
					} catch (Exception e) {
						logger.warn("", e);
					}
				}
			}
			@Override
			//batchSize必须等于entities.size()，若小于会漏掉后面的，若大于会报错
			public int getBatchSize() {  
				return entities.size();
			}
		};
	}
	public BatchPreparedStatementSetter getBatchUpdateStatementSetter(
						final List<String> columns, final List<?> entities){
		return new BatchPreparedStatementSetter(){
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Object entity = entities.get(i);
				int col = 1;
				for (PropertyDescriptor desc : descripts.values()){
					try {
						Object value = desc.getReadMethod().invoke(entity);
						ps.setObject(col++, value);
					} catch (Exception e) {
						logger.warn("", e);
					}
				}
			}
			@Override
			//batchSize必须等于entities.size()，若小于会漏掉后面的，若大于会报错
			public int getBatchSize() {  
				return entities.size();
			}
		};
	}
	
	public RowMapper<?> getRowMapper(){
		return rowMapper;
	}
	
	private final PreparedStatementCreator prepareStatementCreator(
				final String sql, final Collection<?> lstArgs, final String...id){
		logger.debug("SQL: {}", sql);
		logger.debug("args: {}", lstArgs);
		return new PreparedStatementCreator(){
			@Override
			public PreparedStatement createPreparedStatement(Connection conn)
						throws SQLException {
				PreparedStatement preStmt;
				if (id.length > 0){
					preStmt = conn.prepareStatement(sql, new String[]{id[0]});
				}else{
					preStmt = conn.prepareStatement(sql, id);
				}
				int i = 1;
                for (Object arg : lstArgs) {
                	preStmt.setObject(i++, arg);
                }
                lstArgs.clear();
                return preStmt;
			}
		};
	}
	
	private <T> void initRowMapper(final Class<T> entityClass){
		this.rowMapper = new RowMapper<T>(){
			@Override
			public T mapRow(ResultSet rs, int rowNum) throws SQLException {
				T mappedObject = BeanUtils.instantiate(entityClass);
				BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(mappedObject);

				ResultSetMetaData rsmd = rs.getMetaData();
				final int columnCount = rsmd.getColumnCount();
				
				String column, field, errorMsg;
				for (int index = 1; index <= columnCount; index++) {
					column = JdbcUtils.lookupColumnName(rsmd, index);
					field = columnFields.get(column);
					if (field == null){ //JPA对象属性可能少于数据库字段，以JPA为主
						if (fieldFuncs.containsKey(column)){
							field = column;   //就是字段名
						}else{
							logger.warn(String.format("ignore Table column '%s'", column));
							continue;
						}
					}
					errorMsg = String.format("Unable to map column '%s' to property '%s'",
											column, field);
					PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(entityClass, field);
					if (pd == null){
						throw new DataRetrievalFailureException(errorMsg+", no writable method");
					}
					try {
						Object value = JdbcUtils.getResultSetValue(rs, index, pd.getPropertyType());
						if (value != null){
							bw.setPropertyValue(field, value);
						}
					}catch (NotWritablePropertyException ex) {
						throw new DataRetrievalFailureException(errorMsg, ex);
					}
				}

				return mappedObject;
			}
		};
	}
	
	@Override
	protected String field2Expr(final String fieldOper){
		String[] parts = super.extractExpr(fieldOper);
		String operator = parts[1];
		String column = parts[2];
		
		StringBuilder buffer = new StringBuilder(column);
		if ("BETWEEN".equals(operator.trim())){
			buffer.append(" (BETWEEN ? and ?)");
		}else if (operator.endsWith("IN")){ //in or not_in
			buffer.append(String.format(" %s (${IN})", operator));
		}else{ //包括：is [not]，like， =，<> 等
			buffer.append(String.format(" %s ?", operator));
	    }
		return buffer.toString();
	}
	
	@SuppressWarnings("unchecked")
	private final String expr2Sql(Map<String,?> wheresMap, List<Object> lstArgs){
		StringBuilder whereSql = new StringBuilder();
		for (Map.Entry<String,?> entry : wheresMap.entrySet()){
			String entrySql = this.field2Expr(entry.getKey());
			boolean between = entry.getKey().endsWith(" BETWEEN");
			if (between || entry.getKey().endsWith(" IN")){
				Class<?> clazz = entry.getValue().getClass();
				int size = lstArgs.size();
				if (clazz.isArray()){
					lstArgs.addAll(CollectionUtils.arrayToList(entry.getValue()));
				}else if (List.class.isAssignableFrom(clazz)){
					lstArgs.addAll((List<?>)entry.getValue());
				}
				size = lstArgs.size() - size;
				if (between){
					if (size != 2) {
						throw new IllegalArgumentException("Between-sql must have 2 arguments");
					}
				}else{  //in
					if (size < 1){
						throw new IllegalArgumentException("In-sql must have more than 1 arguments");
					}else{
						StringBuilder tmp = new StringBuilder();
						for (int i=0; i<size; i++){  //entrySql='field in (${IN})'
							tmp.append("?").append(SP_FIELD);
						}
						entrySql = entrySql.replace("${IN}", tmp.substring(0, tmp.length()-1));
					}
				}
			}else{
				lstArgs.add(entry.getValue());
			}
			whereSql.append(entrySql).append(" AND ");
		}
		if (whereSql.length() > 0){
			return whereSql.substring(0, whereSql.length()-5);
		}else{
			return "";
		}
	}
	
}
