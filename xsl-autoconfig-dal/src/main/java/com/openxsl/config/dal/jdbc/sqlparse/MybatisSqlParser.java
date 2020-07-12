package com.openxsl.config.dal.jdbc.sqlparse;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.springframework.util.Assert;

import com.openxsl.config.dal.jdbc.anno.JpaResultMap;
import com.openxsl.config.dal.jdbc.anno.JpaResultMaps;
import com.openxsl.config.dal.jdbc.impl.FunctionalField.FunctColumn;
import com.openxsl.config.rpcmodel.QueryMap;
import com.openxsl.config.util.HexEncoder;
import com.openxsl.config.util.Patterns;
import com.openxsl.config.util.bean.GenericArray;

/**
 * Mybatis SQL生成器
 * 
 * 6、自定义语句@NamedQuery中sql表名可使用类名、字段名请使用column
 * 
 * @author xiongsl
 */
public class MybatisSqlParser extends BaseSqlParser {
	private final String namespace;
	private final String findSqlId, deleteSqlId, insertSqlId;
	private final String UNAMED_RESULT_MAP = "java.util.HashMap";

	public MybatisSqlParser(Class<?> entityClass) {
		super(entityClass);
		namespace = entityClass.getName();
		findSqlId = namespace + ".findById";
		deleteSqlId = namespace + ".deleteById";
		insertSqlId = namespace + ".insert";
	}
	public void setConfiguration(Configuration config){
		this.buildDefaultParameterMap(entityClass, config);
		this.buildDefaultResultMap(entityClass, config);
		
		this.buildJpaResultMap(entityClass, config);
		this.buildJpaStatements(entityClass, config);  //NamedQuery
		//CRUD-basic
		String namedId = this.getNamedArg(getIdField());
		String sql = super.getFindSql().replace("?", namedId);
		buildMappedStatement(findSqlId, sql, config);
		sql = super.getDeleteSql().replace("?", namedId);
		buildMappedStatement(deleteSqlId, sql, config);
		sql = this.getNamedInsertSql();
		buildMappedStatement(insertSqlId, sql, config);
	}
	
	private String getNamedInsertSql(){
		StringBuilder fields = new StringBuilder();
		StringBuilder values = new StringBuilder();
		for (PropertyDescriptor desc : descripts.values()){
			fields.append(SP_FIELD).append(fieldColumns.get(desc.getName()));
			values.append(SP_FIELD).append(this.getNamedArg(desc.getName()));
		}
		int size = SP_FIELD.length();
		return String.format(insertSql, fields.substring(size), values.substring(size));
	}
	
	public String getNamedDeleteSql(QueryMap<?> wheres){
		return new StringBuilder("DELETE FROM ").append(tableName).append(" WHERE ")
					.append(this.getWhereSql(wheres)).toString();
	}
	public String getNamedUpdateSql(QueryMap<?> setsMap, QueryMap<?> wheresMap){
		Assert.notEmpty(setsMap, "Set字段为空");
		Assert.notEmpty(wheresMap, "查询条件为空");  //不能无条件修改
		
		QueryMap<Object> tempMap = new QueryMap<Object>();
		tempMap.putAll(setsMap);
		StringBuilder values = new StringBuilder();
		for (Map.Entry<String,?> entry : tempMap.entrySet()){
			String field = entry.getKey();
			String strValue = String.valueOf(entry.getValue());
			values.append(fieldColumns.get(field)).append(" = ");
			if (strValue.startsWith("$this.")) { //复制其他字段
				values.append(strValue.substring("$this.".length()));
				setsMap.remove(field);
			} else {
				values.append(this.getNamedArg(field));
			}
			values.append(SP_FIELD);
		}
		String valueSql = values.substring(0, values.lastIndexOf(SP_FIELD));
		return String.format(updateSql, valueSql, this.getWhereSql(wheresMap));
	}
	
	public String getFindStatement(){
		return findSqlId;
	}
	public String getDeleteStatement(){
		return deleteSqlId;
	}
	public String getInsertStatement(){
		return insertSqlId;
	}
	public String getNamespace(){
		return namespace;
	}
	
	@Override
	public String getWhereSql(QueryMap<?> wheres){
		if (wheres==null || wheres.size()<1) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		for (String fieldOper : wheres.keySet()){
			buffer.append(this.field2Expr(fieldOper)).append(" AND ");
		}
		this.rewriteQueryMap(wheres);
		return buffer.substring(0, buffer.length()-5);
	}
	/**
	 * （非等于条件）重写QueryMap，变成真正的<field,value>
	 */
	@SuppressWarnings("unchecked")
	public void rewriteQueryMap(QueryMap<?> wheres) {
		for (String fieldOper : new LinkedHashSet<String>(wheres.keySet())){
			String[] parts = super.extractExpr(fieldOper);
			switch (parts[1]) {
			case "=":
				break;
			case "BETWEEN":
				GenericArray<Object> array = new GenericArray<Object>();
				array.set(wheres.remove(fieldOper));
				String field = this.extractExpr(fieldOper)[0];
				((QueryMap<Object>)wheres).put(field+"_1", array.get(0));
				((QueryMap<Object>)wheres).put(field+"_2", array.get(1));
				break;
			default:
				((QueryMap<Object>)wheres).put(parts[0], wheres.remove(fieldOper));
			}
		}
	}
	
	public String getSqlType(String sqlId, Configuration config){
		try{
			return config.getMappedStatement(sqlId).getSqlCommandType().name();
		}catch(NullPointerException npe){
			throw new IllegalArgumentException("不存在语句："+sqlId);
		}
	}
	
//	/**
//	 * @see #buildJpaStatements(entityClass, Configuration)
//	 */
//	@Override
//	protected void processNamesSql(NamedQuery namedQuery) {
//		//do-nothing
//	}
	
	private void buildDefaultParameterMap(Class<?> entityClass, Configuration config) {
		ParameterMap paramMap = new ParameterMap.Builder(config, namespace, entityClass,
				 		this.buildParameterMappings(entityClass, config)).build();
		config.addParameterMap(paramMap);
	}
	
	private void buildDefaultResultMap(Class<?> entityClass, Configuration config){
		ResultMap resultMap = new ResultMap.Builder(config, namespace, entityClass,
							buildResultMappings(entityClass,config)).build();
		config.addResultMap(resultMap);
		if (!config.getResultMapNames().contains(UNAMED_RESULT_MAP)) {
			resultMap = new ResultMap.Builder(config, UNAMED_RESULT_MAP, HashMap.class,
								new ArrayList<ResultMapping>(0)).build();
			config.addResultMap(resultMap);
		}
	}
	/**
	 * 构建注解(@JpaResultMap)的ResultMap
	 * @param entityClass
	 * @param config
	 */
	private void buildJpaResultMap(Class<?> entityClass, Configuration config){
		List<JpaResultMap> resultTypes = new ArrayList<JpaResultMap>();
		if (entityClass.isAnnotationPresent(JpaResultMap.class)){
			resultTypes.add(entityClass.getAnnotation(JpaResultMap.class));
		}
		if (entityClass.isAnnotationPresent(JpaResultMaps.class)){
			resultTypes.addAll(Arrays.asList(
				entityClass.getAnnotation(JpaResultMaps.class).value())
			);
		}
		if (resultTypes.size() > 0){
			ResultMap resultMap;
			final String resultId = namespace + ".resultId";
			for (JpaResultMap result : resultTypes){
				Class<?> resultType = result.type();
				resultMap = new ResultMap.Builder(config, 
									resultId.replace("resultId", result.stmtId()),
									resultType,
									buildResultMappings(resultType,config)).build();
				config.addResultMap(resultMap);
			}
			resultTypes.clear();
		}
	}
	
	private List<ParameterMapping> buildParameterMappings(Class<?> entityClass, Configuration config){
		if (config.getResultMapNames().contains(entityClass.getName())){
			return config.getParameterMap(entityClass.getName()).getParameterMappings();
		}

		List<ParameterMapping> fieldMappings = new ArrayList<ParameterMapping>();
		Class<?> ftype;
		for (Field field : entityClass.getDeclaredFields()){
			if (!field.isAnnotationPresent(Column.class)) {
				continue;
			}
			String name = field.getName();
			ftype = field.getType();
			ParameterMapping.Builder builder = new ParameterMapping.Builder(config, name, ftype);
			builder.jdbcType(this.getJdbcType(ftype));  //Mysql is well, but Oracle null-value
			fieldMappings.add(builder.build());
		}
		return fieldMappings;
	}
	private List<ResultMapping> buildResultMappings(Class<?> entityClass, Configuration config){
		if (Map.class.isAssignableFrom(entityClass)){
			return new ArrayList<ResultMapping>(0);
		}else if (config.getResultMapNames().contains(entityClass.getName())){
			return config.getResultMap(entityClass.getName()).getResultMappings();
		}

		List<ResultMapping> fieldMappings = new ArrayList<ResultMapping>();
		String name, column = null;
		for (Field field : entityClass.getDeclaredFields()){
			name = field.getName();
			if (field.isAnnotationPresent(Column.class)){;
				column = field.getAnnotation(Column.class).name();
			}else if (field.isAnnotationPresent(FunctColumn.class)){
				column = field.getAnnotation(FunctColumn.class).alias();
			} else {
				continue;
			}
			if (column==null || column.equals("")) {
				column = name;
			}
			Class<?> ftype = field.getType();
			ResultMapping.Builder builder = new ResultMapping.Builder(config, name, column, ftype);
			builder.jdbcType(this.getJdbcType(ftype));  //Mysql is well, but Oracle null-value
			fieldMappings.add(builder.build());
		}
		return fieldMappings;
	}
	
	/**
	 * 构建注解的映射语句
	 * @param entityClass
	 * @param config
	 */
 	private void buildJpaStatements(Class<?> entityClass, Configuration config){
		List<NamedQuery> queries = new ArrayList<NamedQuery>();
		if (entityClass.isAnnotationPresent(NamedQuery.class)){
			queries.add(entityClass.getAnnotation(NamedQuery.class));
		}
		if (entityClass.isAnnotationPresent(NamedQueries.class)){
			queries.addAll(Arrays.asList(
				entityClass.getAnnotation(NamedQueries.class).value())
			);
		}
		if (queries.size() > 0){
			String sql;
			for (NamedQuery query : queries){
				sql = query.query().replace(entityClass.getSimpleName(), tableName);
				buildMappedStatement(query.name(), sql, config);
			}
			queries.clear();
		}
	}
	private void buildMappedStatement(String sqlId, String namedSql, Configuration config){
		StringBuilder sql = new StringBuilder(namedSql);
		List<String> paramNames = NamedParameterParser.getSqlArgumentNames(sql);
		List<ParameterMapping> mappings = new ArrayList<ParameterMapping>();
		if (insertSqlId.equals(sqlId)) {
			mappings = config.getParameterMap(namespace).getParameterMappings();
		} else {
			for (String argName : paramNames){
				//xml-define #{property="passwd", jdbcType = "VARCHAR"}   null
				Class<?> javaType = Object.class;
				try {
					javaType = descripts.get(argName).getPropertyType();
				} catch(NullPointerException npe) { }
				mappings.add(new ParameterMapping.Builder(config, argName, javaType).build());
			}
		}
		
		StaticSqlSource sqlSource = new StaticSqlSource(config, sql.toString(), mappings);
		MappedStatement.Builder builder;
		boolean flag = false;  //返回Map类型
		if (!sqlId.startsWith(namespace)){
			flag = java.util.regex.Pattern.compile("^(find|query){1}MapBy.*")
								.matcher(sqlId).matches();
			sqlId = namespace + "." + sqlId;
		}
		logger.debug("SQL: {}", sql.toString());
		if (Patterns.SQL_INSERT.matcher(sql).matches()){
			builder = new MappedStatement.Builder(config, sqlId, sqlSource, SqlCommandType.INSERT);
		}else if (Patterns.SQL_DELETE.matcher(sql).matches()){
			builder = new MappedStatement.Builder(config, sqlId, sqlSource, SqlCommandType.DELETE);
		}else if (Patterns.SQL_UPDATE.matcher(sql).matches()
				|| Patterns.SQL_DDL.matcher(sql).find()){
			builder = new MappedStatement.Builder(config, sqlId, sqlSource, SqlCommandType.UPDATE);
		}else {
			builder = new MappedStatement.Builder(config, sqlId, sqlSource, SqlCommandType.SELECT);
			List<ResultMap> maps = new ArrayList<ResultMap>(1);
			if (config.getResultMapNames().contains(sqlId)){
				maps.add(config.getResultMap(sqlId));
			}else if (flag){
				maps.add(config.getResultMap(UNAMED_RESULT_MAP));
			}else{  //default as 'entityClass' eg. findById
				maps.add(config.getResultMap(namespace));
			}
			builder.resultMaps(maps);
		}
		
		config.addMappedStatement(builder.build());
	}
	
	/**
	 * 用SQL语句生成Statement ID
	 * @param sqlId    前缀
	 * @param namedSql 命名参数的sql
	 * @param config   mybatis Configuration
	 * @return
	 */
	public String addQueryStatement(String sqlId, String namedSql, Configuration config){
		String encodedSql = this.encodeSql(namedSql);
		sqlId = String.format("%s_%s", sqlId, encodedSql);
		if (!config.getMappedStatementNames().contains(sqlId)){
			this.buildMappedStatement(sqlId, namedSql, config);
		}
		return sqlId;
	}
	
	@Override
	protected String field2Expr(final String fieldOper){
		String[] parts = super.extractExpr(fieldOper);
		String field = parts[0], operator = parts[1];
		String column = parts[2];
		
		StringBuilder buffer = new StringBuilder(column);
		if ("BETWEEN".equals(operator.trim())){
			String namedArg = this.getNamedArg(field);  //name_1,name_2
			buffer.append(String.format(" BETWEEN %s_1 and %s_2", namedArg,namedArg));
		}else if (operator.endsWith("IN")){ //in or not_in
			buffer.append(String.format(" %s (${IN})", operator));
		}else{ //包括：is [not]，like， =，<> 等
			String expr = String.format(" %s %s", operator,this.getNamedArg(field));
			buffer.append(expr);
	    }
		return buffer.toString();
	}
	
	private String encodeSql(String sql) {
		try {
	        byte[] bytes = MessageDigest.getInstance("MD5").digest(sql.getBytes()); 
	        return HexEncoder.encode(bytes);
		}catch (Exception e) {
			return sql;
		}
	}
	private final String getNamedArg(String field) {
		return NamedParameterParser.ARG_CHAR + field;
	}
	private JdbcType getJdbcType(Class<?> javaType) {
		JdbcType jdbcType = JdbcType.JAVA_OBJECT;
		if (javaType == String.class) {
			jdbcType = JdbcType.VARCHAR;
		} else if (javaType == Integer.class) {
			jdbcType = JdbcType.INTEGER;
		} else if (javaType == Float.class) {
			jdbcType = JdbcType.FLOAT;
		} else if (Date.class.isAssignableFrom(javaType)) {
			jdbcType = (javaType==java.sql.Date.class) ? JdbcType.DATE : JdbcType.TIMESTAMP;
		}
		return jdbcType;
	}

}
