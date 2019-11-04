package com.openxsl.config.dal.jdbc.sqlparse;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import com.openxsl.config.dal.jdbc.impl.FunctionalField;
import com.openxsl.config.dal.jdbc.impl.FunctionalField.FunctColumn;
import com.openxsl.config.util.StringUtils;

/**
 * JPA注解-SQL解析器
 * @author xiongsl
 * @lastModified 2018-07-30  增加createSql
 */
public class SqlParser {
	public static final String SP_FIELD = ", ";     //字段间的间隔符
	protected Class<?> entityClass;
	protected String tableName, id, createSql = "CREATE TABLE %s (\n\t";
	protected LinkedHashMap<String,PropertyDescriptor> descripts; 
	protected Map<String, String> fieldColumns = new HashMap<String, String>();
	protected Map<String, String> columnFields = new HashMap<String, String>();
	protected Map<String, FunctionalField> fieldFuncs = new HashMap<String, FunctionalField>(4);
	protected Map<String, String> namedSqls = new HashMap<String, String>();
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	SqlParser(Class<?> entityClass){
		this.entityClass = entityClass;
		Assert.isTrue(entityClass.isAnnotationPresent(Table.class),
					"Must has annotation @Table");
		tableName = entityClass.getAnnotation(Table.class).name();
		this.initJpaFields(entityClass);
		this.initNamedQueries(entityClass);
	}
	
	public String getNamedSql(String sqlId){
		return namedSqls.get(sqlId);
	}
	/**
	 * 转换成字段名（@Column），注意先判断 field.isAnnotationPresent(Column.class)
	 */
	public final String getColumn(Field field){
		String column = "";
		if (field.isAnnotationPresent(Column.class)){
			column = field.getAnnotation(Column.class).name();  //default ""
		}
		if ("".equals(column)){  //camelToSplitName
			StringBuilder buf = new StringBuilder();
		    for (char ch : field.getName().toCharArray()) {
		        if (Character.isUpperCase(ch)) {
		        	ch = (char)(ch + 32);
		        	buf.append("_").append(ch);
		        }else{
		            buf.append(ch);
		        }
		    }
		    column = buf.toString();
		}
		return column;
	}
	
	private <T> void initJpaFields(final Class<T> entityClass){
		Field[] fields = entityClass.getDeclaredFields();
		int len = fields.length;
		this.descripts = new LinkedHashMap<String,PropertyDescriptor>(len);
		StringBuilder sql = new StringBuilder(String.format(createSql, tableName));
		for (Field field : fields){
			String fieldName = field.getName();
			boolean flag = field.isAnnotationPresent(Id.class);
			if (field.isAnnotationPresent(Column.class) || flag){
				String column = this.getColumn(field);
				if (flag){
					id = fieldName;
				}
				fieldColumns.put(fieldName, column);
				columnFields.put(column, fieldName);
				descripts.put(fieldName, BeanUtils.getPropertyDescriptor(entityClass, fieldName));
				sql.append(this.getColumnDDL(field, column));
			}else if (field.isAnnotationPresent(FunctColumn.class)){
				fieldFuncs.put(fieldName, new FunctionalField(field));
			}
		}
		
		if (id != null) {
			sql.append("PRIMARY KEY (").append(fieldColumns.get(id)).append(")");
		} else {
			sql.delete(sql.lastIndexOf(","), sql.length());
		}
		sql.append("\n);\n");
		for (Index index : entityClass.getAnnotation(Table.class).indexes()) {
			sql.append("CREATE INDEX ").append(index.name()).append(" ON ")
				.append(tableName).append("(")
				.append(StringUtils.camelToSplitName(index.columnList(),"_"))
				.append(")\n");
		}
		this.createSql = sql.toString();
		fields = null;
	}
	private <T> void initNamedQueries(final Class<T> entityClass) {
		if (entityClass.isAnnotationPresent(NamedQueries.class)){
			NamedQueries namedQueries = entityClass.getAnnotation(NamedQueries.class);
			for (NamedQuery namedQ : namedQueries.value()){
				this.processNamesSql(namedQ);
			}
		}else if (entityClass.isAnnotationPresent(NamedQuery.class)){
			NamedQuery namedQuery = entityClass.getAnnotation(NamedQuery.class);
			this.processNamesSql(namedQuery);
		}
	}
	private void processNamesSql(NamedQuery namedQuery){
		String sql = namedQuery.query();
		if (sql.startsWith("FROM ")){ //hibernate not allow for "SELECT *"
			sql = "SELECT * " + sql;
		}
		//将表名、字段名替换掉
		for (Map.Entry<String,String> entry : fieldColumns.entrySet()){
			sql = sql.replace(entry.getKey(), entry.getValue());
		}
		sql = sql.replace(entityClass.getSimpleName(), tableName);
		namedSqls.put(namedQuery.name(), sql);
	}
	
	/**
	 * 创建字段
	 */
	private final String getColumnDDL(Field field, String column) {
		StringBuilder sql = new StringBuilder(16);
		sql.append(column).append("  ");
		final Class<?> ftype = field.getType();
		Column anno = field.getAnnotation(Column.class);
		if (ftype == String.class) {
			sql.append("VARCHAR(").append(anno.length()).append(")");
		} else if (Date.class.isAssignableFrom(ftype)){
			sql.append((java.sql.Date.class==ftype) ? "DATE" : "DATETIME");
		} else if (boolean.class==ftype || Boolean.class.isAssignableFrom(ftype)
				|| char.class==ftype || Character.class.isAssignableFrom(ftype)) {
			sql.append("CHAR(1)");
		} else if (int.class==ftype || Integer.class.isAssignableFrom(ftype)) {
			sql.append("INT");
		} else if (short.class==ftype || Short.class.isAssignableFrom(ftype)){
			sql.append("SMALLINT");
		}  else if (long.class==ftype || Long.class.isAssignableFrom(ftype)){
			sql.append("NUMERIC(10, 0)");
		}else {
			sql.append("NUMERIC(").append(anno.precision())
				.append(",").append(anno.scale()).append(")");
		}
		boolean nilable = anno.nullable();
		if (field.isAnnotationPresent(Id.class)) {
			nilable = false;
		}
		sql.append(nilable ? " NULL" : " NOT NULL").append(",\n\t");
		return sql.toString();
	}
}
