package com.openxsl.config.dal.jdbc.sqlparse;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.openxsl.config.dal.jdbc.QueryMap;

/**
 * SQL语句注意要点：
 *  1、关键字要大写，如：SELECT,FROM,WHERE,IS,LIKE,BETWEEN,AND,OR等，但与BTEWEEN配对的and须小写
 *  2、表达式的操作符前后要有空格，如：name <> 'ketty'
 *  3、QueryMap支持除'等于'外的做法，操作符在key中，如：Entry('name LIKE', 'ketty%')
 *  4、字段列表（SELECT/INSERT的列名）使用特定分隔符（由常量SP_FIELD定义）
 *  5、UPDATE拷贝字段: field1=field2，则调用update()参数：QueryMap(field1,$this.field2)
 *  6、自定义语句@NamedQuery中sql表名、字段名请使用field，会转换为column
 *  
 * @author xiongsl
 */
public abstract class BaseSqlParser extends SqlParser{
	/**查询属性：可能含有操作符 (\\w+)(.*)*/
	protected final Pattern P_FIELD_OPERATOR = Pattern.compile("(\\w+)(.*)");
	protected final Pattern P_NAMED_PARAM = Pattern.compile("(:\\w+)");
	protected final String insertSql, updateSql;
	private final String findSql, deleteSql, whereId;
	
	public BaseSqlParser(Class<?> entityClass){
		super(entityClass);
		whereId = " WHERE " + fieldColumns.get(id) + " = ?";
		findSql = new StringBuilder("SELECT * FROM ").append(tableName)
						.append(whereId).toString();
		deleteSql = new StringBuilder("DELETE FROM ").append(tableName)
						.append(whereId).toString();
		insertSql = new StringBuilder("INSERT INTO ").append(tableName)
						.append("(%s) VALUES (%s)").toString();
		updateSql = new StringBuilder("UPDATE ").append(tableName)
						.append(" SET %s WHERE %s").toString();
	}
	
	public String getFindSql(String... fields){
		if (fields.length == 0){
			return findSql;
		}else{
			return findSql.replace("*", this.getSelectColumns(null, fields));
		}
	}
	public String getDeleteSql(){
		return deleteSql;
	}
	public String getCreateSql() {
		return createSql;
	}
	public String getTableName(){
		return tableName;
	}
	public String getIdField(){
		return id;
	}
	public String getIdWheres(){
		return whereId;
	}
	
	/**
	 * 返回对象的Key值
	 */
	public Object getId(Object bean){
		if (id==null || bean==null) {
			return null;
		}
		for (PropertyDescriptor desc : descripts.values()){
			if (id.equals(desc.getName())){
				try {
					return desc.getReadMethod().invoke(bean);
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	/**
	 * 将所有非空的属性转换为Map
	 */
	public QueryMap<Object> getPropertyMap(Object bean){
		QueryMap<Object> map = new QueryMap<Object>();
		for (PropertyDescriptor desc : descripts.values()){
			try{
				Object value = desc.getReadMethod().invoke(bean);
				if (value != null){
					map.put(desc.getName(), value);
				}
			}catch(Exception e){
				logger.warn("", e);
			}
		}
		return map;
	}
	
	/**
	 * 生成查询语句（SELECT...WHERE...ORDER BY）
	 * @param wheres 查询属性
	 * @param orders 排序
	 * @param fields 返回字段列表
	 */
	public String getQuerySql(Set<String> wheres, String orders, String... fields){
		StringBuilder buffer = new StringBuilder("SELECT ");
		buffer.append(this.getSelectColumns(orders, fields));
		buffer.append(" FROM ").append(tableName);
		if (wheres!=null && wheres.size()>0){
			buffer.append(" WHERE ");  //this.getWhereSql(QueryMap<?> wheres)
			for (String field : wheres){
				buffer.append(this.field2Expr(field)).append(" AND ");
			}
			int len = buffer.length();
			buffer.delete(len-5, len);
		}
		
		buffer.append(this.getOrderSql(orders));
		return buffer.toString();
	}
	
	/**
	 * 将QueryMap转换为 'AND'查询条件
	 * @param wheres
	 */
	public String getWhereSql(QueryMap<?> wheres){
		StringBuilder buffer = new StringBuilder();
		for (String fieldOper : wheres.keySet()){
			buffer.append(this.field2Expr(fieldOper)).append(" AND ");
		}
		return buffer.substring(0, buffer.length()-5);
	}
	
	/**
	 * QueryMap.key拆(解)为三部分：field, operator, column
	 * @param fieldOper
	 * @return
	 */
	public String[] extractExpr(String fieldOper) {
		String field = fieldOper, operator = "=", column = "";
		Matcher m = P_FIELD_OPERATOR.matcher(fieldOper);
		if (m.matches()){
			field = m.group(1);
			if (!m.group(2).equals("")){
				operator = m.group(2).trim();
			}
		}
		column = fieldColumns.get(field);
		if (column==null && fieldFuncs.containsKey(field)){
			column = fieldFuncs.get(field).getSql();
		}
		return new String[] {field, operator, column};
	}
	
	protected abstract String field2Expr(String fieldOper);
	
	/**
	 * SELECT 子句
	 */
	private final String getSelectColumns(String orders, String... fields){
		StringBuilder buffer = new StringBuilder();
		Set<String> fieldSet = new TreeSet<String>();
		if (fields.length < 1){
			fieldSet.addAll(fieldColumns.keySet());
		}else{
			for (String field : fields) {
				fieldSet.addAll(Arrays.asList(field.split(SP_FIELD)));
			}
			fieldSet.remove("*");
		}
		//orderBy聚合字段必须放进去
		if (orders!=null && orders.trim().length()>0){
			for (String orderStr : orders.split(SP_FIELD)){ // f1 desc,f2 asc
				fieldSet.add(orderStr.split("\\s+")[0]); 
			}
		}
		
		for (String f : fieldSet){
			String column = fieldColumns.get(f);
			if (column == null){
				if (fieldFuncs.containsKey(f)){  //max(f)
					column = fieldFuncs.get(f).toSql();
				}else{
					throw new IllegalArgumentException("ill-field:"+f);
				}
			}
			buffer.append(SP_FIELD).append(column);
		}
		
		return buffer.substring(SP_FIELD.length());
	}
	/**
	 * ORDER BY 子句
	 */
	private String getOrderSql(String orders) {
		if (orders!=null && orders.trim().length()>0){
			StringBuilder orderBuf = new StringBuilder(" ORDER BY ");
			String field, column;
			for (String fieldSort : orders.split(SP_FIELD)){
				field = fieldSort.trim().split(" ")[0];
				column = fieldColumns.get(field);
				if (column == null){
					column = fieldFuncs.get(field).getAlias();
				}
				orderBuf.append(fieldSort.replace(field, column)).append(SP_FIELD);
			}
			return orderBuf.substring(0, orderBuf.lastIndexOf(SP_FIELD));
		}
		return "";
	}
}
